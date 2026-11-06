package com.makila.application.service;

import com.makila.application.guardrail.SecurityGuardrails;
import com.makila.domain.exception.InyeccionDetectadaException;
import com.makila.domain.exception.MaterialAmbiguoException;
import com.makila.domain.exception.OperacionInvalidaException;
import com.makila.domain.exception.RegistroNoEncontradoException;
import com.makila.domain.exception.StockInsuficienteException;
import com.makila.domain.model.AgentToolResult;
import com.makila.domain.port.in.AgenteConversacionalPort;
import com.makila.domain.port.out.LlmServicePort;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AgenteInventarioService implements AgenteConversacionalPort {

    private final LlmServicePort llmService;
    private final SecurityGuardrails guardrails;

    public AgenteInventarioService(LlmServicePort llmService, SecurityGuardrails guardrails) {
        this.llmService = llmService;
        this.guardrails = guardrails;
    }

    @Override
    public AgentToolResult procesarMensaje(String mensajeUsuario) {
        if (mensajeUsuario == null || mensajeUsuario.isBlank()) {
            return AgentToolResult.error("input_validator", "El mensaje no puede estar vacío.");
        }

        try {
            // Capa P07: Detección proactiva de Prompt Injection / SQL Injection
            guardrails.validarSeguridadEntrada(mensajeUsuario);

            // Ejecución del agente con Function Calling seguro
            return llmService.procesarIntencion(mensajeUsuario.trim(), new HashMap<>());

        } catch (InyeccionDetectadaException ex) {
            return AgentToolResult.blocked("security_guardrail_p07", ex.getMessage());
        } catch (StockInsuficienteException ex) {
            return AgentToolResult.blocked("stock_guardrail_p07", ex.getMessage());
        } catch (RegistroNoEncontradoException ex) {
            return AgentToolResult.blocked("existence_guardrail_p07", ex.getMessage());
        } catch (MaterialAmbiguoException ex) {
            return AgentToolResult.ambiguous("disambiguation_engine", ex.getMessage(), ex.getCoincidencias());
        } catch (OperacionInvalidaException ex) {
            return AgentToolResult.blocked("validation_guardrail", ex.getMessage());
        } catch (Exception ex) {
            return AgentToolResult.error("agent_execution_error", "Error interno al procesar la solicitud: " + ex.getMessage());
        }
    }
}

