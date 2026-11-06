package com.makila.application.service;

import com.makila.application.guardrail.SecurityGuardrails;
import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import com.makila.domain.port.out.EquipoRepositoryPort;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogoMaterialesService implements CatalogoMaterialesPort {

    private final ReactivoRepositoryPort reactivoRepo;
    private final BiomaterialRepositoryPort biomaterialRepo;
    private final EquipoRepositoryPort equipoRepo;
    private final SecurityGuardrails guardrails;

    public CatalogoMaterialesService(ReactivoRepositoryPort reactivoRepo, 
                                     BiomaterialRepositoryPort biomaterialRepo, 
                                     EquipoRepositoryPort equipoRepo, 
                                     SecurityGuardrails guardrails) {
        this.reactivoRepo = reactivoRepo;
        this.biomaterialRepo = biomaterialRepo;
        this.equipoRepo = equipoRepo;
        this.guardrails = guardrails;
    }

    @Override
    public List<ReactivoQuimico> listarReactivos() {
        return reactivoRepo.findAll();
    }

    @Override
    public Optional<ReactivoQuimico> obtenerReactivoPorId(Long id) {
        return reactivoRepo.findById(id);
    }

    @Override
    public List<ReactivoQuimico> buscarReactivos(String query) {
        guardrails.validarSeguridadEntrada(query);
        return reactivoRepo.findByNombreContainingIgnoreCase(guardrails.sanitizar(query));
    }

    @Override
    public ReactivoQuimico guardarReactivo(ReactivoQuimico reactivo) {
        guardrails.validarSeguridadEntrada(reactivo.getNombre());
        return reactivoRepo.save(reactivo);
    }

    @Override
    public List<AleacionBiomaterial> listarBiomateriales() {
        return biomaterialRepo.findAll();
    }

    @Override
    public Optional<AleacionBiomaterial> obtenerBiomaterialPorId(Long id) {
        return biomaterialRepo.findById(id);
    }

    @Override
    public List<AleacionBiomaterial> buscarBiomateriales(String query) {
        guardrails.validarSeguridadEntrada(query);
        return biomaterialRepo.findByNombreOrTipoContainingIgnoreCase(guardrails.sanitizar(query));
    }

    @Override
    public AleacionBiomaterial guardarBiomaterial(AleacionBiomaterial biomaterial) {
        guardrails.validarSeguridadEntrada(biomaterial.getNombre());
        return biomaterialRepo.save(biomaterial);
    }

    @Override
    public List<EquipoCaracterizacion> listarEquipos() {
        return equipoRepo.findAll();
    }

    @Override
    public Optional<EquipoCaracterizacion> obtenerEquipoPorId(Long id) {
        return equipoRepo.findById(id);
    }

    @Override
    public List<EquipoCaracterizacion> buscarEquipos(String query) {
        guardrails.validarSeguridadEntrada(query);
        return equipoRepo.findByNombreContainingIgnoreCase(guardrails.sanitizar(query));
    }

    @Override
    public EquipoCaracterizacion guardarEquipo(EquipoCaracterizacion equipo) {
        guardrails.validarSeguridadEntrada(equipo.getNombreEquipo());
        return equipoRepo.save(equipo);
    }
}

