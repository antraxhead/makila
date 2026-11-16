package com.makila.application.service;

import com.makila.application.guardrail.SecurityGuardrails;
import com.makila.domain.exception.OperacionInvalidaException;
import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.InsumoGeneral;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import com.makila.domain.port.out.EquipoRepositoryPort;
import com.makila.domain.port.out.InsumoGeneralRepositoryPort;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogoMaterialesService implements CatalogoMaterialesPort {

    private final ReactivoRepositoryPort reactivoRepo;
    private final BiomaterialRepositoryPort biomaterialRepo;
    private final EquipoRepositoryPort equipoRepo;
    private final InsumoGeneralRepositoryPort insumoRepo;
    private final SecurityGuardrails guardrails;

    public CatalogoMaterialesService(ReactivoRepositoryPort reactivoRepo,
                                     BiomaterialRepositoryPort biomaterialRepo,
                                     EquipoRepositoryPort equipoRepo,
                                     SecurityGuardrails guardrails) {
        this(reactivoRepo, biomaterialRepo, equipoRepo, null, guardrails);
    }

    @Autowired
    public CatalogoMaterialesService(ReactivoRepositoryPort reactivoRepo,
                                     BiomaterialRepositoryPort biomaterialRepo,
                                     EquipoRepositoryPort equipoRepo,
                                     InsumoGeneralRepositoryPort insumoRepo,
                                     SecurityGuardrails guardrails) {
        this.reactivoRepo = reactivoRepo;
        this.biomaterialRepo = biomaterialRepo;
        this.equipoRepo = equipoRepo;
        this.insumoRepo = insumoRepo;
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
        guardrails.validarSeguridadEntrada(reactivo.getFormula());
        guardrails.validarSeguridadEntrada(reactivo.getUbicacion());
        guardrails.validarSeguridadEntrada(reactivo.getEstadoSeguridad());
        guardrails.validarSeguridadEntrada(reactivo.getMsdsResumen());
        if (reactivo.getCantidad() == null || reactivo.getCantidad() < 0) {
            throw new OperacionInvalidaException("La cantidad inicial del reactivo no puede ser negativa.");
        }
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
        guardrails.validarSeguridadEntrada(biomaterial.getTipoMaterial());
        guardrails.validarSeguridadEntrada(biomaterial.getLote());
        guardrails.validarSeguridadEntrada(biomaterial.getEspecificacionesTecnicas());
        if (biomaterial.getCantidadDisponible() == null || biomaterial.getCantidadDisponible() < 0) {
            throw new OperacionInvalidaException("La cantidad disponible del biomaterial no puede ser negativa.");
        }
        if (biomaterial.getId() == null && biomaterialRepo.findByLote(biomaterial.getLote()).isPresent()) {
            throw new OperacionInvalidaException("Ya existe un biomaterial registrado con el lote: " + biomaterial.getLote());
        }
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
        guardrails.validarSeguridadEntrada(equipo.getUbicacion());
        guardrails.validarSeguridadEntrada(equipo.getResponsable());
        return equipoRepo.save(equipo);
    }

    @Override
    public List<InsumoGeneral> listarInsumosGenerales() {
        return insumoRepo != null ? insumoRepo.findAll() : List.of();
    }

    @Override
    public Optional<InsumoGeneral> obtenerInsumoGeneralPorId(Long id) {
        return insumoRepo != null ? insumoRepo.findById(id) : Optional.empty();
    }

    @Override
    public List<InsumoGeneral> buscarInsumosGenerales(String query) {
        guardrails.validarSeguridadEntrada(query);
        return insumoRepo != null
                ? insumoRepo.findByNombreOrCategoriaContainingIgnoreCase(guardrails.sanitizar(query))
                : List.of();
    }

    @Override
    public InsumoGeneral guardarInsumoGeneral(InsumoGeneral insumo) {
        if (insumoRepo == null) {
            throw new OperacionInvalidaException("Repositorio de insumos generales no disponible.");
        }
        guardrails.validarSeguridadEntrada(insumo.getNombre());
        guardrails.validarSeguridadEntrada(insumo.getCategoriaInsumo());
        guardrails.validarSeguridadEntrada(insumo.getUbicacion());
        guardrails.validarSeguridadEntrada(insumo.getEspecificaciones());
        if (insumo.getCantidad() == null || insumo.getCantidad() < 0) {
            throw new OperacionInvalidaException("La cantidad del insumo general no puede ser negativa.");
        }
        if (insumo.getStockMinimo() != null && insumo.getStockMinimo() < 0) {
            throw new OperacionInvalidaException("El stock mínimo no puede ser negativo.");
        }
        return insumoRepo.save(insumo);
    }
}
