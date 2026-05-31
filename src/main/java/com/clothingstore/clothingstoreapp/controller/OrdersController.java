package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.dao.ProductDAO;
import com.clothingstore.clothingstoreapp.model.CustomerOrder;
import com.clothingstore.clothingstoreapp.model.Product;
import com.clothingstore.clothingstoreapp.service.OrderHistoryService;
import com.clothingstore.clothingstoreapp.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class OrdersController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final double ORDER_IMAGE_WIDTH = 62;
    private static final double ORDER_IMAGE_HEIGHT = 40;
    private static final double DETAIL_ICON_SIZE = 18;

    @FXML
    private VBox ordersBox;

    @FXML
    private VBox emptyState;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    private void initialize() {
        refresh();
    }

    private void refresh() {
        ordersBox.getChildren().clear();

        var orders = OrderHistoryService.getInstance().getOrders();
        boolean empty = orders.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);

        for (CustomerOrder order : orders) {
            ordersBox.getChildren().add(buildOrderCard(order));
        }
    }

    private VBox buildOrderCard(CustomerOrder order) {
        OrderVisual visual = visualFor(order);

        VBox card = new VBox(18);
        card.getStyleClass().add("order-card");
        card.getStyleClass().add(visual.cardClass());

        HBox cardContent = new HBox(22);
        cardContent.setAlignment(Pos.TOP_LEFT);

        ImageView statusImage = new ImageView(loadResourceImage("/com/clothingstore/clothingstoreapp/images/order-processing-icon.png"));
        statusImage.setFitWidth(24);
        statusImage.setFitHeight(24);
        statusImage.setPreserveRatio(true);
        statusImage.setSmooth(true);
        statusImage.getStyleClass().add("order-status-image-icon");

        StackPane icon = new StackPane(statusImage);
        icon.getStyleClass().addAll("order-status-icon", visual.iconClass());

        VBox main = new VBox(16);
        HBox.setHgrow(main, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(6);
        Label number = new Label("Заказ #" + order.getId());
        number.getStyleClass().add("order-title");
        Label date = new Label(order.getCreatedAt().format(DATE_FORMAT));
        date.getStyleClass().add("order-date");
        titleBox.getChildren().addAll(number, date);

        Label status = new Label(visual.text());
        status.getStyleClass().addAll("order-status-pill", visual.pillClass());
        header.getChildren().addAll(titleBox, status);

        HBox body = new HBox(20);
        body.setAlignment(Pos.TOP_LEFT);

        VBox items = new VBox(8);
        items.getStyleClass().add("order-items-box");
        HBox.setHgrow(items, Priority.ALWAYS);
        for (String item : order.getItems()) {
            items.getChildren().add(buildItemRow(item));
        }

        VBox details = new VBox(14);
        details.getStyleClass().add("order-details-box");
        details.getChildren().addAll(
                detailLine("checkout-user-icon.png", order.getCustomerName() + " | " + order.getPhone()),
                detailLine("checkout-location-icon.png", "Адрес: " + order.getAddress()),
                detailLine("checkout-payment-icon.png", "Оплата: " + order.getPaymentMethod()),
                detailLine("checkout-order-icon.png", "Комментарий: " + cleanComment(order.getComment()))
        );

        body.getChildren().addAll(items, details);
        main.getChildren().addAll(header, body);

        VBox right = new VBox(26);
        right.setAlignment(Pos.TOP_RIGHT);
        Label total = new Label(formatMoney(order.getTotal()));
        total.getStyleClass().addAll("order-total", visual.totalClass());
        right.getChildren().add(total);

        cardContent.getChildren().addAll(icon, main, right);
        card.getChildren().add(cardContent);

        return card;
    }

    private HBox buildItemRow(String rawItem) {
        OrderItem item = parseItem(rawItem);
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("order-item-row");

        Optional<Product> product = findProduct(item.name());
        ImageView image = new ImageView(ImageUtil.loadProductImage(product.map(Product::getImagePath).orElse(""), ORDER_IMAGE_WIDTH, ORDER_IMAGE_HEIGHT));
        image.setFitWidth(ORDER_IMAGE_WIDTH);
        image.setFitHeight(ORDER_IMAGE_HEIGHT);
        image.setPreserveRatio(false);
        image.setSmooth(true);

        VBox imageFrame = new VBox(image);
        imageFrame.getStyleClass().add("order-item-image-frame");
        imageFrame.setAlignment(Pos.CENTER);

        Label name = new Label(item.name());
        name.getStyleClass().add("order-item-name");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label quantity = new Label("x" + item.quantity());
        quantity.getStyleClass().add("order-item-quantity");

        Label price = new Label(item.price());
        price.getStyleClass().add("order-item-price");

        row.getChildren().addAll(imageFrame, name, quantity, price);
        return row;
    }

    private HBox detailLine(String iconFile, String text) {
        HBox line = new HBox(14);
        line.setAlignment(Pos.CENTER_LEFT);
        ImageView icon = new ImageView(ImageUtil.loadProductImage("com/clothingstore/clothingstoreapp/images/" + iconFile, DETAIL_ICON_SIZE, DETAIL_ICON_SIZE));
        icon.setFitWidth(DETAIL_ICON_SIZE);
        icon.setFitHeight(DETAIL_ICON_SIZE);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);
        icon.getStyleClass().add("order-detail-image-icon");
        Label label = new Label(text);
        label.getStyleClass().add("order-detail-text");
        label.setWrapText(true);
        line.getChildren().addAll(icon, label);
        return line;
    }

    private Optional<Product> findProduct(String name) {
        List<Product> products = productDAO.getAllProducts();
        return products.stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    private Image loadResourceImage(String path) {
        var resource = getClass().getResource(path);
        return resource == null ? null : new Image(resource.toExternalForm(), 24, 24, true, true);
    }

    private OrderItem parseItem(String rawItem) {
        String name = rawItem;
        String price = "";
        int quantity = 1;

        int priceIndex = rawItem.lastIndexOf(" - ");
        if (priceIndex >= 0) {
            price = rawItem.substring(priceIndex + 3).replace('.', ',');
            name = rawItem.substring(0, priceIndex);
        }

        int quantityIndex = name.lastIndexOf(" x");
        if (quantityIndex >= 0) {
            String quantityText = name.substring(quantityIndex + 2).trim();
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException ignored) {
                quantity = 1;
            }
            name = name.substring(0, quantityIndex).trim();
        }

        return new OrderItem(name, quantity, price);
    }

    private OrderVisual visualFor(CustomerOrder order) {
        return new OrderVisual("Обрабатывается", "order-card-processing", "order-icon-processing", "order-pill-processing", "order-total-processing");
    }

    private String cleanComment(String comment) {
        return comment == null || comment.isBlank() ? "Нет" : comment;
    }

    private String formatMoney(double value) {
        return "$" + String.format("%.2f", value).replace('.', ',');
    }

    private record OrderItem(String name, int quantity, String price) {}

    private record OrderVisual(String text, String cardClass, String iconClass, String pillClass, String totalClass) {}

    @FXML
    private void handleClose() {
        Stage stage = (Stage) ordersBox.getScene().getWindow();
        stage.close();
    }
}
