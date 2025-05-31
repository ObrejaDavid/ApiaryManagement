package org.apiary.config;

import java.util.Properties;

/**
 * Database configuration properties
 */
public class DatabaseProperties {
    private Properties properties;

    public DatabaseProperties(Properties properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getServerName() {
        return getProperty("db.server", "localhost");
    }

    public String getPort() {
        return getProperty("db.port", "1433");
    }

    public String getDatabaseName() {
        return getProperty("db.name", "apiary");
    }

    public String getUsername() {
        return getProperty("db.username", "david");
    }

    public String getPassword() {
        return getProperty("db.password", "treiezicucuieti");
    }
}