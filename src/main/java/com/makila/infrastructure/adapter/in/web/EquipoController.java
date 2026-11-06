package com.makila.infrastructure.adapter.in.web;

import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@CrossOrigin(origins = "*")
public class EquipoController {

    private final CatalogoMaterialesPort catalogoPort;

    public EquipoController(CatalogoMaterialesPort catalogoPort) {
        this.catalogoPort = catalogoPort;
    }

    @GetMapping
    public ResponseEntity<List<EquipoCaracterizacion>> listarEquipos(@RequestParam(value = "buscar", required = false) String buscar) {
        if (buscar != null && !buscar.isBlank()) {
            return ResponseEntity.ok(catalogoPort.buscarEquipos(buscar));
        }
        return ResponseEntity.ok(catalogoPort.listarEquipos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoCaracterizacion> obtenerPorId(@PathVariable Long id) {
        return catalogoPort.obtenerEquipoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipoCaracterizacion> crearEquipo(@RequestBody EquipoCaracterizacion equipo) {
        return ResponseEntity.ok(catalogoPort.guardarEquipo(equipo));
    }
}

