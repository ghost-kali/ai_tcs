package com.ecommerce.product.service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch._types.FieldValue;
import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.SearchCriteria;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.model.elasticsearch.ProductDocument;
import com.ecommerce.product.repository.elasticsearch.ProductSearchRepository;
import com.ecommerce.product.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ProductRepository productRepository;

    @Override
    public void indexProduct(ProductDocument product) {
        if (product == null || product.getProductId() == null) {
            return;
        }
        productSearchRepository.save(product);
    }



    @Override
    public void deleteProductFromIndex(Long productId) {
        if (productId == null) {
            return;
        }
        productSearchRepository.deleteById(productId);
    }

    @Override
    @Async
    @Transactional(readOnly = true)
    public void reindexAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            List<ProductDocument> docs = products.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());
            productSearchRepository.deleteAll();
            productSearchRepository.saveAll(docs);
        } catch (Exception e) {
            log.error("Failed to reindex products", e);
        }
    }



    @Override
    public Page<ProductDTO> advancedSearch(SearchCriteria criteria, Pageable pageable) {
        try {
            SearchCriteria safeCriteria = criteria == null ? new SearchCriteria() : criteria;
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(es -> es.bool(b -> {
                        // Basic version: only return active products.
                        // Many rows/docs have `active` missing/null, so treat missing as active for now.
                        b.filter(f -> f.bool(activeBool -> activeBool
                                .should(s -> s.term(t -> t.field("active").value(true)))
                                .should(s -> s.bool(missing -> missing.mustNot(m -> m.exists(e -> e.field("active")))))
                                .minimumShouldMatch("1")));

                        String q = safeCriteria.getQuery() == null ? null : safeCriteria.getQuery().trim();
                        if (q != null && !q.isBlank()) {
                            b.must(m -> m.multiMatch(mm -> mm
                                    .query(q)
                                    .fields("productName", "description", "brand", "categoryName")
                                    .type(TextQueryType.BestFields)));
                        }

                        if (safeCriteria.getCategoryIds() != null && !safeCriteria.getCategoryIds().isEmpty()) {
                            List<FieldValue> categoryValues = safeCriteria.getCategoryIds().stream()
                                    .filter(Objects::nonNull)
                                    .map(FieldValue::of)
                                    .collect(Collectors.toList());
                            b.filter(f -> f.terms(t -> t.field("categoryId").terms(tv -> tv.value(categoryValues))));
                        }

                        return b;
                    }))
                    .withPageable(pageable)
                    .build();

            log.info("Executing Elasticsearch search on index='products' query='{}' categoryIds={} page={} size={}",
                    safeCriteria.getQuery(),
                    safeCriteria.getCategoryIds(),
                    pageable == null ? null : pageable.getPageNumber(),
                    pageable == null ? null : pageable.getPageSize());

            SearchHits<ProductDocument> hits = elasticsearchTemplate.search(nativeQuery, ProductDocument.class);
            List<ProductDTO> items = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Elasticsearch search query='{}' categoryIds={} -> totalHits={}",
                    safeCriteria.getQuery(),
                    safeCriteria.getCategoryIds(),
                    hits.getTotalHits());

            return new PageImpl<>(items, pageable, hits.getTotalHits());
        } catch (Exception e) {
            log.error("Error executing product search (query='{}')", criteria == null ? null : criteria.getQuery(), e);
            return Page.empty(pageable);
        }
    }



    @Override
    public List<String> getAutoCompleteSuggestions(String prefix) {
        String safePrefix = prefix == null ? "" : prefix.trim();
        if (safePrefix.isBlank()) {
            return Collections.emptyList();
        }

        // Keep results fast and predictable.
        int limit = 10;
        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(es -> es.bool(b -> {
                        // Only active products (missing treated as active).
                        b.filter(f -> f.bool(activeBool -> activeBool
                                .should(s -> s.term(t -> t.field("active").value(true)))
                                .should(s -> s.bool(missing -> missing.mustNot(m -> m.exists(e -> e.field("active")))))
                                .minimumShouldMatch("1")));

                        // Autocomplete signals.
                        b.should(s -> s.matchPhrasePrefix(mpp -> mpp.field("productName").query(safePrefix).boost(3.0f)));
                        b.should(s -> s.matchPhrasePrefix(mpp -> mpp.field("categoryName").query(safePrefix).boost(2.0f)));
                        // brand is keyword, so prefix is the cheapest option; use case-insensitive when supported.
                        b.should(s -> s.prefix(p -> p.field("brand").value(safePrefix).caseInsensitive(true).boost(1.5f)));
                        b.minimumShouldMatch("1");
                        return b;
                    }))
                    .withPageable(PageRequest.of(0, limit))
                    .build();

            SearchHits<ProductDocument> hits = elasticsearchTemplate.search(query, ProductDocument.class);
            Set<String> out = new LinkedHashSet<>();
            for (SearchHit<ProductDocument> hit : hits) {
                ProductDocument doc = hit.getContent();
                if (doc == null) {
                    continue;
                }
                addSuggestionIfMatches(out, doc.getProductName(), safePrefix, limit);
                addSuggestionIfMatches(out, doc.getBrand(), safePrefix, limit);
                addSuggestionIfMatches(out, doc.getCategoryName(), safePrefix, limit);
                if (out.size() >= limit) {
                    break;
                }
            }
            return new ArrayList<>(out);
        } catch (Exception e) {
            log.warn("Failed to fetch autocomplete suggestions for prefix='{}'", safePrefix, e);
            return Collections.emptyList();
        }
    }


    private void addSuggestionIfMatches(Set<String> output, String candidate, String prefix, int limit) {
        if (output.size() >= limit) {
            return;
        }
        if (candidate == null) {
            return;
        }
        String trimmed = candidate.trim();
        if (trimmed.isBlank()) {
            return;
        }

        String p = prefix.toLowerCase();
        String c = trimmed.toLowerCase();

        // Match if the whole string or any token starts with the prefix.
        boolean matches = c.startsWith(p);
        if (!matches) {
            String[] parts = c.split("\\s+");
            for (String part : parts) {
                if (part.startsWith(p)) {
                    matches = true;
                    break;
                }
            }
        }

        if (matches) {
            output.add(trimmed);
        }
    }

    private ProductDTO convertToDTO(ProductDocument document) {
        if (document == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setProductId(document.getProductId());
        dto.setProductName(document.getProductName());
        dto.setDescription(document.getDescription());
        dto.setBrand(document.getBrand());
        dto.setPrice(document.getPrice());
        dto.setQuantity(document.getQuantity());
        dto.setCategoryId(document.getCategoryId());
        dto.setCategoryName(document.getCategoryName());
        dto.setSku(document.getSku());
        dto.setSellerId(document.getSellerId());
        dto.setActive(document.getActive());
        dto.setRating(document.getRating());
        dto.setReviewCount(document.getReviewCount());
        dto.setCreatedAt(parseLenientDateTime(document.getCreatedAt()));
        dto.setUpdatedAt(parseLenientDateTime(document.getUpdatedAt()));
        dto.setInStock(Boolean.TRUE.equals(document.getInStock()));

        // DTO uses `image`, ES doc uses `imageUrl`
        dto.setImage(document.getImageUrl());

        // Keep existing REST contract: specialPrice/finalPrice derived from discountedPrice
        dto.setFinalPrice(document.getDiscountedPrice());
        dto.setSpecialPrice(document.getDiscountedPrice());

        return dto;
    }

    private LocalDateTime parseLenientDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        try {
            // "2026-05-05T12:30:00"
            return LocalDateTime.parse(v);
        } catch (Exception ignored) {
        }
        try {
            // "2026-05-05" -> start of day
            return LocalDate.parse(v).atStartOfDay();
        } catch (Exception ignored) {
        }
        return null;
    }

    private ProductDocument convertToDocument(Product product) {
        if (product == null) {
            return null;
        }

        ProductDocument.ProductDocumentBuilder builder = ProductDocument.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .sku(product.getSku())
                .imageUrl(product.getImage())
                .sellerId(product.getSellerId())
                .discountPercentage(product.getDiscount())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .active(product.getActive() == null ? true : product.getActive())
                .featured(false)
                .createdAt(product.getCreatedAt() == null ? null : product.getCreatedAt().toString())
                .updatedAt(product.getUpdatedAt() == null ? null : product.getUpdatedAt().toString())
                .inStock(product.getQuantity() != null && product.getQuantity() > 0);

        ProductDocument doc = builder.build();

        // discountedPrice
        if (product.getDiscount() != null && product.getDiscount() > 0 && product.getPrice() != null) {
            BigDecimal discountAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(product.getDiscount()))
                    .divide(BigDecimal.valueOf(100));
            doc.setDiscountedPrice(product.getPrice().subtract(discountAmount));
        } else {
            doc.setDiscountedPrice(product.getPrice());
        }

        // category hierarchy (optional)
        if (product.getCategory() != null && product.getCategory().getParent() != null) {
            Category parent = product.getCategory().getParent();
            doc.setCategoryHierarchy(ProductDocument.CategoryHierarchy.builder()
                    .parentId(parent.getCategoryId())
                    .parentName(parent.getCategoryName())
                    .childId(product.getCategory().getCategoryId())
                    .childName(product.getCategory().getCategoryName())
                    .build());
        }

        return doc;
    }
}