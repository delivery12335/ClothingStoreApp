package com.clothingstore.clothingstoreapp.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class ToastHelper {

    private static final Duration SLIDE_DURATION = Duration.millis(220);
    private static final Duration VISIBLE_DURATION = Duration.seconds(2.1);
    private static final Duration FADE_DURATION = Duration.millis(420);

    private ToastHelper() {
    }

    public static void showAddedToCart(Stage owner, String productName) {
        show(owner, "Товар добавлен в корзину", productName);
    }

    public static void show(Stage owner, String title, String message) {
        if (owner == null || owner.getScene() == null) {
            return;
        }

        Label icon = new Label("✓");
        icon.getStyleClass().add("toast-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("toast-title");

        Label messageLabel = new Label(message == null ? "" : message);
        messageLabel.getStyleClass().add("toast-message");
        messageLabel.setWrapText(true);

        VBox copy = new VBox(2, titleLabel, messageLabel);
        copy.setAlignment(Pos.CENTER_LEFT);

        HBox toast = new HBox(12, icon, copy);
        toast.getStyleClass().add("cart-toast");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(14, 18, 14, 16));
        toast.setMinWidth(320);
        toast.setMaxWidth(430);
        toast.setOpacity(0);
        toast.setTranslateY(42);

        Scene ownerScene = owner.getScene();
        toast.getStylesheets().addAll(ownerScene.getStylesheets());

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(false);
        popup.getContent().add(toast);
        popup.show(owner);

        toast.applyCss();
        toast.layout();

        double toastWidth = Math.max(toast.prefWidth(-1), toast.getLayoutBounds().getWidth());
        double toastHeight = Math.max(toast.prefHeight(toastWidth), toast.getLayoutBounds().getHeight());
        double x = owner.getX() + (owner.getWidth() - toastWidth) / 2.0;
        double y = owner.getY() + owner.getHeight() - toastHeight - 34;
        popup.setX(Math.max(owner.getX() + 16, x));
        popup.setY(Math.max(owner.getY() + 16, y));

        FadeTransition fadeIn = new FadeTransition(SLIDE_DURATION, toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(SLIDE_DURATION, toast);
        slideIn.setFromY(42);
        slideIn.setToY(0);

        PauseTransition pause = new PauseTransition(VISIBLE_DURATION);

        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(FADE_DURATION, toast);
        slideOut.setFromY(0);
        slideOut.setToY(18);

        SequentialTransition animation = new SequentialTransition(
                new ParallelTransition(fadeIn, slideIn),
                pause,
                new ParallelTransition(fadeOut, slideOut)
        );
        animation.setOnFinished(event -> popup.hide());
        animation.play();
    }
}
