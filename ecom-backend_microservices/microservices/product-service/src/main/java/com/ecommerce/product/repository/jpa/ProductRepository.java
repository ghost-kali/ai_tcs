package com.ecommerce.product.repository.jpa;

import com.ecommerce.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {




    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId AND p.active = true")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    


    Page<Product> findByCategoryCategoryId(Long categoryId,    Pageable pageable);
}
