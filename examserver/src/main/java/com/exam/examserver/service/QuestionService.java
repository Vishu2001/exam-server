package com.exam.examserver.service;

import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;

import java.util.Set;

public interface QuestionService {

    Question addQuestion(Question question);
    Question updateQuestion(Question question);
    Set<Question> getQuestions();
    Question getQuestion(Long questionId);
    Set<Question> getQuestionsOfQuiz(Quiz quiz);

}
