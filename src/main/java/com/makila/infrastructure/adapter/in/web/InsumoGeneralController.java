package com.makila.infrastructure.adapter.in.web;

import com.makila.domain.model.InsumoGeneral;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
@CrossOrigin(origins = "*")
public class InsumoGeneralController {

    private final CatalogoMaterialesPort catalogoPort;

    public InsumoGeneralController(CatalogoMaterialesPort catalogoPort) {
        this.catalogoPort = catalogoPort;
    }

    @GetMapping
    public ResponseEntity<List<InsumoGeneral>> listarInsumos(@RequestParam(value = "buscar", required = false) String buscar) {
        if (buscar != null && !buscar.isBlank()) {
            return ResponseEntity.ok(catalogoPort.buscarInsumosGenerales(buscar));
        }
        return ResponseEntity.ok(catalogoPort.listarInsumosGenerales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsumoGeneral> obtenerPorId(@PathVariable Long id) {
        return catalogoPort.obtenerInsumoGeneralPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InsumoGeneral> crearInsumo(@RequestBody InsumoGeneral insumo) {
        return ResponseEntity.ok(catalogoPort.guardarInsumoGeneral(insumo));
    }
}
