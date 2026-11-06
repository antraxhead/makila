package com.makila.domain.model;

public class MovimientoInventario {
    private Long id;
    private TipoMovimiento tipoMovimiento;
    private CategoriaMaterial categoria;
    private Long itemId;
    private String itemNombre;
    private Double cantidad;
    private String unidad;
    private String experimentoOLote;
    private String usuarioResponsable;
    private String fechaHora;
    private String notas;

    public MovimientoInventario() {}

    public MovimientoInventario(Long id, TipoMovimiento tipoMovimiento, CategoriaMaterial categoria, 
                                Long itemId, String itemNombre, Double cantidad, String unidad, 
                                String experimentoOLote, String usuarioResponsable, 
                                String fechaHora, String notas) {
        this.id = id;
        this.tipoMovimiento = tipoMovimiento;
        this.categoria = categoria;
        this.itemId = itemId;
        this.itemNombre = itemNombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.experimentoOLote = experimentoOLote;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaHora = fechaHora;
        this.notas = notas;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public CategoriaMaterial getCategoria() { return categoria; }
    public void setCategoria(CategoriaMaterial categoria) { this.categoria = categoria; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemNombre() { return itemNombre; }
    public void setItemNombre(String itemNombre) { this.itemNombre = itemNombre; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getExperimentoOLote() { return experimentoOLote; }
    public void setExperimentoOLote(String experimentoOLote) { this.experimentoOLote = experimentoOLote; }

    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

