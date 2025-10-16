package com.exam.examserver.service;

import com.exam.examserver.dto.QuizDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface QuizService {

    Quiz addQuiz(Quiz quiz);
    Quiz updateQuiz(Quiz quiz);
    Set<Quiz> getQuizzes();
    Quiz getQuiz(Long quizId);
    QuizDTO patchQuiz(Long qId, Map<String, Object> updates);
    QuizDTO deleteAndReturnQuiz(Long qId);
    List<QuizDTO> getAllQuizzesOfaCategory(Long cId);
    List<Quiz> getActiveQuizzes();
    List<Quiz> getActiveQuizzesOfCategory(Long cid);
    QuizDTO patchQuizOptimised(Long cid,Long qId, Map<String, Object> updates);
    QuizDTO getQuizByCidAndQid(Long qId,Long cid);

    QuizDTO softDeleteQuiz(Long qId,Long cid);



}
