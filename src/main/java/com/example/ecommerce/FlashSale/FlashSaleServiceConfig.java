package com.example.ecommerce.FlashSale;

import com.example.ecommerce.Order.OrderService;
import com.example.ecommerce.Product.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for FlashSaleService beans.
 * Creates two service instances - one with caching, one without.
 */
@Configuration
public class FlashSaleServiceConfig {
    
    @Bean
    @Qualifier("dbFlashSaleService")
    public FlashSaleService dbFlashSaleService(
            @Qualifier("dbFlashSaleRepository") IFlashSaleRepository flashSaleRepository,
            @Qualifier("dbProductService") ProductService productService,
            @Qualifier("dbOrderService") OrderService orderService) {
        return new FlashSaleService(flashSaleRepository, productService, orderService);
    }
    
    @Bean
    @Qualifier("cachedFlashSaleService")
    public FlashSaleService cachedFlashSaleService(
            @Qualifier("cachedFlashSaleRepository") IFlashSaleRepository flashSaleRepository,
            @Qualifier("cachedProductService") ProductService productService,
            @Qualifier("cachedOrderService") OrderService orderService) {
        return new FlashSaleService(flashSaleRepository, productService, orderService);
    }

}

