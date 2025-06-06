package org.apiary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apiary.config.HibernateConfig;
import org.apiary.service.ServiceFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                session.createQuery("SELECT COUNT(*) FROM User", Long.class).uniqueResult();
                session.getTransaction().commit();
                System.out.println("Database connection successful!");
            }
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }

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
        ServiceFactory.shutdown();
        if (HibernateConfig.getSessionFactory() != null) {
            HibernateConfig.getSessionFactory().close();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}