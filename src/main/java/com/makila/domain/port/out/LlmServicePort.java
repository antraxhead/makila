package com.makila.domain.port.out;

import com.makila.domain.model.AgentToolResult;
import java.util.Map;

public interface LlmServicePort {
    /**
     * Procesa la solicitud en lenguaje natural, identifica la intención,
     * evalúa guardrails y ejecuta la herramienta correspondiente.
     */
    AgentToolResult procesarIntencion(String mensajeUsuario, Map<String, Object> contexto);
}

