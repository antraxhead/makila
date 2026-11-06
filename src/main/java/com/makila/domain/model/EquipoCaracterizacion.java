package com.makila.domain.model;

import java.time.LocalDate;

public class EquipoCaracterizacion {
    private Long id;
    private String nombreEquipo;
    private EstadoEquipo estadoActual;
    private String fechaProximaCalibracion;
    private String ubicacion;
    private String responsable;

    public EquipoCaracterizacion() {}

    public EquipoCaracterizacion(Long id, String nombreEquipo, EstadoEquipo estadoActual, 
                                 String fechaProximaCalibracion, String ubicacion, String responsable) {
        this.id = id;
        this.nombreEquipo = nombreEquipo;
        this.estadoActual = estadoActual;
        this.fechaProximaCalibracion = fechaProximaCalibracion;
        this.ubicacion = ubicacion;
        this.responsable = responsable;
    }

    public boolean requiereCalibracionPronto(int diasUmbral) {
        if (fechaProximaCalibracion == null || fechaProximaCalibracion.isBlank()) return false;
        try {
            LocalDate cal = LocalDate.parse(fechaProximaCalibracion);
            return !cal.isAfter(LocalDate.now().plusDays(diasUmbral));
        } catch (Exception e) {
            return false;
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public EstadoEquipo getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoEquipo estadoActual) { this.estadoActual = estadoActual; }

    public String getFechaProximaCalibracion() { return fechaProximaCalibracion; }
    public void setFechaProximaCalibracion(String fechaProximaCalibracion) { this.fechaProximaCalibracion = fechaProximaCalibracion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
}

