package com.example.ecommerce.product.app;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.category.domain.Category;
import com.example.ecommerce.category.infrastructure.persistence.category.CategoryTable;
import com.example.ecommerce.product.api.dto.ProductCreateRequest;
import com.example.ecommerce.product.api.dto.ProductDTO;
import com.example.ecommerce.product.api.dto.ProductUpdateRequest;
import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.domain.ProductPriceHistory;
import com.example.ecommerce.product.domain.Stock;
import com.example.ecommerce.product.infrastructure.persistence.product.IProductRepository;

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
    public List<ProductDTO> findAllDTO() {
        return productRepository.findAllDTO();
    }

    @NonNull
    public Product findById(@NonNull Long id) {
        return Objects.requireNonNull(productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                )));
    }
    
    @NonNull
    public ProductDTO findByIdDTO(@NonNull Long id) {
        return productRepository.findByIdDTO(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                ));
    }
    
    @NonNull
    public Product updatePartial(@NonNull Long id, @NonNull ProductUpdateRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id
                ));
        
        boolean priceChanged = false;
        Double oldPrice = existingProduct.getPrice();
        
        if (request.getName() != null) {
            existingProduct.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingProduct.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            existingProduct.setPrice(request.getPrice());
            priceChanged = !request.getPrice().equals(oldPrice);
        }
        if (request.getDiscount() != null) {
            existingProduct.setDiscount(request.getDiscount());
        }
        if (request.getImageLink() != null) {
            existingProduct.setImageLink(request.getImageLink());
        }
        
        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryTable.findAllById(request.getCategoryIds()));
            existingProduct.setCategories(categories);
        }
        
        Product updated = productRepository.update(existingProduct);
        
        if (priceChanged) {
            ProductPriceHistory priceHistory = new ProductPriceHistory();
            priceHistory.setOldPrice(oldPrice);
            priceHistory.setNewPrice(request.getPrice());
            priceHistory.setChangedAt(LocalDateTime.now());
            priceHistory.setProduct(updated);
            
            List<ProductPriceHistory> history = updated.getPriceHistory();
            if (history != null) {
                history.add(priceHistory);
            }
        }
        
        return updated;
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

