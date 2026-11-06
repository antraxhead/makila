package com.makila.domain.port.out;

import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.EstadoEquipo;
import java.util.List;
import java.util.Optional;

public interface EquipoRepositoryPort {
    List<EquipoCaracterizacion> findAll();
    Optional<EquipoCaracterizacion> findById(Long id);
    List<EquipoCaracterizacion> findByNombreContainingIgnoreCase(String query);
    List<EquipoCaracterizacion> findByEstado(EstadoEquipo estado);
    EquipoCaracterizacion save(EquipoCaracterizacion equipo);
    boolean existsById(Long id);
}

