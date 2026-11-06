package com.makila.domain.model;

public class AleacionBiomaterial {
    private Long id;
    private String tipoMaterial;
    private String nombre;
    private String lote;
    private Double cantidadDisponible;
    private String unidad;
    private String especificacionesTecnicas;

    public AleacionBiomaterial() {}

    public AleacionBiomaterial(Long id, String tipoMaterial, String nombre, String lote, 
                               Double cantidadDisponible, String unidad, String especificacionesTecnicas) {
        this.id = id;
        this.tipoMaterial = tipoMaterial;
        this.nombre = nombre;
        this.lote = lote;
        this.cantidadDisponible = cantidadDisponible;
        this.unidad = unidad;
        this.especificacionesTecnicas = especificacionesTecnicas;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoMaterial() { return tipoMaterial; }
    public void setTipoMaterial(String tipoMaterial) { this.tipoMaterial = tipoMaterial; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public Double getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(Double cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getEspecificacionesTecnicas() { return especificacionesTecnicas; }
    public void setEspecificacionesTecnicas(String especificacionesTecnicas) { this.especificacionesTecnicas = especificacionesTecnicas; }
}

