package com.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.product.dto.CategoryDTO;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.repository.jpa.CategoryRepository;
import com.ecommerce.product.repository.jpa.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private org.modelmapper.ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void updateCategory_whenSelfParent_throwsIllegalArgumentException() {
        // Given: an existing category in DB
        Category existing = new Category();
        existing.setCategoryId(5L);
        existing.setCategoryName("Existing");

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(existing));

        // And: request tries to set parentId = same categoryId (not allowed)
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Updated");
        request.setParentId(5L);

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(5L, request));
    }

    @Test
    void deleteCategory_whenHasProducts_throwsIllegalStateException() {
        // Given
        Category existing = new Category();
        existing.setCategoryId(9L);
        existing.setCategoryName("C");

        when(categoryRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(productRepository.countByCategoryId(9L)).thenReturn(1L);

        // When + Then
        assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(9L));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void isCategoryNameUnique_withExcludeId_invertsExists() {
        // Given
        when(categoryRepository.existsByCategoryNameAndCategoryIdNot("Shoes", 2L)).thenReturn(true);

        // When + Then
        assertThat(categoryService.isCategoryNameUnique("Shoes", 2L)).isFalse();
    }

    @Test
    void createCategory_withParent_publishesEventAndMapsResponse() {
        // Given: create request with a parent category
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Mobiles");
        request.setParentId(1L);

        Category mapped = new Category();
        mapped.setCategoryName("Mobiles");

        Category parent = new Category();
        parent.setCategoryId(1L);
        parent.setCategoryName("Electronics");

        Category saved = new Category();
        saved.setCategoryId(10L);
        saved.setCategoryName("Mobiles");
        saved.setParent(parent);

        CategoryDTO mappedBack = new CategoryDTO();
        mappedBack.setCategoryId(10L);
        mappedBack.setCategoryName("Mobiles");

        when(modelMapper.map(request, Category.class)).thenReturn(mapped);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(productRepository.countByCategoryId(10L)).thenReturn(0L);
        when(modelMapper.map(saved, CategoryDTO.class)).thenReturn(mappedBack);

        // When
        CategoryDTO result = categoryService.createCategory(request);

        // Then
        assertThat(result.getCategoryId()).isEqualTo(10L);
    }

    @Test
    void getCategoryById_whenMissing_throwsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(404L));
    }
}
