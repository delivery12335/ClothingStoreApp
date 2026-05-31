package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.HelloApplication;
import com.clothingstore.clothingstoreapp.dao.ProductDAO;
import com.clothingstore.clothingstoreapp.model.Product;
import com.clothingstore.clothingstoreapp.service.CartService;
import com.clothingstore.clothingstoreapp.util.ImageUtil;
import com.clothingstore.clothingstoreapp.util.ToastHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailController {

    private static final double DETAIL_IMAGE_WIDTH = 520;
    private static final double DETAIL_IMAGE_HEIGHT = 560;
    private static final double THUMBNAIL_SIZE = 96;

    @FXML private ImageView mainImageView;
    @FXML private HBox thumbnailsBox;
    @FXML private Label titleLabel;
    @FXML private Label brandLabel;
    @FXML private Label stockLabel;
    @FXML private Label metaLabel;
    @FXML private Label priceLabel;
    @FXML private HBox colorBox;
    @FXML private HBox sizeBox;
    @FXML private TextArea descriptionArea;
    @FXML private Button addToCartBtn;
    @FXML private Button decreaseQuantityBtn;
    @FXML private Button increaseQuantityBtn;
    @FXML private Label quantityValueLabel;
    @FXML private Label cartCountLabel;
    @FXML private Label breadcrumbProductLabel;
    @FXML private StackPane detailRoot;
    @FXML private ImageView detailBackgroundImage;

    private final ProductDAO productDAO = new ProductDAO();
    private Product currentProduct;
    private int selectedQuantity = 1;

    @FXML
    private void initialize() {
        configureDetailBackground();
        updateCartCount();
    }

    private void configureDetailBackground() {
        if (detailRoot == null || detailBackgroundImage == null) {
            return;
        }

        detailBackgroundImage.fitWidthProperty().bind(detailRoot.widthProperty().add(28));
        detailBackgroundImage.fitHeightProperty().bind(detailRoot.heightProperty().add(28));
        detailBackgroundImage.setMouseTransparent(true);
        detailBackgroundImage.toBack();
    }

    public void setProduct(Product product) {
        if (product == null) {
            return;
        }

        currentProduct = productDAO.findById(product.getId()).orElse(product);
        Product freshProduct = currentProduct;

        titleLabel.setText(freshProduct.getName());
        brandLabel.setText(freshProduct.getBrand() == null || freshProduct.getBrand().isBlank() ? "Brand" : freshProduct.getBrand());
        stockLabel.setText(freshProduct.getQuantity() > 0 ? "В наличии" : "Нет в наличии");
        stockLabel.getStyleClass().removeAll("detail-stock-pill", "detail-stock-pill-empty");
        stockLabel.getStyleClass().add(freshProduct.getQuantity() > 0 ? "detail-stock-pill" : "detail-stock-pill-empty");
        metaLabel.setText("ID " + freshProduct.getId() + "  |  " + freshProduct.getQuantity() + " доступно");
        priceLabel.setText("$" + String.format("%.2f", freshProduct.getPrice()));
        descriptionArea.setText(freshProduct.getDescription() == null ? "" : freshProduct.getDescription());
        if (breadcrumbProductLabel != null) {
            breadcrumbProductLabel.setText(freshProduct.getName());
        }

        Image img = ImageUtil.loadProductImage(freshProduct.getImagePath(), DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT);
        mainImageView.setImage(img);
        mainImageView.setFitWidth(DETAIL_IMAGE_WIDTH);
        mainImageView.setFitHeight(DETAIL_IMAGE_HEIGHT);
        mainImageView.setPreserveRatio(false);
        mainImageView.setSmooth(true);
        buildThumbnails(freshProduct);
        buildColor(freshProduct);
        ToggleGroup sizeGroup = buildSizes(freshProduct);

        selectedQuantity = 1;
        updateQuantityControls();
        updateCartCount();
        addToCartBtn.setDisable(getRemainingQuantity(freshProduct) <= 0);
        addToCartBtn.setOnAction(e -> addCurrentProductToCart(sizeGroup));
    }

    private void buildThumbnails(Product product) {
        thumbnailsBox.getChildren().clear();
        List<String> candidates = new ArrayList<>();
        String original = product.getImagePath() == null ? "" : product.getImagePath();
        if (!original.isBlank()) {
            candidates.add(original);
        }

        String filename = new File(original).getName();
        String base = filename;
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot > 0) {
            base = filename.substring(0, dot);
            ext = filename.substring(dot + 1);
        }

        String[] suffixes = {"_1", "_2", "-1", "-2"};
        for (String suffix : suffixes) {
            String candidate = "/com/clothingstore/clothingstoreapp/images/" + base + suffix + (ext.isEmpty() ? "" : "." + ext);
            if (HelloApplication.class.getResource(candidate) != null) {
                candidates.add(candidate);
            }
        }

        if (candidates.isEmpty()) {
            candidates.add(original);
        }

        boolean first = true;
        for (String path : candidates) {
            ImageView thumb = new ImageView(ImageUtil.loadProductImage(path, THUMBNAIL_SIZE, THUMBNAIL_SIZE));
            thumb.setFitWidth(THUMBNAIL_SIZE);
            thumb.setFitHeight(THUMBNAIL_SIZE);
            thumb.setPreserveRatio(false);
            thumb.setSmooth(true);
            thumb.setCursor(javafx.scene.Cursor.HAND);
            thumb.setOnMouseClicked(e -> mainImageView.setImage(thumb.getImage()));
            if (first) {
                thumb.getStyleClass().add("thumbnail-selected");
                first = false;
            }
            thumbnailsBox.getChildren().add(thumb);
        }
    }

    private void buildColor(Product product) {
        colorBox.getChildren().clear();
        if (product.getColor() != null) {
            Button color = new Button(product.getColor());
            color.getStyleClass().add("detail-option-chip");
            color.setDisable(true);
            colorBox.getChildren().add(color);
        }
    }

    private ToggleGroup buildSizes(Product product) {
        sizeBox.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        if (product.getSize() != null && !product.getSize().isBlank()) {
            String[] parts = product.getSize().split("[,;/]\\\\s*|\\s+");
            for (String size : parts) {
                if (size == null || size.isBlank()) {
                    continue;
                }
                ToggleButton button = new ToggleButton(size.trim());
                button.setToggleGroup(group);
                button.getStyleClass().add("detail-size-toggle");
                sizeBox.getChildren().add(button);
            }
        }
        if (group.getToggles().size() == 1) {
            group.selectToggle(group.getToggles().get(0));
        }
        return group;
    }

    private void addCurrentProductToCart(ToggleGroup sizeGroup) {
        Product freshProduct = productDAO.findById(currentProduct.getId()).orElse(currentProduct);
        currentProduct = freshProduct;

        if (freshProduct.getQuantity() <= 0) {
            setProduct(freshProduct);
            showInfo("Нет в наличии", "Этот товар сейчас закончился.");
            return;
        }

        int remainingQuantity = getRemainingQuantity(freshProduct);
        if (remainingQuantity <= 0) {
            setProduct(freshProduct);
            showInfo("Недостаточно товара", "В корзине уже максимальное доступное количество: " + freshProduct.getQuantity());
            return;
        }

        String selectedSize = null;
        if (sizeGroup.getSelectedToggle() != null) {
            selectedSize = ((ToggleButton) sizeGroup.getSelectedToggle()).getText();
        }

        int quantityToAdd = Math.min(selectedQuantity, remainingQuantity);
        CartService.getInstance().add(freshProduct, selectedSize, quantityToAdd);
        ToastHelper.showAddedToCart((Stage) titleLabel.getScene().getWindow(), freshProduct.getName());
        updateCartCount();
        setProduct(freshProduct);
    }

    @FXML
    private void handleDecreaseQuantity() {
        if (selectedQuantity > 1) {
            selectedQuantity--;
            updateQuantityControls();
        }
    }

    @FXML
    private void handleIncreaseQuantity() {
        int maxQuantity = Math.max(1, getRemainingQuantity(currentProduct));
        if (selectedQuantity < maxQuantity) {
            selectedQuantity++;
            updateQuantityControls();
        }
    }

    private void updateQuantityControls() {
        if (quantityValueLabel == null) {
            return;
        }

        int remainingQuantity = getRemainingQuantity(currentProduct);
        int maxQuantity = Math.max(1, remainingQuantity);
        selectedQuantity = Math.max(1, Math.min(selectedQuantity, maxQuantity));

        quantityValueLabel.setText(String.valueOf(selectedQuantity));
        if (decreaseQuantityBtn != null) {
            decreaseQuantityBtn.setDisable(remainingQuantity <= 0 || selectedQuantity <= 1);
        }
        if (increaseQuantityBtn != null) {
            increaseQuantityBtn.setDisable(remainingQuantity <= 0 || selectedQuantity >= maxQuantity);
        }
    }

    private int getRemainingQuantity(Product product) {
        if (product == null) {
            return 0;
        }
        return Math.max(0, product.getQuantity() - CartService.getInstance().getQuantityForProduct(product.getId()));
    }

    private void updateCartCount() {
        if (cartCountLabel == null) {
            return;
        }

        int itemCount = CartService.getInstance().getItemCount();
        cartCountLabel.setText(String.valueOf(itemCount));
        boolean hasItems = itemCount > 0;
        cartCountLabel.setVisible(hasItems);
        cartCountLabel.setManaged(hasItems);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openCart() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/cart-view.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 820);
            scene.setFill(javafx.scene.paint.Color.WHITE);
            java.net.URL cssUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            HelloApplication.applyApplicationIcon(stage);
            stage.setTitle("Корзина");
            stage.setScene(scene);
            stage.setMinWidth(1260);
            stage.setMinHeight(720);
            stage.setOnHidden(event -> {
                if (currentProduct != null) {
                    productDAO.findById(currentProduct.getId()).ifPresent(this::setProduct);
                }
                updateCartCount();
            });
            stage.show();
        } catch (Exception e) {
            showInfo("Ошибка", "Не удалось открыть корзину: " + e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
