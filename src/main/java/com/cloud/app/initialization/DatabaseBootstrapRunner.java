package com.cloud.app.initialization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseBootstrapRunner implements CommandLineRunner {
    @Value("${setup.datasource.url}") // This should point to a default database like 'postgres' for initial setup
    private String setupDbUrl;

    @Value("${setup.datasource.username}") // A user with permissions to create databases and roles
    private String setupUser;

    @Value("${setup.datasource.password}")
    private String setupPassword;

    @Value("${webapp.target-database.name}")
    private String dbName;

    @Value("${webapp.target-database.user}")
    private String dbUser;

    @Value("${webapp.target-database.password}")
    private String dbPassword;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = DriverManager.getConnection(setupDbUrl, setupUser, setupPassword);
             Statement stmt = conn.createStatement()) {

            // Check if the database exists
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname='" + dbName + "'");
            if (!rs.next()) {
                // Create the database if it does not exist
                stmt.execute("CREATE DATABASE " + dbName);

                // Create the user if not exists, or ensure it has the correct password
                stmt.execute("DO $$ BEGIN " +
                        "IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '" + dbUser + "') THEN " +
                        "CREATE ROLE " + dbUser + " LOGIN PASSWORD '" + dbPassword + "' CREATEDB; " +
                        "END IF; " +
                        "END $$;");

                // Grant all privileges on database to the user and set them as the owner
                stmt.execute("ALTER DATABASE " + dbName + " OWNER TO " + dbUser);

                System.out.println("Database " + dbName + " created and ownership granted successfully.");
            } else {
                System.out.println("Database " + dbName + " already exists. No action was taken.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
    }
}
