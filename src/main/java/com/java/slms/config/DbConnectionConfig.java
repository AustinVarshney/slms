package com.java.slms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DbConnectionConfig
{
    private static final String ENV_SLMS_CONFIG = "SLMS_CONFIG";

    private static final String KEY_DB_HOST = "db.host";
    private static final String KEY_DB_PORT = "db.port";
    private static final String KEY_DB_NAME = "db.name";
    private static final String KEY_DB_USERNAME = "db.username";
    private static final String KEY_DB_PASSWORD = "db.password";

    private static final String JDBC_PREFIX = "jdbc:mysql://";

    @Bean
    public DataSource dataSource(DataSourceProperties properties)
    {
        String configPath = System.getenv(ENV_SLMS_CONFIG);
        if (configPath == null || configPath.isEmpty())
        {
            throw new RuntimeException("Environment variable " + ENV_SLMS_CONFIG + " not set");
        }

        Map<String, String> dbProps = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configPath)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2)
                {
                    dbProps.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to read DB config file: " + e.getMessage(), e);
        }

        String host = dbProps.get(KEY_DB_HOST);
        String port = dbProps.get(KEY_DB_PORT);
        String dbName = dbProps.get(KEY_DB_NAME);
        String username = dbProps.get(KEY_DB_USERNAME);
        String password = dbProps.get(KEY_DB_PASSWORD);

        if (host == null || port == null || dbName == null || username == null || password == null)
        {
            throw new RuntimeException("Missing required DB properties in config file");
        }

        String url = JDBC_PREFIX + host + ":" + port + "/" + dbName;

        properties.setUrl(url);
        properties.setUsername(username);
        properties.setPassword(password);

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
