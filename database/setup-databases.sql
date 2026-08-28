-- ==============================================================================
-- Online Shopping Microservices Database Setup Script
-- Architecture: Database-per-Service Pattern (Amazon-Style Orders & Product Catalog)
-- ==============================================================================

-- 1. Create Databases for each Microservice
CREATE DATABASE IF NOT EXISTS `product_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `order_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Populate Sample Data in product_db
USE `product_db`;

CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10, 2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `category` VARCHAR(100) NOT NULL,
    `image_url` VARCHAR(500),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `products` (`id`, `name`, `description`, `price`, `stock`, `category`, `image_url`) VALUES
(1, 'Apple iPhone 15 Pro', 'Titanium design with A17 Pro chip, 48MP camera, and Action Button (128GB, Natural Titanium).', 999.00, 25, 'Electronics', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&auto=format&fit=crop&q=80'),
(2, 'Sony WH-1000XM5 Wireless Headphones', 'Industry-leading noise canceling with Auto NC Optimizer, crystal clear hands-free calling.', 349.99, 40, 'Audio', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80'),
(3, 'Apple MacBook Air M3', '13.6-inch Liquid Retina Display, 16GB Unified Memory, 512GB SSD Storage, Midnight.', 1299.00, 15, 'Computers', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80'),
(4, 'Logitech MX Master 3S Mouse', 'Performance wireless mouse with 8K DPI any-surface tracking and quiet clicks.', 99.50, 50, 'Accessories', 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&auto=format&fit=crop&q=80'),
(5, 'Samsung 49\" Odyssey G9 Curved Gaming Monitor', 'Dual QHD 240Hz 1ms Curved Gaming Monitor with Quantum Mini-LED technology.', 1099.99, 10, 'Electronics', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80'),
(6, 'Apple Watch Series 9', 'Advanced health sensors, bright Always-On display, S9 SiP chip with Double Tap gesture.', 399.00, 30, 'Wearables', 'https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&auto=format&fit=crop&q=80')
ON DUPLICATE KEY UPDATE 
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `price` = VALUES(`price`),
    `stock` = VALUES(`stock`),
    `category` = VALUES(`category`),
    `image_url` = VALUES(`image_url`);

-- 3. Set Up Tables in order_db
USE `order_db`;

CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_number` VARCHAR(64) NOT NULL UNIQUE,
    `customer_name` VARCHAR(255) NOT NULL,
    `customer_email` VARCHAR(255) NOT NULL,
    `customer_address` TEXT NOT NULL,
    `payment_method` VARCHAR(50) DEFAULT 'Credit/Debit Card',
    `tracking_number` VARCHAR(64),
    `order_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `estimated_delivery_date` DATETIME,
    `status` VARCHAR(50) NOT NULL DEFAULT 'PLACED',
    `cancellation_reason` VARCHAR(500),
    `total_amount` DECIMAL(10, 2) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `quantity` INT NOT NULL,
    `subtotal` DECIMAL(10, 2) NOT NULL,
    `image_url` VARCHAR(500),
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed Sample Amazon-Style Orders
INSERT INTO `orders` (`id`, `order_number`, `customer_name`, `customer_email`, `customer_address`, `payment_method`, `tracking_number`, `order_date`, `status`, `total_amount`) VALUES
(1, 'ORD-20260825-9A8B7C', 'Sarah Connor', 'sarah.connor@example.com', '456 Market St, Apt 12B, Seattle, WA', 'Credit Card', 'TRK-AMZ-883921', NOW() - INTERVAL 3 DAY, 'DELIVERED', 999.00),
(2, 'ORD-20260827-4F1E8D', 'John Doe', 'john.doe@example.com', '123 Silicon Valley Blvd, San Jose, CA', 'UPI / Net Banking', 'TRK-AMZ-773419', NOW() - INTERVAL 1 DAY, 'SHIPPED', 449.49),
(3, 'ORD-20260828-5C2D1A', 'Michael Scott', 'michael.scott@dundermifflin.com', '1725 Slough Avenue, Scranton, PA', 'Cash on Delivery', 'TRK-AMZ-991204', NOW() - INTERVAL 2 HOUR, 'PLACED', 1299.00),
(4, 'ORD-20260826-1188AA', 'Alex Rivera', 'alex.rivera@example.com', '789 Broadway, New York, NY', 'Debit Card', 'TRK-AMZ-332189', NOW() - INTERVAL 2 DAY, 'CANCELLED', 399.00)
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);
