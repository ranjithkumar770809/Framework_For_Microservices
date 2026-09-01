package com.shopping.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopping.product.dto.ApiResponse;
import com.shopping.product.dto.ProductRequest;
import com.shopping.product.dto.StockUpdateRequest;
import com.shopping.product.entity.Product;
import com.shopping.product.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully", product)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword) {

        List<Product> products = productService.searchProducts(keyword);

        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", products)
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(
            @PathVariable String category) {

        List<Product> products = productService.getProductsByCategory(category);

        return ResponseEntity.ok(
                ApiResponse.success("Products by category retrieved successfully", products)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Valid @RequestBody ProductRequest request) {

        Product createdProduct = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Product created successfully",
                        createdProduct
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        Product updatedProduct = productService.updateProduct(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product updated successfully",
                        updatedProduct
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully")
        );
    }

    @PutMapping("/{id}/decrement-stock")
    public ResponseEntity<ApiResponse<Product>> decrementStock(
            @PathVariable Long id,
            @RequestParam(required = false) Integer quantity,
            @RequestBody(required = false) StockUpdateRequest request) {

        int qty = (quantity != null)
                ? quantity
                : (request != null && request.getQuantity() != null
                        ? request.getQuantity()
                        : 1);

        Product updatedProduct = productService.decrementStock(id, qty);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock decremented successfully",
                        updatedProduct
                )
        );
    }

    @PutMapping("/{id}/restore-stock")
    public ResponseEntity<ApiResponse<Product>> restoreStock(
            @PathVariable Long id,
            @RequestParam(required = false) Integer quantity,
            @RequestBody(required = false) StockUpdateRequest request) {

        int qty = (quantity != null)
                ? quantity
                : (request != null && request.getQuantity() != null
                        ? request.getQuantity()
                        : 1);

        Product updatedProduct = productService.restoreStock(id, qty);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock restored successfully",
                        updatedProduct
                )
        );
    }
}