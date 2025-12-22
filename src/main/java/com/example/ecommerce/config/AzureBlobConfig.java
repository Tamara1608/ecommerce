package com.example.ecommerce.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class AzureBlobConfig {

    private String accountName = "eccomstorage";
    private String containerName = "imgs";

    @Bean
    public BlobContainerClient blobContainerClient() {
        String connectStr = String.format(
                accountName);

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();

        return serviceClient.getBlobContainerClient(containerName);
    }
}
