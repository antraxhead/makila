package com.makila.infrastructure.adapter.in.web;

import com.makila.application.dto.MovimientoRequestDTO;
import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.MovimientoInventario;
import com.makila.domain.port.in.GestionMovimientosPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final GestionMovimientosPort movimientosPort;

    public MovimientoController(GestionMovimientosPort movimientosPort) {
        this.movimientosPort = movimientosPort;
    }

    @GetMapping
    public ResponseEntity<List<MovimientoInventario>> listarHistorial(
            @RequestParam(value = "categoria", required = false) CategoriaMaterial categoria,
            @RequestParam(value = "itemId", required = false) Long itemId) {

        if (categoria != null && itemId != null) {
            return ResponseEntity.ok(movimientosPort.listarPorCategoriaYItem(categoria, itemId));
        }
        return ResponseEntity.ok(movimientosPort.listarHistorial());
    }

    @PostMapping
    public ResponseEntity<MovimientoInventario> registrarMovimiento(@Valid @RequestBody MovimientoRequestDTO dto) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setTipoMovimiento(dto.getTipoMovimiento());
        mov.setCategoria(dto.getCategoria());
        mov.setItemId(dto.getItemId());
        mov.setCantidad(dto.getCantidad());
        mov.setUnidad(dto.getUnidad());
        mov.setExperimentoOLote(dto.getExperimentoOLote());
        mov.setUsuarioResponsable(dto.getUsuarioResponsable());
        mov.setNotas(dto.getNotas());

        MovimientoInventario creado = movimientosPort.registrarMovimiento(mov);
        return ResponseEntity.ok(creado);
    }
}

