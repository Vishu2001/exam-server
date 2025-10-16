package com.exam.examserver.service;

import com.exam.examserver.dto.CategoryDTO;
import com.exam.examserver.dto.QuestionDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import com.exam.examserver.exception.BadRequestException;
import com.exam.examserver.exception.ResourceNotFoundException;
import com.exam.examserver.mapper.QuestionMapper;
import com.exam.examserver.repository.QuestionRepository;
import com.exam.examserver.repository.QuizRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizRepository quizRepository;

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
        return this.questionRepository.findByQuestionIdAndDeletedFalse(questionId)
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


    @Override
    @Transactional(readOnly = false)
    public QuestionDTO deleteAndReturn(Long questionId) {
        // 1) fetch the managed entity or throw 404
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        // 2) map to DTO before deleting (so we can return representation)
        QuestionDTO dto = QuestionMapper.toDto(question);

        questionRepository.deleteById(questionId);

        return dto;
    }

    @Override
    public QuestionDTO softDeleteQuestion(Long questionId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(()-> new ResourceNotFoundException("Question not found with id: "+ questionId));
        question.setDeleted(true);
        questionRepository.save(question);

        return new QuestionDTO(question.getQuestionId(),question.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDTO> getAllQuestionsByQIdAndCid(int page,int size, Long qId, Long cid) {
        Quiz quiz = quizRepository.findByQIdAndCategoryCidAndDeletedFalse(qId,cid)
                .orElseThrow(()-> new ResourceNotFoundException(String.format("Quiz with id %d not found in Category %d", qId, cid)));

        Pageable pageable = PageRequest.of(Math.max(0,page),Math.max(1,size),Sort.by("questionId").ascending());

        Page<?> pageOfQuestions = questionRepository.findByQuizQIdAndQuizCategoryCidAndDeletedFalse(qId,cid,pageable);

        // Cast and map Page<Question> -> Page<QuestionDTO> using map()
        @SuppressWarnings("unchecked")
                Page<Question> questionPage = (Page<Question>) pageOfQuestions;

        return questionPage.map(QuestionMapper::toDto);
    }



}
