package com.makila.domain.port.in;

import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.ReactivoQuimico;
import java.util.List;
import java.util.Map;

public interface AuditoriaSeguridadPort {
    List<ReactivoQuimico> consultarReactivosPorCaducar(int diasUmbral);
    List<ReactivoQuimico> consultarReactivosCondicionesCriticas();
    List<EquipoCaracterizacion> consultarEquiposPendientesCalibracion(int diasUmbral);
    Map<String, Object> obtenerReporteGeneralSeguridad();
}

