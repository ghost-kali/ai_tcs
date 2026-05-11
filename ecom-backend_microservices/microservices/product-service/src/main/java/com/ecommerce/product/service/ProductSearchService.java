package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.SearchCriteria;
import com.ecommerce.product.model.elasticsearch.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductSearchService {

    // Indexing operations
    void indexProduct(ProductDocument product);


    void deleteProductFromIndex(Long productId);

    void reindexAllProducts();

    // Search operations

    Page<ProductDTO> advancedSearch(SearchCriteria criteria, Pageable pageable);

    // Auto-complete suggestions
    List<String> getAutoCompleteSuggestions(String prefix);

}

