package com.exam.examserver.service;

import com.exam.examserver.dto.QuizDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Quiz;
import com.exam.examserver.exception.BadRequestException;
import com.exam.examserver.exception.ResourceNotFoundException;
import com.exam.examserver.mapper.QuizMapper;
import com.exam.examserver.repository.CategoryRepository;
import com.exam.examserver.repository.QuestionRepository;
import com.exam.examserver.repository.QuizRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.parser.Entity;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizServiceImpl implements QuizService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private QuizMapper quizMapper;

    @Override
    public Quiz addQuiz(Quiz quiz) {
        return this.quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(Quiz quiz) {
        return this.quizRepository.save(quiz);
    }

    @Override
    public Set<Quiz> getQuizzes() {
        return new HashSet<>(this.quizRepository.findAll());
    }

    @Override
    public Quiz getQuiz(Long quizId) {
        return this.quizRepository.findById(quizId)
                .orElseThrow(()->new ResourceNotFoundException("Quiz not found with id: "+ quizId));
    }

//    @Override
//    public void deleteQuiz(Long qId) {
//            Quiz quiz = new Quiz();
//            quiz.setqId(qId);
//            this.quizRepository.delete(quiz);
//    }

    @Override
    @Transactional(readOnly = false)
    public QuizDTO deleteAndReturnQuiz(Long qId) {
        Quiz quiz = quizRepository.findById(qId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID:" + qId));
        // map to DTO before deleting so you can return representation
        QuizDTO dto = QuizMapper.toDto(quiz);

//         perform the delete
        quizRepository.deleteById(qId);

        return dto;
    }


    //Update a quiz (Supports partial update too)
    @Override
    @Transactional
    public QuizDTO patchQuiz(Long qId, Map<String, Object> updates) {
        Quiz quiz = quizRepository.findById(qId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + qId));

        // 1) whitelist allowed fields to avoid accidental updates
        Set<String> allowed = Set.of(
                "title", "description", "maxMarks", "numberOfQuestions", "active", "categoryId"
                // add other allowed fields here
        );

        // Create a filtered map containing only allowed keys that were actually sent
        Map<String, Object> filtered = updates.entrySet().stream()
                .filter(e -> allowed.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 2) Handle association (categoryId) separately — remove it from the map so ObjectMapper won't try to set 'category'
        Object rawCategory = null;
        if (filtered.containsKey("categoryId")) {
            rawCategory = filtered.remove("categoryId"); // may be null -> means dissociate
        }

        // 3) Use ObjectMapper to update the existing entity from the filtered map
        try {
            // ObjectReader configured to update existing quiz and ignore unknown properties
            ObjectReader updater = objectMapper.readerForUpdating(quiz)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            // readValue accepts JSON bytes or a JsonNode; converting map to bytes is robust
            byte[] patchBytes = objectMapper.writeValueAsBytes(filtered);
            updater.readValue(patchBytes);
        } catch (Exception ex) {
            // convert Jackson exceptions to a controlled BadRequestException so controller advice can return 400
            throw new BadRequestException("Invalid patch content: " + ex.getMessage());
        }

        // 4) Now handle categoryId update (if provided in original updates)
        if (updates.containsKey("categoryId")) {
            if (rawCategory == null) {
                // explicit null -> remove association
                quiz.setCategory(null);
            } else {
                Long cid;
                if (rawCategory instanceof Number) {
                    cid = ((Number) rawCategory).longValue();
                } else {
                    try {
                        cid = Long.parseLong(rawCategory.toString());
                    } catch (NumberFormatException nfe) {
                        throw new BadRequestException("categoryId must be a number");
                    }
                }
                Category category = categoryService.getCategory(cid); // throws ResourceNotFoundException if absent
                quiz.setCategory(category);
            }
        }

        // 5) Persist and return DTO
        Quiz saved = quizRepository.save(quiz);
        return com.exam.examserver.mapper.QuizMapper.toDto(saved);
    }

    @Override
    @Transactional
    public QuizDTO patchQuizOptimised(Long cid, Long qId, Map<String, Object> updates) {
        // quick null checks
        if (cid == null || qId == null) {
            throw new BadRequestException("Both query params 'cid' and 'qId' are required.");
        }

        // DEBUG: log incoming keys (optional, remove in production)
        // System.out.println("PATCH keys: " + updates.keySet());

        // 1) fetch quiz by qId AND cid in single DB call (ensure your repo method name matches)
        Quiz quiz = quizRepository.findByQIdAndCategoryCid(qId, cid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Quiz with id %d not found in category %d", qId, cid)));

        // 2) allowed fields
        Set<String> allowed = Set.of(
                "title",
                "description",
                "maxMarks",
                "numberOfQuestions",
                "active"
        );

        // 3) explicitly forbidden category keys (special error)
        Set<String> forbiddenCategoryKeys = Set.of("category", "categoryId", "category_id");

        // 4) detect invalid keys (anything not allowed and not the special category keys)
        Set<String> invalidKeys = updates.keySet().stream()
                .filter(k -> !allowed.contains(k) && !forbiddenCategoryKeys.contains(k))
                .collect(Collectors.toSet());

        if (!invalidKeys.isEmpty()) {
            // Throw with a clear message listing invalid keys
            throw new BadRequestException("Invalid field(s) in request: " + String.join(", ", invalidKeys));
        }

        // 5) if category keys present -> throw a dedicated message
        for (String fk : forbiddenCategoryKeys) {
            if (updates.containsKey(fk)) {
                throw new BadRequestException("Changing category is not allowed via this endpoint. Use a dedicated endpoint to move quizzes.");
            }
        }

        // 6) Keep only allowed fields to actually patch
        Map<String, Object> filtered = updates.entrySet().stream()
                .filter(e -> allowed.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 7) apply partial update using Jackson ObjectMapper
        try {
            ObjectReader updater = objectMapper.readerForUpdating(quiz)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            byte[] patchBytes = objectMapper.writeValueAsBytes(filtered);
            updater.readValue(patchBytes);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid patch content: " + ex.getMessage());
        }

        // 8) persist and return DTO
        Quiz saved = quizRepository.save(quiz);
        return QuizMapper.toDto(saved);
    }



    // Get all quizzes of a Category by category id;
    @Override
    public List<QuizDTO> getAllQuizzesOfaCategory(Long cId) {
        // ensure category exists; change repository method name if your field is different
        Category category = categoryRepository.findById(cId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with cid: " + cId));
        List<Quiz> quizList = quizRepository.findByCategoryCidAndDeletedFalse(cId);

        return quizList.stream()
                .map(quiz -> new QuizDTO(quiz.getqId(),quiz.getTitle(),quiz.getDescription(),
                        quiz.getMaxMarks(),quiz.getNumberOfQuestions(),quiz.isActive(),quiz.getCategory().getCid()))
                .collect(Collectors.toList());
            }


    //get all active quizzes
    @Override
    public List<Quiz> getActiveQuizzes() {
        return this.quizRepository.findByActive(true);
    }

    //get all active quizzes of a Category
    @Override
    public List<Quiz> getActiveQuizzesOfCategory(Long cId) {
        // ensure category exists; change repository method name if your field is different
        Category category = categoryRepository.findById(cId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with cid: " + cId));

        return this.quizRepository.findByCategoryCidAndActive(cId,true);
    }


    @Override
    public QuizDTO softDeleteQuiz(Long qId,Long cid) {
//        Category category = categoryRepository.findById(cid)
//                .orElseThrow(()-> new ResourceNotFoundException("Category not found with id: "+ cid));

        Quiz quiz = quizRepository.findByQIdAndCategoryCid(qId,cid)
                .orElseThrow(()-> new ResourceNotFoundException("Either Category id and quiz id mismatched OR incorrect cid/qid given"));

        //bulk update questions -> no collection loading
        questionRepository.softDeleteByQuizId(qId);

        quiz.setDeleted(true);
        quizRepository.save(quiz);

        return new QuizDTO(quiz.getqId(),quiz.getTitle(),quiz.getDescription());
    }

    @Override
    public QuizDTO getQuizByCidAndQid(Long qid,Long cid) {
        Category category = categoryRepository.findById(cid)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found with id: "+ cid));
        Quiz quiz = quizRepository.findByQIdAndDeletedFalseAndCategoryCid(qid,cid);
        return QuizMapper.toDto(quiz);
    }


}
