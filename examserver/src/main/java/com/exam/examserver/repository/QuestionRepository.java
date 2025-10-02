package com.exam.examserver.repository;

import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question,Long> {
    Set<Question> findByQuiz(Quiz quiz);
    @Modifying
    @Transactional
    @Query("DELETE FROM Question q WHERE q.quiz.qId = :qId")
    void deleteByQuizId(Long qId);
}
