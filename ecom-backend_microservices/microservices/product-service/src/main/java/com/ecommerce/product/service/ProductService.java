package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    
    ProductDTO createProduct(ProductDTO productDTO);
    
    ProductDTO updateProduct(Long productId, ProductDTO productDTO);
    
    ProductDTO getProductById(Long productId);
    
    Page<ProductDTO> getAllProducts(Pageable pageable);
    
    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    
    Page<ProductDTO> searchProducts(String keyword, Long categoryId, Pageable pageable);

    
    void deleteProduct(Long productId);
    
    ProductDTO updateProductImage(Long productId, MultipartFile image);

    

    

    

} 
