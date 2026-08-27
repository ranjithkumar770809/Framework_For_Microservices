package com.shopping.order.service;

import com.shopping.order.client.ProductServiceClient;
import com.shopping.order.dto.OrderItemRequest;
import com.shopping.order.dto.OrderRequest;
import com.shopping.order.dto.OrderResponse;
import com.shopping.order.dto.ProductDto;
import com.shopping.order.entity.Order;
import com.shopping.order.entity.OrderItem;
import com.shopping.order.entity.OrderStatus;
import com.shopping.order.exception.OrderProcessingException;
import com.shopping.order.exception.ResourceNotFoundException;
import com.shopping.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderProcessingException("Order must contain at least one item.");
        }

        // Step 1: Pre-validate all items and stock
        List<ProductDto> products = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            ProductDto product = productServiceClient.getProductById(itemReq.getProductId());
            if (product.getStock() < itemReq.getQuantity()) {
                throw new OrderProcessingException(String.format(
                        "Insufficient stock for '%s' (ID: %d). Available: %d, Requested: %d",
                        product.getName(), product.getId(), product.getStock(), itemReq.getQuantity()
                ));
            }
            products.add(product);
        }

        // Step 2: Decrement inventory for each item (with compensation rollback on failure)
        List<OrderItemRequest> decrementedItems = new ArrayList<>();
        try {
            for (OrderItemRequest itemReq : request.getItems()) {
                productServiceClient.decrementStock(itemReq.getProductId(), itemReq.getQuantity());
                decrementedItems.add(itemReq);
            }
        } catch (Exception e) {
            // Rollback previously decremented items
            for (OrderItemRequest rollbackItem : decrementedItems) {
                productServiceClient.restoreStock(rollbackItem.getProductId(), rollbackItem.getQuantity());
            }
            throw new OrderProcessingException("Failed during inventory deduction: " + e.getMessage(), e);
        }

        // Step 3: Build Order and calculate total
        String orderNumber = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerName(request.getCustomerName().trim());
        order.setCustomerEmail(request.getCustomerEmail().trim());
        order.setCustomerAddress(request.getCustomerAddress().trim());
        order.setStatus(OrderStatus.PLACED);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemReq = request.getItems().get(i);
            ProductDto product = products.get(i);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    unitPrice,
                    itemReq.getQuantity(),
                    subtotal
            );
            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);

        // Step 4: Persist Order in order_db
        Order savedOrder = orderRepository.save(order);
        return new OrderResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return new OrderResponse(order);
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return new OrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailIgnoreCaseOrderByOrderDateDesc(email)
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new OrderProcessingException("Cannot change status of a cancelled order.");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            return cancelOrder(id);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return new OrderResponse(updated);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderProcessingException("Order is already cancelled.");
        }

        // Restore inventory for all items in this order
        for (OrderItem item : order.getItems()) {
            productServiceClient.restoreStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        return new OrderResponse(updated);
    }
}
