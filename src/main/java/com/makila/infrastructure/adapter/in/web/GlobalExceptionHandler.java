package com.makila.infrastructure.adapter.in.web;

import com.makila.domain.exception.InyeccionDetectadaException;
import com.makila.domain.exception.MaterialAmbiguoException;
import com.makila.domain.exception.OperacionInvalidaException;
import com.makila.domain.exception.RegistroNoEncontradoException;
import com.makila.domain.exception.StockInsuficienteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "GUARDRAIL_STOCK_INSUFICIENTE", ex.getMessage(), null);
    }

    @ExceptionHandler(RegistroNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRegistroNoEncontrado(RegistroNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "GUARDRAIL_REGISTRO_NO_ENCONTRADO", ex.getMessage(), null);
    }

    @ExceptionHandler(InyeccionDetectadaException.class)
    public ResponseEntity<Map<String, Object>> handleInyeccionDetectada(InyeccionDetectadaException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "GUARDRAIL_SEGURIDAD_INYECCION", ex.getMessage(), null);
    }

    @ExceptionHandler(MaterialAmbiguoException.class)
    public ResponseEntity<Map<String, Object>> handleMaterialAmbiguo(MaterialAmbiguoException ex) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("coincidencias", ex.getCoincidencias());
        return buildResponse(HttpStatus.CONFLICT, "AMBIGUEDAD_DETECTADA", ex.getMessage(), extra);
    }

    @ExceptionHandler(OperacionInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacionInvalida(OperacionInvalidaException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "OPERACION_INVALIDA", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACION_CAMPOS", "Error de validación en parámetros", fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", ex.getMessage(), null);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String codigo, String mensaje, Object detalles) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("codigo", codigo);
        body.put("mensaje", mensaje);
        if (detalles != null) {
            body.put("detalles", detalles);
        }
        return ResponseEntity.status(status).body(body);
    }
}

