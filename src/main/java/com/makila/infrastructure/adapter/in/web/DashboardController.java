package com.makila.infrastructure.adapter.in.web;

import com.makila.application.dto.AlertaSeguridadDTO;
import com.makila.application.dto.DashboardStatsDTO;
import com.makila.application.service.AuditoriaSeguridadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final AuditoriaSeguridadService auditoriaService;

    public DashboardController(AuditoriaSeguridadService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(auditoriaService.obtenerEstadisticas());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaSeguridadDTO>> obtenerAlertas() {
        return ResponseEntity.ok(auditoriaService.generarAlertasActivas());
    }

    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Object>> obtenerReporteGeneral() {
        return ResponseEntity.ok(auditoriaService.obtenerReporteGeneralSeguridad());
    }
}

