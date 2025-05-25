package org.apiary.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apiary.model.CartItem;
import org.apiary.model.Client;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.OrderService;
import org.apiary.service.interfaces.ShoppingCartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckoutController {

    private static final Logger LOGGER = Logger.getLogger(CheckoutController.class.getName());

    @FXML private VBox reviewOrderPane;
    @FXML private VBox shippingDetailsPane;
    @FXML private VBox paymentPane;
    @FXML private VBox confirmationPane;
    @FXML private TableView<CartItem> orderReviewTable;
    @FXML private TableColumn<CartItem, String> orderProductColumn;
    @FXML private TableColumn<CartItem, String> orderApiaryColumn;
    @FXML private TableColumn<CartItem, BigDecimal> orderPriceColumn;
    @FXML private TableColumn<CartItem, Integer> orderQuantityColumn;
    @FXML private TableColumn<CartItem, BigDecimal> orderTotalColumn;
    @FXML private Label subtotalLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;
    @FXML private TextField cityField;
    @FXML private TextField countyField;
    @FXML private TextField postalCodeField;
    @FXML private RadioButton creditCardRadioButton;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardholderNameField;
    @FXML private ComboBox<String> expiryMonthComboBox;
    @FXML private ComboBox<String> expiryYearComboBox;
    @FXML private PasswordField cvvField;
    @FXML private Label orderNumberLabel;
    @FXML private Label confirmationTotalLabel;
    @FXML private Label summarySubtotalLabel;
    @FXML private Label summaryShippingLabel;
    @FXML private Label summaryTotalLabel;

    private Client client;
    private ShoppingCartService shoppingCartService;
    private OrderService orderService;
    private ObservableList<CartItem> cartItems;

    @FXML
    private void initialize() {
        shoppingCartService = ServiceFactory.getShoppingCartService();
        orderService = ServiceFactory.getOrderService();

        cartItems = FXCollections.observableArrayList();
        orderReviewTable.setItems(cartItems);

        // Initialize expiry date dropdowns
        for (int i = 1; i <= 12; i++) {
            expiryMonthComboBox.getItems().add(String.format("%02d", i));
        }
        for (int i = 2025; i <= 2035; i++) {
            expiryYearComboBox.getItems().add(String.valueOf(i));
        }

        // Set up table columns
        setupOrderReviewTable();
    }

    private void setupOrderReviewTable() {
        orderProductColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProduct().getName()));

        orderApiaryColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProduct().getApiary().getName()));

        orderPriceColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getPrice()));

        orderQuantityColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getQuantity()));

        orderTotalColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(
                        () -> cellData.getValue().getSubtotal()));
    }

    public void setClient(Client client) {
        this.client = client;

        // Pre-fill shipping details if client has them
        if (client != null) {
            if (client.getFullName() != null) {
                fullNameField.setText(client.getFullName());
            }
            if (client.getPhone() != null) {
                phoneField.setText(client.getPhone());
            }
            if (client.getEmail() != null) {
                emailField.setText(client.getEmail());
            }
            if (client.getAddress() != null) {
                addressField.setText(client.getAddress());
            }
        }
    }

    public void initializeCheckout() {
        // Load cart data and show first step
        loadCartItems();
        showStep(1);
    }

    private void loadCartItems() {
        try {
            List<CartItem> items = shoppingCartService.getCartItems(client);
            cartItems.setAll(items);

            // Update subtotal
            BigDecimal subtotal = shoppingCartService.calculateCartTotal(client);
            subtotalLabel.setText(subtotal + " RON");

            // Update summary labels
            summarySubtotalLabel.setText(subtotal + " RON");
            summaryShippingLabel.setText("0.00 RON"); // Free shipping for now
            summaryTotalLabel.setText(subtotal + " RON");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading cart items for checkout", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load cart items: " + e.getMessage());
        }
    }

    private void showStep(int step) {
        // Hide all panes
        reviewOrderPane.setVisible(false);
        shippingDetailsPane.setVisible(false);
        paymentPane.setVisible(false);
        confirmationPane.setVisible(false);

        // Show selected pane
        switch (step) {
            case 1:
                reviewOrderPane.setVisible(true);
                break;
            case 2:
                shippingDetailsPane.setVisible(true);
                break;
            case 3:
                paymentPane.setVisible(true);
                break;
            case 4:
                confirmationPane.setVisible(true);
                break;
        }
    }

    @FXML
    private void handleBackToCart() {
        // Close checkout window
        subtotalLabel.getScene().getWindow().hide();
    }

    @FXML
    private void handleContinueToShipping() {
        showStep(2);
    }

    @FXML
    private void handleBackToReview() {
        showStep(1);
    }

    @FXML
    private void handleContinueToPayment() {
        // Validate shipping details
        if (validateShippingDetails()) {
            showStep(3);
        }
    }

    private boolean validateShippingDetails() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your full name.");
            fullNameField.requestFocus();
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your phone number.");
            phoneField.requestFocus();
            return false;
        }
        if (addressField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your address.");
            addressField.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void handleBackToShipping() {
        showStep(2);
    }

    @FXML
    private void handlePlaceOrder() {
        try {
            if (cartItems.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Empty Cart", "Your cart is empty.");
                return;
            }

            // Validate payment details
            if (!validatePaymentDetails()) {
                return;
            }

            LOGGER.info("Starting order placement for client: " + client.getUsername());

            // Create order from cart
            var order = orderService.createOrderFromCart(client);
            if (order != null) {
                LOGGER.info("Order created successfully with ID: " + order.getOrderId());

                // Process payment
                boolean paymentSuccess = orderService.processPayment(order.getOrderId());
                if (paymentSuccess) {
                    LOGGER.info("Payment processed successfully for order: " + order.getOrderId());

                    // Clear cart after successful payment
                    shoppingCartService.clearCart(client);

                    orderNumberLabel.setText("Order #" + order.getOrderId());
                    confirmationTotalLabel.setText(order.getTotal() + " RON");
                    showStep(4);
                } else {
                    LOGGER.warning("Payment failed for order: " + order.getOrderId());
                    showAlert(Alert.AlertType.ERROR, "Payment Failed",
                            "Payment could not be processed. Please try again or use a different payment method.");
                }
            } else {
                LOGGER.severe("Order creation returned null for client: " + client.getUsername());
                showAlert(Alert.AlertType.ERROR, "Order Failed",
                        "Could not create order. Please check your cart and try again. If the problem persists, please contact support.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error placing order for client: " + client.getUsername(), e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An unexpected error occurred while placing your order: " + e.getMessage() +
                            ". Please try again or contact support.");
        }
    }

    private boolean validatePaymentDetails() {
        if (creditCardRadioButton.isSelected()) {
            if (cardNumberField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter card number.");
                cardNumberField.requestFocus();
                return false;
            }
            if (cardholderNameField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter cardholder name.");
                cardholderNameField.requestFocus();
                return false;
            }
            if (expiryMonthComboBox.getValue() == null || expiryYearComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select expiry date.");
                return false;
            }
            if (cvvField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter CVV.");
                cvvField.requestFocus();
                return false;
            }
        }
        return true;
    }

    @FXML
    private void handleReturnToShop() {
        // Close checkout window
        subtotalLabel.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}