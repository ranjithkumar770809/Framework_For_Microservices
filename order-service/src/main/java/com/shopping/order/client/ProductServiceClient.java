package com.shopping.order.client;

import com.shopping.order.dto.ApiResponse;
import com.shopping.order.dto.ProductDto;
import com.shopping.order.exception.OrderProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;

    public ProductServiceClient(
            RestTemplate restTemplate,
            @Value("${product-service.base-url:http://localhost:8081/api/products}") String productServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }

    public ProductDto getProductById(Long productId) {
        try {
            String url = productServiceBaseUrl + "/" + productId;
            ResponseEntity<ApiResponse<ProductDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductDto>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                return response.getBody().getData();
            } else {
                throw new OrderProcessingException("Product not found with id: " + productId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new OrderProcessingException("Product with ID " + productId + " does not exist in Product Service.");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new OrderProcessingException("Product Service error: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new OrderProcessingException("Cannot reach Product Service at " + productServiceBaseUrl + ". Ensure Product Service is running on Port 8081.");
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to fetch product details for ID: " + productId + " - " + e.getMessage());
        }
    }

    public void decrementStock(Long productId, Integer quantity) {
        try {
            String url = productServiceBaseUrl + "/" + productId + "/decrement-stock?quantity=" + quantity;
            ResponseEntity<ApiResponse<ProductDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductDto>>() {}
            );

            if (response.getBody() == null || !response.getBody().isSuccess()) {
                throw new OrderProcessingException("Failed to decrement stock for product ID: " + productId);
            }
        } catch (HttpClientErrorException.BadRequest e) {
            throw new OrderProcessingException("Stock error for product ID " + productId + ": " + e.getResponseBodyAsString());
        } catch (HttpClientErrorException.NotFound e) {
            throw new OrderProcessingException("Product with ID " + productId + " not found while decrementing stock.");
        } catch (ResourceAccessException e) {
            throw new OrderProcessingException("Cannot reach Product Service to update stock. Is Product Service running?");
        } catch (Exception e) {
            throw new OrderProcessingException("Error updating inventory for product ID " + productId + ": " + e.getMessage());
        }
    }

    public void restoreStock(Long productId, Integer quantity) {
        try {
            String url = productServiceBaseUrl + "/" + productId + "/restore-stock?quantity=" + quantity;
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductDto>>() {}
            );
        } catch (Exception e) {
            System.err.println("WARNING: Failed to restore stock for product ID: " + productId + ". " + e.getMessage());
        }
    }
}
