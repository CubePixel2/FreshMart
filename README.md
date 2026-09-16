# 🛒 FreshMart - Modern Grocery Store Inventory and Billing System

A production-grade, enterprise-ready **Grocery Store Inventory and Billing POS System** built with **Java 17+ / Spring Boot 3**, **Spring Security**, **Spring Data JPA**, **MySQL / H2**, **Thymeleaf**, **Bootstrap 5**, **AOS animations**, and **Chart.js**.

Designed with a SaaS-grade retail POS interface inspired by modern retail checkout terminals (Square, Toast, Shopify POS).

---

## 🚀 Key Highlights & Features

### 1. 🔐 Role-Based Authentication & Security
- **Admin Role (`ROLE_ADMIN`)**: Full administrative control over products, categories, suppliers, sales logs, and restock actions.
- **Staff Role (`ROLE_STAFF`)**: Optimized for cashiers—access to POS Billing terminal, product viewing, and receipt generation.
- **BCrypt Password Hashing** & session protection.
- **Pre-configured Demo Logins** with **1-Click Auto-Fill** buttons on the login screen.

### 2. 📊 Dynamic Dashboard with Micro-Animations
- **Animated Numeric Counters**: Stats count up smoothly from `0` to actual totals upon page load using easing algorithms.
- **KPI Metrics**: Total Products, Low Stock Alerts, Today's Sales Count, Today's Gross Revenue, and Overall Revenue.
- **Weekly Revenue Trend Graph**: Interactive **Chart.js** line chart with smooth bezier curves and emerald gradient fill.
- **Category Stock Breakdown**: Interactive **Chart.js** doughnut chart displaying product distribution per category.
- **Real-Time Low Stock Warning Banner**: Flashing pulse animation (`@keyframes pulse-danger`) alerting managers to depleted inventory.
- **Recent Invoices Feed**: Quick lookup and link to invoice receipts.

### 3. ⚡ High-Speed POS Billing Terminal
- **Category Filter Tabs**: Fast instant filtering across Fresh Produce, Dairy & Eggs, Bakery, Beverages, Snacks, Pantry, etc.
- **Live Search & Barcode Scanner**: Instant product filter by name, or barcode input with `Enter` scanner support.
- **Dynamic Reactive Cart**:
  - Incremental/decremental quantity controls and line-item deletion.
  - Cart bounce micro-animation on item addition.
  - Automatic calculation of Subtotal, 5% Tax, Custom Discount, and Grand Total.
- **Checkout Modal**:
  - Cash, Card, or UPI / Digital payment method selection.
  - Live cash tendered and change returned calculator.
  - Atomic `@Transactional` stock deduction in the database.
- **Instant Receipt Generation**: Shows invoice number and direct link to printable invoice.

### 4. 📦 Product & Inventory Management
- Full CRUD: Add, Edit, Delete (Admin) and Search products.
- Profit margin calculator with real-time percentage preview.
- Customizable safety stock thresholds (`minStockThreshold`) and measurement units (`kg`, `pcs`, `pack`, `bottle`, etc.).
- Dedicated **Low Stock Alerts Page** with **1-Click Quick Restock Modal**.

### 5. 🏷️ Category & Supplier Directories
- Category management with customizable Bootstrap icons and color tags.
- Supplier directory with contact persons, email, phone numbers, and address.

### 6. 🧾 Printable Retail & Thermal Invoices
- Itemized invoice details with store header, cashier name, customer info, tax breakdown, and barcode.
- Dedicated `@media print` styling: hides sidebar and navbar, formats neatly for standard 80mm thermal receipt printers or A4 PDF.

### 7. 🌱 Automatic Seed Data on First Run
- Automatically populates users, 7 grocery categories, 5 wholesale suppliers, 18 realistic grocery products (with 4 intentional low-stock items for immediate testing), and past sales invoices.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Language** | Java 17+ (Java 21 / 26 compatible) |
| **Backend Framework** | Spring Boot 3.3.4 |
| **Security** | Spring Security 6 + BCrypt |
| **Persistence** | Spring Data JPA + Hibernate |
| **Databases** | In-Memory H2 (default out-of-the-box) + MySQL 8.x (ready to toggle) |
| **Template Engine** | Thymeleaf + Thymeleaf Extras Spring Security 6 |
| **UI Framework** | Bootstrap 5.3.3 + Bootstrap Icons 1.11.3 |
| **Animations** | Custom CSS Keyframes (`pulse-danger`, `cartBounce`, `slideInUp`) + AOS (Animate On Scroll) |
| **Charts** | Chart.js 4.4.4 |
| **Build Tool** | Apache Maven 3.9.8 (Maven Wrapper included) |

---

## 🔑 Default Credentials

| Role | Username | Password | Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full Access: Dashboard, POS, Products, Categories, Suppliers, Restock, Sales |
| **Staff Cashier** | `staff` | `staff123` | Standard Access: Dashboard, POS Billing, Inventory View, Sales History |

*(The login page includes quick 1-click autofill buttons for both roles!)*

---

## 🏁 How to Run the Application

### Option 1: Double-Click or Run `run.bat` (Windows)
Simply run the included batch file:
```cmd
.\run.bat
```
This automatically configures `JAVA_HOME`, compiles the application via the Maven wrapper, and starts the server.

### Option 2: Using Maven Wrapper via Terminal / PowerShell
```powershell
# Set JAVA_HOME if not in your environment:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"

# Run Spring Boot
.\mvnw.cmd spring-boot:run
```

### Accessing the Web Application
Open your browser and navigate to:
👉 **`http://localhost:8080`**

To view the embedded H2 database console:
👉 **`http://localhost:8080/h2-console`**
- **JDBC URL**: `jdbc:h2:mem:groceryposdb`
- **Username**: `sa`
- **Password**: *(leave blank)*

---

## 🗄️ Switching to MySQL Database

To connect to MySQL instead of the default in-memory H2 database:

1. Create a MySQL database (e.g. `grocery_pos`):
   ```sql
   CREATE DATABASE grocery_pos;
   ```
2. Open [`src/main/resources/application.properties`](src/main/resources/application.properties).
3. Comment out the H2 settings and uncomment the MySQL section:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/grocery_pos?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   ```
4. Restart the application. Spring Boot will automatically create all tables and initialize the seed data.

---

## 📁 Project Directory Structure

```
WEB/
├── .mvn/wrapper/                 # Maven wrapper configuration & binaries
├── mvnw                          # Linux/Mac maven wrapper script
├── mvnw.cmd                      # Windows maven wrapper script
├── pom.xml                       # Maven dependencies and build setup
├── run.bat                       # One-click Windows launch script
├── README.md                     # Documentation
└── src/
    └── main/
        ├── java/com/grocery/pos/
        │   ├── GroceryPosApplication.java
        │   ├── config/
        │   │   ├── SecurityConfig.java
        │   │   └── DataInitializer.java
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── DashboardController.java
        │   │   ├── ProductController.java
        │   │   ├── CategoryController.java
        │   │   ├── SupplierController.java
        │   │   ├── PosController.java
        │   │   └── SaleController.java
        │   ├── dto/
        │   │   ├── CartItemDto.java
        │   │   ├── CheckoutRequestDto.java
        │   │   ├── DashboardSummaryDto.java
        │   │   ├── ChartDataDto.java
        │   │   └── ApiResponseDto.java
        │   ├── model/
        │   │   ├── Role.java
        │   │   ├── PaymentMethod.java
        │   │   ├── User.java
        │   │   ├── Category.java
        │   │   ├── Supplier.java
        │   │   ├── Product.java
        │   │   ├── Sale.java
        │   │   └── SaleItem.java
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── CategoryRepository.java
        │   │   ├── SupplierRepository.java
        │   │   ├── ProductRepository.java
        │   │   ├── SaleRepository.java
        │   │   └── SaleItemRepository.java
        │   └── service/
        │       ├── UserService.java
        │       ├── CategoryService.java
        │       ├── SupplierService.java
        │       ├── ProductService.java
        │       └── SaleService.java
        └── resources/
            ├── application.properties
            ├── static/
            │   ├── css/style.css
            │   └── js/
            │       ├── app.js
            │       ├── pos.js
            │       └── dashboard-charts.js
            └── templates/
                ├── layout/base.html
                ├── login.html
                ├── dashboard.html
                ├── pos/index.html
                ├── products/
                │   ├── list.html
                │   ├── form.html
                │   └── low-stock.html
                ├── categories/list.html
                ├── suppliers/list.html
                ├── sales/
                │   ├── list.html
                │   └── invoice.html
                └── error/403.html
```
