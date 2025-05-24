package org.apiary.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.utils.StringUtils;

public class ApiaryDialogController {

    @FXML private TextField apiaryNameField;
    @FXML private TextField apiaryLocationField;
    @FXML private TextArea apiaryDescriptionField;

    private Beekeeper beekeeper;
    private Apiary apiary;
    private boolean isEdit = false;

    public void setBeekeeper(Beekeeper beekeeper) {
        this.beekeeper = beekeeper;
    }

    public void setApiary(Apiary apiary) {
        this.apiary = apiary;
        this.isEdit = true;
        populateFields();
    }

    private void populateFields() {
        if (apiary != null) {
            apiaryNameField.setText(apiary.getName());
            apiaryLocationField.setText(apiary.getLocation());
            // Note: Apiary doesn't have description field in the model, so we skip this
        }
    }

    public Apiary getApiary() {
        String name = apiaryNameField.getText().trim();
        String location = apiaryLocationField.getText().trim();

        if (StringUtils.isBlank(name) || StringUtils.isBlank(location)) {
            return null;
        }

        if (isEdit) {
            apiary.setName(name);
            apiary.setLocation(location);
            return apiary;
        } else {
            return new Apiary(name, location, beekeeper);
        }
    }
}