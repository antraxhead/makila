package com.makila.infrastructure.adapter.in.web;

import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reactivos")
@CrossOrigin(origins = "*")
public class ReactivoController {

    private final CatalogoMaterialesPort catalogoPort;

    public ReactivoController(CatalogoMaterialesPort catalogoPort) {
        this.catalogoPort = catalogoPort;
    }

    @GetMapping
    public ResponseEntity<List<ReactivoQuimico>> listarReactivos(@RequestParam(value = "buscar", required = false) String buscar) {
        if (buscar != null && !buscar.isBlank()) {
            return ResponseEntity.ok(catalogoPort.buscarReactivos(buscar));
        }
        return ResponseEntity.ok(catalogoPort.listarReactivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReactivoQuimico> obtenerPorId(@PathVariable Long id) {
        return catalogoPort.obtenerReactivoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ReactivoQuimico> crearReactivo(@RequestBody ReactivoQuimico reactivo) {
        return ResponseEntity.ok(catalogoPort.guardarReactivo(reactivo));
    }
}

