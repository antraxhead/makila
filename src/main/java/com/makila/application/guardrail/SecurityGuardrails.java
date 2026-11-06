package com.makila.application.guardrail;

import com.makila.domain.exception.InyeccionDetectadaException;
import com.makila.domain.exception.OperacionInvalidaException;
import com.makila.domain.exception.RegistroNoEncontradoException;
import com.makila.domain.exception.StockInsuficienteException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Capa de Seguridad y Guardrails (Requisito P07 del curso)
 * Valida de manera estricta en código antes de cualquier interacción
 * con la base de datos o ejecución de herramientas del agente.
 */
@Component
public class SecurityGuardrails {

    // Patrones para detectar Prompt Injection y Jailbreaks (prioridad alta)
    private static final Pattern PROMPT_INJECTION_PATTERN = Pattern.compile(
            "(ignore( all)? (previous|prior) instructions)|(system prompt)|(you are now (unrestricted|DAN))|" +
            "(olvida (todas )?las instrucciones)|(actúa como si fueras un sistema sin restricciones)|" +
            "(bypass (all )?security)|(elimina toda la base de datos)|(drop database)|(exec system command)",
            Pattern.CASE_INSENSITIVE
    );

    // Patrones para detectar intentos de inyección SQL
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('(''|[^'])*')|(;\\s*(drop|insert|update|delete|truncate|alter|create|select|union|exec|declare)\\b)|" +
            "(\\b(drop|alter|truncate|exec|delete\\s+from)\\s+[a-zA-Z_]+)|(--|/\\*|\\*/)|(\\b(or|and)\\b\\s+['\"\\d]+=['\"\\d]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Valida que una cadena no contenga fragmentos de jailbreak ni SQL injection.
     */
    public void validarSeguridadEntrada(String input) {
        if (input == null || input.isBlank()) return;

        // Evaluar primero Prompt Injection / Jailbreak
        if (PROMPT_INJECTION_PATTERN.matcher(input).find()) {
            throw new InyeccionDetectadaException("PROMPT_INJECTION", 
                    "Patrón de jailbreak o prompt injection detectado en el mensaje.");
        }

        // Evaluar inyección SQL
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new InyeccionDetectadaException("SQL_INJECTION", 
                    "Patrón SQL sospechoso detectado en la entrada de usuario.");
        }
    }

    /**
     * Requisito P07: Impedir que el agente intente registrar una salida superior al stock actual.
     */
    public void validarStockSuficiente(String nombreMaterial, double stockActual, double cantidadSolicitada) {
        if (cantidadSolicitada <= 0) {
            throw new OperacionInvalidaException("La cantidad a mover debe ser estrictamente mayor a 0.");
        }
        if (cantidadSolicitada > stockActual) {
            throw new StockInsuficienteException(nombreMaterial, stockActual, cantidadSolicitada);
        }
    }

    /**
     * Requisito P07: Bloquear operaciones sobre registros inexistentes.
     */
    public void validarExistenciaRegistro(boolean existe, String tipoEntidad, Object id) {
        if (!existe) {
            throw new RegistroNoEncontradoException(tipoEntidad, id);
        }
    }

    /**
     * Sanitiza cadenas eliminando caracteres potencialmente peligrosos de control.
     */
    public String sanitizar(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[\u0000-\u0008\u000B\u000C\u000E-\u001F]", "");
    }
}
