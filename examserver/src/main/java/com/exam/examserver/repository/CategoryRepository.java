package com.exam.examserver.repository;

import com.exam.examserver.entity.exam.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Page<Category> findByDeletedFalse(Pageable pageable);
    List<Category> findByDeletedFalse();
    Optional<Category> findByCidAndDeletedFalse(Long cid);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Quiz q SET q.deleted = true WHERE q.category.cid = :cid")
    int softDeleteQuizzesByCategoryId(@Param("cid") Long cid);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Question q SET q.deleted = true WHERE q.quiz.qId IN (SELECT z.qId FROM Quiz z WHERE z.category.cid = :cid)")
    int softDeleteQuestionByCategoryId(@Param("cid") Long cid);



}
