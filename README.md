# ClothingStoreApp

ClothingStoreApp is a JavaFX desktop application for a clothing shop and store management system. It supports customer shopping workflows and an administrator panel for managing products, orders, reporting, and exports.

## Technologies Used

- Java 17
- JavaFX 21
- Maven
- SQLite
- FXML
- CSS
- JDBC

## Features

- User login and registration
- Role-based navigation for administrators and shop users
- Product catalog with images, brands, sizes, colors, prices, and stock quantities
- Search and filtering for products
- Product detail screen with cart actions
- Shopping cart and checkout workflow
- Customer order history
- Admin dashboard with order statistics
- Product CRUD operations
- Order CRUD operations
- Report generation
- CSV and TXT export support
- Local SQLite database

## Database

The application uses SQLite through JDBC. The active database file is:

```text
database/clothing_store.db
```

The database contains tables for:

- `Users`
- `Categories`
- `Products`
- `Orders`
- `OrderItems`

Schema and sample seed scripts are available in:

```text
database/schema.sql
database/seed.sql
```

## Default Login Credentials

The following credentials were found in the existing SQLite database:

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `admin123` |
| Admin | `manager` | `manager123` |
| User | `ivan_shop` | `pass123` |

## Project Structure

```text
ClothingStoreApp/
+-- README.md
+-- pom.xml
+-- .gitignore
+-- database/
|   +-- README.md
|   +-- schema.sql
|   +-- seed.sql
|   +-- backups/
+-- docs/
|   +-- ARCHITECTURE.md
|   +-- PROJECT_DESCRIPTION.md
|   +-- USER_GUIDE.md
+-- audit/
|   +-- AUDIT_REPORT.md
+-- reports/
|   +-- README.md
|   +-- examples/
+-- exports/
+-- logs/
+-- src/
    +-- main/
    |   +-- java/com/clothingstore/clothingstoreapp/
    |   |   +-- controller/
    |   |   +-- dao/
    |   |   +-- db/
    |   |   +-- enums/
    |   |   +-- model/
    |   |   +-- service/
    |   |   +-- util/
    |   +-- resources/com/clothingstore/clothingstoreapp/
    |       +-- css/
    |       +-- fxml/
    |       +-- images/
    +-- test/java/
```

## Run in IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select **Open** and choose the `ClothingStoreApp` folder.
3. Wait for Maven dependencies to import from `pom.xml`.
4. Configure a JDK 17 or newer in **File > Project Structure > Project SDK**.
5. Ensure `JAVA_HOME` points to the same JDK.
6. Run the Maven goal `javafx:run`, or run `HelloApplication`.

## Run with Maven

On Windows:

```powershell
.\mvnw.cmd javafx:run
```

On macOS or Linux:

```bash
./mvnw javafx:run
```

## Build

On Windows:

```powershell
.\mvnw.cmd clean package
```

On macOS or Linux:

```bash
./mvnw clean package
```

## Reports and Exports

Reports can be generated from the admin area and exported as:

- CSV
- TXT

Generated export files should be saved in the `exports/` folder. Example report files are included in `reports/examples/`.

## Screenshots

Add screenshots here before final submission:

- Login screen
- Shop catalog
- Product detail screen
- Cart screen
- Admin dashboard
- Product management screen
- Report/export screen

## Author

Student project: ClothingStoreApp

