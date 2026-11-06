package com.makila.domain.port.out;

import com.makila.domain.model.AleacionBiomaterial;
import java.util.List;
import java.util.Optional;

public interface BiomaterialRepositoryPort {
    List<AleacionBiomaterial> findAll();
    Optional<AleacionBiomaterial> findById(Long id);
    List<AleacionBiomaterial> findByNombreOrTipoContainingIgnoreCase(String query);
    Optional<AleacionBiomaterial> findByLote(String lote);
    AleacionBiomaterial save(AleacionBiomaterial biomaterial);
    void updateStock(Long id, Double nuevoStock);
    boolean existsById(Long id);
}

