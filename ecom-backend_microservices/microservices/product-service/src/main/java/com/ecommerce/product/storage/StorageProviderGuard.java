package com.ecommerce.product.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class StorageProviderGuard implements ApplicationRunner {

    private final String storageProvider;

    StorageProviderGuard(@Value("${storage.provider:azure}") String storageProvider) {
        this.storageProvider = storageProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!"azure".equalsIgnoreCase(storageProvider)) {
            throw new IllegalStateException("Only Azure storage is supported. Set storage.provider=azure.");
        }
    }
}

