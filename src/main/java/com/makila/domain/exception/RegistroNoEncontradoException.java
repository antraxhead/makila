package com.makila.domain.exception;

public class RegistroNoEncontradoException extends RuntimeException {
    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RegistroNoEncontradoException(String tipoEntidad, Object id) {
        super(String.format("Operación denegada por Guardrail P07: No se encontró registro de tipo '%s' con identificador '%s'.", tipoEntidad, id));
    }
}

