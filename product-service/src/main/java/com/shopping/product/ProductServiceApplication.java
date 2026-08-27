package com.shopping.product;

import com.shopping.product.entity.Product;
import com.shopping.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Arrays;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSampleData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                Product p1 = new Product(
                        "Apple iPhone 15 Pro",
                        "Titanium design with A17 Pro chip, 48MP camera, and Action Button (128GB, Natural Titanium).",
                        new BigDecimal("999.00"),
                        25,
                        "Electronics",
                        "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&auto=format&fit=crop&q=80"
                );

                Product p2 = new Product(
                        "Sony WH-1000XM5 Wireless Headphones",
                        "Industry-leading noise canceling with Auto NC Optimizer, crystal clear hands-free calling.",
                        new BigDecimal("349.99"),
                        40,
                        "Audio",
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80"
                );

                Product p3 = new Product(
                        "Apple MacBook Air M3",
                        "13.6-inch Liquid Retina Display, 16GB Unified Memory, 512GB SSD Storage, Midnight.",
                        new BigDecimal("1299.00"),
                        15,
                        "Computers",
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80"
                );

                Product p4 = new Product(
                        "Logitech MX Master 3S Mouse",
                        "Performance wireless mouse with 8K DPI any-surface tracking and quiet clicks.",
                        new BigDecimal("99.50"),
                        50,
                        "Accessories",
                        "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&auto=format&fit=crop&q=80"
                );

                Product p5 = new Product(
                        "Samsung 49\" Odyssey G9 Curved Gaming Monitor",
                        "Dual QHD 240Hz 1ms Curved Gaming Monitor with Quantum Mini-LED technology.",
                        new BigDecimal("1099.99"),
                        10,
                        "Electronics",
                        "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80"
                );

                Product p6 = new Product(
                        "Apple Watch Series 9",
                        "Advanced health sensors, bright Always-On display, S9 SiP chip with Double Tap gesture.",
                        new BigDecimal("399.00"),
                        30,
                        "Wearables",
                        "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&auto=format&fit=crop&q=80"
                );

                productRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
                System.out.println(">>> Sample Product Data Initialized Successfully!");
            }
        };
    }
}
