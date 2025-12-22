package com.example.ecommerce.controller;

import com.example.ecommerce.DTO.ProductResponseDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/cache")
    public List<ProductResponseDTO> getAllProductsFromCache() {
        return productService.getAllProductsFromCache();
    }

    @GetMapping()
    public List<ProductResponseDTO> getAllProductsFromDB() {
        return productService.getAllProductsFromDB();
    }

    @PostMapping("/product")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    // @GetMapping("/db/{id}")
    // public ProductResponseDTO getProductFromDb(@PathVariable Long id) {
    //     return productService.getProductFromDB(id);
    // }

    @GetMapping("/cache/{id}")
    public Product getProductFromCache(@PathVariable Long id) {
        return productService.getProductFromCache(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}
