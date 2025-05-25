package org.apiary.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apiary.model.*;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.ApiaryService;
import org.apiary.service.interfaces.HiveService;
import org.apiary.service.interfaces.HoneyProductService;
import org.apiary.service.interfaces.OrderService;
import org.apiary.utils.StringUtils;
import org.apiary.utils.ValidationUtils;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.Observer;

import org.apiary.model.OrderItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeekeeperDashboardController implements Observer<EntityChangeEvent<?>> {

    private static final Logger LOGGER = Logger.getLogger(BeekeeperDashboardController.class.getName());

    // FXML controls
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    // Apiaries tab controls
    @FXML private TableView<Apiary> apiariesTable;
    @FXML private TableColumn<Apiary, Integer> apiaryIdColumn;
    @FXML private TableColumn<Apiary, String> apiaryNameColumn;
    @FXML private TableColumn<Apiary, String> apiaryLocationColumn;
    @FXML private TableColumn<Apiary, Integer> apiaryHivesCountColumn;
    @FXML private TableColumn<Apiary, Integer> apiaryProductsCountColumn;
    @FXML private TableColumn<Apiary, Void> apiaryActionsColumn;

    // Hives tab controls
    @FXML private ComboBox<Apiary> apiaryFilterComboBox;
    @FXML private TableView<Hive> hivesTable;
    @FXML private TableColumn<Hive, Integer> hiveIdColumn;
    @FXML private TableColumn<Hive, Integer> hiveNumberColumn;
    @FXML private TableColumn<Hive, String> hiveApiaryColumn;
    @FXML private TableColumn<Hive, Integer> hiveQueenYearColumn;
    @FXML private TableColumn<Hive, Integer> hiveProductsCountColumn;
    @FXML private TableColumn<Hive, Void> hiveActionsColumn;

    // Products tab controls
    @FXML private ComboBox<Apiary> productApiaryFilterComboBox;
    @FXML private ComboBox<Hive> productHiveFilterComboBox;
    @FXML private TableView<HoneyProduct> productsTable;
    @FXML private TableColumn<HoneyProduct, Integer> productIdColumn;
    @FXML private TableColumn<HoneyProduct, String> productNameColumn;
    @FXML private TableColumn<HoneyProduct, String> productApiaryColumn;
    @FXML private TableColumn<HoneyProduct, String> productHiveColumn;
    @FXML private TableColumn<HoneyProduct, BigDecimal> productPriceColumn;
    @FXML private TableColumn<HoneyProduct, BigDecimal> productQuantityColumn;
    @FXML private TableColumn<HoneyProduct, Void> productActionsColumn;

    // Orders tab controls
    @FXML private ComboBox<String> orderStatusFilterComboBox;
    @FXML private DatePicker orderStartDatePicker;
    @FXML private DatePicker orderEndDatePicker;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, LocalDateTime> orderDateColumn;
    @FXML private TableColumn<Order, String> orderCustomerColumn;
    @FXML private TableColumn<Order, String> orderProductsColumn;
    @FXML private TableColumn<Order, BigDecimal> orderTotalColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, Void> orderActionsColumn;

    // Model
    private Beekeeper beekeeper;

    // Services
    private ApiaryService apiaryService;
    private HiveService hiveService;
    private HoneyProductService honeyProductService;
    private OrderService orderService;

    // Observable lists
    private ObservableList<Apiary> apiaries;
    private ObservableList<Hive> hives;
    private ObservableList<HoneyProduct> products;
    private ObservableList<Order> orders;

    @FXML
    private void initialize() {
        // Initialize services
        apiaryService = ServiceFactory.getApiaryService();
        hiveService = ServiceFactory.getHiveService();
        honeyProductService = ServiceFactory.getHoneyProductService();
        orderService = ServiceFactory.getOrderService();

        // Register as observer for entity changes
        LOGGER.info("=== REGISTERING BEEKEEPER DASHBOARD AS OBSERVER ===");

        try {
            apiaryService.addObserver(this);
            LOGGER.info("Registered as observer for ApiaryService");

            hiveService.addObserver(this);
            LOGGER.info("Registered as observer for HiveService");

            honeyProductService.addObserver(this);
            LOGGER.info("Registered as observer for HoneyProductService");

            LOGGER.info("BeekeeperDashboardController registered as observer for all services");

            // Log service instances for debugging
            LOGGER.info("HoneyProductService instance: " + honeyProductService.getClass().getSimpleName() + "@" +
                    Integer.toHexString(honeyProductService.hashCode()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering as observer", e);
        }

        LOGGER.info("=== BEEKEEPER DASHBOARD OBSERVER REGISTRATION COMPLETED ===");

        // Initialize observable lists
        apiaries = FXCollections.observableArrayList();
        hives = FXCollections.observableArrayList();
        products = FXCollections.observableArrayList();
        orders = FXCollections.observableArrayList();

        // Set up table columns
        setupTableColumns();

        // Bind lists to tables
        apiariesTable.setItems(apiaries);
        hivesTable.setItems(hives);
        productsTable.setItems(products);
        ordersTable.setItems(orders);

        // Set up combo boxes
        setupComboBoxes();

        // Set up tab change listener
        mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            // Refresh data when switching tabs
            if (beekeeper != null) {
                switch (newVal.intValue()) {
                    case 0: // Apiaries tab
                        loadApiaries();
                        break;
                    case 1: // Hives tab
                        loadHives();
                        break;
                    case 2: // Products tab
                        loadProducts();
                        break;
                    case 3: // Orders tab
                        loadOrders();
                        break;
                }
            }
        });

        // Set up filter change listeners
        apiaryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadHivesByApiary(newVal);
            } else {
                hives.clear();
            }
        });

        productApiaryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadProductHiveFilter(newVal);
                loadProductsByFilters();
            }
        });

        productHiveFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadProductsByFilters();
        });

        // Initialize order status filter
        orderStatusFilterComboBox.getItems().addAll(
                "All", "PENDING", "PAID", "DELIVERED", "CANCELED");
        orderStatusFilterComboBox.setValue("All");
    }


    public void setBeekeeper(Beekeeper beekeeper) {
        this.beekeeper = beekeeper;
        welcomeLabel.setText("Welcome, " + beekeeper.getUsername());

        LOGGER.info("=== BEEKEEPER SET: " + beekeeper.getUsername() + " ===");

        // Verify observer registration after setting beekeeper
        verifyBeekeeperObserverRegistration();

        // Load initial data
        loadApiaries();
    }

    private void setupTableColumns() {
        // Apiaries table
        apiaryIdColumn.setCellValueFactory(new PropertyValueFactory<>("apiaryId"));
        apiaryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        apiaryLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        apiaryHivesCountColumn.setCellValueFactory(cellData -> {
            Apiary apiary = cellData.getValue();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> {
                try {
                    return (int) hiveService.countByApiary(apiary);
                } catch (Exception e) {
                    return 0;
                }
            });
        });

        apiaryProductsCountColumn.setCellValueFactory(cellData -> {
            Apiary apiary = cellData.getValue();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> {
                try {
                    return (int) honeyProductService.countProductsByApiary(apiary.getApiaryId());
                } catch (Exception e) {
                    return 0;
                }
            });
        });

        // Add action buttons to apiary table
        apiaryActionsColumn.setCellFactory(col -> new TableCell<Apiary, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("secondary-button");
                deleteButton.getStyleClass().add("danger-button");

                editButton.setOnAction(e -> {
                    Apiary apiary = getTableView().getItems().get(getIndex());
                    handleEditApiary(apiary);
                });

                deleteButton.setOnAction(e -> {
                    Apiary apiary = getTableView().getItems().get(getIndex());
                    handleDeleteApiary(apiary);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });

        // Hives table
        hiveIdColumn.setCellValueFactory(new PropertyValueFactory<>("hiveId"));
        hiveNumberColumn.setCellValueFactory(new PropertyValueFactory<>("hiveNumber"));

        hiveApiaryColumn.setCellValueFactory(cellData -> {
            Hive hive = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                return hive.getApiary().getName();
            });
        });

        hiveQueenYearColumn.setCellValueFactory(new PropertyValueFactory<>("queenYear"));

        hiveProductsCountColumn.setCellValueFactory(cellData -> {
            Hive hive = cellData.getValue();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> {
                try {
                    return (int) honeyProductService.countProductsByHive(hive.getHiveId());
                } catch (Exception e) {
                    return 0;
                }
            });
        });

        // Add action buttons to hive table
        hiveActionsColumn.setCellFactory(col -> new TableCell<Hive, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("secondary-button");
                deleteButton.getStyleClass().add("danger-button");

                editButton.setOnAction(e -> {
                    Hive hive = getTableView().getItems().get(getIndex());
                    handleEditHive(hive);
                });

                deleteButton.setOnAction(e -> {
                    Hive hive = getTableView().getItems().get(getIndex());
                    handleDeleteHive(hive);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });

        // Products table
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        productApiaryColumn.setCellValueFactory(cellData -> {
            HoneyProduct product = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                return product.getApiary().getName();
            });
        });

        productHiveColumn.setCellValueFactory(cellData -> {
            HoneyProduct product = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                Hive hive = product.getHive();
                return hive != null ? "Hive #" + hive.getHiveNumber() : "N/A";
            });
        });

        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Add action buttons to product table
        productActionsColumn.setCellFactory(col -> new TableCell<HoneyProduct, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("secondary-button");
                deleteButton.getStyleClass().add("danger-button");

                editButton.setOnAction(e -> {
                    HoneyProduct product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });

                deleteButton.setOnAction(e -> {
                    HoneyProduct product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });

        // Orders table
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        orderCustomerColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                Client client = order.getClient();
                return StringUtils.isBlank(client.getFullName()) ?
                        client.getUsername() : client.getFullName();
            });
        });

        orderProductsColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                int itemCount = order.getItems().size();
                return itemCount + " item" + (itemCount != 1 ? "s" : "");
            });
        });

        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add action buttons to order table
        orderActionsColumn.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button viewDetailsButton = new Button("View Details");
            private final HBox buttonBox = new HBox(5, viewDetailsButton);

            {
                viewDetailsButton.getStyleClass().add("secondary-button");

                viewDetailsButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewOrderDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });
    }

    private void setupComboBoxes() {
        // Set converter for apiaryFilterComboBox to display apiary name
        apiaryFilterComboBox.setConverter(new javafx.util.StringConverter<Apiary>() {
            @Override
            public String toString(Apiary apiary) {
                return apiary != null ? apiary.getName() : "";
            }

            @Override
            public Apiary fromString(String string) {
                return apiaryFilterComboBox.getItems().stream()
                        .filter(ap -> ap.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });

        // Set converter for productApiaryFilterComboBox
        productApiaryFilterComboBox.setConverter(new javafx.util.StringConverter<Apiary>() {
            @Override
            public String toString(Apiary apiary) {
                return apiary != null ? apiary.getName() : "";
            }

            @Override
            public Apiary fromString(String string) {
                return productApiaryFilterComboBox.getItems().stream()
                        .filter(ap -> ap.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });

        // Set converter for productHiveFilterComboBox
        productHiveFilterComboBox.setConverter(new javafx.util.StringConverter<Hive>() {
            @Override
            public String toString(Hive hive) {
                return hive != null ? "Hive #" + hive.getHiveNumber() : "All Hives";
            }

            @Override
            public Hive fromString(String string) {
                if ("All Hives".equals(string)) {
                    return null;
                }

                String hiveNumberStr = string.replace("Hive #", "");
                try {
                    int hiveNumber = Integer.parseInt(hiveNumberStr);
                    return productHiveFilterComboBox.getItems().stream()
                            .filter(h -> h.getHiveNumber() == hiveNumber)
                            .findFirst().orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
    }

    // Load data methods
    private void loadApiaries() {
        try {
            List<Apiary> beekeeperApiaries = apiaryService.findByBeekeeper(beekeeper);
            apiaries.setAll(beekeeperApiaries);

            // Update apiary filter combo boxes
            apiaryFilterComboBox.getItems().setAll(beekeeperApiaries);
            productApiaryFilterComboBox.getItems().setAll(beekeeperApiaries);

            if (!beekeeperApiaries.isEmpty()) {
                if (apiaryFilterComboBox.getValue() == null) {
                    apiaryFilterComboBox.setValue(beekeeperApiaries.get(0));
                }

                if (productApiaryFilterComboBox.getValue() == null) {
                    productApiaryFilterComboBox.setValue(beekeeperApiaries.get(0));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading apiaries", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load apiaries: " + e.getMessage());
        }
    }

    private void loadHives() {
        Apiary selectedApiary = apiaryFilterComboBox.getValue();
        if (selectedApiary != null) {
            loadHivesByApiary(selectedApiary);
        } else {
            hives.clear();
        }
    }

    private void loadHivesByApiary(Apiary apiary) {
        try {
            List<Hive> apiaryHives = hiveService.findByApiary(apiary);
            hives.setAll(apiaryHives);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading hives", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load hives: " + e.getMessage());
        }
    }

    private void loadProducts() {
        Apiary selectedApiary = productApiaryFilterComboBox.getValue();
        if (selectedApiary != null) {
            loadProductHiveFilter(selectedApiary);
            loadProductsByFilters();
        } else {
            loadAllProducts();
        }
    }

    private void loadProductHiveFilter(Apiary apiary) {
        try {
            List<Hive> apiaryHives = hiveService.findByApiary(apiary);

            // Add "All Hives" option
            productHiveFilterComboBox.getItems().clear();
            productHiveFilterComboBox.getItems().add(null); // Null represents "All Hives"
            productHiveFilterComboBox.getItems().addAll(apiaryHives);

            // Select "All Hives" by default
            productHiveFilterComboBox.setValue(null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading hive filter", e);
        }
    }

    private void loadProductsByFilters() {
        try {
            Apiary selectedApiary = productApiaryFilterComboBox.getValue();
            Hive selectedHive = productHiveFilterComboBox.getValue();

            List<HoneyProduct> filteredProducts;
            if (selectedApiary != null) {
                if (selectedHive != null) {
                    // Filter by apiary and hive
                    filteredProducts = honeyProductService.findByHive(selectedHive);
                } else {
                    // Filter by apiary only
                    filteredProducts = honeyProductService.findByApiary(selectedApiary);
                }
            } else {
                // No filters, load all products
                filteredProducts = honeyProductService.findByBeekeeper(beekeeper);
            }

            products.setAll(filteredProducts);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading products", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void loadAllProducts() {
        try {
            List<HoneyProduct> beekeeperProducts = honeyProductService.findByBeekeeper(beekeeper);
            products.setAll(beekeeperProducts);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading products", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            LOGGER.info("Loading orders for beekeeper: " + beekeeper.getUsername());

            // Get filter values
            String statusFilter = orderStatusFilterComboBox.getValue();
            LocalDate startDate = orderStartDatePicker.getValue();
            LocalDate endDate = orderEndDatePicker.getValue();

            LOGGER.info("Order filters - Status: " + statusFilter + ", Start Date: " + startDate + ", End Date: " + endDate);

            // Apply filters
            List<Order> filteredOrders;
            if ("All".equals(statusFilter) && startDate == null && endDate == null) {
                // No filters, load all orders for this beekeeper's products
                LOGGER.info("Loading all orders for beekeeper (no filters)");
                filteredOrders = orderService.findOrdersForBeekeeper(beekeeper);
            } else {
                // Apply filters
                LOGGER.info("Loading orders with filters for beekeeper");
                filteredOrders = orderService.findOrdersWithFilters(
                        beekeeper,
                        "All".equals(statusFilter) ? null : statusFilter,
                        startDate != null ? startDate.atStartOfDay() : null,
                        endDate != null ? endDate.plusDays(1).atStartOfDay() : null);
            }

            LOGGER.info("Setting " + filteredOrders.size() + " orders in table");
            orders.setAll(filteredOrders);

            // Log each order for debugging
            for (Order order : filteredOrders) {
                LOGGER.info("Order in table: ID=" + order.getOrderId() +
                        ", Status=" + order.getStatus() +
                        ", Client=" + order.getClient().getUsername() +
                        ", Date=" + order.getDate() +
                        ", Total=" + order.getTotal());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading orders", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
    }

    // Navigation methods
    @FXML
    private void handleFilterOrders() {
        loadOrders();
    }

    @FXML
    private void handleViewApiaries() {
        mainTabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleViewHives() {
        mainTabPane.getSelectionModel().select(1);
    }

    @FXML
    private void handleViewProducts() {
        mainTabPane.getSelectionModel().select(2);
    }

    @FXML
    private void handleViewOrders() {
        mainTabPane.getSelectionModel().select(3);
    }

    @FXML
    private void handleViewReports() {
        mainTabPane.getSelectionModel().select(4);
    }

    // CRUD operations
    @FXML
    private void handleAddApiary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/apiaryDialog.fxml"));
            DialogPane dialogPane = loader.load();

            ApiaryDialogController controller = loader.getController();
            controller.setBeekeeper(beekeeper);

            Dialog<Apiary> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Apiary");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getApiary();
                }
                return null;
            });

            Optional<Apiary> result = dialog.showAndWait();
            result.ifPresent(apiary -> {
                try {
                    Apiary savedApiary = apiaryService.createApiary(
                            apiary.getName(), apiary.getLocation(), beekeeper);

                    if (savedApiary != null) {
                        loadApiaries();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Apiary created successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to create apiary.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error creating apiary", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to create apiary: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading apiary dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open apiary dialog: " + e.getMessage());
        }
    }

    private void handleEditApiary(Apiary apiary) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/apiaryDialog.fxml"));
            DialogPane dialogPane = loader.load();

            ApiaryDialogController controller = loader.getController();
            controller.setBeekeeper(beekeeper);
            controller.setApiary(apiary);

            Dialog<Apiary> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Apiary");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getApiary();
                }
                return null;
            });

            Optional<Apiary> result = dialog.showAndWait();
            result.ifPresent(updatedApiary -> {
                try {
                    Apiary savedApiary = apiaryService.updateApiary(
                            updatedApiary.getApiaryId(),
                            updatedApiary.getName(),
                            updatedApiary.getLocation(),
                            beekeeper);

                    if (savedApiary != null) {
                        loadApiaries();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Apiary updated successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to update apiary.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating apiary", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update apiary: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading apiary dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open apiary dialog: " + e.getMessage());
        }
    }

    private void handleDeleteApiary(Apiary apiary) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Apiary");
        confirmDialog.setHeaderText("Delete " + apiary.getName());
        confirmDialog.setContentText("Are you sure you want to delete this apiary? " +
                "This will also delete all associated hives and products.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = apiaryService.deleteApiary(apiary.getApiaryId());

                if (deleted) {
                    loadApiaries();
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Apiary deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to delete apiary.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting apiary", e);
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to delete apiary: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddHive() {
        Apiary selectedApiary = apiaryFilterComboBox.getValue();
        if (selectedApiary == null) {
            showAlert(Alert.AlertType.WARNING, "No Apiary Selected",
                    "Please select an apiary first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/hiveDialog.fxml"));
            DialogPane dialogPane = loader.load();

            HiveDialogController controller = loader.getController();
            controller.setBeekeeper(beekeeper);
            controller.setApiary(selectedApiary);

            Dialog<Hive> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Hive");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getHive();
                }
                return null;
            });

            Optional<Hive> result = dialog.showAndWait();
            result.ifPresent(hive -> {
                try {
                    Hive savedHive = hiveService.createHive(
                            hive.getHiveNumber(),
                            hive.getQueenYear(),
                            selectedApiary,
                            beekeeper);

                    if (savedHive != null) {
                        loadHivesByApiary(selectedApiary);
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Hive created successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to create hive.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error creating hive", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to create hive: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading hive dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open hive dialog: " + e.getMessage());
        }
    }

    private void handleEditHive(Hive hive) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/hiveDialog.fxml"));
            DialogPane dialogPane = loader.load();

            HiveDialogController controller = loader.getController();
            controller.setBeekeeper(beekeeper);
            controller.setHive(hive);

            Dialog<Hive> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Hive");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getHive();
                }
                return null;
            });

            Optional<Hive> result = dialog.showAndWait();
            result.ifPresent(updatedHive -> {
                try {
                    Hive savedHive = hiveService.updateHive(
                            updatedHive.getHiveId(),
                            updatedHive.getHiveNumber(),
                            updatedHive.getQueenYear(),
                            beekeeper);

                    if (savedHive != null) {
                        loadHivesByApiary(hive.getApiary());
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Hive updated successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to update hive.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating hive", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update hive: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading hive dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open hive dialog: " + e.getMessage());
        }
    }

    private void handleDeleteHive(Hive hive) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Hive");
        confirmDialog.setHeaderText("Delete Hive #" + hive.getHiveNumber());
        confirmDialog.setContentText("Are you sure you want to delete this hive? " +
                "This will also delete all associated honey products.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = hiveService.deleteHive(hive.getHiveId(), beekeeper);

                if (deleted) {
                    loadHivesByApiary(hive.getApiary());
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Hive deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to delete hive.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting hive", e);
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to delete hive: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddProduct() {
        Apiary selectedApiary = productApiaryFilterComboBox.getValue();
        if (selectedApiary == null) {
            showAlert(Alert.AlertType.WARNING, "No Apiary Selected",
                    "Please select an apiary first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/honeyProductDialog.fxml"));
            DialogPane dialogPane = loader.load();

            HoneyProductDialogController controller = loader.getController();
            controller.setBeekeeper(beekeeper);
            controller.setApiary(selectedApiary);

            Dialog<HoneyProduct> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Honey Product");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getProduct();
                }
                return null;
            });

            Optional<HoneyProduct> result = dialog.showAndWait();
            result.ifPresent(product -> {
                try {
                    LOGGER.info("=== BEEKEEPER ADDING NEW PRODUCT ===");
                    LOGGER.info("Product: " + product.getName() + " | Price: " + product.getPrice() +
                            " | Quantity: " + product.getQuantity());

                    HoneyProduct savedProduct = honeyProductService.createHoneyProduct(
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getQuantity(),
                            selectedApiary,
                            product.getHive(),
                            beekeeper);


                    if (savedProduct != null) {
                        LOGGER.info("=== PRODUCT CREATION SUCCESS - TESTING OBSERVER FLOW ===");
                        LOGGER.info("Product created successfully with ID: " + savedProduct.getProductId());
                        LOGGER.info("Product name: " + savedProduct.getName());
                        LOGGER.info("Product price: " + savedProduct.getPrice());

                        // Log current observer counts BEFORE the service automatically notifies
                        try {
                            if (honeyProductService instanceof org.apiary.utils.observer.EventManager) {
                                org.apiary.utils.observer.EventManager<?> eventManager =
                                        (org.apiary.utils.observer.EventManager<?>) honeyProductService;
                                LOGGER.info("About to notify " + eventManager.countObservers() + " observers");

                                if (eventManager.countObservers() < 2) {
                                    LOGGER.warning("ISSUE DETECTED: Expected at least 2 observers (Client + Beekeeper), but found " +
                                            eventManager.countObservers());
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error checking observer count before notification", e);
                        }

                        // The service should automatically notify observers at this point
                        LOGGER.info("Service should have automatically notified observers");

                        // Force log service instances to see current state
                        ServiceFactory.logServiceInstances();

                        loadProductsByFilters();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Honey product created successfully. All clients should see this update immediately.");

                        LOGGER.info("=== PRODUCT CREATION AND NOTIFICATION FLOW COMPLETED ===");
                    } else {
                        LOGGER.warning("Product creation returned null");
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to create honey product.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error creating honey product", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to create honey product: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading honey product dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open honey product dialog: " + e.getMessage());
        }
    }


    private void verifyBeekeeperObserverRegistration() {
        LOGGER.info("=== VERIFYING BEEKEEPER OBSERVER REGISTRATION ===");

        try {
            // Check HoneyProductService
            if (honeyProductService instanceof org.apiary.utils.observer.EventManager) {
                org.apiary.utils.observer.EventManager<?> eventManager =
                        (org.apiary.utils.observer.EventManager<?>) honeyProductService;
                int observerCount = eventManager.countObservers();
                LOGGER.info("BEEKEEPER: HoneyProductService has " + observerCount + " observers registered");

                if (observerCount == 0) {
                    LOGGER.warning("BEEKEEPER: No observers registered for HoneyProductService! Re-registering...");
                    honeyProductService.addObserver(this);
                }
            }

            // Check OrderService
            if (orderService instanceof org.apiary.utils.observer.EventManager) {
                org.apiary.utils.observer.EventManager<?> eventManager =
                        (org.apiary.utils.observer.EventManager<?>) orderService;
                int observerCount = eventManager.countObservers();
                LOGGER.info("BEEKEEPER: OrderService has " + observerCount + " observers registered");

                if (observerCount == 0) {
                    LOGGER.warning("BEEKEEPER: No observers registered for OrderService! Re-registering...");
                    orderService.addObserver(this);
                }
            }

            // Check ApiaryService
            if (apiaryService instanceof org.apiary.utils.observer.EventManager) {
                org.apiary.utils.observer.EventManager<?> eventManager =
                        (org.apiary.utils.observer.EventManager<?>) apiaryService;
                int observerCount = eventManager.countObservers();
                LOGGER.info("BEEKEEPER: ApiaryService has " + observerCount + " observers registered");

                if (observerCount == 0) {
                    LOGGER.warning("BEEKEEPER: No observers registered for ApiaryService! Re-registering...");
                    apiaryService.addObserver(this);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying beekeeper observer registration", e);
        }

        LOGGER.info("=== BEEKEEPER OBSERVER REGISTRATION VERIFICATION COMPLETED ===");
    }

    private void handleEditProduct(HoneyProduct product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/honeyProductDialog.fxml"));
            DialogPane dialogPane = loader.load();

            HoneyProductDialogController controller = loader.getController();
            controller.setBeekeeper(beekeeper);
            controller.setProduct(product);

            Dialog<HoneyProduct> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Honey Product");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getProduct();
                }
                return null;
            });

            Optional<HoneyProduct> result = dialog.showAndWait();
            result.ifPresent(updatedProduct -> {
                try {
                    LOGGER.info("=== BEEKEEPER UPDATING PRODUCT ===");
                    LOGGER.info("Product ID: " + updatedProduct.getProductId());
                    LOGGER.info("Old Price: " + product.getPrice() + " -> New Price: " + updatedProduct.getPrice());
                    LOGGER.info("Old Quantity: " + product.getQuantity() + " -> New Quantity: " + updatedProduct.getQuantity());

                    HoneyProduct savedProduct = honeyProductService.updateHoneyProduct(
                            updatedProduct.getProductId(),
                            updatedProduct.getName(),
                            updatedProduct.getDescription(),
                            updatedProduct.getPrice(),
                            updatedProduct.getQuantity(),
                            beekeeper);

                    if (savedProduct != null) {
                        LOGGER.info("Product updated successfully. New Price: " + savedProduct.getPrice() +
                                " | New Quantity: " + savedProduct.getQuantity());
                        loadProductsByFilters();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Honey product updated successfully. All clients will see this update immediately.");
                    } else {
                        LOGGER.warning("Product update returned null");
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to update honey product.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating honey product", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update honey product: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading honey product dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open honey product dialog: " + e.getMessage());
        }
    }

    private void handleDeleteProduct(HoneyProduct product) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Honey Product");
        confirmDialog.setHeaderText("Delete " + product.getName());
        confirmDialog.setContentText("Are you sure you want to delete this honey product?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = honeyProductService.deleteHoneyProduct(
                        product.getProductId(), beekeeper);

                if (deleted) {
                    loadProductsByFilters();
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Honey product deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to delete honey product.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting honey product", e);
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to delete honey product: " + e.getMessage());
            }
        }
    }


    private void logObserverNotification(String action, HoneyProduct product) {
        LOGGER.info("=== BEEKEEPER " + action.toUpperCase() + " NOTIFICATION ===");
        LOGGER.info("Product: " + product.getName() + " | ID: " + product.getProductId());

        // Check if services have observers registered
        try {
            if (honeyProductService instanceof org.apiary.utils.observer.EventManager) {
                org.apiary.utils.observer.EventManager<?> eventManager =
                        (org.apiary.utils.observer.EventManager<?>) honeyProductService;
                LOGGER.info("HoneyProductService has " + eventManager.countObservers() + " observers to notify");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking observer count", e);
        }

        LOGGER.info("=== " + action.toUpperCase() + " NOTIFICATION LOGGED ===");
    }

    @FXML
    private void handleGenerateSalesReport() {
        // Implementation for sales report generation
        // This would populate the salesReportTable with data
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon",
                "Sales report generation will be implemented in a future version.");
    }

    @FXML
    private void handleGenerateProductionReport() {
        // Implementation for production report generation
        // This would populate the productionReportTable with data
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon",
                "Production report generation will be implemented in a future version.");
    }

    @FXML
    private void handleExportReport() {
        // Implementation for exporting reports to Excel
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon",
                "Report export functionality will be implemented in a future version.");
    }

    @FXML
    private void handleViewProfile() {
        try {
            // Create dialog for profile view/edit
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("My Profile");
            dialog.setHeaderText("View and Edit Profile");

            // Create content
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Create form fields
            TextField usernameField = new TextField(beekeeper.getUsername());
            usernameField.setDisable(true); // Username cannot be changed
            TextField phoneField = new TextField(beekeeper.getPhone());
            TextArea addressField = new TextArea(beekeeper.getAddress());
            addressField.setPrefRowCount(3);
            PasswordField currentPasswordField = new PasswordField();
            PasswordField newPasswordField = new PasswordField();
            PasswordField confirmPasswordField = new PasswordField();

            // Add labels and fields to grid
            grid.add(new Label("Username:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Phone:"), 0, 1);
            grid.add(phoneField, 1, 1);
            grid.add(new Label("Address:"), 0, 2);
            grid.add(addressField, 1, 2);

            // Add separator
            Separator separator = new Separator();
            grid.add(separator, 0, 3, 2, 1);

            // Add password fields
            grid.add(new Label("Current Password:"), 0, 4);
            grid.add(currentPasswordField, 1, 4);
            grid.add(new Label("New Password:"), 0, 5);
            grid.add(newPasswordField, 1, 5);
            grid.add(new Label("Confirm Password:"), 0, 6);
            grid.add(confirmPasswordField, 1, 6);

            // Add helper text
            Label noteLabel = new Label("Leave password fields empty if you don't want to change it");
            noteLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");
            grid.add(noteLabel, 0, 7, 2, 1);

            dialog.getDialogPane().setContent(grid);

            // Add buttons
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Convert result to user when save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Validate passwords if changing
                    if (!currentPasswordField.getText().isEmpty() &&
                            !newPasswordField.getText().isEmpty() &&
                            !confirmPasswordField.getText().isEmpty()) {

                        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "New passwords do not match.");
                            return null;
                        }

                        if (!ValidationUtils.isValidPassword(newPasswordField.getText())) {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Password must be at least 8 characters and include uppercase, " +
                                            "lowercase, number, and special character.");
                            return null;
                        }
                    }

                    // Update beekeeper info
                    beekeeper.setPhone(phoneField.getText());
                    beekeeper.setAddress(addressField.getText());

                    return beekeeper;
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            result.ifPresent(updatedUser -> {
                try {
                    // Handle password change if needed
                    boolean passwordChanged = false;
                    if (!currentPasswordField.getText().isEmpty() &&
                            !newPasswordField.getText().isEmpty()) {

                        passwordChanged = ServiceFactory.getUserService().changePassword(
                                beekeeper.getUsername(),
                                currentPasswordField.getText(),
                                newPasswordField.getText());

                        if (!passwordChanged) {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Current password is incorrect.");
                            return;
                        }
                    }

                    // Update profile
                    User savedUser = ServiceFactory.getUserService().updateProfile(updatedUser);

                    if (savedUser != null) {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Profile updated successfully." +
                                        (passwordChanged ? " Password changed." : ""));
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to update profile.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating profile", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update profile: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening profile dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open profile dialog: " + e.getMessage());
        }
    }

    // File: src/main/java/org/apiary/controller/BeekeeperDashboardController.java
// Replace the handleViewOrderDetails method

    private void handleViewOrderDetails(Order order) {
        try {
            // Create a dialog to show order details
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order #" + order.getOrderId() + " Details");
            dialog.setHeaderText("Order Details");

            // Create content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.setPrefWidth(600);

            // Order info
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(10);
            infoGrid.setVgap(5);

            infoGrid.add(new Label("Order Number:"), 0, 0);
            infoGrid.add(new Label("#" + order.getOrderId()), 1, 0);

            infoGrid.add(new Label("Date:"), 0, 1);
            infoGrid.add(new Label(order.getDate().toString()), 1, 1);

            infoGrid.add(new Label("Customer:"), 0, 2);
            Client client = order.getClient();
            String customerName = StringUtils.isBlank(client.getFullName()) ?
                    client.getUsername() : client.getFullName();
            infoGrid.add(new Label(customerName), 1, 2);

            infoGrid.add(new Label("Status:"), 0, 3);
            Label statusLabel = new Label(order.getStatus());
            statusLabel.getStyleClass().add(
                    "PAID".equals(order.getStatus()) ? "success-label" :
                            "PENDING".equals(order.getStatus()) ? "warning-label" :
                                    "CANCELED".equals(order.getStatus()) ? "danger-label" : "");
            infoGrid.add(statusLabel, 1, 3);

            content.getChildren().add(infoGrid);
            content.getChildren().add(new Separator());

            // Order items
            content.getChildren().add(new Label("Products:"));

            TableView<OrderItem> itemsTable = new TableView<>();
            itemsTable.setPrefHeight(250);

            TableColumn<OrderItem, String> itemNameCol = new TableColumn<>("Product");
            itemNameCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createStringBinding(
                            () -> data.getValue().getProduct().getName()));
            itemNameCol.setPrefWidth(200);

            TableColumn<OrderItem, String> itemApiaryCol = new TableColumn<>("Apiary");
            itemApiaryCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createStringBinding(
                            () -> data.getValue().getProduct().getApiary().getName()));
            itemApiaryCol.setPrefWidth(150);

            TableColumn<OrderItem, String> itemHiveCol = new TableColumn<>("Hive");
            itemHiveCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> {
                        Hive hive = data.getValue().getProduct().getHive();
                        return hive != null ? "Hive #" + hive.getHiveNumber() : "N/A";
                    }));
            itemHiveCol.setPrefWidth(100);

            TableColumn<OrderItem, BigDecimal> itemPriceCol = new TableColumn<>("Unit Price");
            itemPriceCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> data.getValue().getPrice()));
            itemPriceCol.setPrefWidth(100);

            TableColumn<OrderItem, Integer> itemQuantityCol = new TableColumn<>("Quantity");
            itemQuantityCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> data.getValue().getQuantity()));
            itemQuantityCol.setPrefWidth(80);

            TableColumn<OrderItem, BigDecimal> itemTotalCol = new TableColumn<>("Subtotal");
            itemTotalCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> data.getValue().getSubtotal()));
            itemTotalCol.setPrefWidth(100);

            itemsTable.getColumns().addAll(itemNameCol, itemApiaryCol, itemHiveCol,
                    itemPriceCol, itemQuantityCol, itemTotalCol);

            // Load order items using service
            List<OrderItem> orderItems = orderService.getOrderItems(order.getOrderId());
            itemsTable.getItems().addAll(orderItems);

            content.getChildren().add(itemsTable);

            // Total
            HBox totalBox = new HBox(10);
            totalBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            totalBox.setPadding(new Insets(10, 0, 0, 0));
            Label totalLabel = new Label("Order Total: " + order.getTotal() + " RON");
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            totalBox.getChildren().add(totalLabel);
            content.getChildren().add(totalBox);

            // Add status change option for beekeepers (if needed)
            if ("PAID".equals(order.getStatus())) {
                Separator separator = new Separator();
                content.getChildren().add(separator);

                HBox statusBox = new HBox(10);
                statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                statusBox.getChildren().add(new Label("Mark as:"));

                Button markDeliveredButton = new Button("Mark as Delivered");
                markDeliveredButton.getStyleClass().add("primary-button");
                markDeliveredButton.setOnAction(e -> {
                    LOGGER.info("Beekeeper " + beekeeper.getUsername() +
                            " marking order " + order.getOrderId() + " as DELIVERED");

                    // Use the service which will automatically notify observers
                    boolean updated = orderService.updateOrderStatus(order.getOrderId(), "DELIVERED");

                    if (updated) {
                        LOGGER.info("Order status updated successfully, observers should be notified");

                        showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                                "Order marked as delivered successfully. The customer has been notified.");
                        dialog.close();
                        loadOrders(); // Refresh the orders table locally
                    } else {
                        LOGGER.warning("Failed to update order status to DELIVERED");
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to update order status.");
                    }
                });

                statusBox.getChildren().add(markDeliveredButton);
                content.getChildren().add(statusBox);
            }

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing order details", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not display order details: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Logout");
        confirmDialog.setHeaderText("Confirm Logout");
        confirmDialog.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Close current window
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.close();

                // Open login window
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("Apiary Management System - Login");
                loginStage.setScene(new Scene(root));
                loginStage.show();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error logging out", e);
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Could not return to login screen: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void update(EntityChangeEvent<?> event) {
        if (Platform.isFxApplicationThread()) {
            handleEntityChange(event);
        } else {
            Platform.runLater(() -> handleEntityChange(event));
        }
    }

    private void handleEntityChange(EntityChangeEvent<?> event) {
        switch (event.getEntityType()) {
            case "Apiary":
                loadApiaries();
                break;
            case "Hive":
                loadHives();
                break;
            case "HoneyProduct":
                loadProducts();
                break;
            case "Order":
                loadOrders();
                break;
        }
    }
}