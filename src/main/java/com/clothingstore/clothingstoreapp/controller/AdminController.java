package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.HelloApplication;
import com.clothingstore.clothingstoreapp.dao.ProductDAO;
import com.clothingstore.clothingstoreapp.dao.OrderDAO;
import com.clothingstore.clothingstoreapp.model.*;
import com.clothingstore.clothingstoreapp.service.ExportService;
import com.clothingstore.clothingstoreapp.service.ReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AdminController {

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ReportService reportService = new ReportService();
    private final ExportService exportService = new ExportService();
    private List<ReportRow> lastReportRows = new ArrayList<>();
    private Window activeActionOwner;

    @FXML
    private StackPane adminRoot;

    @FXML
    private ImageView adminBackground;

    @FXML private Label totalOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    @FXML private Label processingOrdersLabel;
    @FXML private Label cancelledOrdersLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label popularBrandLabel;
    @FXML private Label orderStatusCenterLabel;
    @FXML private Label deliveredLegendLabel;
    @FXML private Label processingLegendLabel;
    @FXML private Label shippedLegendLabel;
    @FXML private Label cancelledLegendLabel;
    @FXML private PieChart orderStatusChart;
    @FXML private BarChart<String, Number> brandPopularityChart;

    @FXML
    private void initialize() {
        configureAdminBackground(adminBackground, adminRoot);
        populateDashboard();
    }

    private void populateDashboard() {
        List<Order> orders = orderDAO.findAll();
        List<Product> products = productDAO.findAll();
        Map<OrderStatus, Integer> statusCounts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status, 0);
        }

        double revenue = 0;
        for (Order order : orders) {
            OrderStatus status = order.getStatus() == null ? OrderStatus.PROCESSING : order.getStatus();
            statusCounts.put(status, statusCounts.get(status) + 1);
            if (status == OrderStatus.DELIVERED || status == OrderStatus.SHIPPED) {
                revenue += order.getTotalAmount();
            }
        }

        setLabel(totalOrdersLabel, String.valueOf(orders.size()));
        setLabel(deliveredOrdersLabel, String.valueOf(statusCounts.get(OrderStatus.DELIVERED)));
        setLabel(processingOrdersLabel, String.valueOf(statusCounts.get(OrderStatus.PROCESSING)));
        setLabel(cancelledOrdersLabel, String.valueOf(statusCounts.get(OrderStatus.CANCELED)));
        setLabel(totalRevenueLabel, "$" + String.format(Locale.US, "%.2f", revenue));
        setLabel(orderStatusCenterLabel, String.valueOf(orders.size()));
        setLabel(deliveredLegendLabel, formatStatusLegend(statusCounts.get(OrderStatus.DELIVERED), orders.size()));
        setLabel(processingLegendLabel, formatStatusLegend(statusCounts.get(OrderStatus.PROCESSING), orders.size()));
        setLabel(shippedLegendLabel, formatStatusLegend(statusCounts.get(OrderStatus.SHIPPED), orders.size()));
        setLabel(cancelledLegendLabel, formatStatusLegend(statusCounts.get(OrderStatus.CANCELED), orders.size()));

        Map<String, Integer> brandCounts = new HashMap<>();
        for (Product product : products) {
            String brand = product.getBrand() == null || product.getBrand().isBlank() ? "No brand" : product.getBrand().trim();
            brandCounts.put(brand, brandCounts.getOrDefault(brand, 0) + 1);
        }
        String popularBrand = brandCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
        setLabel(popularBrandLabel, popularBrand);

        if (orderStatusChart != null) {
            orderStatusChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Delivered", statusCounts.get(OrderStatus.DELIVERED)),
                    new PieChart.Data("Processing", statusCounts.get(OrderStatus.PROCESSING)),
                    new PieChart.Data("Shipped", statusCounts.get(OrderStatus.SHIPPED)),
                    new PieChart.Data("Cancelled", statusCounts.get(OrderStatus.CANCELED))
            ));
        }

        if (brandPopularityChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            brandCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                    .limit(8)
                    .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
            brandPopularityChart.getData().setAll(series);
        }
    }

    private void setLabel(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private String formatStatusLegend(int count, int total) {
        int percent = total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
        return count + " (" + percent + "%)";
    }

    @FXML
    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1380, 760);
            scene.setFill(javafx.scene.paint.Color.WHITE);
            java.net.URL cssUrl = HelloApplication.class.getResource("styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            HelloApplication.applyApplicationIcon(stage);
            stage.setTitle("Dashboard");
            stage.setScene(scene);
            stage.setMinWidth(1380);
            stage.setMinHeight(680);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            showError("Ошибка", "Не удалось открыть Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void closeCurrentWindow() {
        if (adminRoot != null && adminRoot.getScene() != null) {
            ((Stage) adminRoot.getScene().getWindow()).close();
        }
    }

    @FXML
    private void openProducts() {
        if (sectionWindowsEnabled()) {
            openActionWindow(
                    "Управление товарами",
                    "Добавляйте, редактируйте, удаляйте и находите товары в каталоге.",
                    List.of(
                            new AdminAction("Просмотреть товары", "Полный список товаров из базы данных", () -> showProducts(productDAO.findAll(), "Список товаров")),
                            new AdminAction("Добавить товар", "Создание новой позиции каталога", this::createProduct),
                            new AdminAction("Редактировать товар", "Изменение товара по его ID", this::updateProduct),
                            new AdminAction("Удалить товар", "Удаление товара из каталога по ID", this::deleteProduct),
                            new AdminAction("Поиск и фильтр", "Фильтрация по названию, бренду, цвету, категории и цене", this::searchProducts)
                    )
            );
            return;
        }
        List<String> actions = List.of(
                "Read: Просмотр товаров",
                "Create: Добавить товар",
                "Update: Редактировать товар",
                "Delete: Удалить товар",
                "Search/Filter: Поиск по критериям"
        );

        ChoiceDialog<String> dialog = new ChoiceDialog<>(actions.get(0), actions);
        dialog.setTitle("Управление товарами");
        dialog.setHeaderText("Выберите действие CRUD");
        styleDialog(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        switch (result.get()) {
            case "Read: Просмотр товаров" -> showProducts(productDAO.findAll(), "Список товаров");
            case "Create: Добавить товар" -> createProduct();
            case "Update: Редактировать товар" -> updateProduct();
            case "Delete: Удалить товар" -> deleteProduct();
            case "Search/Filter: Поиск по критериям" -> searchProducts();
            default -> showInfo("Инфо", "Действие не выбрано");
        }
    }

    @FXML
    private void openOrders() {
        if (sectionWindowsEnabled()) {
            openActionWindow(
                    "Управление заказами",
                    "Работайте с заказами клиентов, статусами и поиском.",
                    List.of(
                            new AdminAction("Просмотреть заказы", "Полный список заказов клиентов", () -> showOrders(orderDAO.findAll(), "Список заказов")),
                            new AdminAction("Добавить заказ", "Создание нового заказа вручную", this::createOrder),
                            new AdminAction("Редактировать заказ", "Изменение заказа по его ID", this::updateOrder),
                            new AdminAction("Удалить заказ", "Удаление заказа по ID", this::deleteOrder),
                            new AdminAction("Поиск заказов", "Фильтрация по клиенту, датам, статусу и сумме", this::searchOrders)
                    )
            );
            return;
        }
        List<String> actions = List.of(
                "Read: Просмотр заказов",
                "Create: Добавить заказ",
                "Update: Редактировать заказ",
                "Delete: Удалить заказ",
                "Search/Filter: Поиск заказов"
        );

        ChoiceDialog<String> dialog = new ChoiceDialog<>(actions.get(0), actions);
        dialog.setTitle("Управление заказами");
        dialog.setHeaderText("Выберите действие CRUD");
        styleDialog(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        switch (result.get()) {
            case "Read: Просмотр заказов" -> showOrders(orderDAO.findAll(), "Список заказов");
            case "Create: Добавить заказ" -> createOrder();
            case "Update: Редактировать заказ" -> updateOrder();
            case "Delete: Удалить заказ" -> deleteOrder();
            case "Search/Filter: Поиск заказов" -> searchOrders();
            default -> showInfo("Инфо", "Действие не выбрано");
        }
    }

    @FXML
    private void openReports() {
        if (sectionWindowsEnabled()) {
            openActionWindow(
                    "Отчеты и аналитика",
                    "Сформируйте отчет, а затем при необходимости экспортируйте его.",
                    List.of(
                            new AdminAction("Сформировать отчет", "Выбор типа отчета и просмотр результата", this::showReportDialog),
                            new AdminAction("Экспорт последнего отчета", "Сохранение последнего сформированного отчета в файл", this::exportLastReportData)
                    )
            );
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(ReportType.CATEGORY_SUMMARY, ReportType.values());
        dialog.setTitle("Отчеты");
        dialog.setHeaderText("Выберите отчет");
        styleDialog(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String selected = result.get();
        lastReportRows = reportService.generateReport(selected);

        if (lastReportRows.isEmpty()) {
            showError("Отчет пуст", "Для выбранного отчета нет данных.");
            return;
        }

        StringBuilder reportText = new StringBuilder("Тип отчета: " + ReportType.displayName(selected) + "\n\n");
        for (ReportRow row : lastReportRows) {
            reportText.append(row.toTxtLine()).append("\n");
        }
        showText("📊 Отчет", reportText.toString());
    }

    @FXML
    private void exportData() {
        if (sectionWindowsEnabled()) {
            openActionWindow(
                    "Экспорт данных",
                    "Сохраняйте товары или последний отчет в CSV и TXT.",
                    List.of(
                            new AdminAction("Экспорт товаров", "Выгрузка текущего каталога товаров", this::exportProductsData),
                            new AdminAction("Экспорт последнего отчета", "Выгрузка отчета, который был сформирован в разделе аналитики", this::exportLastReportData)
                    )
            );
            return;
        }
        List<String> exportOptions = List.of("Экспорт товаров", "Экспорт последнего отчета");
        ChoiceDialog<String> targetDialog = new ChoiceDialog<>(exportOptions.get(0), exportOptions);
        targetDialog.setTitle("Экспорт");
        targetDialog.setHeaderText("Что экспортировать?");
        styleDialog(targetDialog.getDialogPane());

        Optional<String> target = targetDialog.showAndWait();
        if (target.isEmpty()) {
            return;
        }

        ChoiceDialog<String> formatDialog = new ChoiceDialog<>(ExportFormat.CSV, ExportFormat.values());
        formatDialog.setTitle("Экспорт");
        formatDialog.setHeaderText("Выберите формат");
        styleDialog(formatDialog.getDialogPane());

        Optional<String> formatResult = formatDialog.showAndWait();
        if (formatResult.isEmpty()) {
            return;
        }

        String format = formatResult.get();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить файл");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                ExportFormat.CSV.equals(format) ? "CSV (*.csv)" : "TXT (*.txt)",
                ExportFormat.CSV.equals(format) ? "*.csv" : "*.txt"
        ));

        File file = chooser.showSaveDialog(null);
        if (file == null) {
            return;
        }

        try {
            Path path = file.toPath();
            if ("Экспорт товаров".equals(target.get())) {
                exportService.exportProducts(productDAO.findAll(), format, path);
            } else {
                if (lastReportRows.isEmpty()) {
                    showError("Нет отчета", "Сначала сформируйте отчет через кнопку 'Показать отчеты'.");
                    return;
                }
                exportService.exportRows(lastReportRows, format, path);
            }
            showInfo("Экспорт", "Данные успешно экспортированы: " + file.getName());
        } catch (IOException e) {
            showError("Ошибка экспорта", e.getMessage());
        }
    }

    private boolean sectionWindowsEnabled() {
        return true;
    }

    private ImageView createAdminBackground() {
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setOpacity(1.0);
        imageView.setEffect(new GaussianBlur(16));
        return imageView;
    }

    private void configureAdminBackground(ImageView imageView, Region owner) {
        if (imageView == null || owner == null) {
            return;
        }

        String imageUrl = HelloApplication.class.getResource("/images/1.jpg").toExternalForm();
        imageView.setImage(new Image(imageUrl));
        imageView.fitWidthProperty().bind(owner.widthProperty());
        imageView.fitHeightProperty().bind(owner.heightProperty());
    }

    private void openActionWindow(String title, String subtitle, List<AdminAction> actions) {
        Stage window = new Stage();
        HelloApplication.applyApplicationIcon(window);

        StackPane root = new StackPane();
        root.getStyleClass().add("admin-background-root");

        ImageView background = createAdminBackground();
        configureAdminBackground(background, root);

        VBox content = new VBox();
        content.getStyleClass().add("admin-section-shell");

        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("admin-section-topbar");
        topbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("admin-section-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("admin-section-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("←");
        backButton.getStyleClass().add("admin-section-back");
        backButton.setPrefSize(48, 42);
        backButton.setOnAction(event -> window.close());

        topbar.getChildren().addAll(titleBox, spacer, backButton);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("admin-section-grid");
        grid.setHgap(18);
        grid.setVgap(18);
        VBox.setVgrow(grid, Priority.ALWAYS);

        for (int i = 0; i < actions.size(); i++) {
            AdminAction action = actions.get(i);
            VBox card = createActionCard(i + 1, action);
            grid.add(card, i % 2, i / 2);
        }

        content.getChildren().addAll(topbar, grid);
        root.getChildren().addAll(background, content);

        Scene scene = new Scene(root, 1150, 720);
        String css = HelloApplication.class.getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        window.setTitle("Clothing Store - " + title);
        window.setScene(scene);
        window.setResizable(true);
        window.setMaximized(true);
        window.show();
    }

    private VBox createActionCard(int index, AdminAction action) {
        VBox card = new VBox(10);
        card.getStyleClass().add("admin-section-card");

        Label indexLabel = new Label(String.format("%02d", index));
        indexLabel.getStyleClass().add("admin-card-index");

        Label titleLabel = new Label(action.title());
        titleLabel.getStyleClass().add("admin-card-title");

        Label descriptionLabel = new Label(action.description());
        descriptionLabel.getStyleClass().add("admin-card-text");
        descriptionLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button button = new Button(action.title());
        button.getStyleClass().add("admin-section-action");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(44);
        button.setOnAction(event -> {
            activeActionOwner = ((Node) event.getSource()).getScene().getWindow();
            try {
                action.handler().run();
            } finally {
                activeActionOwner = null;
            }
        });

        card.getChildren().addAll(indexLabel, titleLabel, descriptionLabel, spacer, button);
        return card;
    }

    private void showReportDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(ReportType.CATEGORY_SUMMARY, ReportType.values());
        dialog.setTitle("Отчеты");
        dialog.setHeaderText("Выберите отчет");
        styleDialog(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String selected = result.get();
        lastReportRows = reportService.generateReport(selected);

        if (lastReportRows.isEmpty()) {
            showError("Отчет пуст", "Для выбранного отчета нет данных.");
            return;
        }

        StringBuilder reportText = new StringBuilder("Тип отчета: " + ReportType.displayName(selected) + "\n\n");
        for (ReportRow row : lastReportRows) {
            reportText.append(row.toTxtLine()).append("\n");
        }
        showText("Отчет", reportText.toString());
    }

    private void exportProductsData() {
        exportSelectedData("Экспорт товаров");
    }

    private void exportLastReportData() {
        exportSelectedData("Экспорт последнего отчета");
    }

    private void exportSelectedData(String target) {
        ChoiceDialog<String> formatDialog = new ChoiceDialog<>(ExportFormat.CSV, ExportFormat.values());
        formatDialog.setTitle("Экспорт");
        formatDialog.setHeaderText("Выберите формат");
        styleDialog(formatDialog.getDialogPane());

        Optional<String> formatResult = formatDialog.showAndWait();
        if (formatResult.isEmpty()) {
            return;
        }

        String format = formatResult.get();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить файл");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                ExportFormat.CSV.equals(format) ? "CSV (*.csv)" : "TXT (*.txt)",
                ExportFormat.CSV.equals(format) ? "*.csv" : "*.txt"
        ));

        File file = chooser.showSaveDialog(adminRoot.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Path path = file.toPath();
            if ("Экспорт товаров".equals(target)) {
                exportService.exportProducts(productDAO.findAll(), format, path);
            } else {
                if (lastReportRows.isEmpty()) {
                    showError("Нет отчета", "Сначала сформируйте отчет через раздел аналитики.");
                    return;
                }
                exportService.exportRows(lastReportRows, format, path);
            }
            showInfo("Экспорт", "Данные успешно экспортированы: " + file.getName());
        } catch (IOException e) {
            showError("Ошибка экспорта", e.getMessage());
        }
    }

    private record AdminAction(String title, String description, Runnable handler) {
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход");
        alert.setHeaderText("Выйти из админ-панели?");
        alert.setContentText("После выхода откроется экран авторизации.");
        styleDialog(alert.getDialogPane());

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                openLoginScene();
            }
        });
    }

    private void openLoginScene() {
        try {
            Stage stage = (Stage) adminRoot.getScene().getWindow();
            double width = stage.getScene().getWidth();
            double height = stage.getScene().getHeight();

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), width, height);
            String css = HelloApplication.class.getResource("styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setTitle("Clothing Store - Login");
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            showError("Ошибка выхода", "Не удалось открыть экран входа: " + e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert.getDialogPane());
        alert.showAndWait();
    }

    private void showText(String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(860, 500);
        area.getStyleClass().add("admin-data-text");

        showDataWindow(title, "Просмотр данных", area);
    }

    private void styleDialog(DialogPane pane) {
        String css = HelloApplication.class.getResource("styles.css").toExternalForm();
        if (!pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }
        if (!pane.getStyleClass().contains("admin-dialog-pane")) {
            pane.getStyleClass().add("admin-dialog-pane");
        }
        pane.setPrefWidth(760);

        for (ButtonType buttonType : pane.getButtonTypes()) {
            Button button = (Button) pane.lookupButton(buttonType);
            if (button == null) {
                continue;
            }
            button.getStyleClass().removeAll("admin-dialog-primary-button", "admin-dialog-cancel-button");
            if (buttonType.getButtonData().isCancelButton()) {
                button.getStyleClass().add("admin-dialog-cancel-button");
            } else {
                button.getStyleClass().add("admin-dialog-primary-button");
            }
        }
    }

    private void configureAdminDialogGrid(GridPane grid) {
        grid.getColumnConstraints().clear();

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(205);
        labelColumn.setPrefWidth(205);

        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);
        valueColumn.setFillWidth(true);

        grid.getColumnConstraints().addAll(labelColumn, valueColumn);

        for (Node node : grid.getChildren()) {
            Integer columnIndex = GridPane.getColumnIndex(node);
            int column = columnIndex == null ? 0 : columnIndex;

            if (column == 0 && node instanceof Label label) {
                label.setMinWidth(205);
                label.setPrefWidth(205);
                label.setMaxWidth(205);
            } else if (node instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                GridPane.setHgrow(region, Priority.ALWAYS);
            }
        }
    }

    private void showDataWindow(String title, String subtitle, Node contentNode) {
        Stage window = new Stage();
        HelloApplication.applyApplicationIcon(window);

        StackPane root = new StackPane();
        root.getStyleClass().add("admin-background-root");

        ImageView background = createAdminBackground();
        configureAdminBackground(background, root);

        VBox shell = new VBox();
        shell.getStyleClass().add("admin-data-shell");

        HBox topbar = new HBox(16);
        topbar.getStyleClass().addAll("admin-section-topbar", "admin-data-topbar");
        topbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label logo = new Label("CS");
        logo.getStyleClass().add("admin-logo");

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("admin-section-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("admin-section-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("←");
        closeButton.getStyleClass().add("admin-section-back");
        closeButton.setPrefSize(48, 42);
        closeButton.setOnAction(event -> window.close());

        topbar.getChildren().addAll(logo, titleBox, spacer, closeButton);

        StackPane panel = new StackPane(contentNode);
        panel.getStyleClass().add("admin-data-panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        if (contentNode instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        VBox content = new VBox(panel);
        content.getStyleClass().add("admin-data-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        shell.getChildren().addAll(topbar, content);
        root.getChildren().addAll(background, shell);

        Scene scene = new Scene(root, 1060, 660);
        String css = HelloApplication.class.getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        Window owner = activeActionOwner;
        if (owner == null && adminRoot != null && adminRoot.getScene() != null) {
            owner = adminRoot.getScene().getWindow();
        }
        if (owner != null) {
            window.initOwner(owner);
            window.initModality(Modality.WINDOW_MODAL);
        }
        window.setTitle("Clothing Store - " + title);
        window.setScene(scene);
        window.setResizable(true);
        window.show();
        window.toFront();
        window.requestFocus();
    }

    private <T> TableColumn<T, String> textColumn(String title, double width, Function<T, String> valueFactory) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new SimpleStringProperty(displayValue(valueFactory.apply(data.getValue()))));
        return column;
    }

    private String displayValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void chooseProductImage(TextField imagePathField) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите изображение товара");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Изображения (*.png, *.jpg, *.jpeg, *.gif)",
                "*.png", "*.jpg", "*.jpeg", "*.gif"
        ));

        File file = chooser.showOpenDialog(adminRoot.getScene().getWindow());
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
        }
    }

    private void showProducts(List<Product> products, String title) {
        if (products.isEmpty()) {
            showInfo(title, "Список пуст");
            return;
        }

        TableView<Product> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        List<TableColumn<Product, ?>> columns = List.of(
                textColumn("ID", 70, (Product product) -> String.valueOf(product.getId())),
                textColumn("Название", 220, (Product product) -> product.getName()),
                textColumn("Бренд", 130, (Product product) -> product.getBrand()),
                textColumn("Цвет", 110, (Product product) -> product.getColor()),
                textColumn("Размер", 90, (Product product) -> product.getSize()),
                textColumn("Цена", 100, (Product product) -> String.format("%.2f", product.getPrice())),
                textColumn("Остаток", 100, (Product product) -> String.valueOf(product.getQuantity())),
                textColumn("Описание", 260, (Product product) -> product.getDescription())
        );
        table.getColumns().setAll(columns);
        table.getItems().setAll(products);

        showDataWindow(title, "Товаров в списке: " + products.size(), table);
    }

    private void createProduct() {
        Optional<Product> product = showProductDialog(null);
        if (product.isEmpty()) {
            return;
        }
        Product created = productDAO.create(product.get());
        if (created != null) {
            showInfo("Create", "Товар добавлен. ID=" + created.getId());
        } else {
            showError("Create", "Не удалось добавить товар.");
        }
    }

    private void updateProduct() {
        Integer id = askInt("Update", "Введите ID товара для редактирования:");
        if (id == null) {
            return;
        }

        Optional<Product> existing = productDAO.findById(id);
        if (existing.isEmpty()) {
            showError("Update", "Товар с таким ID не найден.");
            return;
        }

        Optional<Product> edited = showProductDialog(existing.get());
        if (edited.isEmpty()) {
            return;
        }

        Product product = edited.get();
        product.setId(id);
        if (productDAO.update(product)) {
            showInfo("Update", "Товар обновлен.");
        } else {
            showError("Update", "Не удалось обновить товар.");
        }
    }

    private void deleteProduct() {
        Integer id = askInt("Delete", "Введите ID товара для удаления:");
        if (id == null) {
            return;
        }

        if (productDAO.deleteById(id)) {
            showInfo("Delete", "Товар удален.");
        } else {
            showError("Delete", "Товар с таким ID не найден или не удален.");
        }
    }

    private void searchProducts() {
        Dialog<ProductFilter> dialog = new Dialog<>();
        dialog.setTitle("Search/Filter");
        dialog.setHeaderText("Фильтр товаров");

        ButtonType searchButton = new ButtonType("Искать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("admin-dialog-form");

        TextField nameField = new TextField();
        TextField brandField = new TextField();
        TextField colorField = new TextField();
        TextField categoryField = new TextField();
        TextField minPriceField = new TextField();
        TextField maxPriceField = new TextField();

        grid.addRow(0, new Label("Название:"), nameField);
        grid.addRow(1, new Label("Бренд:"), brandField);
        grid.addRow(2, new Label("Цвет:"), colorField);
        grid.addRow(3, new Label("Категория ID:"), categoryField);
        grid.addRow(4, new Label("Мин. цена:"), minPriceField);
        grid.addRow(5, new Label("Макс. цена:"), maxPriceField);

        configureAdminDialogGrid(grid);
        dialog.getDialogPane().setContent(grid);
        styleDialog(dialog.getDialogPane());
        dialog.setResultConverter(btn -> {
            if (btn == searchButton) {
                ProductFilter filter = new ProductFilter();
                filter.setNameContains(nameField.getText());
                filter.setBrand(brandField.getText());
                filter.setColor(colorField.getText());
                filter.setCategoryId(parseInteger(categoryField.getText()));
                filter.setMinPrice(parseDouble(minPriceField.getText()));
                filter.setMaxPrice(parseDouble(maxPriceField.getText()));
                return filter;
            }
            return null;
        });

        Optional<ProductFilter> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        List<Product> found = productDAO.search(result.get());
        showProducts(found, "Результаты поиска: " + found.size());
    }

    private Optional<Product> showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Create" : "Update");
        dialog.setHeaderText(product == null ? "Новый товар" : "Редактирование товара");

        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("admin-dialog-form");

        TextField nameField = new TextField(product == null ? "" : product.getName());
        TextField categoryIdField = new TextField(product == null ? "1" : String.valueOf(product.getCategoryId()));
        TextField brandField = new TextField(product == null ? "" : product.getBrand());
        TextField sizeField = new TextField(product == null ? "M" : product.getSize());
        TextField colorField = new TextField(product == null ? "" : product.getColor());
        TextField priceField = new TextField(product == null ? "0" : String.valueOf(product.getPrice()));
        TextField qtyField = new TextField(product == null ? "0" : String.valueOf(product.getQuantity()));
        TextField imagePathField = new TextField(product == null ? "" : product.getImagePath());
        Button chooseImageButton = new Button("Выбрать картинку");
        chooseImageButton.getStyleClass().add("admin-dialog-secondary-button");
        chooseImageButton.setOnAction(event -> chooseProductImage(imagePathField));
        HBox imageRow = new HBox(10, imagePathField, chooseImageButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);
        TextArea descriptionArea = new TextArea(product == null ? "" : product.getDescription());
        descriptionArea.setPrefRowCount(2);

        grid.addRow(0, new Label("Название:"), nameField);
        grid.addRow(1, new Label("CategoryID:"), categoryIdField);
        grid.addRow(2, new Label("Бренд:"), brandField);
        grid.addRow(3, new Label("Размер:"), sizeField);
        grid.addRow(4, new Label("Цвет:"), colorField);
        grid.addRow(5, new Label("Цена:"), priceField);
        grid.addRow(6, new Label("Остаток:"), qtyField);
        grid.addRow(7, new Label("ImagePath:"), imageRow);
        grid.addRow(8, new Label("Описание:"), descriptionArea);

        configureAdminDialogGrid(grid);
        dialog.getDialogPane().setContent(grid);
        styleDialog(dialog.getDialogPane());
        Node saveNode = dialog.getDialogPane().lookupButton(saveButton);
        saveNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            List<String> errors = validateProductFields(
                    nameField,
                    categoryIdField,
                    brandField,
                    sizeField,
                    colorField,
                    priceField,
                    qtyField
            );
            if (!errors.isEmpty()) {
                event.consume();
                showError("Проверьте данные", String.join("\n", errors));
            }
        });
        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Integer categoryId = parseInteger(categoryIdField.getText());
                Double price = parseDouble(priceField.getText());
                Integer quantity = parseInteger(qtyField.getText());
                return new Product(
                        product == null ? 0 : product.getId(),
                        nameField.getText().trim(),
                        categoryId,
                        brandField.getText().trim(),
                        sizeField.getText().trim(),
                        colorField.getText().trim(),
                        price,
                        quantity,
                        imagePathField.getText() == null ? "" : imagePathField.getText().trim(),
                        descriptionArea.getText() == null ? "" : descriptionArea.getText().trim()
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private List<String> validateProductFields(
            TextField nameField,
            TextField categoryIdField,
            TextField brandField,
            TextField sizeField,
            TextField colorField,
            TextField priceField,
            TextField qtyField
    ) {
        List<String> errors = new ArrayList<>();

        if (isBlank(nameField.getText())) {
            errors.add("Название обязательно.");
        }
        if (isBlank(brandField.getText())) {
            errors.add("Бренд обязателен.");
        }
        if (isBlank(sizeField.getText())) {
            errors.add("Размер обязателен.");
        }
        if (isBlank(colorField.getText())) {
            errors.add("Цвет обязателен.");
        }

        Integer categoryId = parseInteger(categoryIdField.getText());
        if (categoryId == null || categoryId <= 0) {
            errors.add("CategoryID должен быть положительным числом.");
        }

        Double price = parseDouble(priceField.getText());
        if (price == null || price <= 0) {
            errors.add("Цена должна быть больше 0.");
        }

        Integer quantity = parseInteger(qtyField.getText());
        if (quantity == null || quantity < 0) {
            errors.add("Остаток должен быть 0 или больше.");
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void showOrders(List<Order> orders, String title) {
        if (orders.isEmpty()) {
            showInfo(title, "Список пуст");
            return;
        }

        TableView<Order> table = new TableView<>();
        table.getStyleClass().add("admin-data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        List<TableColumn<Order, ?>> columns = List.of(
                textColumn("ID", 80, (Order order) -> String.valueOf(order.getId())),
                textColumn("UserID", 120, (Order order) -> String.valueOf(order.getUserId())),
                textColumn("Дата", 180, (Order order) -> order.getOrderDate()),
                textColumn("Сумма", 150, (Order order) -> String.format("%.2f", order.getTotalAmount())),
                textColumn("Статус", 200, (Order order) -> order.getStatus().getDbValue())
        );
        table.getColumns().setAll(columns);
        table.getItems().setAll(orders);

        showDataWindow(title, "Заказов в списке: " + orders.size(), table);
    }

    private void createOrder() {
        Optional<Order> order = showOrderDialog(null);
        if (order.isEmpty()) {
            return;
        }
        Order created = orderDAO.create(order.get());
        if (created != null) {
            showInfo("Create", "Заказ добавлен. ID=" + created.getId());
        } else {
            showError("Create", "Не удалось добавить заказ.");
        }
    }

    private void updateOrder() {
        Integer id = askInt("Update", "Введите ID заказа для редактирования:");
        if (id == null) {
            return;
        }

        Optional<Order> existing = orderDAO.findById(id);
        if (existing.isEmpty()) {
            showError("Update", "Заказ с таким ID не найден.");
            return;
        }

        Optional<Order> edited = showOrderDialog(existing.get());
        if (edited.isEmpty()) {
            return;
        }

        Order order = edited.get();
        order.setId(id);
        if (orderDAO.update(order)) {
            showInfo("Update", "Заказ обновлен.");
        } else {
            showError("Update", "Не удалось обновить заказ.");
        }
    }

    private void deleteOrder() {
        Integer id = askInt("Delete", "Введите ID заказа для удаления:");
        if (id == null) {
            return;
        }

        if (orderDAO.deleteById(id)) {
            showInfo("Delete", "Заказ удален.");
        } else {
            showError("Delete", "Заказ с таким ID не найден или не удален.");
        }
    }

    private void searchOrders() {
        Dialog<OrderFilter> dialog = new Dialog<>();
        dialog.setTitle("Search/Filter Orders");
        dialog.setHeaderText("Фильтр заказов");

        ButtonType searchButton = new ButtonType("Искать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("admin-dialog-form");

        TextField userIdField = new TextField();
        TextField dateFromField = new TextField();
        TextField dateToField = new TextField();
        ComboBox<OrderStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(OrderStatus.values());
        TextField minAmountField = new TextField();
        TextField maxAmountField = new TextField();

        grid.addRow(0, new Label("UserID:"), userIdField);
        grid.addRow(1, new Label("Дата от (YYYY-MM-DD):"), dateFromField);
        grid.addRow(2, new Label("Дата до (YYYY-MM-DD):"), dateToField);
        grid.addRow(3, new Label("Статус:"), statusBox);
        grid.addRow(4, new Label("Мин. сумма:"), minAmountField);
        grid.addRow(5, new Label("Макс. сумма:"), maxAmountField);

        configureAdminDialogGrid(grid);
        dialog.getDialogPane().setContent(grid);
        styleDialog(dialog.getDialogPane());
        dialog.setResultConverter(btn -> {
            if (btn == searchButton) {
                OrderFilter filter = new OrderFilter();
                filter.setUserId(parseInteger(userIdField.getText()));
                filter.setDateFrom(dateFromField.getText());
                filter.setDateTo(dateToField.getText());
                filter.setStatus(statusBox.getValue());
                filter.setMinAmount(parseDouble(minAmountField.getText()));
                filter.setMaxAmount(parseDouble(maxAmountField.getText()));
                return filter;
            }
            return null;
        });

        Optional<OrderFilter> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        List<Order> found = orderDAO.search(result.get());
        showOrders(found, "Результаты поиска заказов: " + found.size());
    }

    private Optional<Order> showOrderDialog(Order order) {
        Dialog<Order> dialog = new Dialog<>();
        dialog.setTitle(order == null ? "Create Order" : "Update Order");
        dialog.setHeaderText(order == null ? "Новый заказ" : "Редактирование заказа");

        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("admin-dialog-form");

        TextField userIdField = new TextField(order == null ? "1" : String.valueOf(order.getUserId()));
        TextField dateField = new TextField(order == null ? "2026-05-05" : order.getOrderDate());
        TextField amountField = new TextField(order == null ? "0" : String.valueOf(order.getTotalAmount()));
        ComboBox<OrderStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(OrderStatus.values());
        statusBox.setValue(order == null ? OrderStatus.PROCESSING : order.getStatus());

        grid.addRow(0, new Label("UserID:"), userIdField);
        grid.addRow(1, new Label("Дата (YYYY-MM-DD):"), dateField);
        grid.addRow(2, new Label("Сумма:"), amountField);
        grid.addRow(3, new Label("Статус:"), statusBox);

        configureAdminDialogGrid(grid);
        dialog.getDialogPane().setContent(grid);
        styleDialog(dialog.getDialogPane());
        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                return new Order(
                        order == null ? 0 : order.getId(),
                        parseInteger(userIdField.getText()) == null ? 1 : parseInteger(userIdField.getText()),
                        dateField.getText(),
                        parseDouble(amountField.getText()) == null ? 0.0 : parseDouble(amountField.getText()),
                        statusBox.getValue() == null ? OrderStatus.PROCESSING : statusBox.getValue()
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Integer askInt(String title, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(prompt);
        styleDialog(dialog.getDialogPane());
        Optional<String> text = dialog.showAndWait();
        if (text.isEmpty()) {
            return null;
        }
        return parseInteger(text.get());
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
