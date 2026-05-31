package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.model.CartItem;
import com.clothingstore.clothingstoreapp.model.CustomerOrder;
import com.clothingstore.clothingstoreapp.dao.ProductDAO;
import com.clothingstore.clothingstoreapp.model.Product;
import com.clothingstore.clothingstoreapp.service.CartService;
import com.clothingstore.clothingstoreapp.service.OrderHistoryService;
import com.clothingstore.clothingstoreapp.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartController {

    private static final double CART_IMAGE_SIZE = 128;

    @FXML
    private VBox itemsBox;

    @FXML
    private Label cartCountLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Button checkoutBtn;

    @FXML
    private TextField customerNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField streetField;

    @FXML
    private TextField houseField;

    @FXML
    private TextField apartmentField;

    @FXML
    private ComboBox<String> paymentCombo;

    @FXML
    private TextArea commentArea;

    private CartService cartService;
    private OrderHistoryService orderHistoryService;
    private ProductDAO productDAO;

    @FXML
    private void initialize() {
        cartService = CartService.getInstance();
        orderHistoryService = OrderHistoryService.getInstance();
        productDAO = new ProductDAO();
        paymentCombo.getSelectionModel().selectFirst();
        configureCheckoutValidation();
        refresh();
        checkoutBtn.setOnAction(e -> checkout());
    }

    private void refresh() {
        itemsBox.getChildren().clear();
        for (CartItem item : cartService.getItems()) {
            itemsBox.getChildren().add(buildRow(item));
        }
        if (cartCountLabel != null) {
            cartCountLabel.setText("Товары в корзине (" + cartService.getItems().size() + ")");
        }
        totalLabel.setText(formatMoney(cartService.getTotal()));
    }

    private HBox buildRow(CartItem item) {
        HBox row = new HBox(18);
        row.getStyleClass().add("cart-row");
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView image = new ImageView(ImageUtil.loadProductImage(item.getProduct().getImagePath(), CART_IMAGE_SIZE, CART_IMAGE_SIZE));
        image.setFitWidth(CART_IMAGE_SIZE);
        image.setFitHeight(CART_IMAGE_SIZE);
        image.setPreserveRatio(false);
        image.setSmooth(true);

        VBox imageBox = new VBox(image);
        imageBox.getStyleClass().add("cart-image-frame");
        imageBox.setMinWidth(166);
        imageBox.setPrefWidth(166);
        imageBox.setAlignment(Pos.CENTER);

        VBox nameBox = new VBox(8);
        nameBox.getStyleClass().add("cart-product-info");
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        nameBox.setMinWidth(280);
        nameBox.setPrefWidth(340);
        Label name = new Label(item.getProduct().getName());
        name.getStyleClass().add("cart-product-name");
        name.setMaxWidth(Double.MAX_VALUE);
        name.setWrapText(true);
        Label brand = new Label(item.getProduct().getBrand() == null ? "" : item.getProduct().getBrand());
        brand.getStyleClass().add("cart-product-brand");
        brand.setMaxWidth(Double.MAX_VALUE);
        brand.setWrapText(true);
        int available = getAvailableQuantity(item);
        if (available <= 0) {
            return buildUnavailableRow(item);
        }
        if (item.getQuantity() > available) {
            item.setQuantity(available);
            cartService.save();
        }
        Label stock = new Label("Доступно: " + available);
        stock.setText("Доступно: " + available + " шт.");
        stock.getStyleClass().add("cart-stock-text");

        String detailsText = buildItemDetails(item);
        Label details = new Label(detailsText);
        details.getStyleClass().add("cart-product-details");
        details.setMaxWidth(Double.MAX_VALUE);
        details.setWrapText(true);
        details.setManaged(!detailsText.isEmpty());
        details.setVisible(!detailsText.isEmpty());

        Label stockDot = new Label("●");
        stockDot.getStyleClass().add("cart-stock-dot");
        HBox stockRow = new HBox(10, stockDot, stock);
        stockRow.getStyleClass().add("cart-stock-row");

        nameBox.getChildren().addAll(name, brand, details, stockRow);

        HBox quantity = buildQuantityStepper(item, available);

        Label price = new Label(formatMoney(item.getProduct().getPrice()));
        price.getStyleClass().add("cart-row-price");

        Button remove = new Button("Удалить");
        remove.setText("🗑");
        remove.getStyleClass().addAll("button-outline", "cart-remove-button");
        remove.setOnAction(e -> {
            cartService.remove(item);
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(16, quantity, remove);
        controls.getStyleClass().add("cart-row-controls");

        VBox rightBox = new VBox(20, price, controls);
        rightBox.getStyleClass().add("cart-row-actions");

        row.getChildren().addAll(imageBox, nameBox, spacer, rightBox);
        return row;
    }

    private HBox buildQuantityStepper(CartItem item, int available) {
        HBox stepper = new HBox(0);
        stepper.getStyleClass().add("cart-quantity-stepper");
        stepper.setAlignment(Pos.CENTER);

        Button minus = new Button("-");
        minus.getStyleClass().add("cart-quantity-button");
        minus.setDisable(item.getQuantity() <= 1);
        minus.setOnAction(event -> {
            item.setQuantity(Math.max(1, item.getQuantity() - 1));
            cartService.save();
            refresh();
        });

        Label value = new Label(String.valueOf(item.getQuantity()));
        value.getStyleClass().add("cart-quantity-value");

        Button plus = new Button("+");
        plus.getStyleClass().add("cart-quantity-button");
        plus.setDisable(item.getQuantity() >= available);
        plus.setOnAction(event -> {
            item.setQuantity(Math.min(available, item.getQuantity() + 1));
            cartService.save();
            refresh();
        });

        stepper.getChildren().addAll(minus, value, plus);
        return stepper;
    }

    private String buildItemDetails(CartItem item) {
        List<String> parts = new ArrayList<>();
        String size = clean(item.getSize());
        if (size.isEmpty()) {
            size = clean(item.getProduct().getSize());
        }
        String color = clean(item.getProduct().getColor());
        if (!size.isEmpty()) {
            parts.add("Размер: " + size);
        }
        if (!color.isEmpty()) {
            parts.add("Цвет: " + color);
        }
        return String.join("  •  ", parts);
    }

    private HBox buildUnavailableRow(CartItem item) {
        HBox row = new HBox(10);
        row.getStyleClass().add("cart-row");
        row.setAlignment(Pos.CENTER_LEFT);
        Label message = new Label(item.getProduct().getName() + " больше нет в наличии");
        message.setPrefWidth(360);
        Button remove = new Button("Удалить");
        remove.getStyleClass().addAll("button-outline", "cart-remove-button");
        remove.setMinWidth(100);
        remove.setPrefWidth(100);
        remove.setOnAction(e -> {
            cartService.remove(item);
            refresh();
        });
        row.getChildren().addAll(message, remove);
        return row;
    }

    private int getAvailableQuantity(CartItem item) {
        Optional<Product> current = productDAO.findById(item.getProduct().getId());
        return current.map(Product::getQuantity).orElse(0);
    }

    private void checkout() {
        if (cartService.getItems().isEmpty()) {
            showInfo("Корзина пуста", "Добавьте товары перед оформлением заказа.");
            return;
        }

        String name = clean(customerNameField.getText());
        String phone = clean(phoneField.getText());
        String street = clean(streetField.getText());
        String house = clean(houseField.getText());
        String apartment = clean(apartmentField.getText());
        String payment = paymentCombo.getValue();

        if (name.isEmpty() || phone.isEmpty() || street.isEmpty() || house.isEmpty()
                || payment == null || payment.isBlank()) {
            showInfo("Заполните заказ", "Укажите имя, телефон, улицу, дом и способ оплаты.");
            return;
        }

        String validationError = validateCheckoutFields(name, phone, street, house, apartment);
        if (!validationError.isEmpty()) {
            showInfo("Проверьте данные", validationError);
            return;
        }

        String stockError = validateStock();
        if (!stockError.isEmpty()) {
            showInfo("Недостаточно товара", stockError);
            refresh();
            return;
        }

        double total = cartService.getTotal();
        List<String> orderedItems = new ArrayList<>();
        for (CartItem item : cartService.getItems()) {
            orderedItems.add(item.getProduct().getName()
                    + " x" + item.getQuantity()
                    + " - " + formatMoney(item.getProduct().getPrice() * item.getQuantity()));
        }

        StringBuilder address = new StringBuilder();
        address.append(street).append(", дом ").append(house);
        if (!apartment.isEmpty()) {
            address.append(", кв. ").append(apartment);
        }

        String comment = clean(commentArea.getText());
        CustomerOrder savedOrder = orderHistoryService.add(new CustomerOrder(
                0,
                LocalDateTime.now(),
                name,
                phone,
                address.toString(),
                payment,
                comment,
                total,
                orderedItems
        ));

        for (CartItem item : cartService.getItems()) {
            productDAO.decreaseQuantity(item.getProduct().getId(), item.getQuantity());
        }

        StringBuilder message = new StringBuilder();
        message.append("Заказ оформлен.\n\n");
        message.append("Номер заказа: #").append(savedOrder.getId()).append("\n");
        message.append("Клиент: ").append(name).append("\n");
        message.append("Телефон: ").append(phone).append("\n");
        message.append("Адрес: ").append(address).append("\n");
        message.append("\nОплата: ").append(payment);

        if (!comment.isEmpty()) {
            message.append("\nКомментарий: ").append(comment);
        }

        message.append("\n\nИтого: ").append(formatMoney(total));

        cartService.clear();
        refresh();
        clearCheckoutForm();
        showInfo("Готово", message.toString());
    }

    private String validateStock() {
        StringBuilder message = new StringBuilder();
        for (CartItem item : cartService.getItems()) {
            int available = getAvailableQuantity(item);
            if (available <= 0) {
                message.append(item.getProduct().getName()).append(": товара больше нет в наличии.\n");
            } else if (item.getQuantity() > available) {
                message.append(item.getProduct().getName())
                        .append(": доступно ")
                        .append(available)
                        .append(", в корзине ")
                        .append(item.getQuantity())
                        .append(".\n");
                item.setQuantity(available);
                cartService.save();
            }
        }
        return message.toString();
    }

    private void clearCheckoutForm() {
        customerNameField.clear();
        phoneField.clear();
        streetField.clear();
        houseField.clear();
        apartmentField.clear();
        commentArea.clear();
        paymentCombo.getSelectionModel().selectFirst();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void configureCheckoutValidation() {
        customerNameField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[A-Za-zА-Яа-яЁё\\s-]{0,40}") ? change : null));
        phoneField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[+\\d\\s()\\-]{0,20}") ? change : null));
        streetField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[A-Za-zА-Яа-яЁё0-9\\s.\\-]{0,60}") ? change : null));
        houseField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[0-9A-Za-zА-Яа-яЁё/-]{0,10}") ? change : null));
        apartmentField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,6}") ? change : null));
    }

    private String validateCheckoutFields(String name, String phone, String street, String house, String apartment) {
        StringBuilder errors = new StringBuilder();

        if (!name.matches("[A-Za-zА-Яа-яЁё]+([\\s-][A-Za-zА-Яа-яЁё]+)*") || name.length() < 2) {
            errors.append("Имя должно содержать только буквы, пробел или дефис и быть не короче 2 символов.\n");
        }

        String phoneDigits = phone.replaceAll("\\D", "");
        if (phoneDigits.length() < 8 || phoneDigits.length() > 15) {
            errors.append("Телефон должен содержать от 8 до 15 цифр. Можно использовать +, пробелы, скобки и дефисы.\n");
        }

        if (!street.matches(".*[A-Za-zА-Яа-яЁё].*") || street.length() < 3) {
            errors.append("Улица должна содержать название, а не только цифры или символы.\n");
        }

        if (!house.matches("\\d+[A-Za-zА-Яа-яЁё]?([/-]\\d+[A-Za-zА-Яа-яЁё]?)?")) {
            errors.append("Дом укажите в формате 12, 12A или 12/1.\n");
        }

        if (!apartment.isEmpty() && !apartment.matches("\\d{1,6}")) {
            errors.append("Квартира должна содержать только цифры.\n");
        }

        return errors.toString();
    }

    private String formatMoney(double value) {
        return "$" + String.format("%.2f", value).replace('.', ',');
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) totalLabel.getScene().getWindow();
        stage.close();
    }
}
