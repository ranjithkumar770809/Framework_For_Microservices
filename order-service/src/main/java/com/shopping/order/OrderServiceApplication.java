package com.shopping.order;

import com.shopping.order.entity.Order;
import com.shopping.order.entity.OrderItem;
import com.shopping.order.entity.OrderStatus;
import com.shopping.order.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner seedSampleOrders(OrderRepository orderRepository) {
        return args -> {
            if (orderRepository.count() == 0) {
                // 1. Delivered Order (Amazon Historical Order)
                Order o1 = new Order(
                        "ORD-20260825-9A8B7C",
                        "Sarah Connor",
                        "sarah.connor@example.com",
                        "456 Market St, Apt 12B, Seattle, WA",
                        "Credit Card (Visa ending in 4242)",
                        "TRK-AMZ-883921",
                        OrderStatus.DELIVERED,
                        new BigDecimal("999.00")
                );
                o1.setOrderDate(LocalDateTime.now().minusDays(3));
                o1.setEstimatedDeliveryDate(LocalDateTime.now().minusDays(1));
                o1.addItem(new OrderItem(
                        1L,
                        "Apple iPhone 15 Pro",
                        new BigDecimal("999.00"),
                        1,
                        new BigDecimal("999.00"),
                        "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&auto=format&fit=crop&q=80"
                ));

                // 2. Shipped Order (In-Transit Package)
                Order o2 = new Order(
                        "ORD-20260827-4F1E8D",
                        "John Doe",
                        "john.doe@example.com",
                        "123 Silicon Valley Blvd, San Jose, CA",
                        "UPI / Instant Net Banking",
                        "TRK-AMZ-773419",
                        OrderStatus.SHIPPED,
                        new BigDecimal("449.49")
                );
                o2.setOrderDate(LocalDateTime.now().minusDays(1));
                o2.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(2));
                o2.addItem(new OrderItem(
                        2L,
                        "Sony WH-1000XM5 Wireless Headphones",
                        new BigDecimal("349.99"),
                        1,
                        new BigDecimal("349.99"),
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80"
                ));
                o2.addItem(new OrderItem(
                        4L,
                        "Logitech MX Master 3S Mouse",
                        new BigDecimal("99.50"),
                        1,
                        new BigDecimal("99.50"),
                        "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&auto=format&fit=crop&q=80"
                ));

                // 3. Newly Placed Order
                Order o3 = new Order(
                        "ORD-20260828-5C2D1A",
                        "Michael Scott",
                        "michael.scott@dundermifflin.com",
                        "1725 Slough Avenue, Scranton, PA",
                        "Cash on Delivery (COD)",
                        "TRK-AMZ-991204",
                        OrderStatus.PLACED,
                        new BigDecimal("1299.00")
                );
                o3.setOrderDate(LocalDateTime.now().minusHours(2));
                o3.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3));
                o3.addItem(new OrderItem(
                        3L,
                        "Apple MacBook Air M3",
                        new BigDecimal("1299.00"),
                        1,
                        new BigDecimal("1299.00"),
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80"
                ));

                // 4. Cancelled Order
                Order o4 = new Order(
                        "ORD-20260826-1188AA",
                        "Alex Rivera",
                        "alex.rivera@example.com",
                        "789 Broadway, New York, NY",
                        "Debit Card (Mastercard)",
                        "TRK-AMZ-332189",
                        OrderStatus.CANCELLED,
                        new BigDecimal("399.00")
                );
                o4.setOrderDate(LocalDateTime.now().minusDays(2));
                o4.setCancellationReason("Customer cancelled order before dispatch. Inventory returned to catalog.");
                o4.addItem(new OrderItem(
                        6L,
                        "Apple Watch Series 9",
                        new BigDecimal("399.00"),
                        1,
                        new BigDecimal("399.00"),
                        "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&auto=format&fit=crop&q=80"
                ));

                orderRepository.save(o1);
                orderRepository.save(o2);
                orderRepository.save(o3);
                orderRepository.save(o4);
                System.out.println(">>> Sample Amazon-style Orders Seeded Successfully into order_db!");
            }
        };
    }
}
