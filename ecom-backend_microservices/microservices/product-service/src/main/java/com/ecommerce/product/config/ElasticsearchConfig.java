package com.ecommerce.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.ecommerce.product.repository.elasticsearch")
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    
    // Prefer Spring Boot's `spring.elasticsearch.uris` but allow older `spring.data.elasticsearch.cluster-nodes` too.
    // Accept values like "http://localhost:9200" and normalize to "localhost:9200".
    @Value("${spring.elasticsearch.uris:${spring.data.elasticsearch.cluster-nodes:localhost:9200}}")
    private String uris;
    
    @Override
    public ClientConfiguration clientConfiguration() {
        List<String> hosts = Arrays.stream(uris.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::stripScheme)
                .collect(Collectors.toList());

        return ClientConfiguration.builder()
                .connectedTo(hosts.toArray(String[]::new))
                .build();
    }

    private String stripScheme(String uri) {
        String v = uri;
        if (v.startsWith("http://")) {
            v = v.substring("http://".length());
        } else if (v.startsWith("https://")) {
            v = v.substring("https://".length());
        }
        // Remove any trailing path
        int slash = v.indexOf('/');
        if (slash >= 0) {
            v = v.substring(0, slash);
        }
        return v;
    }
} 
