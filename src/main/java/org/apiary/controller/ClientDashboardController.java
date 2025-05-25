package org.apiary.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apiary.model.*;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.*;
import org.apiary.utils.StringUtils;
import org.apiary.utils.ValidationUtils;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.Observer;
import org.apiary.utils.pagination.Page;
import org.apiary.utils.pagination.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientDashboardController implements Observer<EntityChangeEvent<?>> {

    private static final Logger LOGGER = Logger.getLogger(ClientDashboardController.class.getName());

    // FXML controls
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    // Honey Products tab controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> sortOptions;
    @FXML private TilePane productsContainer;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;

    // Shopping Cart tab controls
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartProductColumn;
    @FXML private TableColumn<CartItem, String> cartApiaryColumn;
    @FXML private TableColumn<CartItem, BigDecimal> cartPriceColumn;
    @FXML private TableColumn<CartItem, Integer> cartQuantityColumn;
    @FXML private TableColumn<CartItem, BigDecimal> cartTotalColumn;
    @FXML private TableColumn<CartItem, Void> cartActionsColumn;
    @FXML private Label cartTotalLabel;

    // Orders tab controls
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, String> orderItemsColumn;
    @FXML private TableColumn<Order, BigDecimal> orderTotalColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, Void> orderActionsColumn;

    // Apiaries tab controls
    @FXML private TextField apiarySearchField;
    @FXML private ComboBox<String> apiaryLocationFilter;
    @FXML private TableView<Apiary> apiariesTable;
    @FXML private TableColumn<Apiary, String> apiaryNameColumn;
    @FXML private TableColumn<Apiary, String> apiaryLocationColumn;
    @FXML private TableColumn<Apiary, Integer> apiaryHivesColumn;
    @FXML private TableColumn<Apiary, String> apiaryProductsColumn;

    // Model
    private Client client;

    // Services
    private HoneyProductService honeyProductService;
    private ShoppingCartService shoppingCartService;
    private OrderService orderService;
    private ApiaryService apiaryService;
    private UserService userService;
    private HiveService hiveService;

    // Pagination state
    private int currentPage = 0;
    private int pageSize = 12;
    private int totalPages = 0;
    private String currentSearchTerm = "";
    private String currentCategory = null;
    private BigDecimal minPrice = null;
    private BigDecimal maxPrice = null;
    private String currentSortBy = "name";
    private String currentSortDir = "asc";

    // Observable lists
    private ObservableList<CartItem> cartItems;
    private ObservableList<Order> orders;
    private ObservableList<Apiary> apiaries;


    /**
     * Test method to verify observer registration
     */
    private void testObserverRegistration() {
        LOGGER.info("=== TESTING OBSERVER REGISTRATION ===");

        try {
            // Check if services have observers
            if (honeyProductService instanceof org.apiary.utils.observer.EventManager) {
                org.apiary.utils.observer.EventManager<?> eventManager =
                        (org.apiary.utils.observer.EventManager<?>) honeyProductService;
                LOGGER.info("HoneyProductService has " + eventManager.countObservers() + " observers");
            }

            // Create a test method to manually trigger an update
            Platform.runLater(() -> {
                LOGGER.info("Testing manual observer notification...");
                try {
                    // Force a product refresh to test the mechanism
                    forceRefreshProducts();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in test observer", e);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error testing observer registration", e);
        }
    }

    @FXML
    private void initialize() {
        // Initialize services
        honeyProductService = ServiceFactory.getHoneyProductService();
        shoppingCartService = ServiceFactory.getShoppingCartService();
        orderService = ServiceFactory.getOrderService();
        apiaryService = ServiceFactory.getApiaryService();
        userService = ServiceFactory.getUserService();
        hiveService = ServiceFactory.getHiveService();

        // Register as observer for ALL relevant services for real-time updates
        LOGGER.info("=== REGISTERING CLIENT DASHBOARD AS OBSERVER ===");

        try {
            honeyProductService.addObserver(this);
            LOGGER.info("Registered as observer for HoneyProductService");

            orderService.addObserver(this);
            LOGGER.info("Registered as observer for OrderService");

            apiaryService.addObserver(this);
            LOGGER.info("Registered as observer for ApiaryService");

            hiveService.addObserver(this);
            LOGGER.info("Registered as observer for HiveService");

            LOGGER.info("ClientDashboardController registered as observer for all services");

            // Log service instances for debugging
            LOGGER.info("HoneyProductService instance: " + honeyProductService.getClass().getSimpleName() + "@" +
                    Integer.toHexString(honeyProductService.hashCode()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering as observer", e);
        }

        LOGGER.info("=== CLIENT DASHBOARD OBSERVER REGISTRATION COMPLETED ===");

        // Set up filter options
        setupFilterOptions();

        // Setup table cell factories
        setupTableColumns();

        // Initialize observable lists
        cartItems = FXCollections.observableArrayList();
        orders = FXCollections.observableArrayList();
        apiaries = FXCollections.observableArrayList();

        // Bind lists to tables
        cartTable.setItems(cartItems);
        ordersTable.setItems(orders);
        apiariesTable.setItems(apiaries);

        // Set up search listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                currentPage = 0; // Reset to first page on new search
                handleSearchProducts();
            }
        });

        apiarySearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                handleSearchApiaries();
            }
        });

        // Add change listeners to filters
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentCategory = "All".equals(newVal) ? null : newVal;
            currentPage = 0;
            loadProducts();
        });

        priceFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            setPriceRange(newVal);
            currentPage = 0;
            loadProducts();
        });

        sortOptions.valueProperty().addListener((obs, oldVal, newVal) -> {
            setSortOptions(newVal);
            loadProducts();
        });

        apiaryLocationFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            handleSearchApiaries();
        });

        // Set up tab change listener
        mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            // Refresh data when switching tabs
            if (client != null) {
                switch (newVal.intValue()) {
                    case 0: // Honey Products tab
                        loadProducts();
                        break;
                    case 1: // Shopping Cart tab
                        loadCartItems();
                        break;
                    case 2: // Orders tab
                        loadOrders();
                        break;
                    case 3: // Apiaries tab
                        loadApiaries();
                        break;
                }
            }
        });
    }

    @Override
    public void update(EntityChangeEvent<?> event) {
        LOGGER.info("=== CLIENT DASHBOARD UPDATE METHOD CALLED ===");
        LOGGER.info("Thread: " + Thread.currentThread().getName());
        LOGGER.info("Event: " + event.getType() + " | Entity: " + event.getEntityType());

        if (event.getEntity() instanceof HoneyProduct) {
            HoneyProduct product = (HoneyProduct) event.getEntity();
            LOGGER.info("Product update received - ID: " + product.getProductId() +
                    " | Name: " + product.getName() + " | Price: " + product.getPrice());
        }

        if (Platform.isFxApplicationThread()) {
            LOGGER.info("Already on JavaFX Application Thread, processing immediately");
            handleEntityChangeUpdate(event);
        } else {
            LOGGER.info("Not on JavaFX Application Thread, using Platform.runLater");
            Platform.runLater(() -> {
                LOGGER.info("Platform.runLater execution started");
                handleEntityChangeUpdate(event);
                LOGGER.info("Platform.runLater execution completed");
            });
        }

        LOGGER.info("=== CLIENT DASHBOARD UPDATE METHOD COMPLETED ===");
    }

    private void handleEntityChangeUpdate(EntityChangeEvent<?> event) {
        try {
            LOGGER.info("=== CLIENT DASHBOARD PROCESSING ENTITY CHANGE ===");
            LOGGER.info("Event Type: " + event.getType() + " | Entity Type: " + event.getEntityType());
            LOGGER.info("Current tab index: " + mainTabPane.getSelectionModel().getSelectedIndex());

            switch (event.getEntityType()) {
                case "HoneyProduct":
                    LOGGER.info("Processing HoneyProduct change event");

                    if (event.getEntity() instanceof HoneyProduct) {
                        HoneyProduct product = (HoneyProduct) event.getEntity();
                        LOGGER.info("Updated product details - ID: " + product.getProductId() +
                                " | Name: " + product.getName() +
                                " | Price: " + product.getPrice() +
                                " | Quantity: " + product.getQuantity());
                    }

                    // Force refresh products regardless of current tab
                    LOGGER.info("Forcing product refresh...");
                    forceRefreshProducts();

                    // Show notification to user
                    Platform.runLater(() -> {
                        try {
                            String message = event.getType() == EntityChangeEvent.Type.CREATED ?
                                    "New honey product available!" : "Product information updated!";
                            showTemporaryNotification(message);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error showing notification", e);
                        }
                    });
                    break;

                case "Order":
                    LOGGER.info("Processing Order change event");
                    loadOrders();
                    if (event.getType() == EntityChangeEvent.Type.CREATED) {
                        loadCartItems();
                    }
                    break;

                case "Apiary":
                    LOGGER.info("Processing Apiary change event");
                    loadApiaries();
                    forceRefreshProducts();
                    break;

                case "Hive":
                    LOGGER.info("Processing Hive change event");
                    loadApiaries();
                    forceRefreshProducts();
                    break;

                case "CartItem":
                    LOGGER.info("Processing CartItem change event");
                    loadCartItems();
                    break;

                default:
                    LOGGER.info("Unknown entity type: " + event.getEntityType());
                    break;
            }

            LOGGER.info("=== CLIENT DASHBOARD ENTITY CHANGE PROCESSING COMPLETED ===");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling entity change update in ClientDashboard", e);
            e.printStackTrace();
        }
    }

    /**
     * Force refresh products with complete UI clearing
     */
    private void forceRefreshProducts() {
        try {
            LOGGER.info("=== FORCE REFRESH PRODUCTS STARTED ===");
            LOGGER.info("Current products container children count: " + productsContainer.getChildren().size());

            // Clear the products container immediately and force layout update
            productsContainer.getChildren().clear();
            productsContainer.requestLayout();

            LOGGER.info("Products container cleared, children count: " + productsContainer.getChildren().size());

            // Force a layout pass
            Platform.runLater(() -> {
                try {
                    LOGGER.info("=== EXECUTING DELAYED REFRESH ===");

                    // Double-check container is empty
                    if (!productsContainer.getChildren().isEmpty()) {
                        LOGGER.warning("Container not empty after clear, forcing clear again");
                        productsContainer.getChildren().clear();
                    }

                    // Load fresh products
                    loadProducts();

                    // Force layout update
                    productsContainer.requestLayout();

                    LOGGER.info("=== DELAYED REFRESH COMPLETED ===");
                    LOGGER.info("Final products container children count: " + productsContainer.getChildren().size());

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in delayed product refresh", e);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in forceRefreshProducts", e);
        }
    }
    /**
     * Debug method to check current UI state
     */
    private void debugCurrentUIState() {
        LOGGER.info("=== DEBUGGING CURRENT UI STATE ===");
        LOGGER.info("Products container children count: " + productsContainer.getChildren().size());

        for (int i = 0; i < productsContainer.getChildren().size(); i++) {
            Node child = productsContainer.getChildren().get(i);
            if (child instanceof VBox) {
                VBox tile = (VBox) child;
                LOGGER.info("Tile " + i + " ID: " + tile.getId());

                // Find price label
                for (Node tileChild : tile.getChildren()) {
                    if (tileChild instanceof Label) {
                        Label label = (Label) tileChild;
                        if (label.getId() != null && label.getId().startsWith("price-")) {
                            LOGGER.info("  Price label text: '" + label.getText() + "'");
                        } else if (label.getId() != null && label.getId().startsWith("name-")) {
                            LOGGER.info("  Name label text: '" + label.getText() + "'");
                        }
                    }
                }
            }
        }
        LOGGER.info("=== UI STATE DEBUG COMPLETED ===");
    }

    /**
     * Show a temporary notification to the user
     */
    private void showTemporaryNotification(String message) {
        Platform.runLater(() -> {
            try {
                Alert notification = new Alert(Alert.AlertType.INFORMATION);
                notification.setTitle("Update Notification");
                notification.setHeaderText("Real-time Update");
                notification.setContentText(message);

                // Auto-close the notification after 3 seconds
                Timeline timeline = new Timeline(new KeyFrame(
                        Duration.seconds(3),
                        e -> notification.close()
                ));
                timeline.play();

                notification.show();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error showing notification", e);
            }
        });
    }

    public void setClient(Client client) {
        this.client = client;
        welcomeLabel.setText("Welcome, " + (StringUtils.isBlank(client.getFullName()) ?
                client.getUsername() : client.getFullName()));

        LOGGER.info("=== CLIENT SET: " + client.getUsername() + " ===");

        // Test observer registration
        testObserverRegistration();

        // Load initial data
        loadProducts();
    }

    private void setupFilterOptions() {
        // Category filter
        categoryFilter.getItems().add("All");
        categoryFilter.getItems().addAll("Floral", "Forest", "Acacia", "Linden", "Polyfloral", "Organic");
        categoryFilter.setValue("All");

        // Price filter
        priceFilter.getItems().addAll(
                "All Prices",
                "Under 20 RON",
                "20-50 RON",
                "50-100 RON",
                "Over 100 RON"
        );
        priceFilter.setValue("All Prices");

        // Sort options
        sortOptions.getItems().addAll(
                "Name: A to Z",
                "Name: Z to A",
                "Price: Low to High",
                "Price: High to Low",
                "Newest First"
        );
        sortOptions.setValue("Name: A to Z");

        // Apiary location filter
        apiaryLocationFilter.getItems().add("All Locations");
        // Load locations dynamically from database
        try {
            List<String> locations = apiaryService.findAllLocations();
            apiaryLocationFilter.getItems().addAll(locations);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load apiary locations", e);
        }
        apiaryLocationFilter.setValue("All Locations");
    }

    private void setupTableColumns() {
        // Cart table
        cartProductColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProduct().getName()));

        cartApiaryColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProduct().getApiary().getName()));

        cartPriceColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getPrice()));

        cartQuantityColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getQuantity()));

        cartTotalColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getSubtotal()));

        // Add delete button to cart actions column
        cartActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Remove");

            {
                deleteButton.getStyleClass().add("danger-button");
                deleteButton.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    handleRemoveFromCart(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // Orders table
        orderIdColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getOrderId()));

        orderDateColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getDate().toString()));

        orderItemsColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                int itemCount = order.getItems().size();
                return itemCount + " item" + (itemCount != 1 ? "s" : "");
            });
        });

        orderTotalColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getTotal()));

        orderStatusColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getStatus()));

        // Add view details button to order actions column
        orderActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View Details");

            {
                viewButton.getStyleClass().add("secondary-button");
                viewButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewOrderDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        // Apiaries table
        apiaryNameColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getName()));

        apiaryLocationColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getLocation()));

        // FIX: Use HiveService to count hives instead of accessing collection directly
        apiaryHivesColumn.setCellValueFactory(cellData -> {
            Apiary apiary = cellData.getValue();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> {
                try {
                    return (int) hiveService.countByApiary(apiary);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error counting hives for apiary: " + apiary.getApiaryId(), e);
                    return 0;
                }
            });
        });

        apiaryProductsColumn.setCellValueFactory(cellData -> {
            Apiary apiary = cellData.getValue();
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                try {
                    long productCount = honeyProductService.countProductsByApiary(apiary.getApiaryId());
                    return productCount + " product" + (productCount != 1 ? "s" : "");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error counting products for apiary: " + apiary.getApiaryId(), e);
                    return "N/A";
                }
            });
        });
    }

    private void setPriceRange(String priceFilter) {
        switch (priceFilter) {
            case "Under 20 RON":
                minPrice = null;
                maxPrice = new BigDecimal("20");
                break;
            case "20-50 RON":
                minPrice = new BigDecimal("20");
                maxPrice = new BigDecimal("50");
                break;
            case "50-100 RON":
                minPrice = new BigDecimal("50");
                maxPrice = new BigDecimal("100");
                break;
            case "Over 100 RON":
                minPrice = new BigDecimal("100");
                maxPrice = null;
                break;
            default:
                minPrice = null;
                maxPrice = null;
                break;
        }
    }

    private void setSortOptions(String sortOption) {
        switch (sortOption) {
            case "Name: A to Z":
                currentSortBy = "name";
                currentSortDir = "asc";
                break;
            case "Name: Z to A":
                currentSortBy = "name";
                currentSortDir = "desc";
                break;
            case "Price: Low to High":
                currentSortBy = "price";
                currentSortDir = "asc";
                break;
            case "Price: High to Low":
                currentSortBy = "price";
                currentSortDir = "desc";
                break;
            case "Newest First":
                currentSortBy = "id";
                currentSortDir = "desc";
                break;
            default:
                currentSortBy = "name";
                currentSortDir = "asc";
                break;
        }
    }

    @FXML
    private void handleSearchProducts() {
        currentSearchTerm = searchField.getText().trim();
        currentPage = 0;
        loadProducts();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadProducts();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadProducts();
        }
    }

    private void loadProducts() {
        try {
            LOGGER.info("=== LOADING PRODUCTS FOR CLIENT DASHBOARD ===");
            LOGGER.info("Current page: " + currentPage + " | Page size: " + pageSize);
            LOGGER.info("Search term: '" + currentSearchTerm + "' | Category: " + currentCategory);
            LOGGER.info("Price range: " + minPrice + " - " + maxPrice);

            // Debug current UI state before clearing
            debugCurrentUIState();

            // Clear existing products with forced update
            LOGGER.info("Clearing products container...");
            productsContainer.getChildren().clear();
            productsContainer.requestLayout();

            // Verify it's actually cleared
            LOGGER.info("After clear - children count: " + productsContainer.getChildren().size());

            // Create pageable for pagination
            Pageable pageable = new Pageable(currentPage, pageSize, currentSortBy, currentSortDir);

            // Get products from service with fresh data
            Page<HoneyProduct> productPage;
            if (!StringUtils.isBlank(currentSearchTerm)) {
                LOGGER.info("Loading products by search term: " + currentSearchTerm);
                productPage = honeyProductService.findByNameContaining(
                        currentSearchTerm, currentCategory, minPrice, maxPrice, pageable);
            } else if (currentCategory != null || minPrice != null || maxPrice != null) {
                LOGGER.info("Loading products with filters");
                productPage = honeyProductService.findByFilters(
                        currentCategory, minPrice, maxPrice, pageable);
            } else {
                LOGGER.info("Loading all available products");
                productPage = honeyProductService.findAvailableProducts(pageable);
            }

            LOGGER.info("Loaded " + productPage.getContent().size() + " products on page " +
                    (currentPage + 1) + " of " + productPage.getTotalPages());

            // Update pagination controls
            totalPages = productPage.getTotalPages();
            updatePaginationControls();

            // Create product tiles with detailed logging
            int tileCount = 0;
            for (HoneyProduct product : productPage.getContent()) {
                try {
                    LOGGER.info("About to create tile for product: " + product.getName() +
                            " with price: " + product.getPrice());
                    createProductTile(product);
                    tileCount++;
                    LOGGER.info("Successfully created tile " + tileCount + " for product: " + product.getName());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error creating tile for product: " + product.getName(), e);
                }
            }

            // Force layout update after all tiles are added
            productsContainer.requestLayout();

            // Debug final UI state
            LOGGER.info("After creating tiles - children count: " + productsContainer.getChildren().size());
            debugCurrentUIState();

            LOGGER.info("=== PRODUCT LOADING COMPLETED - " + tileCount + " tiles created ===");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading products", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void createProductTile(HoneyProduct product) {
        LOGGER.info("=== CREATING PRODUCT TILE ===");
        LOGGER.info("Product: " + product.getName() + " | Price: " + product.getPrice() + " | ID: " + product.getProductId());

        VBox productTile = new VBox();
        productTile.getStyleClass().add("product-tile");
        productTile.setPrefWidth(200);
        productTile.setPrefHeight(250);
        productTile.setPadding(new Insets(10));
        productTile.setSpacing(5);

        // Set a unique ID for debugging
        productTile.setId("product-tile-" + product.getProductId());

        // Create image placeholder
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 120);

        Rectangle placeholder = new Rectangle(180, 120);
        placeholder.setFill(Color.LIGHTGOLDENRODYELLOW);
        placeholder.setStroke(Color.DARKGOLDENROD);
        placeholder.setStrokeWidth(2);

        Label honeyIcon = new Label("🍯");
        honeyIcon.setStyle("-fx-font-size: 36px;");

        imageContainer.getChildren().addAll(placeholder, honeyIcon);

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setId("name-" + product.getProductId());

        // Apiary name
        Label apiaryLabel = new Label("From: " + product.getApiary().getName());
        apiaryLabel.setWrapText(true);
        apiaryLabel.setId("apiary-" + product.getProductId());

        // Price label - FORCE the text update
        Label priceLabel = new Label();
        priceLabel.getStyleClass().add("product-price");
        priceLabel.setId("price-" + product.getProductId());

        // Force text update with explicit string conversion
        String priceText = String.format("%.2f RON", product.getPrice().doubleValue());
        priceLabel.setText(priceText);

        LOGGER.info("Setting price label text to: '" + priceText + "' for product: " + product.getName());

        // Buttons
        Button viewButton = new Button("View Details");
        viewButton.getStyleClass().add("secondary-button");
        viewButton.setOnAction(e -> handleViewProductDetails(product));

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.getStyleClass().add("primary-button");
        addToCartButton.setMaxWidth(Double.MAX_VALUE);
        addToCartButton.setOnAction(e -> handleQuickAddToCart(product));

        // Add all components
        productTile.getChildren().addAll(
                imageContainer, nameLabel, apiaryLabel, priceLabel,
                new Region(), viewButton, addToCartButton);
        VBox.setVgrow(viewButton, Priority.ALWAYS);

        // Force layout update
        productTile.autosize();

        // Add to container and force immediate layout
        productsContainer.getChildren().add(productTile);

        LOGGER.info("Product tile created and added to container for: " + product.getName() +
                " with price: " + priceText);
    }

    private void updatePaginationControls() {
        pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void handleViewProducts() {
        mainTabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleViewCart() {
        mainTabPane.getSelectionModel().select(1);
        loadCartItems();
    }

    @FXML
    private void handleViewOrders() {
        mainTabPane.getSelectionModel().select(2);
        loadOrders();
    }

    @FXML
    private void handleViewApiaries() {
        mainTabPane.getSelectionModel().select(3);
        loadApiaries();
    }

    private void loadCartItems() {
        try {
            List<CartItem> items = shoppingCartService.getCartItems(client);
            cartItems.setAll(items);

            // Update total
            BigDecimal total = shoppingCartService.calculateCartTotal(client);
            cartTotalLabel.setText(total + " RON");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading cart items", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load cart items: " + e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            List<Order> clientOrders = orderService.findByClient(client);
            orders.setAll(clientOrders);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading orders", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private void loadApiaries() {
        try {
            String searchTerm = apiarySearchField.getText().trim();
            String locationFilter = apiaryLocationFilter.getValue();

            List<Apiary> apiaryList;
            if (!StringUtils.isBlank(searchTerm)) {
                apiaryList = apiaryService.findByNameContaining(searchTerm);
            } else if (!"All Locations".equals(locationFilter)) {
                apiaryList = apiaryService.findByLocationContaining(locationFilter);
            } else {
                apiaryList = apiaryService.findAll();
            }

            apiaries.setAll(apiaryList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading apiaries", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load apiaries: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchApiaries() {
        loadApiaries();
    }

    private void handleViewProductDetails(HoneyProduct product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/productDetail.fxml"));
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setProduct(product);
            controller.setClient(client);

            Stage stage = new Stage();
            stage.setTitle(product.getName() + " - Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh cart after returning from product details
            loadCartItems();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening product details", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open product details: " + e.getMessage());
        }
    }

    private void handleQuickAddToCart(HoneyProduct product) {
        try {
            boolean added = shoppingCartService.addToCart(client, product, 1);

            if (added) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Added " + product.getName() + " to your cart.");
                loadCartItems();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Could not add product to cart. Please try again.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding to cart", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add product to cart: " + e.getMessage());
        }
    }

    private void handleRemoveFromCart(CartItem item) {
        try {
            boolean removed = shoppingCartService.removeFromCart(client, item.getItemId());

            if (removed) {
                cartItems.remove(item);

                // Update total
                BigDecimal total = shoppingCartService.calculateCartTotal(client);
                cartTotalLabel.setText(total + " RON");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Could not remove item from cart. Please try again.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing from cart", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to remove item from cart: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearCart() {
        try {
            // Confirm before clearing
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Clear Cart");
            confirmDialog.setHeaderText("Clear Shopping Cart");
            confirmDialog.setContentText("Are you sure you want to remove all items from your cart?");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean cleared = shoppingCartService.clearCart(client);

                if (cleared) {
                    cartItems.clear();
                    cartTotalLabel.setText("0.00 RON");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Could not clear cart. Please try again.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error clearing cart", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to clear cart: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            if (cartItems.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Cart",
                        "Your cart is empty. Please add products before checking out.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/checkout.fxml"));
            Parent root = loader.load();

            CheckoutController controller = loader.getController();
            controller.setClient(client);
            controller.initializeCheckout();

            Stage stage = new Stage();
            stage.setTitle("Checkout");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> {
                // Refresh cart and orders after checkout
                loadCartItems();
                loadOrders();
            });
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening checkout", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open checkout: " + e.getMessage());
        }
    }

    private void handleViewOrderDetails(Order order) {
        try {
            // Create a dialog to show order details
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order #" + order.getOrderId() + " Details");
            dialog.setHeaderText("Order Details");

            // Create content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            // Order info
            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(10);
            infoGrid.setVgap(5);

            infoGrid.add(new Label("Order Number:"), 0, 0);
            infoGrid.add(new Label("#" + order.getOrderId()), 1, 0);

            infoGrid.add(new Label("Date:"), 0, 1);
            infoGrid.add(new Label(order.getDate().toString()), 1, 1);

            infoGrid.add(new Label("Status:"), 0, 2);
            Label statusLabel = new Label(order.getStatus());
            statusLabel.getStyleClass().add(
                    "PAID".equals(order.getStatus()) ? "success-label" :
                            "PENDING".equals(order.getStatus()) ? "warning-label" :
                                    "CANCELED".equals(order.getStatus()) ? "danger-label" : "");
            infoGrid.add(statusLabel, 1, 2);

            content.getChildren().add(infoGrid);
            content.getChildren().add(new Separator());

            // Order items - USE SERVICE TO LOAD ITEMS PROPERLY
            content.getChildren().add(new Label("Items:"));

            TableView<OrderItem> itemsTable = new TableView<>();
            itemsTable.setPrefHeight(200);

            TableColumn<OrderItem, String> itemNameCol = new TableColumn<>("Product");
            itemNameCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createStringBinding(
                            () -> data.getValue().getProduct().getName()));
            itemNameCol.setPrefWidth(200);

            TableColumn<OrderItem, BigDecimal> itemPriceCol = new TableColumn<>("Price");
            itemPriceCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> data.getValue().getPrice()));

            TableColumn<OrderItem, Integer> itemQuantityCol = new TableColumn<>("Quantity");
            itemQuantityCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> data.getValue().getQuantity()));

            TableColumn<OrderItem, BigDecimal> itemTotalCol = new TableColumn<>("Total");
            itemTotalCol.setCellValueFactory(data ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> data.getValue().getSubtotal()));

            itemsTable.getColumns().addAll(itemNameCol, itemPriceCol, itemQuantityCol, itemTotalCol);

            // FIX: Load order items using service instead of accessing collection directly
            List<OrderItem> orderItems = orderService.getOrderItems(order.getOrderId());
            itemsTable.getItems().addAll(orderItems);

            content.getChildren().add(itemsTable);

            // Total
            HBox totalBox = new HBox(10);
            totalBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            totalBox.getChildren().addAll(
                    new Label("Total:"),
                    new Label(order.getTotal() + " RON")
            );
            content.getChildren().add(totalBox);

            // Add cancel button if order is pending
            if ("PENDING".equals(order.getStatus())) {
                Button cancelButton = new Button("Cancel Order");
                cancelButton.getStyleClass().add("danger-button");
                cancelButton.setOnAction(e -> {
                    handleCancelOrder(order, dialog);
                });
                content.getChildren().add(cancelButton);
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

    private void handleCancelOrder(Order order, Dialog<Void> dialog) {
        try {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Cancel Order");
            confirmDialog.setHeaderText("Cancel Order #" + order.getOrderId());
            confirmDialog.setContentText("Are you sure you want to cancel this order?");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean canceled = orderService.cancelOrder(order.getOrderId(), client);

                if (canceled) {
                    showAlert(Alert.AlertType.INFORMATION, "Order Canceled",
                            "Order #" + order.getOrderId() + " has been canceled.");

                    // Close the dialog
                    dialog.close();

                    // Refresh orders
                    loadOrders();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Could not cancel order. Please try again.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error canceling order", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to cancel order: " + e.getMessage());
        }
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
            grid.setPadding(new Insets(20));

            // Username (read-only)
            Label usernameLabel = new Label("Username:");
            TextField usernameField = new TextField(client.getUsername());
            usernameField.setEditable(false);
            grid.add(usernameLabel, 0, 0);
            grid.add(usernameField, 1, 0);

            // Full Name
            Label fullNameLabel = new Label("Full Name:");
            TextField fullNameField = new TextField(client.getFullName());
            grid.add(fullNameLabel, 0, 1);
            grid.add(fullNameField, 1, 1);

            // Email
            Label emailLabel = new Label("Email:");
            TextField emailField = new TextField(client.getEmail());
            grid.add(emailLabel, 0, 2);
            grid.add(emailField, 1, 2);

            // Phone
            Label phoneLabel = new Label("Phone:");
            TextField phoneField = new TextField(client.getPhone());
            grid.add(phoneLabel, 0, 3);
            grid.add(phoneField, 1, 3);

            // Address
            Label addressLabel = new Label("Address:");
            TextArea addressArea = new TextArea(client.getAddress());
            addressArea.setPrefRowCount(3);
            grid.add(addressLabel, 0, 4);
            grid.add(addressArea, 1, 4);

            // Password change section
            Separator separator = new Separator();
            grid.add(separator, 0, 5, 2, 1);

            Label passwordLabel = new Label("Change Password:");
            grid.add(passwordLabel, 0, 6, 2, 1);

            Label currentPasswordLabel = new Label("Current Password:");
            PasswordField currentPasswordField = new PasswordField();
            grid.add(currentPasswordLabel, 0, 7);
            grid.add(currentPasswordField, 1, 7);

            Label newPasswordLabel = new Label("New Password:");
            PasswordField newPasswordField = new PasswordField();
            grid.add(newPasswordLabel, 0, 8);
            grid.add(newPasswordField, 1, 8);

            Label confirmPasswordLabel = new Label("Confirm Password:");
            PasswordField confirmPasswordField = new PasswordField();
            grid.add(confirmPasswordLabel, 0, 9);
            grid.add(confirmPasswordField, 1, 9);

            // Add buttons
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = ButtonType.CANCEL;
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

            // Enable/disable save button based on validation
            Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);

            // Set dialog content
            dialog.getDialogPane().setContent(grid);

            // Set result converter
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Update user info
                    client.setFullName(fullNameField.getText().trim());
                    client.setEmail(emailField.getText().trim());
                    client.setPhone(phoneField.getText().trim());
                    client.setAddress(addressArea.getText().trim());

                    // Check if password changed
                    String currentPassword = currentPasswordField.getText();
                    String newPassword = newPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();

                    if (!StringUtils.isBlank(currentPassword) &&
                            !StringUtils.isBlank(newPassword) &&
                            !StringUtils.isBlank(confirmPassword)) {

                        if (!newPassword.equals(confirmPassword)) {
                            showAlert(Alert.AlertType.ERROR, "Password Error",
                                    "New passwords do not match.");
                            return null;
                        }

                        if (!ValidationUtils.isValidPassword(newPassword)) {
                            showAlert(Alert.AlertType.ERROR, "Password Error",
                                    "Password must be at least 8 characters and include uppercase, " +
                                            "lowercase, number, and special character.");
                            return null;
                        }

                        // Change password
                        boolean passwordChanged =
                                userService.changePassword(client.getUsername(), currentPassword, newPassword);

                        if (!passwordChanged) {
                            showAlert(Alert.AlertType.ERROR, "Password Error",
                                    "Current password is incorrect. Password not changed.");
                            return null;
                        }
                    }

                    return client;
                }
                return null;
            });

            // Show dialog and process result
            Optional<User> result = dialog.showAndWait();
            result.ifPresent(updatedUser -> {
                try {
                    User saved = userService.updateProfile(updatedUser);
                    if (saved != null) {
                        showAlert(Alert.AlertType.INFORMATION, "Profile Updated",
                                "Your profile has been updated successfully.");

                        // Update welcome label with new name if changed
                        welcomeLabel.setText("Welcome, " + (StringUtils.isBlank(client.getFullName()) ?
                                client.getUsername() : client.getFullName()));
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Update Error",
                                "Could not update profile. Please try again.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating profile", e);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update profile: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening profile", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Navigate back to login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Apiary Management System");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logout", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error",
                    "An error occurred during logout: " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}