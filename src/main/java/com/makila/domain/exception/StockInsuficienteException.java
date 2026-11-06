package com.makila.domain.exception;

public class StockInsuficienteException extends RuntimeException {
    private final double stockActual;
    private final double cantidadSolicitada;

    public StockInsuficienteException(String material, double stockActual, double cantidadSolicitada) {
        super(String.format("Operación denegada por Guardrail P07: Stock insuficiente para '%s'. Stock actual: %.2f, Solicitado: %.2f.",
                material, stockActual, cantidadSolicitada));
        this.stockActual = stockActual;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public double getStockActual() { return stockActual; }
    public double getCantidadSolicitada() { return cantidadSolicitada; }
}

