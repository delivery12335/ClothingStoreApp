<div align="center">

# ClothingStoreApp

### JavaFX Desktop Application for Clothing Store Management

A modern desktop system for managing products, customers, orders, reports, and exports.

<br>

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven)
![SQLite](https://img.shields.io/badge/SQLite-Database-green?style=for-the-badge&logo=sqlite)
![JDBC](https://img.shields.io/badge/JDBC-Data_Access-lightgrey?style=for-the-badge)
![FXML](https://img.shields.io/badge/FXML-UI_Layout-blueviolet?style=for-the-badge)
![CSS](https://img.shields.io/badge/CSS-Styling-1572B6?style=for-the-badge&logo=css3)
![Status](https://img.shields.io/badge/Status-Student_Project-purple?style=for-the-badge)

</div>

---

## Project Overview

ClothingStoreApp is a JavaFX desktop application created for managing a clothing store. The system contains two main areas: a customer shopping interface and an administrator management panel.

Customers can browse products, view product details, manage cart items, and place orders. Administrators can manage products, users, orders, reports, and exported files through a structured desktop interface backed by a local SQLite database.

---

## ✨ Main Features

### User Features

- User login and registration
- Product catalog browsing
- Product detail view
- Shopping cart
- Checkout workflow
- Customer order history

### Admin Features

- Admin dashboard
- Product CRUD operations
- User management
- Order management
- Report generation
- Export to TXT and CSV

### Database Features

- SQLite database
- JDBC connection
- DAO architecture
- Data validation
- Search and filtering

---

## Technologies Used

| Technology | Purpose |
| --- | --- |
| Java 17 | Main programming language |
| JavaFX 21 | Desktop graphical interface |
| FXML | Interface layout |
| CSS | Application styling |
| Maven | Project build and dependency management |
| SQLite | Local database |
| JDBC | Database connection |
| DAO Pattern | Data access layer |

---

## Project Architecture

```text
ClothingStoreApp/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/clothingstore/clothingstoreapp/
│       │       ├── controller/     # JavaFX controllers
│       │       ├── dao/            # Database access classes
│       │       ├── db/             # Database connection
│       │       ├── model/          # Data models
│       │       ├── service/        # Business logic
│       │       ├── util/           # Helper classes
│       │       └── enums/          # Enumerations
│       └── resources/
│           └── com/clothingstore/clothingstoreapp/
│               ├── fxml/           # FXML UI files
│               ├── css/            # Stylesheets
│               └── images/         # Images and icons
├── database/                       # SQL scripts and database info
├── docs/                           # Documentation
├── audit/                          # Audit report
├── reports/                        # Report examples
├── exports/                        # Exported files
├── logs/                           # Log files
├── README.md
└── pom.xml
```

---

## Application Data Flow

```text
User Action
   ↓
JavaFX Controller
   ↓
Service / Validation Layer
   ↓
DAO Layer
   ↓
SQLite Database
   ↓
Model Objects
   ↓
Updated JavaFX Interface
```

The user interacts with JavaFX views defined in FXML. Controllers handle events, validate input, call services or DAO classes, and then update the interface with model data loaded from SQLite.

---

## Application Preview

| Login Window | Admin Panel | Shop Interface |
| --- | --- | --- |
| Add screenshot here | Add screenshot here | Add screenshot here |

Screenshots can be added later in the `docs/` or `src/main/resources/com/clothingstore/clothingstoreapp/images/` folder.

---

## Documentation

| Document | Description |
| --- | --- |
| [Architecture](docs/ARCHITECTURE.md) | Explains project layers and structure |
| [Project Description](docs/PROJECT_DESCRIPTION.md) | Full academic project description |
| [User Guide](docs/USER_GUIDE.md) | Instructions for using the application |
| [Audit Report](audit/AUDIT_REPORT.md) | Project audit and recommendations |
| [Reports](reports/README.md) | Information about generated reports |
| [Database](database/README.md) | Database structure and usage |

---

## Reports and Export

The application supports generation and export of reports.

Available report examples:

- Products report
- Users report
- Orders report
- Sales report

Supported export formats:

- TXT
- CSV

Example report files are available in [`reports/examples`](reports/examples).

---

## Database

The project uses SQLite as a local database. Database scripts and documentation are stored in the [`database`](database) folder.

- [Database Schema](database/schema.sql)
- [Seed Data](database/seed.sql)
- [Database Documentation](database/README.md)

Active database file:

```text
database/clothing_store.db
```

---

## How to Run

### Run in IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select **Open Project**.
3. Choose the `ClothingStoreApp` folder.
4. Wait for Maven dependencies to load.
5. Configure JDK 17 or newer.
6. Run `HelloApplication.java`.

### Run with Maven

Using the Maven wrapper on Windows:

```powershell
.\mvnw.cmd clean javafx:run
```

Using Maven directly:

```bash
mvn clean javafx:run
```

---

## Default Accounts

These credentials were found in the existing SQLite database:

| Role | Login | Password |
| --- | --- | --- |
| Admin | `admin` | `admin123` |
| Admin | `manager` | `manager123` |
| User | `ivan_shop` | `pass123` |

If the real credentials are different, update this table according to the database.

---

## Academic Requirements Covered

| Requirement | Status |
| --- | --- |
| JavaFX GUI | ✅ Completed |
| Database connection | ✅ Completed |
| CRUD operations | ✅ Completed |
| Search and filtering | ✅ Completed |
| Reports | ✅ Completed |
| Export to TXT / CSV | ✅ Completed |
| OOP principles | ✅ Completed |
| Project documentation | ✅ Completed |
| Audit report | ✅ Completed |

---

## OOP Principles Used

- Encapsulation through private fields and getters/setters in model classes
- Abstraction through DAO and service layers
- Enumerations and constants for fixed values such as roles, export formats, and statuses
- Separation of responsibilities between controllers, models, DAO classes, and services
- Interface-based export behavior through the `Exporter` contract

---

## Author

Created by **Cordineanu Andrei**

Student project for database application development.

