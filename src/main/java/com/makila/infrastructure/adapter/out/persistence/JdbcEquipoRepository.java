package com.makila.infrastructure.adapter.out.persistence;

import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.EstadoEquipo;
import com.makila.domain.port.out.EquipoRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEquipoRepository implements EquipoRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEquipoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EquipoCaracterizacion> rowMapper = (rs, rowNum) -> new EquipoCaracterizacion(
            rs.getLong("id"),
            rs.getString("nombre_equipo"),
            EstadoEquipo.valueOf(rs.getString("estado_actual")),
            rs.getString("fecha_proxima_calibracion"),
            rs.getString("ubicacion"),
            rs.getString("responsable")
    );

    @Override
    public List<EquipoCaracterizacion> findAll() {
        return jdbcTemplate.query("SELECT * FROM equipos_caracterizacion ORDER BY id ASC", rowMapper);
    }

    @Override
    public Optional<EquipoCaracterizacion> findById(Long id) {
        List<EquipoCaracterizacion> results = jdbcTemplate.query(
                "SELECT * FROM equipos_caracterizacion WHERE id = ?",
                rowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<EquipoCaracterizacion> findByNombreContainingIgnoreCase(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(
                "SELECT * FROM equipos_caracterizacion WHERE lower(nombre_equipo) LIKE ?",
                rowMapper,
                pattern
        );
    }

    @Override
    public List<EquipoCaracterizacion> findByEstado(EstadoEquipo estado) {
        return jdbcTemplate.query(
                "SELECT * FROM equipos_caracterizacion WHERE estado_actual = ?",
                rowMapper,
                estado.name()
        );
    }

    @Override
    public EquipoCaracterizacion save(EquipoCaracterizacion e) {
        if (e.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO equipos_caracterizacion (nombre_equipo, estado_actual, fecha_proxima_calibracion, ubicacion, responsable) VALUES (?, ?, ?, ?, ?)",
                    e.getNombreEquipo(), e.getEstadoActual().name(), e.getFechaProximaCalibracion(), e.getUbicacion(), e.getResponsable()
            );
            Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
            e.setId(id);
        } else {
            jdbcTemplate.update(
                    "UPDATE equipos_caracterizacion SET nombre_equipo = ?, estado_actual = ?, fecha_proxima_calibracion = ?, ubicacion = ?, responsable = ? WHERE id = ?",
                    e.getNombreEquipo(), e.getEstadoActual().name(), e.getFechaProximaCalibracion(), e.getUbicacion(), e.getResponsable(), e.getId()
            );
        }
        return e;
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM equipos_caracterizacion WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}

