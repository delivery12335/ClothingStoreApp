# User Guide

## Opening the Application

1. Install JDK 17 or newer.
2. Open the project in IntelliJ IDEA or run it with Maven.
3. Start the app with:

```powershell
.\mvnw.cmd javafx:run
```

## Logging In

Use one of the existing demo accounts:

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `admin123` |
| Admin | `manager` | `manager123` |
| User | `ivan_shop` | `pass123` |

New users can register from the login screen.

## Admin Panel

After logging in as an admin, the application opens the admin interface. The admin can:

- View dashboard statistics.
- Open product management.
- Open order management.
- Generate reports.
- Export reports.

## Shop Panel

After logging in as a user, the application opens the shop interface. The user can:

- Browse the catalog.
- Search and filter products.
- Open product details.
- Add available products to the cart.
- Open cart and checkout.
- View previous orders.

## Add, Edit, and Delete Records

In the admin panel:

1. Open the products or orders section.
2. Use the create button to add a new record.
3. Select an existing record and use edit/update to modify it.
4. Select an existing record and use delete to remove it.
5. Use search/filter controls to find specific records.

## Export Reports

1. Log in as an admin.
2. Open the reports/export action.
3. Select a report type.
4. Select CSV or TXT format.
5. Choose a file location, preferably inside `exports/`.

## Common Errors and Fixes

| Problem | Cause | Fix |
| --- | --- | --- |
| `JAVA_HOME not found` | JDK environment variable is missing | Install JDK 17+ and set `JAVA_HOME` |
| Maven dependencies not found | Maven import did not finish | Reimport Maven project in IntelliJ |
| Database connection fails | Database file missing or wrong working directory | Ensure `database/clothing_store.db` exists |
| Images do not show | Image path not found | Check image exists in `src/main/resources/.../images/` |
| Login fails | Wrong username/password | Use demo credentials or register a user |

