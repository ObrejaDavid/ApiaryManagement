package org.apiary.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.model.HoneyProduct;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.ApiaryService;
import org.apiary.service.interfaces.HiveService;
import org.apiary.utils.StringUtils;
import org.apiary.utils.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class HoneyProductDialogController {

    @FXML private TextField productNameField;
    @FXML private ComboBox<Apiary> apiaryComboBox;
    @FXML private ComboBox<Hive> hiveComboBox;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> honeyTypeComboBox;
    @FXML private DatePicker harvestDatePicker;

    private Beekeeper beekeeper;
    private HoneyProduct product;
    private boolean isEdit = false;
    private ApiaryService apiaryService;
    private HiveService hiveService;

    @FXML
    private void initialize() {
        apiaryService = ServiceFactory.getApiaryService();
        hiveService = ServiceFactory.getHiveService();

        // Set up honey types
        honeyTypeComboBox.getItems().addAll(
                "Floral", "Forest", "Acacia", "Linden", "Polyfloral", "Organic"
        );

        // Set up converters
        apiaryComboBox.setConverter(new javafx.util.StringConverter<Apiary>() {
            @Override
            public String toString(Apiary apiary) {
                return apiary != null ? apiary.getName() : "";
            }

            @Override
            public Apiary fromString(String string) {
                return apiaryComboBox.getItems().stream()
                        .filter(a -> a.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });

        hiveComboBox.setConverter(new javafx.util.StringConverter<Hive>() {
            @Override
            public String toString(Hive hive) {
                return hive != null ? "Hive #" + hive.getHiveNumber() : "";
            }

            @Override
            public Hive fromString(String string) {
                if (StringUtils.isBlank(string)) return null;
                String numberStr = string.replace("Hive #", "");
                try {
                    int number = Integer.parseInt(numberStr);
                    return hiveComboBox.getItems().stream()
                            .filter(h -> h.getHiveNumber() == number)
                            .findFirst().orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // Set up apiary change listener to load hives
        apiaryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadHives(newVal);
            } else {
                hiveComboBox.getItems().clear();
            }
        });
    }

    public void setBeekeeper(Beekeeper beekeeper) {
        this.beekeeper = beekeeper;
        loadApiaries();
    }

    public void setApiary(Apiary apiary) {
        apiaryComboBox.setValue(apiary);
    }

    public void setProduct(HoneyProduct product) {
        this.product = product;
        this.isEdit = true;
        populateFields();
    }

    private void loadApiaries() {
        if (beekeeper != null) {
            List<Apiary> apiaries = apiaryService.findByBeekeeper(beekeeper);
            apiaryComboBox.getItems().setAll(apiaries);
        }
    }

    private void loadHives(Apiary apiary) {
        List<Hive> hives = hiveService.findByApiary(apiary);
        hiveComboBox.getItems().setAll(hives);
    }

    private void populateFields() {
        if (product != null) {
            productNameField.setText(product.getName());
            apiaryComboBox.setValue(product.getApiary());
            hiveComboBox.setValue(product.getHive());
            priceField.setText(product.getPrice().toString());
            quantityField.setText(product.getQuantity().toString());
            descriptionField.setText(product.getDescription());
            // Set honey type based on product name
            honeyTypeComboBox.getItems().stream()
                    .filter(type -> product.getName().toLowerCase().contains(type.toLowerCase()))
                    .findFirst()
                    .ifPresent(honeyTypeComboBox::setValue);
        }
    }

    public HoneyProduct getProduct() {
        String name = productNameField.getText().trim();
        Apiary selectedApiary = apiaryComboBox.getValue();
        Hive selectedHive = hiveComboBox.getValue();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String description = descriptionField.getText().trim();

        // Validate required fields
        if (StringUtils.isBlank(name) || selectedApiary == null ||
                StringUtils.isBlank(priceText) || StringUtils.isBlank(quantityText)) {
            return null;
        }

        // Validate price and quantity
        if (!ValidationUtils.isPositiveNumeric(priceText) || !ValidationUtils.isPositiveNumeric(quantityText)) {
            return null;
        }

        BigDecimal price = new BigDecimal(priceText);
        BigDecimal quantity = new BigDecimal(quantityText);

        if (isEdit) {
            product.setName(name);
            product.setApiary(selectedApiary);
            product.setHive(selectedHive);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setDescription(description);
            return product;
        } else {
            HoneyProduct newProduct = new HoneyProduct(name, description, price, quantity, selectedApiary);
            newProduct.setHive(selectedHive);
            return newProduct;
        }
    }
}