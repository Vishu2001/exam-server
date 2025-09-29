package com.exam.examserver.controller;

import com.exam.examserver.dto.ApiResponse;
import com.exam.examserver.dto.CategoryDTO;
import com.exam.examserver.entity.exam.Category;
import com.exam.examserver.exception.DuplicateCategoryException;
import com.exam.examserver.exception.ResouceNotFoundException;
import com.exam.examserver.service.CategoryService;
import com.exam.examserver.service.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


@RestController
@RequestMapping("/v1/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    //add category
    @PostMapping("/")
    public ResponseEntity<ApiResponse<Category>> addCategory(@RequestBody Category category){
        Category category1 = this.categoryService.addCategory(category);
        ApiResponse<Category> response = new ApiResponse<>("Category created successfully",category1);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //get Category
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> getCategory(@PathVariable("categoryId") Long categoryId){
        Category category = this.categoryService.getCategory(categoryId);
        ApiResponse<Category> response = new ApiResponse<>("Category Fetched Successfully",category);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //get all categories
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Set<Category>>> getAllCategories(){
        Set<Category> categories = this.categoryService.getCategory();
        ApiResponse<Set<Category>> response = new ApiResponse<>("All Categories fetched Successfully",categories);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    //update Category
//    @PutMapping("/")
//    public ResponseEntity<ApiResponse<Category>> updateCategory(@RequestBody Category category){
//        Category category1 = this.categoryService.updateCategory(category);
//        ApiResponse<Category> response = new ApiResponse<>("Update category successful",category1);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    //update Category
    @PatchMapping(value = "/{cid}",consumes = "application/json")
    public ResponseEntity<ApiResponse<CategoryDTO>> patchCategory(
            @PathVariable("cid") Long cid,
            @RequestBody Map<String, Object> updates){
            CategoryDTO updated = categoryService.patchCategory(cid, updates);
            ApiResponse<CategoryDTO> response = new ApiResponse<>("Category patched successfully",updated);
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //delete Category
    @DeleteMapping(value = "/{cId}")
    public ResponseEntity<ApiResponse<CategoryDTO>> deleteCategory(@PathVariable ("cId") Long cId){
        try {

            CategoryDTO deleted = categoryService.deleteAndReturn(cId);
            ApiResponse<CategoryDTO> response = new ApiResponse<>("Category deleted sucessfully", deleted);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResouceNotFoundException e) {
            ApiResponse<CategoryDTO> response = new ApiResponse<>(e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
