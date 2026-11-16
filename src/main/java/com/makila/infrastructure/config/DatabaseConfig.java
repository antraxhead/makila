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
                } else {
                    migrarTablaHistorialSiEsNecesario(jdbcTemplate);
                    sembrarInsumosGeneralesSiVacio(jdbcTemplate);
                }
                log.info("Base de datos SQLite inicializada exitosamente.");
            } catch (Exception e) {
                log.error("Error al inicializar esquema SQLite: {}", e.getMessage(), e);
            }
        };
    }

    private void migrarTablaHistorialSiEsNecesario(JdbcTemplate jdbcTemplate) {
        String ddl = jdbcTemplate.queryForObject(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name='historial_movimientos'",
                String.class
        );
        if (ddl != null && !ddl.contains("INSUMO_GENERAL")) {
            log.info("Actualizando restricción CHECK de historial_movimientos para soportar INSUMO_GENERAL...");
            jdbcTemplate.execute("CREATE TABLE historial_movimientos_v2 (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "tipo_movimiento TEXT NOT NULL CHECK(tipo_movimiento IN ('ENTRADA', 'SALIDA', 'CONSUMO')), " +
                    "categoria TEXT NOT NULL CHECK(categoria IN ('REACTIVO', 'BIOMATERIAL', 'EQUIPO', 'INSUMO_GENERAL')), " +
                    "item_id INTEGER NOT NULL, " +
                    "item_nombre TEXT NOT NULL, " +
                    "cantidad REAL NOT NULL CHECK(cantidad > 0), " +
                    "unidad TEXT NOT NULL, " +
                    "experimento_o_lote TEXT NOT NULL, " +
                    "usuario_responsable TEXT NOT NULL, " +
                    "fecha_hora TEXT NOT NULL, " +
                    "notas TEXT)");
            jdbcTemplate.execute("INSERT INTO historial_movimientos_v2 SELECT id, tipo_movimiento, categoria, item_id, item_nombre, cantidad, unidad, experimento_o_lote, usuario_responsable, fecha_hora, notas FROM historial_movimientos");
            jdbcTemplate.execute("DROP TABLE historial_movimientos");
            jdbcTemplate.execute("ALTER TABLE historial_movimientos_v2 RENAME TO historial_movimientos");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON historial_movimientos(fecha_hora)");
        }
    }

    private void sembrarInsumosGeneralesSiVacio(JdbcTemplate jdbcTemplate) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM insumos_generales", Integer.class);
        if (count != null && count == 0) {
            log.info("Cargando datos iniciales de insumos generales y EPP...");
            jdbcTemplate.execute("INSERT OR IGNORE INTO insumos_generales (id, categoria_insumo, nombre, cantidad, unidad, ubicacion, stock_minimo, especificaciones) VALUES " +
                    "(1, 'EPP', 'Guantes de Nitrilo sin Polvo (Talla M/L)', 400.0, 'pares', 'Estación de Bioseguridad E-01', 100.0, 'Grado médico AQL 1.5, resistentes a solventes orgánicos y fluidos biológicos.')," +
                    "(2, 'EPP', 'Cofias Desechables Plisadas', 350.0, 'piezas', 'Estación de Bioseguridad E-01', 80.0, 'Tela no tejida polipropileno 14 g/m2, ajuste elástico estéril para sala limpia.')," +
                    "(3, 'EPP', 'Batas de Laboratorio Antiestáticas', 45.0, 'piezas', 'Vestidor Sala Blanca V-01', 15.0, 'Poliéster con fibra de carbono ESD, puños ajustables para manejo de nanomateriales.')," +
                    "(4, 'Cristalería', 'Matraces Erlenmeyer Borosilicato 250 mL', 32.0, 'piezas', 'Gabinete de Cristalería C-02', 10.0, 'Vidrio borosilicato 3.3 graduado, resistente a choque térmico y autoclave.')," +
                    "(5, 'Preparación', 'Lumina / Alúmina de Pulido (0.05 µm)', 950.0, 'mL', 'Área de Metalografía M-01', 250.0, 'Suspensión abrasiva ultrafina para acabado espejo de probetas Ti-Zr y Nitinol.')," +
                    "(6, 'Limpieza Crítica', 'Paños Libres de Pelusa (Cleanroom Wipes)', 500.0, 'hojas', 'Sala Blanca Nanoescala N-02', 120.0, 'Microfibra sellada por láser 22x22 cm para limpieza óptica y AFM.')");
        }
    }
}
