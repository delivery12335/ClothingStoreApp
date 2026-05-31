package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.model.Product;
import com.clothingstore.clothingstoreapp.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ProductDetailController {

    private static final double DETAIL_IMAGE_WIDTH = 520;
    private static final double DETAIL_IMAGE_HEIGHT = 560;
    private static final double THUMBNAIL_SIZE = 96;

    @FXML
    private ImageView mainImageView;

    @FXML
    private HBox thumbnailsBox;

    @FXML
    private Label titleLabel;

    @FXML
    private Label brandLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private HBox colorBox;

    @FXML
    private HBox sizeBox;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button addToCartBtn;

    public void setProduct(Product product) {
        if (product == null) return;

        titleLabel.setText(product.getName());
        brandLabel.setText(product.getBrand());
        priceLabel.setText("$" + String.format("%.2f", product.getPrice()));
        descriptionArea.setText(product.getDescription() == null ? "" : product.getDescription());

        Image img = ImageUtil.loadProductImage(product.getImagePath(), DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_HEIGHT);
        mainImageView.setImage(img);
        mainImageView.setFitWidth(DETAIL_IMAGE_WIDTH);
        mainImageView.setFitHeight(DETAIL_IMAGE_HEIGHT);
        mainImageView.setPreserveRatio(false);
        mainImageView.setSmooth(true);

        thumbnailsBox.getChildren().clear();
        ImageView thumb1 = new ImageView(ImageUtil.loadProductImage(product.getImagePath(), THUMBNAIL_SIZE, THUMBNAIL_SIZE));
        thumb1.setFitWidth(THUMBNAIL_SIZE);
        thumb1.setFitHeight(THUMBNAIL_SIZE);
        thumb1.setPreserveRatio(false);
        thumb1.setSmooth(true);

        ImageView thumb2 = new ImageView(ImageUtil.loadProductImage(product.getImagePath(), THUMBNAIL_SIZE, THUMBNAIL_SIZE));
        thumb2.setFitWidth(THUMBNAIL_SIZE);
        thumb2.setFitHeight(THUMBNAIL_SIZE);
        thumb2.setPreserveRatio(false);
        thumb2.setSmooth(true);

        thumbnailsBox.getChildren().addAll(thumb1, thumb2);

        colorBox.getChildren().clear();
        if (product.getColor() != null) {
            Button c = new Button(product.getColor());
            c.setDisable(true);
            colorBox.getChildren().add(c);
        }

        sizeBox.getChildren().clear();
        if (product.getSize() != null) {
            ToggleButton tb = new ToggleButton(product.getSize());
            ToggleGroup tg = new ToggleGroup();
            tb.setToggleGroup(tg);
            sizeBox.getChildren().add(tb);
        }

        addToCartBtn.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Товар добавлен в корзину: " + product.getName(), ButtonType.OK);
            a.showAndWait();
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}

