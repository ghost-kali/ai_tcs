package com.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.jpa.CategoryRepository;
import com.ecommerce.product.repository.jpa.ProductRepository;
import com.ecommerce.product.storage.ProductImageStorage;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private org.modelmapper.ModelMapper modelMapper;

    @Mock
    private ProductSearchService productSearchService;

    @Mock
    private ProductImageStorage productImageStorage;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_appliesDiscount_setsCategory_indexesInElasticsearch() {
        // Given
        ProductDTO requestDto = new ProductDTO();
        requestDto.setProductName("Phone");
        requestDto.setCategoryId(10L);

        Product mappedEntity = new Product();
        mappedEntity.setProductName("Phone");
        mappedEntity.setPrice(new BigDecimal("100.00"));
        mappedEntity.setDiscount(10.0);

        Category category = new Category();
        category.setCategoryId(10L);
        category.setCategoryName("Electronics");

        Product savedEntity = new Product();
        savedEntity.setProductId(99L);
        savedEntity.setProductName("Phone");
        savedEntity.setPrice(new BigDecimal("100.00"));
        savedEntity.setDiscount(10.0);
        savedEntity.setSpecialPrice(new BigDecimal("90.00"));
        savedEntity.setQuantity(5);
        savedEntity.setCategory(category);

        ProductDTO responseDto = new ProductDTO();
        responseDto.setProductId(99L);
        responseDto.setProductName("Phone");

        when(modelMapper.map(requestDto, Product.class)).thenReturn(mappedEntity);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, ProductDTO.class)).thenReturn(responseDto);

        // When
        ProductDTO actual = productService.createProduct(requestDto);

        // Then
        assertThat(actual.getProductId()).isEqualTo(99L);
        verify(productSearchService).indexProduct(any(com.ecommerce.product.model.elasticsearch.ProductDocument.class));
    }

    @Test
    void updateProduct_whenProductDoesNotExist_throwsResourceNotFoundException() {
        // Given
        when(productRepository.findById(123L)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(123L, new ProductDTO()));
    }
}
