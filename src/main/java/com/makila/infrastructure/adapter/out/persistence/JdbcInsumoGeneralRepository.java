package com.makila.infrastructure.adapter.out.persistence;

import com.makila.domain.model.InsumoGeneral;
import com.makila.domain.port.out.InsumoGeneralRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcInsumoGeneralRepository implements InsumoGeneralRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcInsumoGeneralRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<InsumoGeneral> rowMapper = (rs, rowNum) -> new InsumoGeneral(
            rs.getLong("id"),
            rs.getString("categoria_insumo"),
            rs.getString("nombre"),
            rs.getDouble("cantidad"),
            rs.getString("unidad"),
            rs.getString("ubicacion"),
            rs.getObject("stock_minimo") != null ? rs.getDouble("stock_minimo") : 0.0,
            rs.getString("especificaciones")
    );

    @Override
    public List<InsumoGeneral> findAll() {
        return jdbcTemplate.query("SELECT * FROM insumos_generales ORDER BY id ASC", rowMapper);
    }

    @Override
    public Optional<InsumoGeneral> findById(Long id) {
        List<InsumoGeneral> results = jdbcTemplate.query(
                "SELECT * FROM insumos_generales WHERE id = ?",
                rowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<InsumoGeneral> findByNombreOrCategoriaContainingIgnoreCase(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String[] words = query.trim().split("\\s+");
        StringBuilder sql = new StringBuilder("SELECT * FROM insumos_generales WHERE (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sql.append(" AND ");
            sql.append("(lower(nombre) LIKE ? OR lower(categoria_insumo) LIKE ? OR lower(especificaciones) LIKE ?)");
            String p = "%" + words[i].toLowerCase() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
        }
        sql.append(") ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public InsumoGeneral save(InsumoGeneral insumo) {
        if (insumo.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO insumos_generales (categoria_insumo, nombre, cantidad, unidad, ubicacion, stock_minimo, especificaciones) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    insumo.getCategoriaInsumo(), insumo.getNombre(), insumo.getCantidad(), insumo.getUnidad(),
                    insumo.getUbicacion(), insumo.getStockMinimo() != null ? insumo.getStockMinimo() : 0.0,
                    insumo.getEspecificaciones()
            );
            Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
            insumo.setId(id);
        } else {
            jdbcTemplate.update(
                    "UPDATE insumos_generales SET categoria_insumo = ?, nombre = ?, cantidad = ?, unidad = ?, ubicacion = ?, stock_minimo = ?, especificaciones = ? WHERE id = ?",
                    insumo.getCategoriaInsumo(), insumo.getNombre(), insumo.getCantidad(), insumo.getUnidad(),
                    insumo.getUbicacion(), insumo.getStockMinimo() != null ? insumo.getStockMinimo() : 0.0,
                    insumo.getEspecificaciones(), insumo.getId()
            );
        }
        return insumo;
    }

    @Override
    public void updateStock(Long id, Double nuevoStock) {
        jdbcTemplate.update("UPDATE insumos_generales SET cantidad = ? WHERE id = ?", nuevoStock, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM insumos_generales WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}
