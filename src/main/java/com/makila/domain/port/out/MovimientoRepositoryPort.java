package com.makila.domain.port.out;

import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.MovimientoInventario;
import java.util.List;

public interface MovimientoRepositoryPort {
    MovimientoInventario save(MovimientoInventario movimiento);
    List<MovimientoInventario> findAll();
    List<MovimientoInventario> findByCategoriaAndItemId(CategoriaMaterial categoria, Long itemId);
}

