package com.makila.application.dto;

import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MovimientoRequestDTO {
    @NotNull(message = "El tipo de movimiento es requerido")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La categoría es requerida")
    private CategoriaMaterial categoria;

    @NotNull(message = "El ID del material o equipo es requerido")
    private Long itemId;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidad;

    private String unidad;
    private String experimentoOLote;
    private String usuarioResponsable;
    private String notas;

    public MovimientoRequestDTO() {}

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public CategoriaMaterial getCategoria() { return categoria; }
    public void setCategoria(CategoriaMaterial categoria) { this.categoria = categoria; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getExperimentoOLote() { return experimentoOLote; }
    public void setExperimentoOLote(String experimentoOLote) { this.experimentoOLote = experimentoOLote; }

    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

