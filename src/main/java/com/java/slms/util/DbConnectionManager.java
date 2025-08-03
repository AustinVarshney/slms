package com.java.slms.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class DbConnectionManager {

    private static DbConnectionManager instance;
    private Connection connection;

    private String host;
    private String port;
    private String dbName;
    private String username;
    private String password;
    private String url;

    private DbConnectionManager() {
        try {
            String configPath = System.getenv("DB_CONFIG_PATH");
            if (configPath == null || configPath.isEmpty()) {
                throw new RuntimeException("Environment variable DB_CONFIG_PATH not set.");
            }

            Properties props = new Properties();
            try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            props.setProperty(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            }

            host = props.getProperty("db.host");
            port = props.getProperty("db.port");
            dbName = props.getProperty("db.name");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

            if (host == null || port == null || dbName == null || username == null || password == null) {
                throw new RuntimeException("Missing required database configuration properties.");
            }

            // Construct JDBC URL
            url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully to: " + url);
        } catch (SQLException | IOException ex) {
            throw new RuntimeException("Failed to initialize database connection", ex);
        }
    }

    public static synchronized DbConnectionManager getInstance() {
        if (instance == null) {
            instance = new DbConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
