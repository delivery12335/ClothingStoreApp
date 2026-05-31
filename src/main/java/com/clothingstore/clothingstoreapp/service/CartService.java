package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.dao.ProductDAO;
import com.clothingstore.clothingstoreapp.model.CartItem;
import com.clothingstore.clothingstoreapp.model.Product;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CartService {
    private static final Path CART_FILE = AppStorage.file("cart.tsv");
    private static CartService instance;
    private final List<CartItem> items = new ArrayList<>();
    private final ProductDAO productDAO = new ProductDAO();

    private CartService() {
        load();
    }

    public static synchronized CartService getInstance() {
        if (instance == null) instance = new CartService();
        return instance;
    }

    public synchronized void add(Product product, String size, int quantity) {
        int available = Math.max(0, product.getQuantity());
        if (available == 0) {
            return;
        }

        // try to merge with existing item (same product id and size)
        for (CartItem it : items) {
            if (it.getProduct().getId() == product.getId()) {
                String existingSize = it.getSize();
                if ((existingSize == null && size == null) || (existingSize != null && existingSize.equals(size))) {
                    it.setQuantity(Math.min(available, it.getQuantity() + Math.max(1, quantity)));
                    save();
                    return;
                }
            }
        }
        items.add(new CartItem(product, size, Math.min(available, Math.max(1, quantity))));
        save();
    }

    public synchronized int getQuantityForProduct(int productId) {
        int count = 0;
        for (CartItem it : items) {
            if (it.getProduct().getId() == productId) {
                count += it.getQuantity();
            }
        }
        return count;
    }

    public synchronized List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public synchronized void remove(CartItem item) {
        items.remove(item);
        save();
    }

    public synchronized void clear() {
        items.clear();
        save();
    }

    public synchronized double getTotal() {
        double total = 0;
        for (CartItem it : items) {
            total += it.getProduct().getPrice() * it.getQuantity();
        }
        return total;
    }

    public synchronized int getItemCount() {
        int count = 0;
        for (CartItem it : items) {
            count += it.getQuantity();
        }
        return count;
    }

    public synchronized void save() {
        List<String> lines = new ArrayList<>();
        for (CartItem item : items) {
            lines.add(item.getProduct().getId()
                    + "\t" + AppStorage.encode(item.getSize())
                    + "\t" + item.getQuantity());
        }
        AppStorage.writeLines(CART_FILE, lines);
    }

    private void load() {
        items.clear();
        for (String line : AppStorage.readLines(CART_FILE)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 3) {
                continue;
            }
            try {
                int productId = Integer.parseInt(parts[0]);
                String size = AppStorage.decode(parts[1]);
                int quantity = Math.max(1, Integer.parseInt(parts[2]));
                Optional<Product> product = productDAO.findById(productId);
                if (product.isPresent() && product.get().getQuantity() > 0) {
                    int safeQuantity = Math.min(quantity, product.get().getQuantity());
                    items.add(new CartItem(product.get(), size.isBlank() ? null : size, safeQuantity));
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }
}

