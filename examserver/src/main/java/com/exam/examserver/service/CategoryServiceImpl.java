package com.exam.examserver.service;

import com.exam.examserver.dto.CategoryDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.entity.exam.Question;
import com.exam.examserver.entity.exam.Quiz;
import com.exam.examserver.exception.BadRequestException;
import com.exam.examserver.exception.ResourceNotFoundException;
import com.exam.examserver.repository.CategoryRepository;
import com.exam.examserver.repository.QuestionRepository;
import com.exam.examserver.repository.QuizRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Category addCategory(Category category) {
        return this.categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        return this.categoryRepository.save(category);
    }

    @Override
    public Set<Category> getCategory() {
        return new LinkedHashSet<>( this.categoryRepository.findAll());
    }

    @Override
    public Category getCategory(Long cId) {
        return this.categoryRepository.findById(cId).get();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = new Category();
        category.setCid(categoryId);
        this.categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDTO patchCategory(Long cId, Map<String, Object> updates) {

        Category existingEntity = categoryRepository.findById(cId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with cid "+ cId));

        //convert update into json node
        JsonNode updatesNode = objectMapper.valueToTree(updates);

        try {
            //merge updates into existing entity
            existingEntity = objectMapper.readerForUpdating(existingEntity)
                    .readValue(updatesNode);
        } catch (IOException e) {
            throw new BadRequestException("invalid Json for update");
        }
        if(existingEntity.getTitle()==null || existingEntity.getTitle().isBlank()){
            throw new BadRequestException("Title must not be empty");
        }

        Category saved = categoryRepository.save(existingEntity);
        return objectMapper.convertValue(saved,CategoryDTO.class);
    }

    @Override
    @Transactional
    public CategoryDTO deleteAndReturn(Long cId) {

        Category category = categoryRepository.findById(cId)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found with cId: "+ cId));

        CategoryDTO dto = new CategoryDTO(category.getCid(),category.getTitle(),category.getDescription());
        categoryRepository.delete(category);
        return dto;
    }

    @Override
    public CategoryDTO softDeleteCategory(Long cid) {
        Category category = categoryRepository.findByCidAndDeletedFalse(cid)
                .orElseThrow(()->new ResourceNotFoundException("Category not found with cid: "+cid));

        // 1) bulk-update questions (child rows) first
        int questionsUpdated = categoryRepository.softDeleteQuestionByCategoryId(cid);

        // 2) bulk-update quizzes (children of category)
        int quizzesUpdated = categoryRepository.softDeleteQuizzesByCategoryId(cid);

        category.setDeleted(true);

//        //soft delete quizzes and question attached with given cid
//        if (category.getQuizSet()!=null){
//            for (Quiz quiz: category.getQuizSet()){
//                quiz.setDeleted(true);
//                if(quiz.getQuestionSet()!=null){
//                    for (Question question: quiz.getQuestionSet()){
//                        question.setDeleted(true);
//                    }
//                }
//            }
//        }

        categoryRepository.save(category);


        return new CategoryDTO(category.getCid(), category.getTitle() , category.getDescription());
    }

    @Override
    public CategoryDTO getCategoryByCid(Long cId) {
        Category category = categoryRepository.findByCidAndDeletedFalse(cId)
                .orElseThrow(()-> new ResourceNotFoundException("No Category found with cid: "+cId));
        return new CategoryDTO(category.getCid(), category.getTitle(), category.getDescription());
    }

    @Override
    public Page<CategoryDTO> listCategories(int page, int size, boolean includeDeleted) {
        //note: page is always by default 0-based in pagination
        Pageable pageable = PageRequest.of(Math.max(0,page),Math.max(1,size), Sort.by("title").ascending());
        Page<Category> pageResult;

        if(includeDeleted){
            pageResult = categoryRepository.findAll(pageable);
        }
        else{
            pageResult = categoryRepository.findByDeletedFalse(pageable);
        }

        return pageResult.map(c->new CategoryDTO(c.getCid(),c.getTitle(), c.getDescription()));
    }


}
