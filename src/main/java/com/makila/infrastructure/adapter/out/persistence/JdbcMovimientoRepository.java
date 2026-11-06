package com.makila.infrastructure.adapter.out.persistence;

import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.MovimientoInventario;
import com.makila.domain.model.TipoMovimiento;
import com.makila.domain.port.out.MovimientoRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcMovimientoRepository implements MovimientoRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMovimientoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MovimientoInventario> rowMapper = (rs, rowNum) -> new MovimientoInventario(
            rs.getLong("id"),
            TipoMovimiento.valueOf(rs.getString("tipo_movimiento")),
            CategoriaMaterial.valueOf(rs.getString("categoria")),
            rs.getLong("item_id"),
            rs.getString("item_nombre"),
            rs.getDouble("cantidad"),
            rs.getString("unidad"),
            rs.getString("experimento_o_lote"),
            rs.getString("usuario_responsable"),
            rs.getString("fecha_hora"),
            rs.getString("notas")
    );

    @Override
    public MovimientoInventario save(MovimientoInventario m) {
        jdbcTemplate.update(
                "INSERT INTO historial_movimientos (tipo_movimiento, categoria, item_id, item_nombre, cantidad, unidad, experimento_o_lote, usuario_responsable, fecha_hora, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                m.getTipoMovimiento().name(), m.getCategoria().name(), m.getItemId(), m.getItemNombre(),
                m.getCantidad(), m.getUnidad(), m.getExperimentoOLote(), m.getUsuarioResponsable(),
                m.getFechaHora(), m.getNotas()
        );
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        m.setId(id);
        return m;
    }

    @Override
    public List<MovimientoInventario> findAll() {
        return jdbcTemplate.query("SELECT * FROM historial_movimientos ORDER BY id DESC", rowMapper);
    }

    @Override
    public List<MovimientoInventario> findByCategoriaAndItemId(CategoriaMaterial categoria, Long itemId) {
        return jdbcTemplate.query(
                "SELECT * FROM historial_movimientos WHERE categoria = ? AND item_id = ? ORDER BY id DESC",
                rowMapper,
                categoria.name(), itemId
        );
    }
}

