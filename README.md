# ClothingStoreApp

ClothingStoreApp is a desktop clothing store application built with JavaFX, Maven, and SQLite.

The application includes a customer-facing shop interface, product details, cart and checkout flow, order history, and an admin area for managing products and orders.

## Features

- User login and registration
- Product catalog with images
- Product filtering and search
- Product detail page
- Shopping cart with quantity controls
- Checkout and order creation
- Order history view
- Admin dashboard
- Product CRUD operations
- Order CRUD operations
- CSV and TXT export support
- Local SQLite database storage

## Tech Stack

- Java 17
- JavaFX 21
- Maven
- SQLite
- FXML and CSS

## Project Structure

```text
ClothingStoreApp/
+-- database/
|   +-- clothing_store.db
+-- src/
|   +-- main/
|       +-- java/
|       |   +-- com/clothingstore/clothingstoreapp/
|       +-- resources/
|           +-- com/clothingstore/clothingstoreapp/
|           +-- images/
+-- pom.xml
+-- mvnw
+-- mvnw.cmd
```

## Requirements

- JDK 17 or newer
- `JAVA_HOME` configured to point to your JDK installation

You do not need to install Maven separately because the project includes the Maven wrapper.

## Run the Application

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

Build output is generated in the `target/` directory.

## Database

The app uses the local SQLite database at:

```text
database/clothing_store.db
```

The database file is included in the repository because the current application connects directly to this file.

## Notes

- Generated build files are ignored through `.gitignore`.
- IDE files, local cache files, logs, and SQLite temporary files are not committed.
- If Maven commands fail with `JAVA_HOME not found`, install/configure a JDK and set the `JAVA_HOME` environment variable.
