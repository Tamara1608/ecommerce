package com.example.ecommerce.Product;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.Product.entity.Category;
import com.example.ecommerce.Product.entity.Product;
import com.example.ecommerce.Product.entity.ProductPriceHistory;
import com.example.ecommerce.Product.entity.Stock;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service for Product CRUD operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class ProductService implements IProductService {
    
    private final IProductRepository productRepository;
    private final CategoryTable categoryTable;
    
    public ProductService(IProductRepository productRepository, CategoryTable categoryTable) {
        this.productRepository = productRepository;
        this.categoryTable = categoryTable;
    }
    
    @NonNull
    public Product create(@NonNull Product product) {
        return productRepository.create(product);
    }
    
    /**
     * Create a product with stock, price history, and categories.
     * 
     * @param request the product creation request
     * @return the created product
     */
    @NonNull
    public Product createWithDetails(@NonNull ProductCreateRequest request) {
        // Create product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscount(request.getDiscount());
        product.setImageLink(request.getImageLink());
        
        // Create and set stock
        if (request.getStockQuantity() != null) {
            Stock stock = new Stock();
            stock.setTotalStock(request.getStockQuantity());
            stock.setCurrentValue(request.getStockQuantity());
            stock.setUpdatedAt(LocalDateTime.now());
            product.setStock(stock);
        }
        
        // Link existing categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryTable.findAllById(request.getCategoryIds()));
            product.setCategories(categories);
        }
        
        // Create initial price history entry
        ProductPriceHistory priceHistory = new ProductPriceHistory();
        priceHistory.setOldPrice(null); // No old price for initial entry
        priceHistory.setNewPrice(request.getPrice());
        priceHistory.setChangedAt(LocalDateTime.now());
        priceHistory.setProduct(product);
        product.setPriceHistory(List.of(priceHistory));
        
        return productRepository.create(product);
    }
    
    @NonNull
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @NonNull
    public Product findById(@NonNull Long id) {
        return Objects.requireNonNull(productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                )));
    }
    
    @NonNull
    public Product update(@NonNull Product product) {
        Long productId = product.getId();
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required for update");
        }
        productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + productId
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

