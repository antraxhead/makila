package com.makila.application.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessageDTO {
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;
    private String usuario;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String mensaje, String usuario) {
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}

