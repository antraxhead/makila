package com.makila.infrastructure.adapter.in.web;

import com.makila.application.dto.ChatMessageDTO;
import com.makila.application.dto.ChatResponseDTO;
import com.makila.domain.model.AgentToolResult;
import com.makila.domain.port.in.AgenteConversacionalPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class AgentChatController {

    private final AgenteConversacionalPort agentePort;

    public AgentChatController(AgenteConversacionalPort agentePort) {
        this.agentePort = agentePort;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> interactuarConAgente(@Valid @RequestBody ChatMessageDTO request) {
        AgentToolResult result = agentePort.procesarMensaje(request.getMensaje());
        return ResponseEntity.ok(ChatResponseDTO.fromToolResult(result));
    }
}

