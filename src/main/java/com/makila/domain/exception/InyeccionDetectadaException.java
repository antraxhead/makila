package com.makila.domain.exception;

public class InyeccionDetectadaException extends RuntimeException {
    public InyeccionDetectadaException(String tipoInyeccion, String detalle) {
        super(String.format("Seguridad P07: Intento de inyección detectado y bloqueado [%s]: %s", tipoInyeccion, detalle));
    }
}

