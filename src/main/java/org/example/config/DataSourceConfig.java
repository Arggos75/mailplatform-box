package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configures the DataSource from environment variables.
 *
 * Priority order:
 * 1. DATABASE_URL  — e.g. postgresql://user:pass@host:5432/dbname
 *                       or jdbc:postgresql://host:5432/dbname  (already in JDBC format)
 * 2. PGHOST / PGPORT / PGDATABASE / PGUSER / PGPASSWORD  — individual Postgres env vars
 * 3. Defaults: localhost:5432/mailbox with no credentials.
 *
 * Spring Boot's DataSourceAutoConfiguration is excluded in application.properties so
 * this bean is the sole DataSource provider.  JdbcTemplate is still auto-configured
 * by JdbcTemplateAutoConfiguration which only requires a DataSource bean.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        HikariConfig config = new HikariConfig();

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            applyDatabaseUrl(config, databaseUrl);
        } else {
            applyPgEnvVars(config);
        }

        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Handles both formats:
     *  - postgresql://user:pass@host:port/db   (Railway / Render style)
     *  - jdbc:postgresql://host:port/db        (already in JDBC format)
     */
    private void applyDatabaseUrl(HikariConfig config, String databaseUrl) {
        String url = databaseUrl.trim();

        if (url.startsWith("jdbc:")) {
            // Already a valid JDBC URL — use as-is; credentials may be embedded.
            config.setJdbcUrl(url);
            return;
        }

        // Strip optional "postgres://" or "postgresql://" scheme prefix and parse.
        try {
            String normalized = url;
            if (normalized.startsWith("postgresql://")) {
                normalized = "http://" + normalized.substring("postgresql://".length());
            } else if (normalized.startsWith("postgres://")) {
                normalized = "http://" + normalized.substring("postgres://".length());
            }

            URI uri = new URI(normalized);

            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            // Path is "/dbname" — strip leading slash.
            String dbName = uri.getPath() != null ? uri.getPath().replaceFirst("^/", "") : "";

            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);

            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                int colonIdx = userInfo.indexOf(':');
                if (colonIdx > -1) {
                    config.setUsername(userInfo.substring(0, colonIdx));
                    config.setPassword(userInfo.substring(colonIdx + 1));
                } else {
                    config.setUsername(userInfo);
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Cannot parse DATABASE_URL: " + databaseUrl, e);
        }
    }

    /**
     * Falls back to individual PG* environment variables.
     */
    private void applyPgEnvVars(HikariConfig config) {
        String host = envOrDefault("PGHOST", "localhost");
        String port = envOrDefault("PGPORT", "5432");
        String db   = envOrDefault("PGDATABASE", "mailbox");

        config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + db);

        String user = System.getenv("PGUSER");
        String pass = System.getenv("PGPASSWORD");
        if (user != null) config.setUsername(user);
        if (pass != null) config.setPassword(pass);
    }

    private static String envOrDefault(String name, String defaultValue) {
        String val = System.getenv(name);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }
}
