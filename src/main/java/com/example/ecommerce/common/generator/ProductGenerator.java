package com.example.ecommerce.common.generator;

import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;
import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.domain.Stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductGenerator implements CommandLineRunner {

    @Autowired
    private ProductTable productRepository;

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

                double price = ThreadLocalRandom.current().nextDouble(5.0, 500.0);
                price = Math.round(price * 100.0) / 100.0;  // round to 2 decimal places
                product.setPrice(price);

                int discount = ThreadLocalRandom.current().nextInt(0, 8 + 1) * 10;  // 0,10,20,...,80
                product.setDiscount(discount);
                String imageLink = azureImageLinks.get(ThreadLocalRandom.current().nextInt(azureImageLinks.size()));
                product.setImageLink(imageLink);


                // creating stocks
                int totalStock = ThreadLocalRandom.current().nextInt(10, 101); // totalStock between 10-100
                int currentStock = ThreadLocalRandom.current().nextInt(0, totalStock + 1); // current stock <= totalStock

                Stock stock = new Stock();
                stock.setTotalStock(totalStock);
                stock.setCurrentValue(currentStock);
                stock.setUpdatedAt(LocalDateTime.now());
               

                product.setStock(stock);

                productRepository.save(product);
            }
            System.out.println("Inserted 500 products successfully!");
        } else {
            System.out.println("Products already exist, skipping insertion.");
        }
    }
}

