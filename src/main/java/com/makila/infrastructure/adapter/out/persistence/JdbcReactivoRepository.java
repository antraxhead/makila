package com.makila.infrastructure.adapter.out.persistence;

import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReactivoRepository implements ReactivoRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReactivoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReactivoQuimico> rowMapper = (rs, rowNum) -> new ReactivoQuimico(
            rs.getLong("id"),
            rs.getString("nombre"),
            rs.getString("formula"),
            rs.getDouble("cantidad"),
            rs.getString("unidad"),
            rs.getString("ubicacion"),
            rs.getString("fecha_caducidad"),
            rs.getString("estado_seguridad"),
            rs.getObject("temp_max_celsius") != null ? rs.getDouble("temp_max_celsius") : null,
            rs.getInt("requiere_aislamiento_luz") == 1,
            rs.getString("msds_resumen")
    );

    @Override
    public List<ReactivoQuimico> findAll() {
        return jdbcTemplate.query("SELECT * FROM reactivos_quimicos ORDER BY id ASC", rowMapper);
    }

    @Override
    public Optional<ReactivoQuimico> findById(Long id) {
        List<ReactivoQuimico> results = jdbcTemplate.query(
                "SELECT * FROM reactivos_quimicos WHERE id = ?",
                rowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<ReactivoQuimico> findByNombreContainingIgnoreCase(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String[] words = query.trim().split("\\s+");
        StringBuilder sql = new StringBuilder("SELECT * FROM reactivos_quimicos WHERE (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(lower(nombre) LIKE ? OR lower(formula) LIKE ? OR lower(estado_seguridad) LIKE ?)");
            String p = "%" + words[i].toLowerCase() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
        }
        sql.append(") ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public ReactivoQuimico save(ReactivoQuimico r) {
        if (r.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO reactivos_quimicos (nombre, formula, cantidad, unidad, ubicacion, fecha_caducidad, estado_seguridad, temp_max_celsius, requiere_aislamiento_luz, msds_resumen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    r.getNombre(), r.getFormula(), r.getCantidad(), r.getUnidad(), r.getUbicacion(),
                    r.getFechaCaducidad(), r.getEstadoSeguridad(), r.getTempMaxCelsius(),
                    (r.getRequiereAislamientoLuz() != null && r.getRequiereAislamientoLuz()) ? 1 : 0,
                    r.getMsdsResumen()
            );
            Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
            r.setId(id);
        } else {
            jdbcTemplate.update(
                    "UPDATE reactivos_quimicos SET nombre = ?, formula = ?, cantidad = ?, unidad = ?, ubicacion = ?, fecha_caducidad = ?, estado_seguridad = ?, temp_max_celsius = ?, requiere_aislamiento_luz = ?, msds_resumen = ? WHERE id = ?",
                    r.getNombre(), r.getFormula(), r.getCantidad(), r.getUnidad(), r.getUbicacion(),
                    r.getFechaCaducidad(), r.getEstadoSeguridad(), r.getTempMaxCelsius(),
                    (r.getRequiereAislamientoLuz() != null && r.getRequiereAislamientoLuz()) ? 1 : 0,
                    r.getMsdsResumen(), r.getId()
            );
        }
        return r;
    }

    @Override
    public void updateStock(Long id, Double nuevoStock) {
        jdbcTemplate.update("UPDATE reactivos_quimicos SET cantidad = ? WHERE id = ?", nuevoStock, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reactivos_quimicos WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}
