package com.github.rha.storagerest;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Value("${storage.account.key}")
    private String storageAccountKey;

    @Value("${storage.account.endpoint}")
    private String storageAccountEndpoint;

    @Value("${storage.account.name}")
    private String storageAccountName;

    @Value("${storage.account.container}")
    private String storageAccountContainer;

    @Bean
    public BlobServiceClient blobServiceClient() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .credential(new StorageSharedKeyCredential(storageAccountName, storageAccountKey))
                .endpoint(storageAccountEndpoint)
                .buildClient();
        return blobServiceClient;
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient containerClient = null;
        try {
            containerClient = blobServiceClient.createBlobContainerIfNotExists(storageAccountContainer);
        } catch (BlobStorageException ex) {
            // The container may already exist, so don't throw an error
            if (!ex.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                throw ex;
            }
        }
        if (containerClient == null) {
            containerClient = blobServiceClient.getBlobContainerClient(storageAccountContainer);
        }
        return containerClient;
    }
}
