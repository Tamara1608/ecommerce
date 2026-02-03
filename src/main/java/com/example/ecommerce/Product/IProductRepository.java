package com.example.ecommerce.Product;

import org.springframework.lang.NonNull;

import com.example.ecommerce.Product.entity.Product;

import java.util.List;
import java.util.Optional;


public interface IProductRepository {
    
    @NonNull
    Product create(@NonNull Product product);
    
    @NonNull
    List<Product> findAll();
    
    
    @NonNull
    Optional<Product> findById(@NonNull Long id);
    
    @NonNull
    Product update(@NonNull Product product);
  
    void delete(@NonNull Long id);
    
    @NonNull
    Optional<Product> returnIfInStock(@NonNull Long productId, int quantity);
}
