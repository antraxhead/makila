package com.makila.domain.port.in;

import com.makila.domain.model.AgentToolResult;

public interface AgenteConversacionalPort {
    AgentToolResult procesarMensaje(String mensajeUsuario);
}

