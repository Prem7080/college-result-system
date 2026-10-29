package com.entc.resultportal.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Render usually provides a single {@code DATABASE_URL} (internal or external).
 * Alternatively set {@code PGHOST}, {@code PGPORT}, {@code PGUSER}, {@code PGPASSWORD}, {@code PGDATABASE}.
 */
@Configuration
@Profile("prod")
public class ProdDataSourceConfiguration {

    @Bean
    public DataSource prodDataSource(
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${PGHOST:}") String pgHost,
            @Value("${PGPORT:5432}") String pgPort,
            @Value("${PGUSER:}") String pgUser,
            @Value("${PGPASSWORD:}") String pgPassword,
            @Value("${PGDATABASE:}") String pgDatabase) {

        if (StringUtils.hasText(databaseUrl)) {
            return fromDatabaseUrl(databaseUrl);
        }
        if (!StringUtils.hasText(pgHost) || !StringUtils.hasText(pgUser) || !StringUtils.hasText(pgDatabase)) {
            throw new IllegalStateException(
                    "Production database not configured: set DATABASE_URL (recommended on Render) or PGHOST, PGUSER, PGPASSWORD, PGDATABASE.");
        }
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase + "?sslmode=require");
        ds.setUsername(pgUser);
        ds.setPassword(StringUtils.hasText(pgPassword) ? pgPassword : "");
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    private static DataSource fromDatabaseUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String userInfo = uri.getUserInfo();
        String user = "";
        String password = "";
        if (StringUtils.hasText(userInfo)) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
                password = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);
            } else {
                user = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
            }
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String host = uri.getHost();
        String path = uri.getPath();
        if (path == null || path.length() <= 1) {
            throw new IllegalArgumentException("DATABASE_URL must include a database name path, e.g. /mydb");
        }
        String dbName = path.substring(1);
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=require";

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
}
