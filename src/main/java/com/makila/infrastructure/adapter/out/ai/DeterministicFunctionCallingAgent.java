package com.makila.infrastructure.adapter.out.ai;

import com.makila.application.dto.PlanificadorExperimentoDTO;
import com.makila.application.guardrail.SecurityGuardrails;
import com.makila.domain.exception.MaterialAmbiguoException;
import com.makila.domain.exception.RegistroNoEncontradoException;
import com.makila.domain.model.AgentToolResult;
import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.MovimientoInventario;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.model.TipoMovimiento;
import com.makila.domain.port.in.AuditoriaSeguridadPort;
import com.makila.domain.port.in.CatalogoMaterialesPort;
import com.makila.domain.port.in.GestionMovimientosPort;
import com.makila.domain.port.out.LlmServicePort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Motor de Intenciones y Function Calling para Makila
 * Traduce lenguaje natural a ejecución segura de herramientas de inventario,
 * implementando manejo de ambigüedad y guardrails P07.
 */
@Component
public class DeterministicFunctionCallingAgent implements LlmServicePort {

    private final CatalogoMaterialesPort catalogoPort;
    private final GestionMovimientosPort movimientosPort;
    private final AuditoriaSeguridadPort auditoriaPort;
    private final SecurityGuardrails guardrails;

    // Patrón flexible para detectar movimientos (Entrada / Salida / Consumo)
    private static final Pattern PATTERN_MOVIMIENTO = Pattern.compile(
            "(?:registrar\\s+)?(?:un[a]?\\s+)?(entrada|ingreso|ingresar|salida|retirar|sacar|consumo|consumir|usar)\\s+(?:de\\s+)?(\\d+(?:\\.\\d+)?)\\s*([a-zA-ZáéíóúÁÉÍÓÚñÑ]+)?\\s*(?:de|del)?\\s*(.+)",
            Pattern.CASE_INSENSITIVE
    );

    // Patrón para consultas de seguridad / MSDS / almacenamiento
    private static final Pattern PATTERN_MSDS = Pattern.compile(
            "\\b(msds|seguridad|ficha|almacenamiento|cuidados?|conservaci[oó]n|temperatura|luz)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patrón para consultas de stock / existencias
    private static final Pattern PATTERN_STOCK = Pattern.compile(
            "\\b(stock|cu[aá]nto[s]?|cu[aá]nta[s]?|cantidad|disponible|existencias?|inventario|tenemos|hay)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patrón para equipos y calibración
    private static final Pattern PATTERN_CALIBRACION = Pattern.compile(
            "\\b(calibraci[oó]n|mantenimiento|equipos?|instrumento[s]?|operativo)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patrón para alertas generales y caducidades
    private static final Pattern PATTERN_CADUCIDADES = Pattern.compile(
            "\\b(caduc|venc|alerta[s]?|cr[ií]tic[ao]s?)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patrón para planificación de lotes de experimentos
    private static final Pattern PATTERN_PLANIFICAR = Pattern.compile(
            "\\b(planificar|planeaci[oó]n|lote de experimentos?|ensayo[s]? cl[ií]nico[s]?)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public DeterministicFunctionCallingAgent(CatalogoMaterialesPort catalogoPort,
                                           GestionMovimientosPort movimientosPort,
                                           AuditoriaSeguridadPort auditoriaPort,
                                           SecurityGuardrails guardrails) {
        this.catalogoPort = catalogoPort;
        this.movimientosPort = movimientosPort;
        this.auditoriaPort = auditoriaPort;
        this.guardrails = guardrails;
    }

    @Override
    public AgentToolResult procesarIntencion(String mensajeUsuario, Map<String, Object> contexto) {
        String msgTrim = mensajeUsuario.trim();
        String msgLower = msgTrim.toLowerCase();

        // 1. Detección de intención: Salida / Consumo / Entrada de material
        Matcher matcherMov = PATTERN_MOVIMIENTO.matcher(msgTrim);
        if (matcherMov.find()) {
            String accion = matcherMov.group(1).toLowerCase();
            double cantidad = Double.parseDouble(matcherMov.group(2));
            String unidad = matcherMov.group(3) != null ? matcherMov.group(3).trim() : "unidades";
            String rawMaterial = matcherMov.group(4).trim();

            // Limpiar posibles sufijos contextuales (ej: "para EXP-99", "por Dr. Quiroz")
            String materialQuery = rawMaterial.replaceAll("(?i)\\s+(para|por|en)\\s+(exp|lote|ensayo|usuario|proyecto).*", "").trim();

            TipoMovimiento tipo;
            if (accion.contains("salida") || accion.contains("retirar") || accion.contains("sacar")) {
                tipo = TipoMovimiento.SALIDA;
            } else if (accion.contains("consum") || accion.contains("usar")) {
                tipo = TipoMovimiento.CONSUMO;
            } else {
                tipo = TipoMovimiento.ENTRADA;
            }

            return ejecutarToolRegistrarMovimiento(tipo, materialQuery, cantidad, unidad, "EXP-AGENTE-2026", "Usuario Agente");
        }

        // 2. Detección de intención: Ficha MSDS / Condiciones de seguridad
        if (PATTERN_MSDS.matcher(msgLower).find()) {
            String target = extraerEntidadLimpia(msgTrim,
                    "consultar|verificar|revisar|ver|dame|muestra|ficha|de|del|para|seguridad|temperatura|almacenamiento|condiciones|cuidados?|conservaci[oó]n|msds|y|en|el|la|los|las|un|una|favor|por");
            return ejecutarToolVerificarMsds(target.isEmpty() ? msgTrim : target);
        }

        // 3. Detección de intención: Consulta de Equipos y Calibración
        if (PATTERN_CALIBRACION.matcher(msgLower).find()) {
            return ejecutarToolConsultarEquipos();
        }

        // 4. Detección de intención: Alertas Críticas y Caducidades
        if (PATTERN_CADUCIDADES.matcher(msgLower).find()) {
            return ejecutarToolConsultarAlertas();
        }

        // 5. Detección de intención: Planificar lote de experimentos
        if (PATTERN_PLANIFICAR.matcher(msgLower).find()) {
            return ejecutarToolPlanificarExperimento(msgTrim);
        }

        // 5. Detección de intención: Consulta de Stock / Existencia
        if (PATTERN_STOCK.matcher(msgLower).find()) {
            String target = extraerEntidadLimpia(msgTrim,
                    "cu[aá]nto[s]?|cu[aá]nta[s]?|stock|tenemos|hay|existencias?|cantidad|disponible|inventario|de|del|en|el|la|los|las|un|una|por|favor|qu[eé]");
            return ejecutarToolConsultarStock(target.isEmpty() ? msgTrim : target);
        }

        // 6. Búsqueda general en catálogo
        return ejecutarToolBuscarMateriales(msgTrim);
    }

    private String extraerEntidadLimpia(String texto, String palabrasHuecasRegex) {
        return texto.replaceAll("(?i)\\b(" + palabrasHuecasRegex + ")\\b", " ")
                .replaceAll("[¿?¡!.,;]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Tool: consultar_stock
     * Maneja ambigüedad si el término arroja múltiples coincidencias.
     */
    public AgentToolResult ejecutarToolConsultarStock(String termino) {
        guardrails.validarSeguridadEntrada(termino);
        String clean = guardrails.sanitizar(termino);

        List<ReactivoQuimico> reactivos = catalogoPort.buscarReactivos(clean);
        List<AleacionBiomaterial> biomateriales = catalogoPort.buscarBiomateriales(clean);

        int totalCoincidencias = reactivos.size() + biomateriales.size();

        if (totalCoincidencias == 0) {
            throw new RegistroNoEncontradoException(
                    String.format("Operación denegada por Guardrail P07: No se encontró ningún material que coincida con '%s'.", clean));
        }

        // Manejo de ambigüedad cuando hay más de una coincidencia
        if (totalCoincidencias > 1) {
            List<String> opciones = new ArrayList<>();
            for (ReactivoQuimico r : reactivos) {
                opciones.add("[Reactivo] " + r.getNombre() + " (" + r.getFormula() + ") - Stock: " + r.getCantidad() + " " + r.getUnidad());
            }
            for (AleacionBiomaterial b : biomateriales) {
                opciones.add("[Biomaterial] " + b.getNombre() + " (Lote: " + b.getLote() + ") - Stock: " + b.getCantidadDisponible() + " " + b.getUnidad());
            }
            throw new MaterialAmbiguoException(clean, opciones);
        }

        if (!reactivos.isEmpty()) {
            ReactivoQuimico r = reactivos.get(0);
            String respuesta = String.format("Stock disponible de %s (%s): %.2f %s. Ubicación: %s. Caducidad: %s.",
                    r.getNombre(), r.getFormula(), r.getCantidad(), r.getUnidad(), r.getUbicacion(), r.getFechaCaducidad());
            return AgentToolResult.success("consultar_stock", respuesta, r);
        } else {
            AleacionBiomaterial b = biomateriales.get(0);
            String respuesta = String.format("Stock disponible de %s (Lote %s): %.2f %s. Especificaciones: %s.",
                    b.getNombre(), b.getLote(), b.getCantidadDisponible(), b.getUnidad(), b.getEspecificacionesTecnicas());
            return AgentToolResult.success("consultar_stock", respuesta, b);
        }
    }

    /**
     * Tool: registrar_movimiento
     * Ejecuta salidas, entradas o consumos con validaciones estrictas en código (P07).
     */
    public AgentToolResult ejecutarToolRegistrarMovimiento(TipoMovimiento tipo, String materialQuery, 
                                                         double cantidad, String unidad, 
                                                         String experimentoOLote, String responsable) {
        guardrails.validarSeguridadEntrada(materialQuery);
        String clean = guardrails.sanitizar(materialQuery);

        List<ReactivoQuimico> reactivos = catalogoPort.buscarReactivos(clean);
        List<AleacionBiomaterial> biomats = catalogoPort.buscarBiomateriales(clean);

        int total = reactivos.size() + biomats.size();
        if (total == 0) {
            throw new RegistroNoEncontradoException(
                    String.format("Operación denegada por Guardrail P07: No se encontró el material '%s' para registrar el movimiento.", clean));
        }

        // Manejo estricto de ambigüedad antes de ejecutar acción destructiva o de modificación
        if (total > 1) {
            List<String> opciones = new ArrayList<>();
            reactivos.forEach(r -> opciones.add("[Reactivo ID:" + r.getId() + "] " + r.getNombre()));
            biomats.forEach(b -> opciones.add("[Biomaterial ID:" + b.getId() + "] " + b.getNombre() + " - " + b.getLote()));
            throw new MaterialAmbiguoException(clean, opciones);
        }

        MovimientoInventario mov = new MovimientoInventario();
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setUnidad(unidad);
        mov.setExperimentoOLote(experimentoOLote);
        mov.setUsuarioResponsable(responsable);
        mov.setNotas("Operación ejecutada mediante Agente Conversacional Makila.");

        if (!reactivos.isEmpty()) {
            ReactivoQuimico r = reactivos.get(0);
            mov.setCategoria(CategoriaMaterial.REACTIVO);
            mov.setItemId(r.getId());
        } else {
            AleacionBiomaterial b = biomats.get(0);
            mov.setCategoria(CategoriaMaterial.BIOMATERIAL);
            mov.setItemId(b.getId());
        }

        MovimientoInventario registrado = movimientosPort.registrarMovimiento(mov);
        String mensaje = String.format("Movimiento de %s registrado exitosamente para '%s': %.2f %s. ID Transacción: %d.",
                tipo, registrado.getItemNombre(), registrado.getCantidad(), registrado.getUnidad(), registrado.getId());

        return AgentToolResult.success("registrar_movimiento", mensaje, registrado);
    }

    /**
     * Tool: verificar_msds_seguridad
     */
    public AgentToolResult ejecutarToolVerificarMsds(String query) {
        guardrails.validarSeguridadEntrada(query);
        String clean = guardrails.sanitizar(query);

        List<ReactivoQuimico> reactivos = catalogoPort.buscarReactivos(clean);
        if (reactivos.isEmpty()) {
            throw new RegistroNoEncontradoException("Reactivo para consulta MSDS", clean);
        }

        ReactivoQuimico r = reactivos.get(0);
        Map<String, Object> msdsData = new HashMap<>();
        msdsData.put("reactivo", r.getNombre());
        msdsData.put("formula", r.getFormula());
        msdsData.put("estadoSeguridad", r.getEstadoSeguridad());
        msdsData.put("msdsResumen", r.getMsdsResumen());
        msdsData.put("tempMaxCelsius", r.getTempMaxCelsius());
        msdsData.put("requiereAislamientoLuz", r.getRequiereAislamientoLuz());
        msdsData.put("ubicacion", r.getUbicacion());

        String respuesta = String.format("Ficha de Seguridad y Manejo para %s (%s):\n- Estado de Seguridad: %s\n- Condiciones: %s\n- Ubicación: %s.",
                r.getNombre(), r.getFormula(), r.getEstadoSeguridad(), r.getMsdsResumen(), r.getUbicacion());

        return AgentToolResult.success("verificar_msds_seguridad", respuesta, msdsData);
    }

    /**
     * Tool: consultar_equipos_calibracion
     */
    public AgentToolResult ejecutarToolConsultarEquipos() {
        List<EquipoCaracterizacion> equipos = catalogoPort.listarEquipos();
        List<EquipoCaracterizacion> alertas = auditoriaPort.consultarEquiposPendientesCalibracion(30);

        String detalle = equipos.stream()
                .map(e -> String.format("- %s [Estado: %s, Próx. Calibración: %s, Responsable: %s]",
                        e.getNombreEquipo(), e.getEstadoActual(), e.getFechaProximaCalibracion(), e.getResponsable()))
                .collect(Collectors.joining("\n"));

        String respuesta = String.format("Inventario de Instrumentación y Equipos (%d registrados, %d requieren atención/calibración):\n%s",
                equipos.size(), alertas.size(), detalle);

        return AgentToolResult.success("consultar_equipos_calibracion", respuesta, equipos);
    }

    /**
     * Tool: consultar_caducidades_y_alertas
     */
    public AgentToolResult ejecutarToolConsultarAlertas() {
        Map<String, Object> reporte = auditoriaPort.obtenerReporteGeneralSeguridad();
        return AgentToolResult.success("consultar_caducidades_y_alertas", "Auditoría de seguridad y stock completada.", reporte);
    }

    /**
     * Tool: buscar_materiales
     */
    public AgentToolResult ejecutarToolBuscarMateriales(String query) {
        guardrails.validarSeguridadEntrada(query);
        String clean = guardrails.sanitizar(query);

        List<ReactivoQuimico> reactivos = catalogoPort.buscarReactivos(clean);
        List<AleacionBiomaterial> biomats = catalogoPort.buscarBiomateriales(clean);
        List<EquipoCaracterizacion> equipos = catalogoPort.buscarEquipos(clean);

        Map<String, Object> resultados = new HashMap<>();
        resultados.put("reactivos", reactivos);
        resultados.put("biomateriales", biomats);
        resultados.put("equipos", equipos);

        int total = reactivos.size() + biomats.size() + equipos.size();
        String respuesta = String.format("Búsqueda finalizada para '%s'. Se encontraron %d resultados en total (%d reactivos, %d biomateriales, %d equipos).",
                clean, total, reactivos.size(), biomats.size(), equipos.size());

        return AgentToolResult.success("buscar_materiales", respuesta, resultados);
    }

    /**
     * Tool: planificar_stock_experimento
     * Planifica el stock necesario para un lote de experimentos evaluando condiciones críticas (Requisito MVP).
     */
    public AgentToolResult ejecutarToolPlanificarExperimento(String mensajeUsuario) {
        List<PlanificadorExperimentoDTO.RequerimientoItem> items = new ArrayList<>();
        boolean todosViables = true;

        List<ReactivoQuimico> reactivos = catalogoPort.listarReactivos();
        List<AleacionBiomaterial> biomats = catalogoPort.listarBiomateriales();
        String msgLower = mensajeUsuario.toLowerCase();

        for (AleacionBiomaterial b : biomats) {
            if (msgLower.contains("titanio") || msgLower.contains("polimero") || msgLower.contains("nitinol") 
                    || msgLower.contains(b.getNombre().toLowerCase()) || msgLower.contains(b.getTipoMaterial().toLowerCase())) {
                boolean ok = b.getCantidadDisponible() > 0;
                if (!ok) todosViables = false;
                items.add(new PlanificadorExperimentoDTO.RequerimientoItem(
                        b.getNombre() + " (" + b.getLote() + ")",
                        5.0, b.getUnidad(), b.getCantidadDisponible(), ok,
                        "Especificación: " + b.getEspecificacionesTecnicas()
                ));
            }
        }

        for (ReactivoQuimico r : reactivos) {
            if (msgLower.contains("bismuto") || msgLower.contains("micras") || msgLower.contains("quitosano") 
                    || msgLower.contains("hialuronico") || msgLower.contains(r.getNombre().toLowerCase())) {
                boolean ok = r.getCantidad() > 0;
                if (!ok) todosViables = false;
                String adv = "";
                if (r.getTempMaxCelsius() != null && r.getTempMaxCelsius() <= 30.0) {
                    adv += "Control térmico: Termolábil (<=" + r.getTempMaxCelsius() + "°C). ";
                }
                if (Boolean.TRUE.equals(r.getRequiereAislamientoLuz())) {
                    adv += "Fotosensibilidad: Aislamiento total de luz solar UV. ";
                }
                items.add(new PlanificadorExperimentoDTO.RequerimientoItem(
                        r.getNombre() + " (" + r.getFormula() + ")",
                        10.0, r.getUnidad(), r.getCantidad(), ok,
                        adv.isEmpty() ? "Condiciones estándar de laboratorio." : adv
                ));
            }
        }

        if (items.isEmpty()) {
            return AgentToolResult.success("planificar_stock_experimento",
                    "Para planificar tu lote de experimentos, menciona los insumos o materiales requeridos (ej: 'Planificar lote con titanio-zirconio y fluido de micras').", null);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 **Planificación de Lote de Ensayos Biomédicos**:\n");
        sb.append(todosViables ? "✅ **ESTADO: VIABLE** (Todos los insumos cuentan con stock disponible)\n" 
                               : "⚠️ **ESTADO: REQUIERE ABASTECIMIENTO**\n");
        for (PlanificadorExperimentoDTO.RequerimientoItem item : items) {
            sb.append(String.format("- %s: Stock actual %.2f %s. %s\n",
                    item.getMaterial(), item.getStockActual(), item.getUnidad(), item.getAdvertenciaSeguridad()));
        }

        PlanificadorExperimentoDTO plan = new PlanificadorExperimentoDTO("Lote de Ensayo Biomédico", todosViables, items, sb.toString());
        return AgentToolResult.success("planificar_stock_experimento", sb.toString(), plan);
    }
}
