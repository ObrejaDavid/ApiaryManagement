// src/main/java/org/apiary/MainApplication.java
package org.apiary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apiary.config.HibernateConfig;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Hibernate
        HibernateConfig.getSessionFactory();

        // Load the login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Apiary Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Close Hibernate SessionFactory
        if (HibernateConfig.getSessionFactory() != null) {
            HibernateConfig.getSessionFactory().close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}