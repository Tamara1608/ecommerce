package com.example.ecommerce.generator;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductGenerator implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    // Example: replace these with actual Azure image URLs
    private final List<String> azureImageLinks = List.of(
            "https://azurecdn.example.com/product1.png",
            "https://azurecdn.example.com/product2.png",
            "https://azurecdn.example.com/product3.png",
            "https://azurecdn.example.com/product4.png",
            "https://azurecdn.example.com/product5.png"
    );

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            for (int i = 1; i <= 500; i++) {
                Product product = new Product();
                product.setName("Product " + i);
                product.setDescription("This is the description for product " + i);
                product.setPrice(ThreadLocalRandom.current().nextDouble(5.0, 500.0));

                int totalStock = ThreadLocalRandom.current().nextInt(10, 101); // totalStock between 10-100
                product.setTotalStock(totalStock);

                int stock = ThreadLocalRandom.current().nextInt(0, totalStock + 1); // current stock <= totalStock
                product.setStock(stock);

                int percentageOff = ThreadLocalRandom.current().nextInt(0, 71);
                product.setPercentageOff(percentageOff);

                String imageLink = azureImageLinks.get(ThreadLocalRandom.current().nextInt(azureImageLinks.size()));
                product.setImageLink(imageLink);

                productRepository.save(product);
            }
            System.out.println("Inserted 500 products successfully!");
        } else {
            System.out.println("Products already exist, skipping insertion.");
        }
    }
}

