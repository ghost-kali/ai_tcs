package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    
    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
    
    CategoryDTO getCategoryById(Long categoryId);


    @Cacheable(value = "categories", key = "'page_' + #pageNumber + '_' + #pageSize")
    List<CategoryDTO> getAllCategories();

    List<CategoryDTO> getActiveCategories();
    
    List<CategoryDTO> getRootCategories();
    
    List<CategoryDTO> getChildCategories(Long parentId);
    
    void deleteCategory(Long categoryId);
    
    void activateCategory(Long categoryId);
    
    void deactivateCategory(Long categoryId);
    
    boolean isCategoryNameUnique(String categoryName, Long excludeId);
} 