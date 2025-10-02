package com.exam.examserver.service;

import com.exam.examserver.dto.QuizDTO;
import com.exam.examserver.entity.exam.Quiz;

import java.util.Map;
import java.util.Set;

public interface QuizService {

    Quiz addQuiz(Quiz quiz);
    Quiz updateQuiz(Quiz quiz);
    Set<Quiz> getQuizzes();
    Quiz getQuiz(Long quizId);
//    void deleteQuiz(Long qId);
    QuizDTO patchQuiz(Long qId, Map<String, Object> updates);
    QuizDTO deleteAndReturnQuiz(Long qId);

}
