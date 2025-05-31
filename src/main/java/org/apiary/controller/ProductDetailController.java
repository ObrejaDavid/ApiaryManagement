package org.apiary.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apiary.model.Client;
import org.apiary.model.HoneyProduct;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.ShoppingCartService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDetailController {

    private static final Logger LOGGER = Logger.getLogger(ProductDetailController.class.getName());

    @FXML private ImageView productImage;
    @FXML private Label productTypeLabel;
    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Label productAvailabilityLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private TextFlow productDescriptionFlow;
    @FXML private TextArea productDescriptionArea;
    @FXML private Label apiaryNameLabel;
    @FXML private Label apiaryLocationLabel;
    @FXML private Label harvestDateLabel;
    @FXML private HBox relatedProductsContainer;

    private HoneyProduct product;
    private Client client;
    private ShoppingCartService shoppingCartService;

    @FXML
    private void initialize() {
        shoppingCartService = ServiceFactory.getShoppingCartService();
    }

    public void setProduct(HoneyProduct product) {
        this.product = product;
        populateFields();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void populateFields() {
        if (product != null) {
            productNameLabel.setText(product.getName());
            productPriceLabel.setText(product.getPrice() + " RON");
            productAvailabilityLabel.setText(product.getQuantity() + " kg available");
            productDescriptionArea.setText(product.getDescription() != null ? product.getDescription() : "No description available.");
            apiaryNameLabel.setText(product.getApiary().getName());
            apiaryLocationLabel.setText(product.getApiary().getLocation());
            harvestDateLabel.setText("Recently harvested");
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    1, product.getQuantity().intValue(), 1));
        }
    }

    @FXML
    private void handleBack() {
        quantitySpinner.getScene().getWindow().hide();
    }

    @FXML
    private void handleAddToCart() {
        if (product != null && client != null) {
            int quantity = quantitySpinner.getValue();
            try {
                boolean added = shoppingCartService.addToCart(client, product, quantity);
                if (added) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Added " + quantity + " kg of " + product.getName() + " to your cart.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product to cart.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error adding to cart", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product to cart: " + e.getMessage());
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
}