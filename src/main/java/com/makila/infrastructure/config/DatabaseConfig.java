package com.makila.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public CommandLineRunner initDatabase(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("Inicializando base de datos SQLite para Makila...");
            try {
                // Verificar si las tablas ya existen
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='reactivos_quimicos'",
                        Integer.class
                );

                ResourceDatabasePopulator schemaPopulator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
                schemaPopulator.execute(dataSource);

                if (count == null || count == 0) {
                    log.info("Cargando datos semilla iniciales de laboratorio biomédico...");
                    ResourceDatabasePopulator dataPopulator = new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
                    dataPopulator.execute(dataSource);
                }
                log.info("Base de datos SQLite inicializada exitosamente.");
            } catch (Exception e) {
                log.error("Error al inicializar esquema SQLite: {}", e.getMessage(), e);
            }
        };
    }
}

