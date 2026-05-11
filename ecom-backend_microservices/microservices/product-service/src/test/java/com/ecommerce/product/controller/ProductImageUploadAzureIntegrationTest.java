package com.ecommerce.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.ProductServiceApplication;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.jpa.ProductRepository;
import com.ecommerce.product.service.ProductSearchService;
import com.ecommerce.product.storage.ProductImageStorage;

@SpringBootTest(
        classes = { ProductServiceApplication.class, ProductImageUploadAzureIntegrationTest.Config.class },
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.config.import=optional:configserver:",
                "spring.cloud.config.fail-fast=false",
                "spring.cloud.config.import-check.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:productdb2;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "app.elasticsearch.enabled=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration"
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductImageUploadAzureIntegrationTest {

    @MockBean
    private ProductSearchService productSearchService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FakeAzureStorage fakeAzureStorage;

    @BeforeEach
    void setUpAuth() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin",
                "N/A",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadProductImage_usesAzureProvider_andPersistsUrl() throws Exception {
        Product product = new Product();
        product.setProductName("Test Product");
        product.setQuantity(10);
        product.setPrice(new BigDecimal("99.99"));
        product.setSellerId(1L);
        Product saved = productRepository.save(product);

        MockMultipartFile file = new MockMultipartFile("image", "hello.png", "image/png", "png-bytes".getBytes());

        mockMvc.perform(multipart("/api/products/{productId}/image", saved.getProductId()).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(saved.getProductId()))
                .andExpect(jsonPath("$.image").value("https://ecomtcs.blob.core.windows.net/ecom-product-images/products/"
                        + saved.getProductId() + "/hello.png"));

        Product reloaded = productRepository.findById(saved.getProductId()).orElseThrow();
        assertThat(reloaded.getImage()).isEqualTo("https://ecomtcs.blob.core.windows.net/ecom-product-images/products/"
                + saved.getProductId() + "/hello.png");

        assertThat(fakeAzureStorage.lastProductId.get()).isEqualTo(saved.getProductId());
        assertThat(fakeAzureStorage.lastOriginalFilename.get()).isEqualTo("hello.png");
    }

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        FakeAzureStorage productImageStorage() {
            return new FakeAzureStorage();
        }
    }

    static class FakeAzureStorage implements ProductImageStorage {
        private final AtomicLong lastProductId = new AtomicLong(-1);
        private final AtomicReference<String> lastOriginalFilename = new AtomicReference<>();
        private final AtomicReference<String> lastDeletedRef = new AtomicReference<>();

        @Override
        public String uploadProductImage(long productId, org.springframework.web.multipart.MultipartFile file) throws IOException {
            lastProductId.set(productId);
            lastOriginalFilename.set(file.getOriginalFilename());
            return "https://ecomtcs.blob.core.windows.net/ecom-product-images/products/" + productId + "/"
                    + file.getOriginalFilename();
        }

        @Override
        public void deleteProductImage(String imageRef) throws IOException {
            lastDeletedRef.set(imageRef);
        }
    }
}
