package com.foodapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {

    private static Connection connection;

    private DbConnection() {

    }

    public static Connection connect() {

        try {
            if (connection == null) {
                Properties properties = new Properties();

                InputStream inputStream = DbConnection.class.getClassLoader().getResourceAsStream("db.properties");

                properties.load(inputStream);

                String url = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");

                connection = DriverManager.getConnection(
                        url,
                        username,
                        password
                );

                System.out.println("Database connection established");

                return connection;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
}