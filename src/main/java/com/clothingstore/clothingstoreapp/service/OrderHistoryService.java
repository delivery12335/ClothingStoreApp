package com.clothingstore.clothingstoreapp.service;

import com.clothingstore.clothingstoreapp.model.CustomerOrder;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryService {
    private static final Path ORDERS_FILE = AppStorage.file("orders.tsv");
    private static OrderHistoryService instance;

    private final List<CustomerOrder> orders = new ArrayList<>();
    private int nextId = 1;

    private OrderHistoryService() {
        load();
    }

    public static synchronized OrderHistoryService getInstance() {
        if (instance == null) {
            instance = new OrderHistoryService();
        }
        return instance;
    }

    public synchronized CustomerOrder add(CustomerOrder order) {
        CustomerOrder saved = new CustomerOrder(
                nextId++,
                order.getCreatedAt(),
                order.getCustomerName(),
                order.getPhone(),
                order.getAddress(),
                order.getPaymentMethod(),
                order.getComment(),
                order.getTotal(),
                order.getItems()
        );
        orders.add(0, saved);
        save();
        return saved;
    }

    public synchronized List<CustomerOrder> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (CustomerOrder order : orders) {
            List<String> encodedItems = new ArrayList<>();
            for (String item : order.getItems()) {
                encodedItems.add(AppStorage.encode(item));
            }

            lines.add(order.getId()
                    + "\t" + order.getCreatedAt()
                    + "\t" + AppStorage.encode(order.getCustomerName())
                    + "\t" + AppStorage.encode(order.getPhone())
                    + "\t" + AppStorage.encode(order.getAddress())
                    + "\t" + AppStorage.encode(order.getPaymentMethod())
                    + "\t" + AppStorage.encode(order.getComment())
                    + "\t" + order.getTotal()
                    + "\t" + String.join(",", encodedItems));
        }
        AppStorage.writeLines(ORDERS_FILE, lines);
    }

    private void load() {
        orders.clear();
        int maxId = 0;
        for (String line : AppStorage.readLines(ORDERS_FILE)) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 9) {
                continue;
            }
            try {
                int id = Integer.parseInt(parts[0]);
                LocalDateTime createdAt = LocalDateTime.parse(parts[1]);
                double total = Double.parseDouble(parts[7]);
                List<String> items = new ArrayList<>();
                if (!parts[8].isBlank()) {
                    for (String item : parts[8].split(",", -1)) {
                        if (!item.isBlank()) {
                            items.add(AppStorage.decode(item));
                        }
                    }
                }

                orders.add(new CustomerOrder(
                        id,
                        createdAt,
                        AppStorage.decode(parts[2]),
                        AppStorage.decode(parts[3]),
                        AppStorage.decode(parts[4]),
                        AppStorage.decode(parts[5]),
                        AppStorage.decode(parts[6]),
                        total,
                        items
                ));
                maxId = Math.max(maxId, id);
            } catch (RuntimeException ignored) {
            }
        }
        nextId = maxId + 1;
    }
}
