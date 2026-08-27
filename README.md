# Online Shopping Website Using Microservices Architecture

A comprehensive **2-Microservice Online Shopping Platform** built with **Java**, **Spring Boot 3.3**, **Spring Data JPA**, **MySQL**, and a **Modern Web Frontend (HTML5 / CSS3 / JavaScript)**.

---

## 1. Project Overview & Architecture

This project demonstrates the core fundamentals of **Microservices Architecture** using the **Database-per-Service** design pattern and **Synchronous Inter-Service HTTP Communication**.

```
+---------------------------------------------------------------------------------+
|                        ShopEase Frontend (Browser / SPA)                        |
+----------------------------------------+----------------------------------------+
                                         |
                       +-----------------+-----------------+
                       |                                   |
         REST API (Port 8081)                 REST API (Port 8082)
                       |                                   |
                       v                                   v
            +--------------------+               +--------------------+
            |   Product Service  |               |   Order Service    |
            |   (Spring Boot)    |<==============|   (Spring Boot)    |
            +---------+----------+  REST Client  +---------+----------+
                      |       (Stock Check & Deduction)    |
                      v                                    v
            +--------------------+               +--------------------+
            | MySQL: product_db  |               |  MySQL: order_db   |
            +--------------------+               +--------------------+
```

### Microservices Decomposition:
1. **Product Service (Port 8081)**:
   - Manages product catalog, pricing, category classification, and real-time inventory levels.
   - Provides endpoints for atomic stock decrement when an order is placed and stock restoration when an order is cancelled.
   - Dedicated Database: `product_db`.
2. **Order Service (Port 8082)**:
   - Handles customer orders, multi-item calculation, order lifecycle (`PLACED`, `PENDING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
   - Interacts with Product Service via Spring `RestTemplate` to validate products, verify available inventory, decrement stock on order creation, and restore stock on order cancellation.
   - Dedicated Database: `order_db`.
3. **Interactive Web Frontend (`frontend/`)**:
   - Single-Page Application (SPA) with live service health indicators, product browsing, dynamic search, category filtering, cart drawer, customer checkout, orders management, and product CRUD administration.

---

## 2. Technology Stack

| Layer | Technologies | Description |
| :--- | :--- | :--- |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+), FontAwesome | Responsive SPA with zero npm dependencies |
| **Backend Framework** | Java 17+, Spring Boot 3.3.2 | RESTful microservice architectures |
| **Data & ORM** | Spring Data JPA, Hibernate, MySQL Connector | Object-relational mapping and persistence |
| **Database** | MySQL 8.0 (with H2 embedded fallback) | Independent schema per service |
| **Inter-Service Comms** | HTTP / REST, Spring `RestTemplate` | Synchronous REST client orchestration |
| **Observability** | Spring Boot Actuator | Real-time `/actuator/health` telemetry |
| **API Testing** | Postman Collection JSON | Pre-configured request suites |

---

## 3. Microservices & Database Architecture

### The Database-per-Service Pattern
Each microservice maintains its own dedicated database schema to guarantee loose coupling, domain autonomy, and fault isolation:
- `product-service` $\rightarrow$ `product_db` (`products` table)
- `order-service` $\rightarrow$ `order_db` (`orders` and `order_items` tables)

```sql
-- product_db schema
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME
);

-- order_db schema
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(64) NOT NULL UNIQUE,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_address TEXT NOT NULL,
    order_date DATETIME,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
```

---

## 4. REST API Endpoints Reference

### Product Service (`http://localhost:8081`)

| Method | Endpoint | Description | Request Body / Query |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/products` | Get all active products | None |
| `GET` | `/api/products/{id}` | Get product details by ID | None |
| `GET` | `/api/products/search` | Search by keyword | `?keyword=Apple` |
| `GET` | `/api/products/category/{category}` | Filter by category | None |
| `POST` | `/api/products` | Create a new product | JSON `{ name, description, price, stock, category, imageUrl }` |
| `PUT` | `/api/products/{id}` | Update product information | JSON `{ name, description, price, stock, category, imageUrl }` |
| `DELETE`| `/api/products/{id}` | Delete a product | None |
| `PUT` | `/api/products/{id}/decrement-stock`| Decrement product stock | `?quantity=2` or JSON `{ "quantity": 2 }` |
| `PUT` | `/api/products/{id}/restore-stock`  | Restock product inventory | `?quantity=2` or JSON `{ "quantity": 2 }` |

### Order Service (`http://localhost:8082`)

| Method | Endpoint | Description | Request Body / Query |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/orders` | Place order (orchestrates stock deduction) | JSON `{ customerName, customerEmail, customerAddress, items: [{ productId, quantity }] }` |
| `GET` | `/api/orders` | Get all placed orders | None |
| `GET` | `/api/orders/{id}` | Get order details by ID | None |
| `GET` | `/api/orders/number/{orderNumber}` | Get order by unique order number | None |
| `GET` | `/api/orders/user/{email}` | Get all orders by customer email | None |
| `PUT` | `/api/orders/{id}/status` | Update order status | JSON `{ "status": "SHIPPED" }` |
| `PUT` | `/api/orders/{id}/cancel` | Cancel order & auto-restore stock | None |
| `DELETE`| `/api/orders/{id}` | Cancel order & auto-restore stock | None |

---

## 5. How to Run the Project (Step-by-Step)

### Prerequisites
1. **Java JDK 17+** (Java 17, 21, or 26) installed.
2. **MySQL Server 8.0** running on `localhost:3306` (default user `root`, password `root` or updated in `application.properties`).

---

### Step 1: Initialize Database
Execute the database setup script in MySQL:
```bash
mysql -u root -p < database/setup-databases.sql
```
*(Note: If MySQL is not used, the applications include an auto-initializer and H2 embedded support).*

---

### Step 2: Start Microservices

#### Option A: One-Click Launch (Windows)
Double-click `run-all.bat` or run in PowerShell:
```powershell
.\start-all.ps1
```

#### Option B: Manual Launch
Open two separate terminal windows:

**Terminal 1 (Product Service):**
```bash
cd product-service
mvn spring-boot:run
```

**Terminal 2 (Order Service):**
```bash
cd order-service
mvn spring-boot:run
```

---

### Step 3: Open the Web Frontend
Open `frontend/index.html` in any web browser:
- You will see live status pills showing `Product Svc (:8081): Online` and `Order Svc (:8082): Online`.
- You can browse products, add items to cart, place orders, modify order status, or manage inventory in the admin tab.

---

### Step 4: Import Postman Collection
1. Open **Postman**.
2. Click **Import**.
3. Select `postman/Online_Shopping_Microservices.postman_collection.json`.
4. Execute tests for both Product and Order services.

---

## 6. Microservices College Assignment & Viva Q&A Guide

### Q1: What is a Microservice Architecture?
> **Answer:** An architectural style where a complex application is composed of small, autonomous, and independently deployable services organized around business capabilities (e.g., Product Service and Order Service).

### Q2: Why did we use the Database-per-Service pattern?
> **Answer:** Database-per-Service ensures loose coupling. Each service owns its domain data and schema. Changes or scaling requirements in `order_db` do not lock or break tables in `product_db`, and services cannot make unauthorized direct database queries against other domains.

### Q3: How do Product Service and Order Service communicate?
> **Answer:** Synchronously over HTTP REST APIs using Spring `RestTemplate`. When an order is placed in Order Service, it calls Product Service's `GET /api/products/{id}` to verify price/stock, and `PUT /api/products/{id}/decrement-stock` to reduce inventory.

### Q4: How is inventory rollback handled if order placement fails or is cancelled?
> **Answer:** In `OrderService`, if an item fails validation or stock deduction midway, a compensating transaction restores previously decremented stock (`restoreStock`). When a customer cancels an order, Order Service triggers `restoreStock` on the Product Service for every item in that order.

---

## 7. Project File Directory Structure

```
Assignment-2 ( Project )/
├── database/
│   └── setup-databases.sql               # MySQL schemas & initial seed data
├── product-service/
│   ├── pom.xml                           # Maven dependencies
│   ├── src/main/java/com/shopping/product/
│   │   ├── ProductServiceApplication.java# Main app & data seed runner
│   │   ├── entity/Product.java           # JPA entity
│   │   ├── repository/ProductRepository.java # JPA repository
│   │   ├── service/ProductService.java   # Business logic & stock handling
│   │   ├── controller/ProductController.java # REST endpoints
│   │   ├── dto/                          # ProductRequest, StockUpdateRequest, ApiResponse
│   │   └── exception/                    # Custom exceptions & GlobalExceptionHandler
│   └── src/main/resources/
│       └── application.properties        # Port 8081 & MySQL config
├── order-service/
│   ├── pom.xml                           # Maven dependencies
│   ├── src/main/java/com/shopping/order/
│   │   ├── OrderServiceApplication.java  # Main app & RestTemplate bean
│   │   ├── entity/                       # Order, OrderItem, OrderStatus
│   │   ├── repository/OrderRepository.java   # JPA repository
│   │   ├── client/ProductServiceClient.java  # Inter-service REST client
│   │   ├── service/OrderService.java     # Order lifecycle & orchestration
│   │   ├── controller/OrderController.java   # REST endpoints
│   │   ├── dto/                          # OrderRequest, OrderResponse, ProductDto
│   │   └── exception/                    # GlobalExceptionHandler
│   └── src/main/resources/
│       └── application.properties        # Port 8082 & Product Svc URL
├── frontend/
│   ├── index.html                        # Modern SPA UI
│   ├── style.css                         # Modern responsive stylesheet
│   └── app.js                            # Async client & health polling
├── postman/
│   └── Online_Shopping_Microservices.postman_collection.json
├── run-product-service.bat
├── run-order-service.bat
├── run-all.bat
├── start-all.ps1
└── README.md
```
