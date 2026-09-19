# Mini Order & Inventory Management System

## 1. Project Overview

This is a backend application for managing products, customers, orders, and inventory.

The system allows users to:

* Manage products
* Manage customers
* Create orders with multiple products
* Automatically reduce product inventory
* Cancel orders and restore inventory
* View customer order history
* Generate customer and product sales reports
* View top-selling products

The application is developed using Java and Spring Boot with PostgreSQL as the database.

---

## 2. Technologies Used

* Java 21
* Spring Boot 4.1.1
* Spring Data JPA
* Hibernate
* PostgreSQL
* Maven
* Lombok
* Jakarta Bean Validation
* JUnit 5
* Mockito
* Postman

---

## 3. Project Structure

```text
src
├── main
│   └── java
│       └── com.example.order_management_system
│           ├── controller
│           ├── service
│           ├── repository
│           ├── entity
│           ├── dto
│           └── exception
│
└── test
    └── java
        └── com.example.order_management_system
            └── service
```

The application follows a layered architecture:

```text
Controller
     ↓
Service
     ↓
Repository
     ↓
Database
```

DTOs are used for API requests and responses instead of directly exposing entities.

---

## 4. Database Setup

The project uses PostgreSQL.

Create the database:

```sql
CREATE DATABASE order_management_db;
```

Update the database credentials in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.application.name=order-management-system

spring.datasource.url=jdbc:postgresql://localhost:5432/order_management_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

Replace `YOUR_PASSWORD` with your PostgreSQL password.

---

## 5. How to Run the Application

### Step 1

Clone the repository.

```bash
git clone <your-github-repository-url>
```

### Step 2

Open the project in IntelliJ IDEA.

### Step 3

Configure PostgreSQL and update `application.properties`.

### Step 4

Run the main application:

```text
OrderManagementSystemApplication.java
```

The application runs on:

```text
http://localhost:8080
```

---

# 6. API Documentation

## Product APIs

### Add Product

```http
POST /products
```

Example request:

```json
{
  "name": "Gaming Mouse",
  "category": "Electronics",
  "price": 2500,
  "availableQuantity": 10,
  "active": true
}
```

### Get All Products

```http
GET /products
```

### Get Product By ID

```http
GET /products/{id}
```

Example:

```http
GET /products/2
```

### Search Products

```http
GET /products/search?name=mouse
```

### Update Product

```http
PUT /products/{id}
```

### Deactivate Product

```http
PUT /products/{id}/deactivate
```

Products are deactivated instead of physically deleted.

---

# 7. Customer APIs

### Create Customer

```http
POST /customers
```

Example:

```json
{
  "name": "John",
  "email": "john@gmail.com",
  "phone": "9876543210"
}
```

### Get All Customers

```http
GET /customers
```

### Get Customer By ID

```http
GET /customers/{id}
```

### Update Customer

```http
PUT /customers/{id}
```

Customer email must be unique.

---

# 8. Order APIs

## Create Order

```http
POST /orders
```

Example:

```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

Example response:

```json
{
  "id": 1,
  "customerId": 1,
  "orderDate": "2026-09-19T10:30:00",
  "totalAmount": 5000.00,
  "status": "CREATED",
  "items": [
    {
      "id": 1,
      "productId": 2,
      "quantity": 2,
      "unitPrice": 2500.00,
      "totalPrice": 5000.00
    }
  ]
}
```

The product price at the time of ordering is stored in `unitPrice`.

This keeps historical orders accurate even if the product price changes later.

---

## Get Order By ID

```http
GET /orders/{id}
```

## Get All Orders

```http
GET /orders
```

## Cancel Order

```http
PUT /orders/{id}/cancel
```

When an order is cancelled, its product quantities are returned to inventory.

A `COMPLETED` order cannot be cancelled.

---

# 9. Customer Order History

```http
GET /customers/{customerId}/orders
```

Example:

```http
GET /customers/1/orders
```

Returns all orders belonging to the specified customer.

---

# 10. Reports

## Customer Report

```http
GET /reports/customers/{customerId}
```

Example response:

```json
{
  "customerId": 1,
  "customerName": "John",
  "totalOrders": 2,
  "totalAmount": 7500.00
}
```

Cancelled orders are excluded from the customer totals.

---

## Product Sales Report

```http
GET /reports/products/sales
```

The report contains:

* Product ID
* Product name
* Total quantity sold
* Total revenue

Products are sorted by total quantity sold.

---

## Top Products

```http
GET /reports/products/top?limit=5
```

The `limit` parameter controls the number of products returned.

Example:

```http
GET /reports/products/top?limit=3
```

---

# 11. Business Rules

The application handles the following business rules:

1. Customer email must be unique.
2. Customer name, email and phone are mandatory.
3. Invalid customer IDs return an error.
4. Invalid product IDs return an error.
5. Inactive products cannot be ordered.
6. Order quantity must be greater than zero.
7. An order must contain at least one item.
8. Insufficient inventory prevents order creation.
9. Inventory is reduced only after all order items pass validation.
10. If order creation fails, inventory changes are rolled back.
11. Duplicate products in the same order are rejected.
12. Cancelled orders restore inventory.
13. A cancelled order cannot be cancelled again.
14. A completed order cannot be cancelled.
15. Product prices are stored as snapshots in order items.

---

# 12. Transaction Management

Order creation and cancellation use Spring's `@Transactional`.

This ensures that related database changes are handled as one transaction.

For example, if an order contains multiple products and one product has insufficient inventory, the order is not created and inventory changes are rolled back.

---

# 13. Exception Handling

The application uses centralized exception handling through:

```text
GlobalExceptionHandler
```

Examples:

* `400 Bad Request` - Invalid request data
* `404 Not Found` - Customer or product does not exist
* `409 Conflict` - Business rule violation or duplicate email

---

# 14. Testing

Unit tests are implemented using:

* JUnit 5
* Mockito

The following scenarios are tested:

* Successful order creation
* Insufficient inventory
* Invalid customer
* Order cancellation and inventory restoration
* Completed order cancellation
* Duplicate customer email

Tests are located under:

```text
src/test/java
```

---

# 15. Database Relationships

The main relationships are:

```text
Customer
   │
   │ 1
   │
   └──────< Order
               │
               │ 1
               │
               └──────< OrderItem
                           │
                           │ many-to-one
                           │
                           └──── Product
```

Main tables:

```text
customers
products
orders
order_items
```

---

# 16. Assumptions

* Product price is captured when the order is created.
* Cancelled orders are excluded from sales reports.
* Completed orders cannot be cancelled.
* Products are deactivated instead of physically deleted.
* One product can appear only once in an order.
* Inventory is maintained using `availableQuantity`.

---

# 17. Limitations

* No authentication or authorization is implemented.
* No payment processing is included.
* No frontend application is included.
* No email or notification service is included.
* Inventory concurrency handling can be enhanced further for high-traffic production environments.

---

## 18. Author

Hariharan S.
