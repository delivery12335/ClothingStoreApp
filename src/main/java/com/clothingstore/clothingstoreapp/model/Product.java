package com.clothingstore.clothingstoreapp.model;

public class Product extends BaseEntity {

    private String name;
    private int categoryId;
    private String brand;
    private String size;
    private String color;
    private double price;
    private int quantity;
    private String imagePath;
    private String description;

    public Product(int id, String name, double price) {
        this(id, name, 0, "", "", "", price, 0, "", "");
    }

    public Product(
            int id,
            String name,
            int categoryId,
            String brand,
            String size,
            String color,
            double price,
            int quantity,
            String imagePath,
            String description
    ) {
        super(id);
        this.name = name;
        this.categoryId = categoryId;
        this.brand = brand;
        this.size = size;
        this.color = color;
        this.price = price;
        this.quantity = quantity;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getName() { return name; }
    public int getCategoryId() { return categoryId; }
    public String getBrand() { return brand; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }

    public void setName(String name) { this.name = name; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setSize(String size) { this.size = size; }
    public void setColor(String color) { this.color = color; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setDescription(String description) { this.description = description; }
}

