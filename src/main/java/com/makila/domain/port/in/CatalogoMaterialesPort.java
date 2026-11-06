package com.makila.domain.port.in;

import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.ReactivoQuimico;
import java.util.List;
import java.util.Optional;

public interface CatalogoMaterialesPort {
    List<ReactivoQuimico> listarReactivos();
    Optional<ReactivoQuimico> obtenerReactivoPorId(Long id);
    List<ReactivoQuimico> buscarReactivos(String query);
    ReactivoQuimico guardarReactivo(ReactivoQuimico reactivo);

    List<AleacionBiomaterial> listarBiomateriales();
    Optional<AleacionBiomaterial> obtenerBiomaterialPorId(Long id);
    List<AleacionBiomaterial> buscarBiomateriales(String query);
    AleacionBiomaterial guardarBiomaterial(AleacionBiomaterial biomaterial);

    List<EquipoCaracterizacion> listarEquipos();
    Optional<EquipoCaracterizacion> obtenerEquipoPorId(Long id);
    List<EquipoCaracterizacion> buscarEquipos(String query);
    EquipoCaracterizacion guardarEquipo(EquipoCaracterizacion equipo);
}

