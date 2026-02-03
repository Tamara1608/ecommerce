package com.example.ecommerce.Product;

import com.example.ecommerce.Product.entity.Product;
import com.example.ecommerce.Product.entity.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for Product CRUD operations.
 * Uses ProductTable (JPA repository) to perform database operations.
 * Direct database access without caching.
 */
@Repository
@Qualifier("dbProductRepository")
@RequiredArgsConstructor
public class DatabaseProductRepository implements IProductRepository {

    private final ProductTable productTable;

    @Override
    @NonNull
    public Product create(@NonNull Product product) {
        return productTable.save(product);
    }

    @Override
    @NonNull
    public List<Product> findAll() {
        return productTable.findAllWithStock();
    }

    @Override
    @NonNull
    public Optional<Product> findById(@NonNull Long id) {
        return productTable.findById(id);
    }

    @Override
    @NonNull
    public Product update(@NonNull Product product) {
        return productTable.save(product);
    }

    @Override
    public void delete(@NonNull Long id) {
        productTable.deleteById(id);
    }

    @Override
    @Transactional
    @NonNull
    public Optional<Product> returnIfInStock(@NonNull Long productId, int quantity) {
        // Get product with stock from database
        Optional<Product> productOpt = productTable.findByIdWithStock(productId);

        if (productOpt.isEmpty()) {
            return Optional.empty();
        }

        Product product = productOpt.get();
        Stock stock = product.getStock();

        if (stock == null) {
            return Optional.empty();
        }

        // Check if sufficient stock exists (use currentValue from database)
        Integer currentStock = stock.getCurrentValue();
        if (currentStock == null || currentStock < quantity) {
            return Optional.empty();
        }

        // Update stock quantity in database via Product (cascades to Stock)
        stock.setCurrentValue(currentStock - quantity);
        productTable.save(product);

        return Optional.of(product);
    }
}
