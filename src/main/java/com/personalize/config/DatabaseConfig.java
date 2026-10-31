package com.personalize.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url:${DATABASE_URL:jdbc:postgresql://localhost:5432/personalizedb}}")
    private String rawUrl;

    @Value("${spring.datasource.username:${DATABASE_USERNAME:postgres}}")
    private String username;

    @Value("${spring.datasource.password:${DATABASE_PASSWORD:password}}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    @Profile("!test")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = rawUrl;
        String user = username;
        String pass = password;

        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                // Parse postgresql:// or postgres:// URI (e.g. from Render / Supabase)
                URI uri = new URI(rawUrl.replace("postgres://", "http://").replace("postgresql://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "postgres";

                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    user = parts[0];
                    pass = parts[1];
                }

                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                    jdbcUrl += "?" + uri.getQuery();
                } else if (!jdbcUrl.contains("sslmode=")) {
                    jdbcUrl += "?sslmode=require";
                }
            } catch (Exception e) {
                log.warn("Could not parse PostgreSQL URI, prefixing with jdbc: {}", e.getMessage());
                if (!rawUrl.startsWith("jdbc:")) {
                    jdbcUrl = "jdbc:" + rawUrl;
                }
            }
        } else if (rawUrl != null && rawUrl.startsWith("jdbc:postgresql:") && !rawUrl.contains("sslmode=")) {
            if (rawUrl.contains("supabase.co") || rawUrl.contains("render.com") || rawUrl.contains("neon.tech")) {
                jdbcUrl = rawUrl.contains("?") ? rawUrl + "&sslmode=require" : rawUrl + "?sslmode=require";
            }
        }

        String maskedUrl = (jdbcUrl != null) ? jdbcUrl.replaceAll(":[^/@]+@", ":***@") : "null";
        log.info("Configuring HikariDataSource with JDBC URL: {} and username: {}", maskedUrl, user);

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName(driverClassName);

        // Connection pool optimization for cloud databases like Supabase
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(600000); // 10 minutes

        return new HikariDataSource(config);
    }
}

