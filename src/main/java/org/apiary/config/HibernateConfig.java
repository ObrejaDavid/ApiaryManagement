package org.apiary.config;

import org.apiary.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateConfig {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                // Hibernate settings
                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");

                // FORCE SQL Server Authentication (disable Windows auth)
                boolean useWindowsAuth = false; // Change this line from true to false

                String serverName = System.getProperty("db.server", "localhost");
                String port = System.getProperty("db.port", "1433");
                String databaseName = System.getProperty("db.name", "apiary");

                if (useWindowsAuth) {
                    // Windows Authentication - DISABLED
                    settings.put(Environment.URL,
                            String.format("jdbc:sqlserver://%s:%s;" +
                                            "databaseName=%s;" +
                                            "integratedSecurity=true;" +
                                            "trustServerCertificate=true;" +
                                            "encrypt=false",
                                    serverName, port, databaseName));
                } else {
                    // SQL Server Authentication - ENABLED
                    String username = System.getProperty("db.username", "david");
                    String password = System.getProperty("db.password", "treiezicucuieti");

                    settings.put(Environment.URL,
                            String.format("jdbc:sqlserver://%s:%s;" +
                                            "databaseName=%s;" +
                                            "trustServerCertificate=true;" +
                                            "encrypt=false",
                                    serverName, port, databaseName));
                    settings.put(Environment.USER, username);
                    settings.put(Environment.PASS, password);
                }

                // ... rest of the method remains the same

                settings.put(Environment.USER, "david");
                settings.put(Environment.PASS, "treiezicucuieti");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.SQLServer2012Dialect");
                settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.FORMAT_SQL, "true");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.connection.C3P0ConnectionProvider");
                settings.put(Environment.C3P0_MIN_SIZE, "5");
                settings.put(Environment.C3P0_MAX_SIZE, "20");
                settings.put(Environment.C3P0_TIMEOUT, "300");
                settings.put(Environment.C3P0_MAX_STATEMENTS, "50");
                settings.put(Environment.C3P0_IDLE_TEST_PERIOD, "3000");

                configuration.setProperties(settings);

                // Add annotated classes
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Client.class);
                configuration.addAnnotatedClass(Beekeeper.class);
                configuration.addAnnotatedClass(Apiary.class);
                configuration.addAnnotatedClass(Hive.class);
                configuration.addAnnotatedClass(HoneyProduct.class);
                configuration.addAnnotatedClass(ShoppingCart.class);
                configuration.addAnnotatedClass(CartItem.class);
                configuration.addAnnotatedClass(Order.class);
                configuration.addAnnotatedClass(OrderItem.class);
                configuration.addAnnotatedClass(Payment.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                System.err.println("Initial SessionFactory creation failed: " + e);
                e.printStackTrace();
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}