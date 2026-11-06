package com.makila.domain.port.out;

import com.makila.domain.model.ReactivoQuimico;
import java.util.List;
import java.util.Optional;

public interface ReactivoRepositoryPort {
    List<ReactivoQuimico> findAll();
    Optional<ReactivoQuimico> findById(Long id);
    List<ReactivoQuimico> findByNombreContainingIgnoreCase(String query);
    ReactivoQuimico save(ReactivoQuimico reactivo);
    void updateStock(Long id, Double nuevoStock);
    boolean existsById(Long id);
}

