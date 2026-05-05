package com.ecommerce.product.storage;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageStorage {
    String uploadProductImage(long productId, MultipartFile file) throws IOException;

    void deleteProductImage(String imageRef) throws IOException;
}
