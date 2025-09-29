package com.exam.examserver.service;

import com.exam.examserver.dto.CategoryDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.exception.BadRequestException;
import com.exam.examserver.exception.ResouceNotFoundException;
import com.exam.examserver.repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

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
                .orElseThrow(() -> new ResouceNotFoundException("Category not found with cid "+ cId));

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
                .orElseThrow(()-> new ResouceNotFoundException("Category not found with cId: "+ cId));

        CategoryDTO dto = new CategoryDTO(category.getCid(),category.getTitle(),category.getDescription());
        categoryRepository.delete(category);
        return dto;
    }

}
