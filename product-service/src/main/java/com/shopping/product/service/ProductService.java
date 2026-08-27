package com.shopping.product.service;

import com.shopping.product.dto.ProductRequest;
import com.shopping.product.entity.Product;
import com.shopping.product.exception.InsufficientStockException;
import com.shopping.product.exception.ResourceNotFoundException;
import com.shopping.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchProducts(keyword.trim());
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock(),
                request.getCategory(),
                request.getImageUrl()
        );
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            product.setImageUrl(request.getImageUrl());
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    public Product decrementStock(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity to decrement must be greater than 0");
        }
        Product product = getProductById(id);
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                            product.getName(), product.getStock(), quantity)
            );
        }
        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product restoreStock(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity to restore must be greater than 0");
        }
        Product product = getProductById(id);
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }
}
