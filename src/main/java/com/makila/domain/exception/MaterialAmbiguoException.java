package com.makila.domain.exception;

import java.util.List;

public class MaterialAmbiguoException extends RuntimeException {
    private final List<String> coincidencias;

    public MaterialAmbiguoException(String termino, List<String> coincidencias) {
        super(String.format("El término '%s' es ambiguo. Se encontraron %d coincidencias. Se requiere aclaración previa.",
                termino, coincidencias.size()));
        this.coincidencias = coincidencias;
    }

    public List<String> getCoincidencias() {
        return coincidencias;
    }
}

