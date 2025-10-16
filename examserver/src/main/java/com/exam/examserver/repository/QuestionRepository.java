package com.exam.examserver.repository;

import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question,Long> {
    Set<Question> findByQuiz(Quiz quiz);
    @Modifying
    @Transactional
    @Query("DELETE FROM Question q WHERE q.quiz.qId = :qId")
    void deleteByQuizId(Long qId);

    //soft deleted methods
    List<Question> findByQuizQIdAndDeletedFalse(Long qId);
    Optional<Question> findByQuestionIdAndDeletedFalse(Long questionId);

    @Modifying
    @Query("UPDATE Question q SET q.deleted = true WHERE q.quiz.qId = :qId")
    int softDeleteByQuizId(@Param("qId") Long qId);

    Page<Question> findByQuizQIdAndQuizCategoryCidAndDeletedFalse(Long qId, Long cid, Pageable pageable);


}
