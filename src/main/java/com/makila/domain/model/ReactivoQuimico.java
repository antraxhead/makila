package com.makila.domain.model;

import java.time.LocalDate;

public class ReactivoQuimico {
    private Long id;
    private String nombre;
    private String formula;
    private Double cantidad;
    private String unidad;
    private String ubicacion;
    private String fechaCaducidad;
    private String estadoSeguridad;
    private Double tempMaxCelsius;
    private Boolean requiereAislamientoLuz;
    private String msdsResumen;

    public ReactivoQuimico() {}

    public ReactivoQuimico(Long id, String nombre, String formula, Double cantidad, String unidad, 
                           String ubicacion, String fechaCaducidad, String estadoSeguridad, 
                           Double tempMaxCelsius, Boolean requiereAislamientoLuz, String msdsResumen) {
        this.id = id;
        this.nombre = nombre;
        this.formula = formula;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.ubicacion = ubicacion;
        this.fechaCaducidad = fechaCaducidad;
        this.estadoSeguridad = estadoSeguridad;
        this.tempMaxCelsius = tempMaxCelsius;
        this.requiereAislamientoLuz = requiereAislamientoLuz;
        this.msdsResumen = msdsResumen;
    }

    public boolean estaPorCaducar(int diasUmbral) {
        if (fechaCaducidad == null || fechaCaducidad.isBlank()) return false;
        try {
            LocalDate caducidad = LocalDate.parse(fechaCaducidad);
            return !caducidad.isAfter(LocalDate.now().plusDays(diasUmbral));
        } catch (Exception e) {
            return false;
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(String fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public String getEstadoSeguridad() { return estadoSeguridad; }
    public void setEstadoSeguridad(String estadoSeguridad) { this.estadoSeguridad = estadoSeguridad; }

    public Double getTempMaxCelsius() { return tempMaxCelsius; }
    public void setTempMaxCelsius(Double tempMaxCelsius) { this.tempMaxCelsius = tempMaxCelsius; }

    public Boolean getRequiereAislamientoLuz() { return requiereAislamientoLuz; }
    public void setRequiereAislamientoLuz(Boolean requiereAislamientoLuz) { this.requiereAislamientoLuz = requiereAislamientoLuz; }

    public String getMsdsResumen() { return msdsResumen; }
    public void setMsdsResumen(String msdsResumen) { this.msdsResumen = msdsResumen; }
}

