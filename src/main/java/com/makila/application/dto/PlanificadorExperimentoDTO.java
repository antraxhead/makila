package com.makila.application.dto;

import java.util.List;

public class PlanificadorExperimentoDTO {

    public static class RequerimientoItem {
        private String material;
        private double cantidadRequerida;
        private String unidad;
        private double stockActual;
        private boolean disponible;
        private String advertenciaSeguridad;

        public RequerimientoItem() {}

        public RequerimientoItem(String material, double cantidadRequerida, String unidad, 
                                 double stockActual, boolean disponible, String advertenciaSeguridad) {
            this.material = material;
            this.cantidadRequerida = cantidadRequerida;
            this.unidad = unidad;
            this.stockActual = stockActual;
            this.disponible = disponible;
            this.advertenciaSeguridad = advertenciaSeguridad;
        }

        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }

        public double getCantidadRequerida() { return cantidadRequerida; }
        public void setCantidadRequerida(double cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }

        public String getUnidad() { return unidad; }
        public void setUnidad(String unidad) { this.unidad = unidad; }

        public double getStockActual() { return stockActual; }
        public void setStockActual(double stockActual) { this.stockActual = stockActual; }

        public boolean isDisponible() { return disponible; }
        public void setDisponible(boolean disponible) { this.disponible = disponible; }

        public String getAdvertenciaSeguridad() { return advertenciaSeguridad; }
        public void setAdvertenciaSeguridad(String advertenciaSeguridad) { this.advertenciaSeguridad = advertenciaSeguridad; }
    }

    private String nombreExperimento;
    private boolean viable;
    private List<RequerimientoItem> items;
    private String resumen;

    public PlanificadorExperimentoDTO() {}

    public PlanificadorExperimentoDTO(String nombreExperimento, boolean viable, List<RequerimientoItem> items, String resumen) {
        this.nombreExperimento = nombreExperimento;
        this.viable = viable;
        this.items = items;
        this.resumen = resumen;
    }

    public String getNombreExperimento() { return nombreExperimento; }
    public void setNombreExperimento(String nombreExperimento) { this.nombreExperimento = nombreExperimento; }

    public boolean isViable() { return viable; }
    public void setViable(boolean viable) { this.viable = viable; }

    public List<RequerimientoItem> getItems() { return items; }
    public void setItems(List<RequerimientoItem> items) { this.items = items; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
}

