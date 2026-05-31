# Architecture

ClothingStoreApp follows a practical MVC-style JavaFX architecture. FXML files define the views, controller classes handle UI events, DAO classes communicate with SQLite, and model classes represent application data.

## MVC Overview

| Layer | Responsibility | Main Package |
| --- | --- | --- |
| View | FXML layouts, CSS styles, images | `src/main/resources/com/clothingstore/clothingstoreapp/` |
| Controller | JavaFX event handling and screen logic | `controller` |
| Model | Data objects used by the UI and DAO layer | `model` |
| DAO | Database CRUD and query logic | `dao` |
| Database | JDBC connection to SQLite | `db` |
| Service | Cart, reporting, export, and workflow logic | `service` |
| Utility | Reusable helpers for images, dialogs, and toast messages | `util` |
| Enums | Constants and status values | `enums` |

## Controller Layer

Controllers receive user actions from FXML views. They validate input, call services or DAOs, and update the JavaFX controls.

Examples:

- `LoginController` handles authentication and opens the admin or shop view.
- `ShopController` displays products and opens cart, order, and product detail screens.
- `CartController` manages cart rows, checkout validation, and order submission.
- `AdminController` manages dashboard data, CRUD screens, reports, and exports.

## DAO Layer

DAO classes isolate SQL logic from the UI. They use `DatabaseConnection` to obtain a JDBC connection and execute queries.

Examples:

- `UserDAO` authenticates users and registers new accounts.
- `ProductDAO` reads, searches, creates, updates, and deletes products.
- `OrderDAO` manages orders and order filtering.
- `CategoryDAO` reads category data.

## Model Layer

Model classes represent application entities and report rows.

Examples:

- `User`
- `Product`
- `Category`
- `Order`
- `CartItem`
- `CustomerOrder`
- `ReportRow`

## Database Layer

The database layer contains `DatabaseConnection`, which connects to:

```text
database/clothing_store.db
```

The database is SQLite and contains users, categories, products, orders, and order items.

## Service Layer

Services contain application logic that does not belong directly in UI controllers or DAOs.

Examples:

- `CartService` stores cart state.
- `OrderHistoryService` stores customer order history.
- `ReportService` builds report data.
- `ExportService`, `CsvExporter`, and `TxtExporter` export reports.

## Utility Layer

Utility classes provide reusable UI and resource behavior.

- `ImageUtil` loads product images from packaged resources or compatible legacy paths.
- `DialogHelper` centralizes dialog behavior.
- `ToastHelper` displays short UI notifications.

## Resources

Resources are organized by type:

```text
src/main/resources/com/clothingstore/clothingstoreapp/
+-- fxml/
+-- css/
+-- images/
```

FXML files should be loaded with absolute classpath paths such as:

```java
new FXMLLoader(getClass().getResource("/com/clothingstore/clothingstoreapp/fxml/login-view.fxml"));
```

CSS should be loaded with:

```java
scene.getStylesheets().add(getClass().getResource("/com/clothingstore/clothingstoreapp/css/styles.css").toExternalForm());
```

Images should be loaded from:

```java
getClass().getResource("/com/clothingstore/clothingstoreapp/images/app-icon.png");
```

## Data Flow Example

1. User clicks a button in a JavaFX view.
2. The FXML controller receives the event.
3. The controller validates input and calls a service or DAO.
4. The DAO executes SQL through `DatabaseConnection`.
5. Result rows are mapped into model objects.
6. The controller updates a `TableView`, labels, cards, or other UI controls.

