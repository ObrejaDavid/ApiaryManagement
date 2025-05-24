package org.apiary.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apiary.model.Client;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.OrderService;
import org.apiary.service.interfaces.ShoppingCartService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckoutController {

    private static final Logger LOGGER = Logger.getLogger(CheckoutController.class.getName());

    @FXML private VBox reviewOrderPane;
    @FXML private VBox shippingDetailsPane;
    @FXML private VBox paymentPane;
    @FXML private VBox confirmationPane;
    @FXML private TableView orderReviewTable;
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

    private Client client;
    private ShoppingCartService shoppingCartService;
    private OrderService orderService;

    @FXML
    private void initialize() {
        shoppingCartService = ServiceFactory.getShoppingCartService();
        orderService = ServiceFactory.getOrderService();

        // Initialize expiry date dropdowns
        for (int i = 1; i <= 12; i++) {
            expiryMonthComboBox.getItems().add(String.format("%02d", i));
        }
        for (int i = 2025; i <= 2035; i++) {
            expiryYearComboBox.getItems().add(String.valueOf(i));
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void initializeCheckout() {
        // Load cart data and show first step
        showStep(1);
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
        showStep(3);
    }

    @FXML
    private void handleBackToShipping() {
        showStep(2);
    }

    @FXML
    private void handlePlaceOrder() {
        try {
            // Create order from cart
            var order = orderService.createOrderFromCart(client);
            if (order != null) {
                // Process payment
                boolean paymentSuccess = orderService.processPayment(order.getOrderId());
                if (paymentSuccess) {
                    orderNumberLabel.setText("Order #" + order.getOrderId());
                    confirmationTotalLabel.setText(order.getTotal() + " RON");
                    showStep(4);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Payment Failed", "Payment could not be processed.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Order Failed", "Could not create order.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error placing order", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to place order: " + e.getMessage());
        }
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