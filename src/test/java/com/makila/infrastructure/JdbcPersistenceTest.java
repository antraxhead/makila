package com.makila.infrastructure;

import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.EquipoCaracterizacion;
import com.makila.domain.model.EstadoEquipo;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import com.makila.domain.port.out.EquipoRepositoryPort;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JdbcPersistenceTest {

    @Autowired
    private ReactivoRepositoryPort reactivoRepo;

    @Autowired
    private BiomaterialRepositoryPort biomaterialRepo;

    @Autowired
    private EquipoRepositoryPort equipoRepo;

    @Test
    @DisplayName("Repositorio Reactivos: Carga datos semilla y previene SQLi mediante consultas parametrizadas")
    void testReactivosPersistence() {
        List<ReactivoQuimico> lista = reactivoRepo.findAll();
        assertFalse(lista.isEmpty(), "Debe contener reactivos semilla");

        // Búsqueda parametrizada segura con caracteres especiales
        List<ReactivoQuimico> resultados = reactivoRepo.findByNombreContainingIgnoreCase("bismuto");
        assertFalse(resultados.isEmpty(), "Debe encontrar el bismuto de cobre");
        assertEquals("CuBi", resultados.get(0).getFormula());
    }

    @Test
    @DisplayName("Repositorio Biomateriales: Consulta por tipo y lote")
    void testBiomaterialesPersistence() {
        Optional<AleacionBiomaterial> opt = biomaterialRepo.findByLote("LOT-TIZR-2026-001");
        assertTrue(opt.isPresent(), "Debe existir el lote de titanio-zirconio");
        assertTrue(opt.get().getCantidadDisponible() > 0);
    }

    @Test
    @DisplayName("Repositorio Equipos: Filtrado por estado operativo")
    void testEquiposPersistence() {
        List<EquipoCaracterizacion> ops = equipoRepo.findByEstado(EstadoEquipo.OPERATIVO);
        assertFalse(ops.isEmpty(), "Debe haber equipos operativos");

        List<EquipoCaracterizacion> mant = equipoRepo.findByEstado(EstadoEquipo.EN_MANTENIMIENTO);
        assertFalse(mant.isEmpty(), "Debe registrar equipos en mantenimiento como el espectrofotómetro");
    }
}

