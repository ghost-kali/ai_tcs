package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.SearchCriteria;
import com.ecommerce.product.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/advanced-search")
@RequiredArgsConstructor
@Tag(name = "Product Search", description = "Advanced product search endpoints")
public class ProductSearchController {
    
    private final ProductSearchService productSearchService;
    

    
    @GetMapping("/suggestions")
    @Operation(summary = "Get autocomplete suggestions")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(
            @RequestParam String prefix) {
        
        List<String> suggestions = productSearchService.getAutoCompleteSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }
    

} 