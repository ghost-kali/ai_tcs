package com.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.model.elasticsearch.ProductDocument;
import com.ecommerce.product.repository.elasticsearch.ProductSearchRepository;
import com.ecommerce.product.repository.jpa.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceImplTest {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ElasticsearchTemplate elasticsearchTemplate;

    @Mock
    private org.modelmapper.ModelMapper modelMapper;

    @InjectMocks
    private ProductSearchServiceImpl service;

    @Test
    void searchProducts_whenQueryBlank_usesActiveSearchRepository() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        ProductDocument doc = ProductDocument.builder().productId(1L).productName("Phone").active(true).build();
        when(productSearchRepository.findByActiveTrue(pageable)).thenReturn(new PageImpl<>(List.of(doc)));

        ProductDTO dto = new ProductDTO();
        dto.setProductId(1L);
        dto.setProductName("Phone");
        when(modelMapper.map(doc, ProductDTO.class)).thenReturn(dto);

        // When
        Page<ProductDTO> result = service.searchProducts("   ", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo(1L);
        verify(productSearchRepository).findByActiveTrue(pageable);
    }

    @Test
    void reindexAllProducts_deletesAndIndexesAll() {
        // Given: one product returned by DB
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Phone");
        product.setPrice(new BigDecimal("10.00"));
        product.setQuantity(2);
        product.setActive(true);
        Category category = new Category();
        category.setCategoryId(5L);
        category.setCategoryName("Electronics");
        product.setCategory(category);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

        // When
        service.reindexAllProducts();

        // Then: service clears index and re-saves documents
        verify(productSearchRepository).deleteAll();
        verify(productSearchRepository).saveAll(any());
    }
}
