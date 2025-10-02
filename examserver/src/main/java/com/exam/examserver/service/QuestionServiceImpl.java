package com.exam.examserver.service;

import com.exam.examserver.dto.QuestionDTO;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import com.exam.examserver.exception.BadRequestException;
import com.exam.examserver.exception.ResourceNotFoundException;
import com.exam.examserver.mapper.QuestionMapper;
import com.exam.examserver.repository.QuestionRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizService quizService;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public Question addQuestion(QuestionDTO dto) {
        // Ensure quiz exists (quizService#getQuiz should throw ResourceNotFoundException if missing)
        Quiz managedQuiz = quizService.getQuiz(dto.getQuizId());
        if (managedQuiz == null) {
            throw new ResourceNotFoundException("Quiz not found with id: " + dto.getQuizId());
        }

        // Use mapper to create a Question entity from DTO
        Question q = QuestionMapper.toEntity(dto);

        // Mapper sets a transient Quiz with only id; replace with managed quiz to avoid detached entity issues
        q.setQuiz(managedQuiz);

        // Ignore any client-supplied id for creation
        q.setQuestionId(null);

        return questionRepository.save(q);
    }

    @Override
    public Question updateQuestion(Question question) {
        return this.questionRepository.save(question);
    }

    @Override
    public Set<Question> getQuestions() {
        return new HashSet<>(this.questionRepository.findAll());
    }

    @Override
    public Question getQuestion(Long questionId) {
        return this.questionRepository.findById(questionId)
                .orElseThrow(()-> new ResourceNotFoundException("Question not found with id: "+ questionId));
    }


    @Override
    public Set<Question> getQuestionsOfQuiz(Quiz quiz) {
        return this.questionRepository.findByQuiz(quiz);
    }

    @Override
    @Transactional
    public QuestionDTO patchQuestion(Long questionId, Map<String, Object> updates) {
        // 1) fetch existing question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        // 2) whitelist allowed fields to avoid accidental updates
        Set<String> allowed = Set.of(
                "content", "image",
                "option1", "option2", "option3", "option4",
                "answer",
                "quizId"
        );

        Map<String, Object> filtered = updates.entrySet().stream()
                .filter(e -> allowed.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 3) handle quizId separately so ObjectMapper won't try to set 'quiz' field directly
        Object rawQuiz = null;
        if (filtered.containsKey("quizId")) {
            rawQuiz = filtered.remove("quizId"); // may be null -> means dissociate quiz
        }

        // 4) apply the remaining filtered values to the existing entity using ObjectReader
        try {
            ObjectReader updater = objectMapper.readerForUpdating(question)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            byte[] patchBytes = objectMapper.writeValueAsBytes(filtered);
            updater.readValue(patchBytes);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid patch content: " + ex.getMessage());
        }

        // 5) handle quizId update if present in original updates map
        if (updates.containsKey("quizId")) {
            if (rawQuiz == null) {
                // explicit null -> remove association
                question.setQuiz(null);
            } else {
                Long qid;
                if (rawQuiz instanceof Number) {
                    qid = ((Number) rawQuiz).longValue();
                } else {
                    try {
                        qid = Long.parseLong(rawQuiz.toString());
                    } catch (NumberFormatException nfe) {
                        throw new BadRequestException("quizId must be a number");
                    }
                }
                // fetch managed quiz (throws ResouceNotFoundException if absent)
                Quiz managedQuiz = quizService.getQuiz(qid);
                question.setQuiz(managedQuiz);
            }
        }

        // 6) persist and return DTO
        Question saved = questionRepository.save(question);
        return QuestionMapper.toDto(saved);
    }

//    @Override
//    @Transactional
//    public QuestionDTO deleteAndReturn(Long questionId) {
//        // 1) fetch the managed entity or throw 404
//        Question question = questionRepository.findById(questionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
//
//        // 2) map to DTO before deleting (so we can return representation)
//        QuestionDTO dto = QuestionMapper.toDto(question);
//
//        // 3) delete the entity
//        questionRepository.delete(question);
//
//        // 4) return DTO
//        return dto;
//    }

    @Override
    @Transactional(readOnly = false)
    public QuestionDTO deleteAndReturn(Long questionId) {
        // 1) fetch the managed entity or throw 404
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        // 2) map to DTO before deleting (so we can return representation)
        QuestionDTO dto = QuestionMapper.toDto(question);

        questionRepository.deleteById(questionId);

//        // 3) dissociate from parent Quiz (important to avoid persistence-context re-sync)
//        Quiz parentQuiz = question.getQuiz();
//        if (parentQuiz != null) {
//            // remove the question from the parent's collection
//            parentQuiz.getQuestionSet().remove(question);
//
//            // optional: adjust stored numberOfQuestions if you maintain it as a field
//            String numStr = parentQuiz.getNumberOfQuestions();
//            if (numStr != null) {
//                try {
//                    int n = Integer.parseInt(numStr);
//                    n = Math.max(0, n - 1);
//                    parentQuiz.setNumberOfQuestions(String.valueOf(n));
//                } catch (NumberFormatException ignored) {
//                    // ignore if it's not a number; don't fail delete for this
//                }
//            }
//
//            // persist the parent change so Hibernate knows we've removed the relationship
//            entityManager.merge(parentQuiz);
//            entityManager.flush();
//        }
//
//        // 4) now delete the question itself (safe even if orphanRemoval already deleted it)
//        if (questionRepository.existsById(questionId)) {
//            questionRepository.deleteById(questionId);
//            questionRepository.flush(); // force SQL and surface constraint errors if any
//        }

        // 5) return DTO
        return dto;
    }

}
