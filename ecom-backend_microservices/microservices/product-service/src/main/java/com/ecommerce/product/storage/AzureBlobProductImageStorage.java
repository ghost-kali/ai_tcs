package com.ecommerce.product.storage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;

@Component
public class AzureBlobProductImageStorage implements ProductImageStorage {

    private final BlobContainerClient containerClient;
    private final String publicBaseUrl;
    private final String containerName;
    private final String accountName;

    public AzureBlobProductImageStorage(
            @Value("${storage.azure.connection-string}") String connectionString,
            @Value("${storage.azure.container}") String containerName,
            @Value("${storage.azure.account-name:}") String accountName,
            @Value("${storage.azure.public-base-url:}") String publicBaseUrl) {

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerName = containerName;
        this.accountName = accountName;
        this.publicBaseUrl = publicBaseUrl;
        this.containerClient = serviceClient.getBlobContainerClient(containerName);
    }

    @Override
    public String uploadProductImage(long productId, MultipartFile file) throws IOException {
        containerClient.createIfNotExists();

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) {
                ext = originalName.substring(dot);
            }
        }

        String blobName = "products/" + productId + "/" + UUID.randomUUID() + ext;
        BlobClient blob = containerClient.getBlobClient(blobName);

        blob.upload(file.getInputStream(), file.getSize(), true);
        if (StringUtils.hasText(file.getContentType())) {
            blob.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));
        }

        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + blobName;
        }

        if (StringUtils.hasText(accountName)) {
            return "https://" + accountName + ".blob.core.windows.net/" + containerName + "/" + blobName;
        }

        return blob.getBlobUrl();
    }

    @Override
    public void deleteProductImage(String imageRef) throws IOException {
        if (!StringUtils.hasText(imageRef)) {
            return;
        }

        String blobName = resolveBlobName(imageRef);
        if (!StringUtils.hasText(blobName)) {
            return;
        }

        if (!blobName.startsWith("products/")) {
            return;
        }

        containerClient.getBlobClient(blobName).deleteIfExists();
    }

    private String resolveBlobName(String imageRef) {
        String trimmed = imageRef.trim();

        try {
            URI uri = new URI(trimmed);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return "";
            }
            String normalized = path.replaceFirst("^/+", "");

            String prefix = containerName + "/";
            if (normalized.startsWith(prefix)) {
                return normalized.substring(prefix.length());
            }

            return normalized;
        } catch (URISyntaxException ignored) {
            // Not a URL; assume it's a blob name
            return trimmed;
        }
    }
}
