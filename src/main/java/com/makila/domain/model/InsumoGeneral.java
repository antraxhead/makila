package com.makila.domain.model;

/**
 * Representa un insumo general o consumible de laboratorio biomédico
 * (EPP: guantes, cofias, batas; cristalería: matraces; preparación: lumina/alúmina, paños, etc.).
 */
public class InsumoGeneral {
    private Long id;
    private String categoriaInsumo;
    private String nombre;
    private Double cantidad;
    private String unidad;
    private String ubicacion;
    private Double stockMinimo;
    private String especificaciones;

    public InsumoGeneral() {}

    public InsumoGeneral(Long id, String categoriaInsumo, String nombre, Double cantidad,
                         String unidad, String ubicacion, Double stockMinimo, String especificaciones) {
        this.id = id;
        this.categoriaInsumo = categoriaInsumo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.ubicacion = ubicacion;
        this.stockMinimo = stockMinimo;
        this.especificaciones = especificaciones;
    }

    public boolean estaEnStockBajo() {
        return stockMinimo != null && cantidad != null && cantidad <= stockMinimo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoriaInsumo() { return categoriaInsumo; }
    public void setCategoriaInsumo(String categoriaInsumo) { this.categoriaInsumo = categoriaInsumo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Double stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getEspecificaciones() { return especificaciones; }
    public void setEspecificaciones(String especificaciones) { this.especificaciones = especificaciones; }
}
