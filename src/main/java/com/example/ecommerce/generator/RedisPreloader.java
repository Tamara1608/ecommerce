package com.example.ecommerce.generator;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisPreloader implements CommandLineRunner {

    private final ProductService productService;

    public RedisPreloader(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        Iterable<Product> products = productService.getAllProducts();
        for (Product p : products) {
            productService.createProduct(p); // this will save to DB (no change) + cache
        }
        System.out.println("Redis preloaded with products.");
    }
}
