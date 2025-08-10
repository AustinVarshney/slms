package com.java.slms.config;

import com.java.slms.util.ConfigUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;

@Configuration
public class DbConnectionConfig
{

    private static final String JDBC_PREFIX = "jdbc:mysql://";

    @Bean
    public DataSource dataSource(DataSourceProperties properties)
    {

        String host = ConfigUtil.getRequired("db.host");
        String port = ConfigUtil.getRequired("db.port");
        String dbName = ConfigUtil.getRequired("db.name");
        String username = ConfigUtil.getRequired("db.username");
        String password = ConfigUtil.getRequired("db.password");

        String url = JDBC_PREFIX + host + ":" + port + "/" + dbName;

        properties.setUrl(url);
        properties.setUsername(username);
        properties.setPassword(password);

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
