package com.example.ecommerce.flashsale.config;

import com.example.ecommerce.flashsale.infrastructure.persistence.flashsale.IFlashSaleRepository;
import com.example.ecommerce.flashsale.app.FlashSaleService;
import com.example.ecommerce.order.app.OrderService;    
import com.example.ecommerce.product.app.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

