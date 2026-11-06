package com.makila.application.dto;

public class AlertaSeguridadDTO {
    public enum NivelAlerta {
        CRITICO,
        ADVERTENCIA,
        INFORMATIVO
    }

    private NivelAlerta nivel;
    private String titulo;
    private String descripcion;
    private String recomendacion;
    private String categoria;
    private Long itemId;

    public AlertaSeguridadDTO() {}

    public AlertaSeguridadDTO(NivelAlerta nivel, String titulo, String descripcion, String recomendacion, String categoria, Long itemId) {
        this.nivel = nivel;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.recomendacion = recomendacion;
        this.categoria = categoria;
        this.itemId = itemId;
    }

    public NivelAlerta getNivel() { return nivel; }
    public void setNivel(NivelAlerta nivel) { this.nivel = nivel; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}

