# Database

The application uses a local SQLite database:

```text
database/clothing_store.db
```

## Tables

- `Users`: stores usernames, passwords, roles, and emails.
- `Categories`: stores product categories.
- `Products`: stores clothing items and stock details.
- `Orders`: stores order headers.
- `OrderItems`: stores products included in orders.

## Recreate the Database

1. Create a new SQLite database file.
2. Run `schema.sql`.
3. Run `seed.sql`.
4. Ensure the file is saved as:

```text
database/clothing_store.db
```

## Backup the Database

Copy the active database file into:

```text
database/backups/
```

Example backup name:

```text
clothing_store_YYYY-MM-DD.db
```

The `database/backups/` folder is ignored by Git except for `.gitkeep`.

