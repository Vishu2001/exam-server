package com.exam.examserver.service;

import com.exam.examserver.dto.QuestionDTO;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface QuestionService {

    Question addQuestion(QuestionDTO questionDTO);
    Question updateQuestion(Question question);
    Set<Question> getQuestions();
    Question getQuestion(Long questionId);
    Set<Question> getQuestionsOfQuiz(Quiz quiz);
    QuestionDTO patchQuestion (Long questionId, Map<String,Object> updates);
    QuestionDTO deleteAndReturn(Long questionId);

    //soft delete question
    QuestionDTO softDeleteQuestion(Long questionId);

    Page<QuestionDTO> getAllQuestionsByQIdAndCid(int page,int size,Long qId, Long cid);

}
