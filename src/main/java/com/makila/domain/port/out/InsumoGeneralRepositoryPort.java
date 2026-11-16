package com.makila.domain.port.out;

import com.makila.domain.model.InsumoGeneral;

import java.util.List;
import java.util.Optional;

public interface InsumoGeneralRepositoryPort {
    List<InsumoGeneral> findAll();
    Optional<InsumoGeneral> findById(Long id);
    List<InsumoGeneral> findByNombreOrCategoriaContainingIgnoreCase(String query);
    InsumoGeneral save(InsumoGeneral insumo);
    void updateStock(Long id, Double nuevoStock);
    boolean existsById(Long id);
}
