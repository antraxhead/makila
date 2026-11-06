package com.makila.infrastructure.adapter.out.persistence;

import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcBiomaterialRepository implements BiomaterialRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBiomaterialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AleacionBiomaterial> rowMapper = (rs, rowNum) -> new AleacionBiomaterial(
            rs.getLong("id"),
            rs.getString("tipo_material"),
            rs.getString("nombre"),
            rs.getString("lote"),
            rs.getDouble("cantidad_disponible"),
            rs.getString("unidad"),
            rs.getString("especificaciones_tecnicas")
    );

    @Override
    public List<AleacionBiomaterial> findAll() {
        return jdbcTemplate.query("SELECT * FROM aleaciones_biomateriales ORDER BY id ASC", rowMapper);
    }

    @Override
    public Optional<AleacionBiomaterial> findById(Long id) {
        List<AleacionBiomaterial> results = jdbcTemplate.query(
                "SELECT * FROM aleaciones_biomateriales WHERE id = ?",
                rowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<AleacionBiomaterial> findByNombreOrTipoContainingIgnoreCase(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String[] words = query.trim().split("\\s+");
        StringBuilder sql = new StringBuilder("SELECT * FROM aleaciones_biomateriales WHERE (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(lower(nombre) LIKE ? OR lower(tipo_material) LIKE ? OR lower(lote) LIKE ?)");
            String p = "%" + words[i].toLowerCase() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
        }
        sql.append(") ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public Optional<AleacionBiomaterial> findByLote(String lote) {
        List<AleacionBiomaterial> results = jdbcTemplate.query(
                "SELECT * FROM aleaciones_biomateriales WHERE lote = ?",
                rowMapper,
                lote
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public AleacionBiomaterial save(AleacionBiomaterial b) {
        if (b.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO aleaciones_biomateriales (tipo_material, nombre, lote, cantidad_disponible, unidad, especificaciones_tecnicas) VALUES (?, ?, ?, ?, ?, ?)",
                    b.getTipoMaterial(), b.getNombre(), b.getLote(), b.getCantidadDisponible(), b.getUnidad(), b.getEspecificacionesTecnicas()
            );
            Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
            b.setId(id);
        } else {
            jdbcTemplate.update(
                    "UPDATE aleaciones_biomateriales SET tipo_material = ?, nombre = ?, lote = ?, cantidad_disponible = ?, unidad = ?, especificaciones_tecnicas = ? WHERE id = ?",
                    b.getTipoMaterial(), b.getNombre(), b.getLote(), b.getCantidadDisponible(), b.getUnidad(), b.getEspecificacionesTecnicas(), b.getId()
            );
        }
        return b;
    }

    @Override
    public void updateStock(Long id, Double nuevoStock) {
        jdbcTemplate.update("UPDATE aleaciones_biomateriales SET cantidad_disponible = ? WHERE id = ?", nuevoStock, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM aleaciones_biomateriales WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}
