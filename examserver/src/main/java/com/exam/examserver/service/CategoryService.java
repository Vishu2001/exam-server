package com.exam.examserver.service;

import com.exam.examserver.dto.CategoryDTO;
import com.exam.examserver.entity.exam.Category;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CategoryService {

     Category addCategory(Category category);
     Category updateCategory(Category category);
     Set<Category> getCategory();
     Category getCategory(Long cId);
     void deleteCategory(Long categoryId);
     CategoryDTO patchCategory(Long cId, Map<String,Object> updates);
     CategoryDTO deleteAndReturn(Long cId);

     CategoryDTO softDeleteCategory(Long cId);

     //modified fetch apis
     CategoryDTO getCategoryByCid(Long cId);
     Page<CategoryDTO> listCategories(int page,int size,boolean includeDeleted);

}
