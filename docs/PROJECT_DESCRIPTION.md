# Project Description

## Purpose

ClothingStoreApp is a student database application that demonstrates how a desktop clothing store system can manage users, products, orders, and reports using JavaFX and SQLite.

## Target Users

- Shop customers who browse products, add items to cart, and place orders.
- Administrators who manage product and order data.
- Teachers or reviewers evaluating database, CRUD, and JavaFX functionality.

## Admin Functionality

Administrators can:

- View dashboard statistics.
- Manage products.
- Create, update, delete, and search product records.
- Manage orders.
- Filter order data by status and other fields.
- Generate reports.
- Export data to CSV or TXT files.

## User and Shop Functionality

Users can:

- Register a new account.
- Log in to the shop interface.
- Browse clothing products.
- Filter/search products.
- Open product details.
- Add products to cart.
- Checkout with customer information.
- View order history.

## Database Role

SQLite stores the main application data:

- Users and roles
- Product categories
- Products and stock information
- Orders
- Order items

The app uses JDBC and DAO classes to keep SQL separate from the UI layer.

## CRUD Operations

The project demonstrates CRUD operations through the admin panel:

| Entity | Create | Read | Update | Delete |
| --- | --- | --- | --- | --- |
| Products | Yes | Yes | Yes | Yes |
| Orders | Yes | Yes | Yes | Yes |
| Users | Registration/read for login | Yes | Limited | Limited |
| Categories | Read | Yes | Not primary UI focus | Not primary UI focus |

## Search and Filtering

The product catalog and admin screens include search/filter capabilities. Filters help users find products by attributes such as name, brand, category, size, color, and availability.

## Reports and Export

The admin area supports report generation and export. Supported export formats:

- TXT
- CSV

Reports can include product, category, stock, order, and sales-related information.

## OOP Principles Used

- Encapsulation: model fields are accessed through methods.
- Abstraction: DAO and service classes hide implementation details.
- Separation of concerns: controllers, models, DAOs, services, and utilities have separate responsibilities.
- Polymorphism: the `Exporter` interface supports multiple export implementations.
- Reusability: utilities and services are shared across controllers.

