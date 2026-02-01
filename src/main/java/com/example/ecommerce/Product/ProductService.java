package com.example.ecommerce.Product;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.Product.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service for Product CRUD operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class ProductService implements IProductService {
    
    private final IProductRepository productRepository;
    
    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @NonNull
    public Product create(@NonNull Product product) {
        return productRepository.create(product);
    }
    
    @NonNull
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    @NonNull
    public Product findById(@NonNull Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                ));
    }
    
    @NonNull
    public Product update(@NonNull Product product) {
        if (product.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required for update");
        }
        productRepository.findById(product.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + product.getId()
                ));
        return productRepository.update(product);
    }
    
    public void delete(@NonNull Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                ));
        productRepository.delete(id);
    }
    
    @Override
    @NonNull
    public Optional<Product> returnIfInStock(@NonNull Long productId, int quantity) {
        return productRepository.returnIfInStock(productId, quantity);
    }
}

