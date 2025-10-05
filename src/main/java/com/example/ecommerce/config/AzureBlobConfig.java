package com.example.ecommerce.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class AzureBlobConfig {
//    @Value("${azure.storage.account-name}")
    private String accountName = "eccomstorage";

//    @Value("${azure.storage.account-key}")

//    @Value("${azure.storage.container-name}")
    private String containerName = "imgs";

    @Bean
    public BlobContainerClient blobContainerClient() {
        String connectStr = String.format(
                "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                accountName,
                accountKey
        );

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();

        return serviceClient.getBlobContainerClient(containerName);
    }
}
