package com.exam.examserver.repository;

import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface QuizRepository extends JpaRepository<Quiz,Long> {
        Set<Quiz> findByCategoryCid(Long cid);
        List<Quiz> findByActive(Boolean b);
        List<Quiz> findByCategoryCidAndActive(Long cid, Boolean b);
        Optional<Quiz> findByQIdAndCategoryCid(Long qId,Long cid);

}
