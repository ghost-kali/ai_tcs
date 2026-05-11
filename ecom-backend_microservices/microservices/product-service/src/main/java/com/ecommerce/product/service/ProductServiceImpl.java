package com.ecommerce.product.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.SearchCriteria;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.model.elasticsearch.ProductDocument;
import com.ecommerce.product.repository.jpa.CategoryRepository;
import com.ecommerce.product.repository.jpa.ProductRepository;
import com.ecommerce.product.storage.ProductImageStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    private final ProductSearchService productSearchService;
    private final ProductImageStorage productImageStorage;

    @Override
    @CacheEvict(value = {"products", "productsByCategory", "productSearch"}, allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO) {

        log.info("Creating new product: {}", productDTO.getProductName());

        Product product = modelMapper.map(productDTO, Product.class);

        // Set category
        if (productDTO.getCategoryId() != null) {

            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Category",
                                    "id",
                                    productDTO.getCategoryId()
                            ));

            product.setCategory(category);
        }

        // Calculate special price
        if (product.getDiscount() != null && product.getDiscount() > 0) {

            BigDecimal discountAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(product.getDiscount()))
                    .divide(BigDecimal.valueOf(100));

            product.setSpecialPrice(
                    product.getPrice().subtract(discountAmount)
            );
        }

        Product savedProduct = productRepository.save(product);

        // Elasticsearch sync
        indexProductInElasticsearch(savedProduct);

        return convertToDTO(savedProduct);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = {"productsByCategory", "productSearch"}, allEntries = true)
    })
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {

        log.info("Updating product with id: {}", productId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "id",
                                productId
                        ));

        // Update fields
        existingProduct.setProductName(productDTO.getProductName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setBrand(productDTO.getBrand());
        existingProduct.setDiscount(productDTO.getDiscount());
        existingProduct.setSku(productDTO.getSku());
        existingProduct.setTags(productDTO.getTags());
        existingProduct.setMinOrderQuantity(productDTO.getMinOrderQuantity());
        existingProduct.setMaxOrderQuantity(productDTO.getMaxOrderQuantity());

        // Update category
        if (productDTO.getCategoryId() != null
                && !productDTO.getCategoryId()
                .equals(existingProduct.getCategory().getCategoryId())) {

            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Category",
                                    "id",
                                    productDTO.getCategoryId()
                            ));

            existingProduct.setCategory(category);
        }

        // Recalculate special price
        if (existingProduct.getDiscount() != null
                && existingProduct.getDiscount() > 0) {

            BigDecimal discountAmount = existingProduct.getPrice()
                    .multiply(BigDecimal.valueOf(existingProduct.getDiscount()))
                    .divide(BigDecimal.valueOf(100));

            existingProduct.setSpecialPrice(
                    existingProduct.getPrice().subtract(discountAmount)
            );

        } else {

            existingProduct.setSpecialPrice(existingProduct.getPrice());
        }

        Product updatedProduct = productRepository.save(existingProduct);

        // Elasticsearch update
        indexProductInElasticsearch(updatedProduct);

        return convertToDTO(updatedProduct);
    }

    @Override
    @Cacheable(value = "products", key = "#productId")
    public ProductDTO getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "id",
                                productId
                        ));

        return convertToDTO(product);
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {

        Page<Product> products = productRepository.findAll(pageable);

        return products.map(this::convertToDTO);
    }

    @Override
    @Cacheable(value = "productsByCategory", key = "#categoryId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products for category: {}", categoryId);

        Page<Product> products = productRepository.findByCategoryCategoryId(categoryId, pageable);
        return products.map(this::convertToDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(
            String keyword,
            Long categoryId,
            Pageable pageable
    ) {

        SearchCriteria criteria = SearchCriteria.builder()
                .query(keyword)
                .categoryIds(
                        categoryId == null
                                ? Collections.emptyList()
                                : List.of(categoryId)
                )
                .build();

        return productSearchService.advancedSearch(criteria, pageable);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = {"productsByCategory", "productSearch"}, allEntries = true)
    })
    public void deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "id",
                                productId
                        ));

        // Delete image
        String imageRef = product.getImage();

        if (imageRef != null && !imageRef.isBlank()) {

            try {
                productImageStorage.deleteProductImage(imageRef);

            } catch (Exception ex) {

                log.warn(
                        "Failed to delete image for productId={}",
                        productId,
                        ex
                );
            }
        }

        // Soft delete
        product.setActive(false);

        Product deletedProduct = productRepository.save(product);

        // Update Elasticsearch
        indexProductInElasticsearch(deletedProduct);
    }

    @Override
    @CacheEvict(value = "products", key = "#productId")
    public ProductDTO updateProductImage(
            Long productId,
            MultipartFile image
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product",
                                "id",
                                productId
                        ));

        try {

            String previousImage = product.getImage();

            String imageRef =
                    productImageStorage.uploadProductImage(productId, image);

            product.setImage(imageRef);

            Product updatedProduct = productRepository.save(product);

            // Elasticsearch update
            indexProductInElasticsearch(updatedProduct);

            if (previousImage != null
                    && !previousImage.isBlank()
                    && !previousImage.equals(imageRef)) {

                try {

                    productImageStorage.deleteProductImage(previousImage);

                } catch (Exception ex) {

                    log.warn(
                            "Failed to delete previous image for productId={}",
                            productId,
                            ex
                    );
                }
            }

            return convertToDTO(updatedProduct);

        } catch (IOException e) {

            log.error("Error uploading image", e);

            throw new RuntimeException("Failed to upload image", e);
        }
    }


    private ProductDTO convertToDTO(Product product) {

        ProductDTO dto = modelMapper.map(product, ProductDTO.class);

        dto.setInStock(product.getQuantity() > 0);

        dto.setFinalPrice(
                product.getSpecialPrice() != null
                        ? product.getSpecialPrice()
                        : product.getPrice()
        );

        if (product.getCategory() != null) {

            dto.setCategoryId(product.getCategory().getCategoryId());

            dto.setCategoryName(product.getCategory().getCategoryName());
        }

        return dto;
    }

    // Elasticsearch sync methods

    private void indexProductInElasticsearch(Product product) {

        try {

            ProductDocument document = convertToDocument(product);

            productSearchService.indexProduct(document);

            log.info(
                    "Indexed/Updated product in Elasticsearch: {}",
                    product.getProductId()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to index product in Elasticsearch: {}",
                    product.getProductId(),
                    e
            );
        }
    }

    private ProductDocument convertToDocument(Product product) {

        ProductDocument document = ProductDocument.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .categoryId(
                        product.getCategory() != null
                                ? product.getCategory().getCategoryId()
                                : null
                )
                .categoryName(
                        product.getCategory() != null
                                ? product.getCategory().getCategoryName()
                                : null
                )
                .sku(product.getSku())
                .imageUrl(product.getImage())
                .sellerId(product.getSellerId())
                .discountPercentage(product.getDiscount())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .active(product.getActive())
                .featured(false)
                .createdAt(
                        product.getCreatedAt() == null
                                ? null
                                : product.getCreatedAt().toString()
                )
                .updatedAt(
                        product.getUpdatedAt() == null
                                ? null
                                : product.getUpdatedAt().toString()
                )
                .inStock(product.getQuantity() > 0)
                .build();

        // discounted price

        if (product.getDiscount() != null
                && product.getDiscount() > 0) {

            BigDecimal discount = product.getPrice()
                    .multiply(BigDecimal.valueOf(product.getDiscount()))
                    .divide(BigDecimal.valueOf(100));

            document.setDiscountedPrice(
                    product.getPrice().subtract(discount)
            );

        } else {

            document.setDiscountedPrice(product.getPrice());
        }

        // category hierarchy

        if (product.getCategory() != null
                && product.getCategory().getParent() != null) {

            Category parentCategory = product.getCategory().getParent();

            document.setCategoryHierarchy(
                    ProductDocument.CategoryHierarchy.builder()
                            .parentId(parentCategory.getCategoryId())
                            .parentName(parentCategory.getCategoryName())
                            .childId(product.getCategory().getCategoryId())
                            .childName(product.getCategory().getCategoryName())
                            .build()
            );
        }

        return document;
    }
}