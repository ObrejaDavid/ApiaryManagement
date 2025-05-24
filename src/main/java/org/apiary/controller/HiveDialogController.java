package org.apiary.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.service.ServiceFactory;
import org.apiary.service.interfaces.ApiaryService;

import java.time.LocalDate;
import java.util.List;

public class HiveDialogController {

    @FXML private ComboBox<Apiary> apiaryComboBox;
    @FXML private Spinner<Integer> hiveNumberSpinner;
    @FXML private Spinner<Integer> queenYearSpinner;
    @FXML private TextArea hiveNotesField;

    private Beekeeper beekeeper;
    private Hive hive;
    private boolean isEdit = false;
    private ApiaryService apiaryService;

    @FXML
    private void initialize() {
        apiaryService = ServiceFactory.getApiaryService();

        // Set current year as default for queen year
        queenYearSpinner.getValueFactory().setValue(LocalDate.now().getYear());

        // Set converter for apiary combo box
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
    }

    public void setBeekeeper(Beekeeper beekeeper) {
        this.beekeeper = beekeeper;
        loadApiaries();
    }

    public void setApiary(Apiary apiary) {
        apiaryComboBox.setValue(apiary);
    }

    public void setHive(Hive hive) {
        this.hive = hive;
        this.isEdit = true;
        populateFields();
    }

    private void loadApiaries() {
        if (beekeeper != null) {
            List<Apiary> apiaries = apiaryService.findByBeekeeper(beekeeper);
            apiaryComboBox.getItems().setAll(apiaries);
        }
    }

    private void populateFields() {
        if (hive != null) {
            apiaryComboBox.setValue(hive.getApiary());
            hiveNumberSpinner.getValueFactory().setValue(hive.getHiveNumber());
            if (hive.getQueenYear() != null) {
                queenYearSpinner.getValueFactory().setValue(hive.getQueenYear());
            }
        }
    }

    public Hive getHive() {
        Apiary selectedApiary = apiaryComboBox.getValue();
        Integer hiveNumber = hiveNumberSpinner.getValue();
        Integer queenYear = queenYearSpinner.getValue();

        if (selectedApiary == null || hiveNumber == null) {
            return null;
        }

        if (isEdit) {
            hive.setApiary(selectedApiary);
            hive.setHiveNumber(hiveNumber);
            hive.setQueenYear(queenYear);
            return hive;
        } else {
            return new Hive(hiveNumber, queenYear, selectedApiary);
        }
    }
}