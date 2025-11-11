package com.example.ecommerce.preloader;

import com.example.ecommerce.DTO.ProductDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Stock;
import com.example.ecommerce.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class ProductPreload implements CommandLineRunner {

    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String PRODUCT_KEY_PREFIX = "products::";
    private final String STOCK_KEY_PREFIX = "stock:";
    private final String PRODUCT_IDS_KEY = "products:all_ids";

    public ProductPreload(ProductService productService, RedisTemplate<String, Object> redisTemplate) {
        this.productService = productService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Iterable<Product> products = productService.getAllProducts();

        for (Product p : products) {
            Stock stock = p.getStock();
            Integer currentStock = (stock.getCurrentValue() != null) ? stock.getCurrentValue() : 0;

            ProductDTO dto = new ProductDTO (
                    p.getId(),
                    p.getName(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getDiscount(),
                    p.getImageLink()
            );

            // cache product metadata
            redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + p.getId(), dto);

            // cache stock counter separately
            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + p.getId(), currentStock);

            // cache for retrieving all ids of product entity
            redisTemplate.opsForSet().add(PRODUCT_IDS_KEY, p.getId().toString());
        }
        System.out.println("Redis preloaded with products");
    }
}
