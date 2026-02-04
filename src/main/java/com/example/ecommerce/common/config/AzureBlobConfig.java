package com.example.ecommerce.common.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class AzureBlobConfig {

    @Value("${azure.storage.container-name:your-container-name}")
    private String containerName;

    @Value("${azure.storage.key:your-account-key}")
    private String accountKey;

    @Value("${azure.storage.account-name:your-account-name}")
    private String accountName;
      
    @Bean
    public BlobContainerClient blobContainerClient() {
        String connectStr = String.format(
            "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
            accountName,
            accountKey);
                            
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();

        return serviceClient.getBlobContainerClient(containerName);
    }
}
