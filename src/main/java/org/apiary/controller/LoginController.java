package org.apiary.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apiary.model.Beekeeper;
import org.apiary.model.Client;
import org.apiary.model.User;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.UserService;
import org.apiary.utils.StringUtils;
import org.apiary.utils.ValidationUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private VBox loginForm;
    @FXML private VBox signupForm;

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    @FXML private TextField signupUsername;
    @FXML private PasswordField signupPassword;
    @FXML private PasswordField signupConfirmPassword;
    @FXML private TextField signupFullName;
    @FXML private TextField signupEmail;
    @FXML private TextField signupPhone;
    @FXML private TextArea signupAddress;
    @FXML private RadioButton clientRadioButton;
    @FXML private RadioButton beekeeperRadioButton;
    @FXML private Label yearsExperienceLabel;
    @FXML private Spinner<Integer> yearsExperienceSpinner;

    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private static final Set<Stage> openDashboards = new HashSet<>();

    private UserService userService;

    @FXML
    private void initialize() {
        userService = ServiceFactory.getUserService();
        loginButton.setOnAction(event -> handleLogin());
        signupButton.setOnAction(event -> handleSignup());
        beekeeperRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            yearsExperienceLabel.setVisible(newValue);
            yearsExperienceSpinner.setVisible(newValue);
        });
        clientRadioButton.setSelected(true);
    }

    private void trackDashboard(Stage stage) {
        openDashboards.add(stage);
        stage.setOnCloseRequest(e -> {
            openDashboards.remove(stage);
            LOGGER.info("Dashboard closed. Open dashboards: " + openDashboards.size());
        });
    }
    @FXML
    private void toggleForms() {
        // Toggle between login and signup forms
        boolean loginVisible = loginForm.isVisible();
        loginForm.setVisible(!loginVisible);
        loginForm.setManaged(!loginVisible);
        signupForm.setVisible(loginVisible);
        signupForm.setManaged(loginVisible);
        if (loginVisible) {
            clearSignupFields();
        } else {
            clearLoginFields();
        }
    }

    private void clearLoginFields() {
        loginUsername.clear();
        loginPassword.clear();
    }

    private void clearSignupFields() {
        signupUsername.clear();
        signupPassword.clear();
        signupConfirmPassword.clear();
        signupFullName.clear();
        signupEmail.clear();
        signupPhone.clear();
        signupAddress.clear();
        clientRadioButton.setSelected(true);
        yearsExperienceSpinner.getValueFactory().setValue(0);
    }

    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            showAlert(Alert.AlertType.ERROR, "Login Error",
                    "Please enter both username and password.");
            return;
        }

        try {
            if (userService.authenticate(username, password)) {
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user instanceof Beekeeper) {
                        openBeekeeperDashboard((Beekeeper) user);
                    } else if (user instanceof Client) {
                        openClientDashboard((Client) user);
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid username or password. Please try again.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during login", e);
            showAlert(Alert.AlertType.ERROR, "Login Error",
                    "An error occurred during login: " + e.getMessage());
        }
    }

    private void handleSignup() {
        String username = signupUsername.getText().trim();
        String password = signupPassword.getText().trim();
        String confirmPassword = signupConfirmPassword.getText().trim();
        String fullName = signupFullName.getText().trim();
        String email = signupEmail.getText().trim();
        String phone = signupPhone.getText().trim();
        String address = signupAddress.getText().trim();
        boolean isBeekeeper = beekeeperRadioButton.isSelected();
        Integer yearsExperience = isBeekeeper ? yearsExperienceSpinner.getValue() : null;

        if (StringUtils.isBlank(username)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Username is required.");
            signupUsername.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidUsername(username)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Username must be 3-20 characters and contain only letters, numbers, underscores, and hyphens.");
            signupUsername.requestFocus();
            return;
        }

        if (StringUtils.isBlank(password)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Password is required.");
            signupPassword.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character.");
            signupPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Passwords do not match. Please try again.");
            signupConfirmPassword.requestFocus();
            return;
        }

        if (StringUtils.isBlank(fullName)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Full name is required.");
            signupFullName.requestFocus();
            return;
        }

        if (!StringUtils.isBlank(email) && !ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please enter a valid email address.");
            signupEmail.requestFocus();
            return;
        }

        if (!StringUtils.isBlank(phone) && !ValidationUtils.isValidPhone(phone)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please enter a valid phone number.");
            signupPhone.requestFocus();
            return;
        }

        try {
            User registeredUser = null;
            if (isBeekeeper) {
                Beekeeper beekeeper = userService.registerBeekeeper(username, password, phone, address, yearsExperience);
                registeredUser = beekeeper;
                if (beekeeper != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                            "Beekeeper account created successfully! Opening dashboard...");
                    openBeekeeperDashboard(beekeeper);
                    toggleForms();
                }
            } else {
                Client client = userService.registerClient(username, password, fullName, email, address, phone);
                registeredUser = client;
                if (client != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                            "Client account created successfully! Opening dashboard...");
                    openClientDashboard(client);
                    toggleForms();
                }
            }
            if (registeredUser == null) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Failed to create account. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during registration", e);
            showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "An error occurred during registration: " + e.getMessage());
        }
    }

    private void openClientDashboard(Client client) {
        try {
            LOGGER.info("=== OPENING CLIENT DASHBOARD ===");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/clientDashboard.fxml"));
            Parent root = loader.load();

            ClientDashboardController controller = loader.getController();
            LOGGER.info("Controller obtained: " + controller.getClass().getSimpleName() + "@" +
                    Integer.toHexString(controller.hashCode()));

            Stage clientStage = new Stage();
            clientStage.setScene(new Scene(root));
            clientStage.setTitle("Client Dashboard - " + client.getUsername() + " - Apiary Management System");

            clientStage.setUserData(controller);
            clientStage.setOnCloseRequest(e -> {
                LOGGER.info("Client window closing, cleaning up observers...");
                controller.cleanup();
            });

            trackDashboard(clientStage);
            clientStage.show();
            Platform.runLater(() -> {
                try {
                    LOGGER.info("=== DELAYED CLIENT SETUP STARTING ===");
                    Thread.sleep(500);
                    controller.setClient(client);

                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000);
                            controller.verifyAndForceObserverRegistration();
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error in delayed verification", e);
                        }
                    });

                    LOGGER.info("=== DELAYED CLIENT SETUP COMPLETED ===");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in delayed client setup", e);
                }
            });

            clearLoginFields();
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Client dashboard opened successfully. You can now login as another user if needed.");

            LOGGER.info("=== CLIENT DASHBOARD WINDOW OPENED ===");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening client dashboard", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open client dashboard: " + e.getMessage());
        }
    }

    private void openBeekeeperDashboard(Beekeeper beekeeper) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/beekeeperDashboard.fxml"));
            Parent root = loader.load();
            BeekeeperDashboardController controller = loader.getController();

            Stage beekeeperStage = new Stage();
            beekeeperStage.setScene(new Scene(root));
            beekeeperStage.setTitle("Beekeeper Dashboard - " + beekeeper.getUsername() + " - Apiary Management System");

            beekeeperStage.setUserData(controller);
            beekeeperStage.setOnCloseRequest(e -> {
                LOGGER.info("Beekeeper window closing, cleaning up observers...");
            });

            trackDashboard(beekeeperStage);
            beekeeperStage.show();
            Platform.runLater(() -> {
                try {
                    Thread.sleep(500);
                    controller.setBeekeeper(beekeeper);
                    if (ServiceFactory.getHoneyProductService() instanceof org.apiary.utils.observer.EventManager) {
                        org.apiary.utils.observer.EventManager<?> eventManager =
                                (org.apiary.utils.observer.EventManager<?>) ServiceFactory.getHoneyProductService();
                        LOGGER.info("FINAL BEEKEEPER DASHBOARD - HoneyProductService has " +
                                eventManager.countObservers() + " observers");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error in delayed beekeeper setup", e);
                }
            });
            clearLoginFields();
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Beekeeper dashboard opened successfully. You can now login as another user if needed.");
            LOGGER.info("Opened Beekeeper Dashboard in separate window, login window remains open");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening beekeeper dashboard", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not open beekeeper dashboard: " + e.getMessage());
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