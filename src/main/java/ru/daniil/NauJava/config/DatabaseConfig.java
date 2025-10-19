package ru.daniil.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        try {
            Properties props = loadCredentials();
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.pass");

            org.springframework.jdbc.datasource.DriverManagerDataSource dataSource =
                    new org.springframework.jdbc.datasource.DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl("jdbc:postgresql://localhost:5432/calorie_tracker");
            dataSource.setUsername(user);
            dataSource.setPassword(pass);

            return dataSource;

        } catch (Exception e) {
            throw new RuntimeException("Failed to configure DataSource", e);
        }
    }

    private Properties loadCredentials() throws Exception {
        Properties props = new Properties();
        java.nio.file.Path path = java.nio.file.Paths.get("db.cache");

        if (java.nio.file.Files.exists(path)) {
            String encoded = java.nio.file.Files.readString(path);
            String decoded = new String(java.util.Base64.getDecoder().decode(encoded));
            props.load(new java.io.StringReader(decoded));
        } else {
            props.setProperty("db.user", prompt("Enter DB username: "));
            props.setProperty("db.pass", prompt("Enter DB password: "));
            String serialized = serialize(props);
            String encoded = java.util.Base64.getEncoder().encodeToString(serialized.getBytes());
            java.nio.file.Files.writeString(path, encoded);
        }

        return props;
    }

    private String prompt(String message) {
        System.out.print(message);
        return new java.util.Scanner(System.in).nextLine();
    }

    private String serialize(Properties props) throws Exception {
        java.io.StringWriter writer = new java.io.StringWriter();
        props.store(writer, null);
        return writer.toString();
    }
}