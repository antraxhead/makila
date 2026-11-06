package com.makila.infrastructure.adapter.in.web;

import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biomateriales")
@CrossOrigin(origins = "*")
public class BiomaterialController {

    private final CatalogoMaterialesPort catalogoPort;

    public BiomaterialController(CatalogoMaterialesPort catalogoPort) {
        this.catalogoPort = catalogoPort;
    }

    @GetMapping
    public ResponseEntity<List<AleacionBiomaterial>> listarBiomateriales(@RequestParam(value = "buscar", required = false) String buscar) {
        if (buscar != null && !buscar.isBlank()) {
            return ResponseEntity.ok(catalogoPort.buscarBiomateriales(buscar));
        }
        return ResponseEntity.ok(catalogoPort.listarBiomateriales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AleacionBiomaterial> obtenerPorId(@PathVariable Long id) {
        return catalogoPort.obtenerBiomaterialPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AleacionBiomaterial> crearBiomaterial(@RequestBody AleacionBiomaterial biomaterial) {
        return ResponseEntity.ok(catalogoPort.guardarBiomaterial(biomaterial));
    }
}

