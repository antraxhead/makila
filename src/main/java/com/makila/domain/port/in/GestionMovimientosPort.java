package com.makila.domain.port.in;

import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.MovimientoInventario;
import java.util.List;

public interface GestionMovimientosPort {
    MovimientoInventario registrarMovimiento(MovimientoInventario movimiento);
    List<MovimientoInventario> listarHistorial();
    List<MovimientoInventario> listarPorCategoriaYItem(CategoriaMaterial categoria, Long itemId);
}

