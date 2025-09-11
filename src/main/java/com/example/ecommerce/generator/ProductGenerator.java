package com.example.ecommerce.generator;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class ProductGenerator implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only insert if the table is empty (so it runs just once)
        if (productRepository.count() == 0) {
            for (int i = 1; i <= 500; i++) {
                Product product = new Product();
                product.setName("Product " + i);
                product.setDescription("This is the description for product " + i);
                product.setPrice(ThreadLocalRandom.current().nextDouble(5.0, 500.0));
                product.setStock(ThreadLocalRandom.current().nextInt(0, 100));
                productRepository.save(product);
            }
            System.out.println("Inserted 500 products successfully!");
        } else {
            System.out.println("Products already exist, skipping insertion.");
        }
    }
}
