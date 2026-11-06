package com.makila.application.dto;

import com.makila.domain.model.AgentToolResult;
import java.util.List;

public class ChatResponseDTO {
    private AgentToolResult.Status status;
    private String toolUsed;
    private String respuesta;
    private Object data;
    private List<String> opcionesDesambiguacion;

    public ChatResponseDTO() {}

    public ChatResponseDTO(AgentToolResult.Status status, String toolUsed, String respuesta, Object data, List<String> opciones) {
        this.status = status;
        this.toolUsed = toolUsed;
        this.respuesta = respuesta;
        this.data = data;
        this.opcionesDesambiguacion = opciones;
    }

    public static ChatResponseDTO fromToolResult(AgentToolResult result) {
        return new ChatResponseDTO(
                result.getStatus(),
                result.getToolName(),
                result.getMessage(),
                result.getData(),
                result.getAmbiguousOptions()
        );
    }

    // Getters y Setters
    public AgentToolResult.Status getStatus() { return status; }
    public void setStatus(AgentToolResult.Status status) { this.status = status; }

    public String getToolUsed() { return toolUsed; }
    public void setToolUsed(String toolUsed) { this.toolUsed = toolUsed; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public List<String> getOpcionesDesambiguacion() { return opcionesDesambiguacion; }
    public void setOpcionesDesambiguacion(List<String> opcionesDesambiguacion) { this.opcionesDesambiguacion = opcionesDesambiguacion; }
}

