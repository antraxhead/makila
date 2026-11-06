package com.makila.application.dto;

public class DashboardStatsDTO {
    private long totalReactivos;
    private long totalBiomateriales;
    private long totalEquipos;
    private long equiposOperativos;
    private long equiposMantenimiento;
    private long totalMovimientos;
    private long alertasCriticas;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(long totalReactivos, long totalBiomateriales, long totalEquipos, 
                             long equiposOperativos, long equiposMantenimiento, 
                             long totalMovimientos, long alertasCriticas) {
        this.totalReactivos = totalReactivos;
        this.totalBiomateriales = totalBiomateriales;
        this.totalEquipos = totalEquipos;
        this.equiposOperativos = equiposOperativos;
        this.equiposMantenimiento = equiposMantenimiento;
        this.totalMovimientos = totalMovimientos;
        this.alertasCriticas = alertasCriticas;
    }

    public long getTotalReactivos() { return totalReactivos; }
    public void setTotalReactivos(long totalReactivos) { this.totalReactivos = totalReactivos; }

    public long getTotalBiomateriales() { return totalBiomateriales; }
    public void setTotalBiomateriales(long totalBiomateriales) { this.totalBiomateriales = totalBiomateriales; }

    public long getTotalEquipos() { return totalEquipos; }
    public void setTotalEquipos(long totalEquipos) { this.totalEquipos = totalEquipos; }

    public long getEquiposOperativos() { return equiposOperativos; }
    public void setEquiposOperativos(long equiposOperativos) { this.equiposOperativos = equiposOperativos; }

    public long getEquiposMantenimiento() { return equiposMantenimiento; }
    public void setEquiposMantenimiento(long equiposMantenimiento) { this.equiposMantenimiento = equiposMantenimiento; }

    public long getTotalMovimientos() { return totalMovimientos; }
    public void setTotalMovimientos(long totalMovimientos) { this.totalMovimientos = totalMovimientos; }

    public long getAlertasCriticas() { return alertasCriticas; }
    public void setAlertasCriticas(long alertasCriticas) { this.alertasCriticas = alertasCriticas; }
}

