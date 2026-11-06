package com.makila.application.service;

import com.makila.application.dto.AlertaSeguridadDTO;
import com.makila.application.dto.DashboardStatsDTO;
import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.EstadoEquipo;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.port.in.AuditoriaSeguridadPort;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import com.makila.domain.port.out.EquipoRepositoryPort;
import com.makila.domain.port.out.MovimientoRepositoryPort;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuditoriaSeguridadService implements AuditoriaSeguridadPort {

    private final ReactivoRepositoryPort reactivoRepo;
    private final BiomaterialRepositoryPort biomaterialRepo;
    private final EquipoRepositoryPort equipoRepo;
    private final MovimientoRepositoryPort movimientoRepo;

    public AuditoriaSeguridadService(ReactivoRepositoryPort reactivoRepo,
                                   BiomaterialRepositoryPort biomaterialRepo,
                                   EquipoRepositoryPort equipoRepo,
                                   MovimientoRepositoryPort movimientoRepo) {
        this.reactivoRepo = reactivoRepo;
        this.biomaterialRepo = biomaterialRepo;
        this.equipoRepo = equipoRepo;
        this.movimientoRepo = movimientoRepo;
    }

    @Override
    public List<ReactivoQuimico> consultarReactivosPorCaducar(int diasUmbral) {
        return reactivoRepo.findAll().stream()
                .filter(r -> r.estaPorCaducar(diasUmbral))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReactivoQuimico> consultarReactivosCondicionesCriticas() {
        return reactivoRepo.findAll().stream()
                .filter(r -> (r.getTempMaxCelsius() != null && r.getTempMaxCelsius() <= 30.0)
                        || Boolean.TRUE.equals(r.getRequiereAislamientoLuz()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipoCaracterizacion> consultarEquiposPendientesCalibracion(int diasUmbral) {
        return equipoRepo.findAll().stream()
                .filter(e -> e.requiereCalibracionPronto(diasUmbral) || e.getEstadoActual() != EstadoEquipo.OPERATIVO)
                .collect(Collectors.toList());
    }

    public List<AlertaSeguridadDTO> generarAlertasActivas() {
        List<AlertaSeguridadDTO> alertas = new ArrayList<>();

        // Revisar reactivos con condiciones críticas de conservación
        for (ReactivoQuimico r : consultarReactivosCondicionesCriticas()) {
            if (r.getTempMaxCelsius() != null && r.getTempMaxCelsius() <= 30.0) {
                alertas.add(new AlertaSeguridadDTO(
                        AlertaSeguridadDTO.NivelAlerta.CRITICO,
                        "Control Térmico Estricto: " + r.getNombre(),
                        r.getEstadoSeguridad(),
                        "Mantener en refrigeración continua por debajo de " + r.getTempMaxCelsius() + "°C para prevenir degradación.",
                        "REACTIVO",
                        r.getId()
                ));
            }
            if (Boolean.TRUE.equals(r.getRequiereAislamientoLuz())) {
                alertas.add(new AlertaSeguridadDTO(
                        AlertaSeguridadDTO.NivelAlerta.CRITICO,
                        "Fotosensibilidad Crítica: " + r.getNombre(),
                        r.getEstadoSeguridad(),
                        "Almacenar en contenedor ámbar en gabinete oscuro UV.",
                        "REACTIVO",
                        r.getId()
                ));
            }
        }

        // Revisar reactivos por caducar (próximos 60 días)
        for (ReactivoQuimico r : consultarReactivosPorCaducar(60)) {
            alertas.add(new AlertaSeguridadDTO(
                    AlertaSeguridadDTO.NivelAlerta.ADVERTENCIA,
                    "Próximo a Caducar: " + r.getNombre(),
                    "Fecha límite de caducidad: " + r.getFechaCaducidad(),
                    "Priorizar uso en lotes de experimentos antes de su vencimiento.",
                    "REACTIVO",
                    r.getId()
            ));
        }

        // Revisar equipos con calibración próxima o mantenimiento
        for (EquipoCaracterizacion e : consultarEquiposPendientesCalibracion(30)) {
            alertas.add(new AlertaSeguridadDTO(
                    e.getEstadoActual() == EstadoEquipo.OPERATIVO ? AlertaSeguridadDTO.NivelAlerta.ADVERTENCIA : AlertaSeguridadDTO.NivelAlerta.CRITICO,
                    "Calibración / Estado: " + e.getNombreEquipo(),
                    "Estado: " + e.getEstadoActual() + " | Calibración programada: " + e.getFechaProximaCalibracion(),
                    "Coordinar servicio técnico con el responsable: " + e.getResponsable(),
                    "EQUIPO",
                    e.getId()
            ));
        }

        return alertas;
    }

    public DashboardStatsDTO obtenerEstadisticas() {
        List<ReactivoQuimico> reactivos = reactivoRepo.findAll();
        List<AleacionBiomaterial> biomats = biomaterialRepo.findAll();
        List<EquipoCaracterizacion> equipos = equipoRepo.findAll();
        long ops = equipos.stream().filter(e -> e.getEstadoActual() == EstadoEquipo.OPERATIVO).count();
        long mant = equipos.stream().filter(e -> e.getEstadoActual() != EstadoEquipo.OPERATIVO).count();
        long movs = movimientoRepo.findAll().size();
        long alertas = generarAlertasActivas().size();

        return new DashboardStatsDTO(
                reactivos.size(),
                biomats.size(),
                equipos.size(),
                ops,
                mant,
                movs,
                alertas
        );
    }

    @Override
    public Map<String, Object> obtenerReporteGeneralSeguridad() {
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("estadisticas", obtenerEstadisticas());
        reporte.put("alertasActivas", generarAlertasActivas());
        return reporte;
    }
}

