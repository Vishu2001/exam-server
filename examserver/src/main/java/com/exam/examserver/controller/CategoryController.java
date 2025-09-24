package com.exam.examserver.controller;

import com.exam.examserver.dto.ApiResponse;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.exception.DuplicateCategoryException;
import com.exam.examserver.service.CategoryService;
import com.exam.examserver.service.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @PostMapping(value = "/")
    public ResponseEntity<ApiResponse<Category>> addCategory(@RequestBody Category category){
        Category category1 = this.categoryService.addCategory(category);
        ApiResponse<Category> response = new ApiResponse<>("Category created successfully",category1);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




}
