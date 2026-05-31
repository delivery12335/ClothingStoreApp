PRAGMA foreign_keys = ON;

INSERT OR IGNORE INTO Categories (CategoryID, Name) VALUES
(1, 'Футболки'),
(2, 'Джинсы'),
(3, 'Худи и толстовки'),
(4, 'Кроссовки'),
(5, 'Куртки'),
(6, 'Платья'),
(7, 'Рубашки'),
(8, 'Аксессуары');

INSERT OR IGNORE INTO Users (UserID, Username, Password, Role, Email) VALUES
(1, 'admin', 'admin123', 'ADMIN', 'admin@mail.com'),
(2, 'manager', 'manager123', 'ADMIN', 'manager@mail.com'),
(3, 'ivan_shop', 'pass123', 'USER', 'ivan_shop@mail.com'),
(4, 'maria_style', 'maria2026', 'USER', 'maria_style@mail.com');

INSERT OR IGNORE INTO Products (ProductID, ProductName, CategoryID, Brand, Size, Color, Price, Quantity, ImagePath, Description) VALUES
(1, 'Базовая футболка', 1, 'Nike', 'M', 'Черный', 29.99, 15, '/images/nike_black_tshirt.png', 'Классическая черная футболка из хлопка'),
(2, 'Спортивная футболка', 1, 'Adidas', 'L', 'Белый', 25.99, 32, '/images/adidas_white_tshirt-removebg-preview.png', 'Белая футболка для тренировок'),
(6, 'Классические джинсы', 2, 'Levi''s', '32', 'Синий', 89.99, 25, '/images/levis_blue_jeans.png', 'Прямые классические джинсы'),
(14, 'Вечернее платье', 6, 'Zara', 'M', 'Черный', 129.99, 8, '/images/zara_evening_dress.png', 'Элегантное вечернее платье');

INSERT OR IGNORE INTO Orders (OrderID, UserID, OrderDate, TotalAmount, Status) VALUES
(1, 3, '2026-04-01', 149.97, 'Доставлен'),
(2, 4, '2026-04-05', 129.99, 'Доставлен');

INSERT OR IGNORE INTO OrderItems (OrderItemID, OrderID, ProductID, Quantity, Price) VALUES
(1, 1, 1, 2, 29.99),
(2, 1, 6, 1, 89.99),
(3, 2, 14, 1, 129.99);
