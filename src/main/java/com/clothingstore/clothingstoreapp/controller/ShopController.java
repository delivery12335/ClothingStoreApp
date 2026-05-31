package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.HelloApplication;
import com.clothingstore.clothingstoreapp.dao.ProductDAO;
import com.clothingstore.clothingstoreapp.model.Product;
import com.clothingstore.clothingstoreapp.model.ProductFilter;

import com.clothingstore.clothingstoreapp.service.CartService;
import com.clothingstore.clothingstoreapp.util.ImageUtil;
import com.clothingstore.clothingstoreapp.util.ToastHelper;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopController {

    private static final double TILE_WIDTH = 246;
    private static final double TILE_HEIGHT = 402;
    private static final double TILE_HGAP = 22;
    private static final double PRODUCT_IMAGE_WIDTH = 178;
    private static final double PRODUCT_IMAGE_HEIGHT = 148;
    private static final int PRICE_MIN = 0;
    private static final int PRICE_MAX = 1000;
    private static final int PRICE_STEP = 10;
    private static final double PRICE_THUMB_SIZE = 18;
    private static final String FILTER_ALL = "Все категории";
    private static final String FILTER_ANY = "Любой";
    private static final String FILTER_ALL_BRANDS = "Все бренды";
    private static final String FILTER_ANY_COLOR = "Любой цвет";
    private static final String FILTER_ANY_SIZE = "Любой размер";
    private static final String CATEGORY_WOMEN = "Женщины";
    private static final String CATEGORY_MEN = "Мужчины";

    @FXML
    private TilePane productsContainer;

    @FXML
    private ScrollPane productsScrollPane;

    @FXML
    private VBox emptyState;

    @FXML
    private TextField minPriceSpinner;

    @FXML
    private TextField maxPriceSpinner;

    @FXML
    private Pane priceRangeSlider;

    @FXML
    private Region priceRangeTrack;

    @FXML
    private Region priceRangeActive;

    @FXML
    private StackPane minPriceThumb;

    @FXML
    private StackPane maxPriceThumb;

    @FXML
    private ComboBox<String> categoryCombo;

    @FXML
    private ComboBox<String> brandCombo;

    @FXML
    private ComboBox<String> colorCombo;

    @FXML
    private ComboBox<String> sizeCombo;

    @FXML
    private Label cartCountLabel;

    @FXML
    private TextField searchField;

    @FXML
    private StackPane shopRoot;

    @FXML
    private ImageView shopBackgroundImage;

    private List<Product> allProducts;
    private ProductDAO productDAO;
    private CartService cartService;
    private final Map<String, Integer> categoryIdsByName = new HashMap<>();
    private final Map<Integer, String> categoryNamesById = new HashMap<>();
    private boolean updatingPriceControls;

    @FXML
    public void initialize() {
        try {
            configureShopBackground();

            productDAO = new ProductDAO();
            cartService = CartService.getInstance();
            allProducts = productDAO.getAllProducts();

            // Setup price fields
            minPriceSpinner.setText("$" + PRICE_MIN);
            maxPriceSpinner.setText("$" + PRICE_MAX);
            setupPriceRangeSlider();

            // Populate filter combos
            categoryIdsByName.clear();
            categoryNamesById.clear();
            categoryCombo.getItems().clear();
            categoryCombo.getItems().add(FILTER_ALL);
            // try to load categories from DB and also include common categories
            try {
                com.clothingstore.clothingstoreapp.dao.CategoryDAO catDao = new com.clothingstore.clothingstoreapp.dao.CategoryDAO();
                java.util.List<com.clothingstore.clothingstoreapp.model.Category> cats = catDao.getAllCategories();
                for (com.clothingstore.clothingstoreapp.model.Category c : cats) {
                    if (shouldShowCategory(c.getName())) {
                        String displayName = localizeCategoryName(c.getName());
                        categoryIdsByName.put(c.getName().toLowerCase(), c.getId());
                        categoryIdsByName.put(displayName.toLowerCase(), c.getId());
                        categoryNamesById.put(c.getId(), c.getName());
                        if (!categoryCombo.getItems().contains(displayName)) categoryCombo.getItems().add(displayName);
                    }
                }
            } catch (Exception ignored) {}
            // ensure some common categories exist
            String[] commons = new String[]{CATEGORY_WOMEN, CATEGORY_MEN};
            for (String s : commons) {
                if (shouldShowCategory(s) && !categoryCombo.getItems().contains(s)) categoryCombo.getItems().add(s);
            }
            categoryCombo.getSelectionModel().selectFirst();

            populateBrandCombo();
            populateColorCombo();

            sizeCombo.getItems().clear();
            sizeCombo.getItems().addAll(FILTER_ANY_SIZE, "XS", "S", "M", "L", "XL", "XXL", "40", "41", "42", "43");
            sizeCombo.getSelectionModel().selectFirst();
            installAnimatedComboBox(categoryCombo);
            installAnimatedComboBox(brandCombo);
            installAnimatedComboBox(colorCombo);
            installAnimatedComboBox(sizeCombo);
            categoryCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters(false));
            brandCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters(false));
            colorCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters(false));
            sizeCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters(false));
            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters(false));
            }
            updateCartCount();

            productsContainer.setPrefColumns(5);
            productsContainer.setHgap(TILE_HGAP);
            productsContainer.setVgap(24);
            productsContainer.setPrefTileWidth(TILE_WIDTH);
            productsContainer.setPrefTileHeight(TILE_HEIGHT);
            productsScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                if (newBounds == null) {
                    return;
                }
                updateColumns(newBounds.getWidth());
            });

            displayAllProducts();
        } catch (Exception e) {
            System.err.println("Ошибка загрузки магазина: " + e.getMessage());
            e.printStackTrace(System.err);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка загрузки магазина");
            alert.setHeaderText(e.getClass().getSimpleName() + ": " + e.getMessage());

            StringBuilder sb = new StringBuilder();
            for (StackTraceElement el : e.getStackTrace()) {
                sb.append(el.toString()).append("\n");
            }

            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();
        }
    }

    private void configureShopBackground() {
        if (shopRoot == null || shopBackgroundImage == null) {
            return;
        }

        shopBackgroundImage.fitWidthProperty().bind(shopRoot.widthProperty().add(28));
        shopBackgroundImage.fitHeightProperty().bind(shopRoot.heightProperty().add(28));
        shopBackgroundImage.setMouseTransparent(true);
        shopBackgroundImage.setEffect(new GaussianBlur(2));
        shopBackgroundImage.toBack();
    }

    private void installAnimatedComboBox(ComboBox<String> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setSkin(new AnimatedComboBoxSkin<>(comboBox));
    }

    private void populateBrandCombo() {
        if (brandCombo == null) {
            return;
        }

        brandCombo.getItems().clear();
        brandCombo.getItems().add(FILTER_ALL_BRANDS);

        if (allProducts != null) {
            brandCombo.getItems().addAll(allProducts.stream()
                    .map(Product::getBrand)
                    .filter(brand -> brand != null && !brand.isBlank())
                    .map(String::trim)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList());
        }

        brandCombo.setVisibleRowCount(Math.min(brandCombo.getItems().size(), 16));
        brandCombo.getSelectionModel().selectFirst();
    }

    private void populateColorCombo() {
        if (colorCombo == null) {
            return;
        }

        colorCombo.getItems().clear();
        colorCombo.getItems().add(FILTER_ANY_COLOR);

        if (allProducts != null) {
            colorCombo.getItems().addAll(allProducts.stream()
                    .map(Product::getColor)
                    .filter(color -> color != null && !color.isBlank())
                    .map(String::trim)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList());
        }

        colorCombo.setVisibleRowCount(Math.min(colorCombo.getItems().size(), 16));
        colorCombo.getSelectionModel().selectFirst();
    }

    private static final class AnimatedComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

        private static final Duration OPEN_DURATION = Duration.millis(150);

        private AnimatedComboBoxSkin(ComboBox<T> comboBox) {
            super(comboBox);
        }

        @Override
        public void show() {
            super.show();

            Node popupContent = getPopupContent();
            if (popupContent == null) {
                return;
            }

            popupContent.setOpacity(0);
            popupContent.setTranslateY(-8);

            FadeTransition fade = new FadeTransition(OPEN_DURATION, popupContent);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(OPEN_DURATION, popupContent);
            slide.setFromY(-8);
            slide.setToY(0);

            ParallelTransition animation = new ParallelTransition(fade, slide);
            animation.setOnFinished(event -> {
                popupContent.setOpacity(1);
                popupContent.setTranslateY(0);
            });
            animation.play();
        }
    }

    private void displayAllProducts() {
        displayProducts(allProducts);
    }

    private void setupPriceRangeSlider() {
        if (priceRangeSlider == null) {
            return;
        }

        priceRangeSlider.widthProperty().addListener((obs, oldValue, newValue) -> updatePriceRangeSliderVisuals());
        priceRangeSlider.heightProperty().addListener((obs, oldValue, newValue) -> updatePriceRangeSliderVisuals());
        priceRangeSlider.setOnMousePressed(event -> {
            int value = priceValueForLocalX(event.getX());
            int minDistance = Math.abs(value - currentMinPrice());
            int maxDistance = Math.abs(value - currentMaxPrice());
            if (minDistance <= maxDistance) {
                setMinPriceValue(value, true);
            } else {
                setMaxPriceValue(value, true);
            }
            event.consume();
        });

        if (minPriceThumb != null) {
            minPriceThumb.setOnMouseDragged(event -> {
                setMinPriceValue(priceValueForScenePosition(event.getSceneX(), event.getSceneY()), true);
                event.consume();
            });
        }

        if (maxPriceThumb != null) {
            maxPriceThumb.setOnMouseDragged(event -> {
                setMaxPriceValue(priceValueForScenePosition(event.getSceneX(), event.getSceneY()), true);
                event.consume();
            });
        }

        updatePriceRangeSliderVisuals();
    }

    private void setMinPriceValue(Integer value, boolean applyFilter) {
        if (value == null || minPriceSpinner == null) {
            return;
        }
        int normalized = Math.min(normalizePrice(value), currentMaxPrice());
        setPriceFieldValue(minPriceSpinner, normalized);
        updatePriceRangeSliderVisuals();
        if (applyFilter) {
            applyFilters(false);
        }
    }

    private void setMaxPriceValue(Integer value, boolean applyFilter) {
        if (value == null || maxPriceSpinner == null) {
            return;
        }
        int normalized = Math.max(normalizePrice(value), currentMinPrice());
        setPriceFieldValue(maxPriceSpinner, normalized);
        updatePriceRangeSliderVisuals();
        if (applyFilter) {
            applyFilters(false);
        }
    }

    private void setPriceFieldValue(TextField field, int value) {
        String newValue = "$" + value;
        if (newValue.equals(field.getText())) {
            return;
        }

        updatingPriceControls = true;
        try {
            field.setText(newValue);
        } finally {
            updatingPriceControls = false;
        }
    }

    private int currentMinPrice() {
        return minPriceSpinner == null ? PRICE_MIN : parsePriceField(minPriceSpinner, PRICE_MIN);
    }

    private int currentMaxPrice() {
        return maxPriceSpinner == null ? PRICE_MAX : parsePriceField(maxPriceSpinner, PRICE_MAX);
    }

    private int parsePriceField(TextField field, int fallback) {
        String text = field.getText();
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return normalizePrice(Integer.parseInt(text.trim().replaceAll("[^0-9]", "")));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int normalizePrice(int value) {
        int clamped = Math.max(PRICE_MIN, Math.min(PRICE_MAX, value));
        int stepped = (int) Math.round(clamped / (double) PRICE_STEP) * PRICE_STEP;
        return Math.max(PRICE_MIN, Math.min(PRICE_MAX, stepped));
    }

    private int priceValueForScenePosition(double sceneX, double sceneY) {
        if (priceRangeSlider == null) {
            return PRICE_MIN;
        }
        Point2D localPoint = priceRangeSlider.sceneToLocal(sceneX, sceneY);
        return priceValueForLocalX(localPoint.getX());
    }

    private int priceValueForLocalX(double localX) {
        double trackStart = PRICE_THUMB_SIZE / 2.0;
        double trackWidth = getPriceSliderTrackWidth();
        double clampedX = Math.max(trackStart, Math.min(trackStart + trackWidth, localX));
        double ratio = (clampedX - trackStart) / trackWidth;
        return normalizePrice((int) Math.round(PRICE_MIN + ratio * (PRICE_MAX - PRICE_MIN)));
    }

    private void updatePriceRangeSliderVisuals() {
        if (priceRangeSlider == null || priceRangeTrack == null || priceRangeActive == null
                || minPriceThumb == null || maxPriceThumb == null) {
            return;
        }

        double width = getPriceSliderWidth();
        double height = getPriceSliderHeight();
        double trackStart = PRICE_THUMB_SIZE / 2.0;
        double trackWidth = getPriceSliderTrackWidth();
        double trackY = Math.max(0, (height - 4) / 2.0);
        double thumbY = Math.max(0, (height - PRICE_THUMB_SIZE) / 2.0);

        double minX = priceXForValue(currentMinPrice());
        double maxX = priceXForValue(currentMaxPrice());

        priceRangeTrack.setLayoutX(trackStart);
        priceRangeTrack.setLayoutY(trackY);
        priceRangeTrack.setPrefWidth(trackWidth);
        priceRangeTrack.setPrefHeight(4);

        priceRangeActive.setLayoutX(minX);
        priceRangeActive.setLayoutY(trackY);
        priceRangeActive.setPrefWidth(Math.max(0, maxX - minX));
        priceRangeActive.setPrefHeight(4);

        minPriceThumb.setLayoutX(minX - PRICE_THUMB_SIZE / 2.0);
        minPriceThumb.setLayoutY(thumbY);
        maxPriceThumb.setLayoutX(maxX - PRICE_THUMB_SIZE / 2.0);
        maxPriceThumb.setLayoutY(thumbY);

        priceRangeSlider.setMinWidth(width);
        priceRangeSlider.setPrefWidth(width);
        priceRangeSlider.setMaxWidth(width);
    }

    private double priceXForValue(int value) {
        double ratio = (normalizePrice(value) - PRICE_MIN) / (double) (PRICE_MAX - PRICE_MIN);
        return PRICE_THUMB_SIZE / 2.0 + ratio * getPriceSliderTrackWidth();
    }

    private double getPriceSliderTrackWidth() {
        return Math.max(1, getPriceSliderWidth() - PRICE_THUMB_SIZE);
    }

    private double getPriceSliderWidth() {
        if (priceRangeSlider == null) {
            return 210;
        }
        double width = priceRangeSlider.getWidth();
        if (width <= 0) {
            width = priceRangeSlider.getPrefWidth();
        }
        return width <= 0 ? 210 : width;
    }

    private double getPriceSliderHeight() {
        if (priceRangeSlider == null) {
            return 26;
        }
        double height = priceRangeSlider.getHeight();
        if (height <= 0) {
            height = priceRangeSlider.getPrefHeight();
        }
        return height <= 0 ? 26 : height;
    }

    private void displayProducts(List<Product> products) {
        productsContainer.getChildren().clear();

        if (products.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);

        for (Product product : products) {
            VBox productCard = createReferenceProductCard(product);
            productsContainer.getChildren().add(productCard);
        }
    }

    private void updateColumns(double viewportWidth) {
        if (viewportWidth <= 0) {
            productsContainer.setPrefColumns(1);
            return;
        }

        int calculated = (int) Math.floor((viewportWidth + TILE_HGAP) / (TILE_WIDTH + TILE_HGAP));
        int columns = viewportWidth >= ((TILE_WIDTH * 5) + (TILE_HGAP * 4)) ? 5 : Math.max(1, calculated);
        productsContainer.setPrefColumns(columns);
    }

    private VBox createReferenceProductCard(Product product) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setSpacing(7);
        card.setPrefWidth(TILE_WIDTH);
        card.setMinWidth(TILE_WIDTH);
        card.setMaxWidth(TILE_WIDTH);
        card.setPrefHeight(TILE_HEIGHT);
        card.setMinHeight(TILE_HEIGHT);
        card.setMaxHeight(TILE_HEIGHT);
        card.setAlignment(Pos.TOP_CENTER);

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label promoLabel = new Label(product.getId() % 5 == 0 ? "-15%" : "Хит");
        promoLabel.getStyleClass().addAll("product-promo-badge", promoStyleFor(product));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, javafx.scene.layout.Priority.ALWAYS);

        topRow.getChildren().addAll(promoLabel, topSpacer);

        ImageView imageView = new ImageView(ImageUtil.loadProductImage(product.getImagePath(), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT));
        imageView.getStyleClass().add("product-photo");
        imageView.setFitWidth(PRODUCT_IMAGE_WIDTH);
        imageView.setFitHeight(PRODUCT_IMAGE_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCursor(javafx.scene.Cursor.HAND);

        VBox imageWrap = new VBox(imageView);
        imageWrap.getStyleClass().add("product-image-frame");
        if (isLightProduct(product)) {
            imageWrap.getStyleClass().add("product-image-frame-contrast");
        }
        imageWrap.setStyle("-fx-alignment: CENTER;");

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setMaxWidth(218);

        Label brandLabel = new Label(product.getBrand() == null || product.getBrand().isBlank() ? "Brand" : product.getBrand());
        brandLabel.getStyleClass().add("product-brand-card");
        brandLabel.setMaxWidth(218);

        HBox chipsBox = new HBox(6);
        chipsBox.setAlignment(Pos.CENTER_LEFT);
        for (String size : visibleSizeChips(product)) {
            Label sizeLabel = new Label(size);
            sizeLabel.getStyleClass().add("product-chip");
            chipsBox.getChildren().add(sizeLabel);
        }

        HBox stockRow = new HBox(6);
        stockRow.setAlignment(Pos.CENTER_LEFT);
        Label stockDot = new Label("●");
        stockDot.getStyleClass().add(product.getQuantity() > 0 ? "stock-dot" : "stock-dot-empty");
        Label stockLabel = new Label(product.getQuantity() > 0 ? "В наличии" : "Нет в наличии");
        stockLabel.getStyleClass().add(product.getQuantity() > 0 ? "product-stock-text" : "product-stock-text-empty");
        stockRow.getChildren().addAll(stockDot, stockLabel);

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label(formatCardPrice(product.getPrice()));
        priceLabel.getStyleClass().add("label-price");
        priceRow.getChildren().add(priceLabel);
        if (product.getId() % 5 == 0) {
            Label oldPriceLabel = new Label(formatCardPrice(product.getPrice() * 1.15));
            oldPriceLabel.getStyleClass().add("label-old-price");
            priceRow.getChildren().add(oldPriceLabel);
        }

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setTranslateY(0);
        Button buyButton = new Button("🛒  В корзину");
        buyButton.getStyleClass().addAll("button-primary", "product-buy-button");
        buyButton.setCursor(javafx.scene.Cursor.HAND);
        buyButton.setOnAction(e -> buyProduct(product));
        buyButton.setPrefWidth(218);
        buttonBox.getChildren().add(buyButton);

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, javafx.scene.layout.Priority.ALWAYS);

        card.setOnMouseClicked(evt -> {
            Object target = evt.getTarget();
            if (target instanceof Node n) {
                while (n != null) {
                    if (n instanceof Button) return;
                    n = n.getParent();
                }
            }
            showProductDetails(product);
        });

        card.getChildren().addAll(topRow, imageWrap, nameLabel, brandLabel, chipsBox, stockRow, priceRow, bottomSpacer, buttonBox);
        return card;
    }

    private List<String> visibleSizeChips(Product product) {
        if (product.getSize() == null || product.getSize().isBlank()) {
            return List.of("S", "M", "L");
        }

        List<String> sizes = java.util.Arrays.stream(product.getSize().split("[,;/]\\s*|\\s+"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .limit(3)
                .toList();
        return sizes.isEmpty() ? List.of("S", "M", "L") : sizes;
    }

    private String formatCardPrice(double price) {
        return "$" + String.format("%.2f", price).replace('.', ',');
    }

    private String promoTextFor(Product product) {
        if (product.getId() % 5 == 0) {
            return "-15%";
        }
        if (product.getId() % 3 == 0) {
            return "Хит";
        }
        return "Новинка";
    }

    private String promoStyleFor(Product product) {
        if (product.getId() % 5 == 0) {
            return "product-promo-sale";
        }
        if (product.getId() % 3 == 0) {
            return "product-promo-hit";
        }
        return "product-promo-new";
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setSpacing(12);
        card.setPrefWidth(224);
        card.setMinWidth(224);
        card.setMaxWidth(224);
        card.setPrefHeight(430);
        card.setMinHeight(430);
        card.setMaxHeight(430);
        card.setAlignment(Pos.TOP_CENTER);

        HBox brandRow = new HBox(8);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = new Label(product.getBrand() == null || product.getBrand().isBlank() ? "Бренд" : product.getBrand());
        brandLabel.getStyleClass().add("brand-badge");

        Region brandSpacer = new Region();
        HBox.setHgrow(brandSpacer, javafx.scene.layout.Priority.ALWAYS);

        Label stockLabel = new Label(product.getQuantity() > 0 ? "В наличии" : "Нет в наличии");
        stockLabel.getStyleClass().add(product.getQuantity() > 0 ? "stock-badge" : "stock-badge-empty");

        brandRow.getChildren().addAll(brandLabel, brandSpacer, stockLabel);

        ImageView imageView = new ImageView(ImageUtil.loadProductImage(product.getImagePath(), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT));
        imageView.getStyleClass().add("product-photo");
        imageView.setFitWidth(PRODUCT_IMAGE_WIDTH);
        imageView.setFitHeight(PRODUCT_IMAGE_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCursor(javafx.scene.Cursor.HAND);

        VBox imageWrap = new VBox(imageView);
        imageWrap.getStyleClass().add("product-image-frame");
        if (isLightProduct(product)) {
            imageWrap.getStyleClass().add("product-image-frame-contrast");
        }
        imageWrap.setStyle("-fx-alignment: CENTER;");


        // Product Name
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setMaxWidth(190);

        HBox chipsBox = new HBox(7);
        chipsBox.setAlignment(Pos.CENTER_LEFT);

        Label colorLabel = new Label(shortText(product.getColor(), "Цвет"));
        colorLabel.getStyleClass().add("product-chip");

        Label sizeLabel = new Label(shortText(product.getSize(), "Размер"));
        sizeLabel.getStyleClass().add("product-chip");

        chipsBox.getChildren().addAll(colorLabel, sizeLabel);

        // Product ID and Price
        HBox infoBox = new HBox(12);
        infoBox.setStyle("-fx-alignment: CENTER_LEFT;");

        Label idLabel = new Label("Арт.: " + product.getId());
        idLabel.getStyleClass().add("product-meta");

        Label priceLabel = new Label("$" + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("label-price");

        infoBox.getChildren().addAll(idLabel, priceLabel);

        // Action Buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setTranslateY(-4);

        Button buyButton = new Button("В корзину");
        buyButton.getStyleClass().add("button-primary");
        buyButton.setCursor(javafx.scene.Cursor.HAND);
        buyButton.setOnAction(e -> buyProduct(product));

        buyButton.setPrefWidth(190);

        buttonBox.getChildren().add(buyButton);

        // make whole card clickable (open product detail)
        card.setOnMouseClicked(evt -> {
            // ignore clicks that originate from buttons (so buttons keep their actions)
            Object target = evt.getTarget();
            if (target instanceof Node n) {
                while (n != null) {
                    if (n instanceof Button) return;
                    n = n.getParent();
                }
            }
            showProductDetails(product);
        });

        // Hover effect

        card.getChildren().addAll(brandRow, imageWrap, nameLabel, chipsBox, infoBox, buttonBox);
        return card;
    }

    private boolean isLightProduct(Product product) {
        String color = product.getColor() == null ? "" : product.getColor().trim().toLowerCase();
        String name = product.getName() == null ? "" : product.getName().trim().toLowerCase();
        return containsAny(color + " " + name, "white", "бел", "light", "светл");
    }

    private boolean shouldShowCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return false;
        }
        String normalized = categoryName.trim().toLowerCase();
        return !normalized.contains("unisex")
                && !normalized.contains("inisex")
                && !normalized.contains("hoodie")
                && !normalized.contains("худи");
    }

    private String localizeCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return "";
        }

        String normalized = categoryName.trim().toLowerCase();
        if (normalized.equals("women") || normalized.equals("woman")) {
            return CATEGORY_WOMEN;
        }
        if (normalized.equals("men") || normalized.equals("man")) {
            return CATEGORY_MEN;
        }

        return categoryName;
    }

    private Integer getCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isBlank() || categoryName.startsWith("Все")) {
            return null;
        }
        return categoryIdsByName.get(categoryName.toLowerCase());
    }

    private boolean matchesSpecialCategory(Product product, String selectedCategory) {
        if (selectedCategory == null) {
            return false;
        }

        String category = selectedCategory.trim().toLowerCase();
        if (category.equals("women") || category.equals(CATEGORY_WOMEN.toLowerCase())) {
            return isWomenProduct(product);
        }
        if (category.equals("men") || category.equals(CATEGORY_MEN.toLowerCase())) {
            return !isWomenProduct(product);
        }
        return false;
    }

    private boolean isWomenProduct(Product product) {
        String text = (product.getName() + " "
                + product.getDescription() + " "
                + categoryNamesById.getOrDefault(product.getCategoryId(), "")).toLowerCase();

        return containsAny(text, "women", "жен", "плать", "dress", "skirt", "юбк");
    }

    private String shortText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String text = value.trim();
        return text.length() > 16 ? text.substring(0, 15) + "..." : text;
    }

    @FXML
    private void filterByPrice() {
        applyFilters(true);
    }

    private void applyFilters(boolean showSuccessAlert) {
        try {
            Integer minPrice = currentMinPrice();
            Integer maxPrice = currentMaxPrice();

            if (minPrice > maxPrice) {
                showAlert("Ошибка", "Минимальная цена не может быть больше максимальной!");
                return;
            }

            ProductFilter filter = new ProductFilter();
            filter.setMinPrice(minPrice.doubleValue());
            filter.setMaxPrice(maxPrice.doubleValue());

            String selectedBrand = brandCombo == null ? null : brandCombo.getValue();
            if (selectedBrand != null && !selectedBrand.equals(FILTER_ALL_BRANDS)) {
                filter.setBrand(selectedBrand);
            }

            String selectedColor = colorCombo.getValue();

            String selectedCategory = categoryCombo.getValue();
            Integer selectedCategoryId = getCategoryId(selectedCategory);
            if (selectedCategoryId != null) {
                filter.setCategoryId(selectedCategoryId);
            }

            java.util.List<Product> results = productDAO.search(filter);

            if (selectedCategory != null && !selectedCategory.startsWith("Все") && selectedCategoryId == null) {
                results = results.stream()
                        .filter(p -> matchesSpecialCategory(p, selectedCategory))
                        .toList();
            }

            if (selectedColor != null && !selectedColor.startsWith(FILTER_ANY)) {
                results = results.stream()
                        .filter(p -> matchesColor(p.getColor(), selectedColor))
                        .toList();
            }

            // Filter by size client-side (ProductFilter doesn't include size)
            String selectedSize = sizeCombo.getValue();
            if (selectedSize != null && !selectedSize.startsWith(FILTER_ANY)) {
                results = results.stream()
                        .filter(p -> productHasSize(p, selectedSize))
                        .toList();
            }

            String query = searchField == null ? "" : searchField.getText();
            if (query != null && !query.isBlank()) {
                String normalizedQuery = query.trim().toLowerCase();
                results = results.stream()
                        .filter(p -> matchesSearch(p, normalizedQuery))
                        .toList();
            }

            displayProducts(results);
            if (!showSuccessAlert) {
                return;
            }
            showAlert("Успешно", "Найдено товаров: " + results.size());
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage());
        }
    }

    @FXML
    private void resetFilter() {
        setMinPriceValue(PRICE_MIN, false);
        setMaxPriceValue(PRICE_MAX, false);
        updatePriceRangeSliderVisuals();
        if (categoryCombo != null) categoryCombo.getSelectionModel().selectFirst();
        if (brandCombo != null) brandCombo.getSelectionModel().selectFirst();
        if (colorCombo != null) colorCombo.getSelectionModel().selectFirst();
        if (sizeCombo != null) sizeCombo.getSelectionModel().selectFirst();
        if (searchField != null) searchField.clear();
        allProducts = productDAO.getAllProducts();
        populateBrandCombo();
        populateColorCombo();
        displayAllProducts();
        showAlert("Информация", "Фильтр сброшен. Показаны все товары.");
    }

    private boolean matchesSearch(Product product, String query) {
        String source = ((product.getName() == null ? "" : product.getName()) + " "
                + (product.getBrand() == null ? "" : product.getBrand()) + " "
                + (product.getColor() == null ? "" : product.getColor()) + " "
                + (product.getSize() == null ? "" : product.getSize())).toLowerCase();
        return source.contains(query);
    }

    private void buyProduct(Product product) {
        if (product.getQuantity() <= 0) {
            showAlert("Нет в наличии", "Этот товар сейчас недоступен для покупки.");
            return;
        }
        if (cartService.getQuantityForProduct(product.getId()) >= product.getQuantity()) {
            showAlert("Недостаточно товара", "В корзине уже максимальное доступное количество: " + product.getQuantity());
            return;
        }
        cartService.add(product, null, 1);
        updateCartCount();
        ToastHelper.showAddedToCart((Stage) productsContainer.getScene().getWindow(), product.getName());
    }

    private void showProductDetails(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/product-detail.fxml"));
            Scene scene = new Scene(loader.load());

            // apply CSS if available
            try {
                java.net.URL cssUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            } catch (Exception ignored) {}

            ProductDetailController ctrl = loader.getController();
            ctrl.setProduct(product);

            Stage stage = new Stage();
            HelloApplication.applyApplicationIcon(stage);
            stage.setTitle(product.getName());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Ошибка открытия страницы товара: " + e.getMessage());
            e.printStackTrace(System.err);
            showAlert("Ошибка", "Не удалось открыть страницу товара: " + e.getMessage());
        }
    }

    @FXML
    private void openCart() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/cart-view.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 820);
            scene.setFill(javafx.scene.paint.Color.WHITE);
            try {
                java.net.URL cssUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            } catch (Exception ignored) {}

            Stage stage = new Stage();
            HelloApplication.applyApplicationIcon(stage);
            stage.setTitle("Корзина");
            stage.setScene(scene);
            stage.setMinWidth(1260);
            stage.setMinHeight(720);
            stage.setOnHidden(event -> {
                allProducts = productDAO.getAllProducts();
                displayAllProducts();
                updateCartCount();
            });
            stage.show();
        } catch (Exception e) {
            System.err.println("Ошибка открытия корзины: " + e.getMessage());
            showAlert("Ошибка", "Не удалось открыть корзину: " + e.getMessage());
        }
    }

    @FXML
    private void openOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/orders-view.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 820);
            scene.setFill(javafx.scene.paint.Color.WHITE);
            try {
                java.net.URL cssUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css");
                if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            } catch (Exception ignored) {}

            Stage stage = new Stage();
            HelloApplication.applyApplicationIcon(stage);
            stage.setTitle("Мои заказы");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Ошибка открытия заказов: " + e.getMessage());
            showAlert("Ошибка", "Не удалось открыть мои заказы: " + e.getMessage());
        }
    }

    private void updateCartCount() {
        if (cartCountLabel == null || cartService == null) {
            return;
        }
        cartCountLabel.setText(String.valueOf(cartService.getItemCount()));
        boolean hasItems = cartService.getItemCount() > 0;
        cartCountLabel.setVisible(hasItems);
        cartCountLabel.setManaged(hasItems);
    }

    private boolean productHasSize(Product p, String size) {
        if (p.getSize() == null || p.getSize().isBlank()) return false;
        String s = p.getSize().toLowerCase();
        String wanted = size.toLowerCase();
        // sizes may be comma-separated or space-separated
        String[] parts = s.split("[,;/]\\\\s*|\\s+");
        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(wanted)) return true;
        }
        return false;
    }

    private boolean matchesColor(String productColor, String selectedColor) {
        if (productColor == null || productColor.isBlank() || selectedColor == null || selectedColor.isBlank()) {
            return false;
        }

        String color = productColor.trim().toLowerCase();
        String wanted = selectedColor.trim().toLowerCase();

        if (color.contains(wanted)) {
            return true;
        }

        return switch (wanted) {
            case "black", "черный", "чёрный" -> containsAny(color, "black", "черн", "чёрн");
            case "white", "белый" -> containsAny(color, "white", "бел");
            case "gray", "grey", "серый" -> containsAny(color, "gray", "grey", "сер");
            case "blue", "синий" -> containsAny(color, "blue", "син");
            case "red", "красный" -> containsAny(color, "red", "красн");
            default -> color.contains(wanted);
        };
    }

    private boolean containsAny(String source, String... tokens) {
        for (String token : tokens) {
            if (source.contains(token)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход");
        alert.setHeaderText(null);
        alert.setContentText("Вы уверены, что хотите выйти?");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Stage stage = (Stage) productsContainer.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
