package com.shopping.order.controller;

import com.shopping.order.dto.ApiResponse;
import com.shopping.order.dto.OrderRequest;
import com.shopping.order.dto.OrderResponse;
import com.shopping.order.dto.OrderStatusUpdateRequest;
import com.shopping.order.entity.OrderStatus;
import com.shopping.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", orderResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByCustomerEmail(@PathVariable String email) {
        List<OrderResponse> orders = orderService.getOrdersByCustomerEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Customer orders retrieved successfully", orders));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderViaPut(@PathVariable Long id) {
        OrderResponse cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled and inventory restored successfully", cancelledOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderViaDelete(@PathVariable Long id) {
        OrderResponse cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled and inventory restored successfully", cancelledOrder));
    }
}
