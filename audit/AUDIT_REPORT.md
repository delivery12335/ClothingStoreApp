# Audit Report

## Checked Components

| Component | Status | Notes |
| --- | --- | --- |
| Maven project | Checked | Uses `pom.xml` and Maven wrapper |
| JavaFX views | Checked | FXML files moved into `resources/.../fxml` |
| CSS | Checked | Styles moved into `resources/.../css` |
| Images | Checked | Images moved into `resources/.../images` |
| Controllers | Checked | Controllers are organized under `controller` |
| DAO layer | Checked | SQL access is separated into DAO classes |
| Database | Checked | SQLite database is preserved |
| Reports | Checked | CSV/TXT export services exist |

## Database Audit

| Item | Result |
| --- | --- |
| SQLite file exists | Yes |
| Main database path | `database/clothing_store.db` |
| Tables found | `Users`, `Categories`, `Products`, `Orders`, `OrderItems` |
| Primary keys | Present |
| Foreign keys | Present for products/orders/order items |
| Seed scripts added | Yes |
| Schema script added | Yes |

## UI Audit

| Item | Result |
| --- | --- |
| Login screen | Present |
| Register screen | Present |
| Shop screen | Present |
| Product detail screen | Present |
| Cart screen | Present |
| Orders screen | Present |
| Admin screen | Present |
| Dashboard screen | Present |

## Code Structure Audit

| Area | Result |
| --- | --- |
| Controllers | Organized in `controller` |
| DAOs | Organized in `dao` |
| Models | Organized in `model` |
| Enums/constants | Organized in `enums` |
| Database connection | Organized in `db` |
| Services | Organized in `service` |
| Utilities | Organized in `util` |

## Security Notes

| Finding | Risk | Recommendation |
| --- | --- | --- |
| Passwords are stored as plain text | High for real use | Hash passwords before storing |
| SQLite file is committed | Acceptable for student demo | Use schema/seed scripts for production-like setup |
| Admin demo credentials are documented | Acceptable for submission/demo | Change credentials for real deployment |

## Validation Notes

- Registration checks empty fields.
- Registration validates email format.
- Product and order dialogs include field validation.
- Database constraints enforce positive quantities and non-negative prices.

## Problems Found

| Problem | Impact | Recommendation |
| --- | --- | --- |
| No automated tests currently present | Medium | Add DAO and service tests |
| Some UI text/data appears encoded from existing files | Low/Medium | Review text encoding before final presentation |
| `JAVA_HOME` was not configured in the local environment | Build check blocked | Install/configure JDK 17+ |

## Final Conclusion

The project is suitable as a student JavaFX database application after restructuring. It demonstrates JavaFX UI development, SQLite persistence, CRUD operations, reporting, exports, and layered code organization. Before final defense, configure JDK 17+, run a full Maven build, and review UI text encoding.

