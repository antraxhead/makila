package com.makila.domain;

import com.makila.application.guardrail.SecurityGuardrails;
import com.makila.application.service.GestionMovimientosService;
import com.makila.domain.exception.StockInsuficienteException;
import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.MovimientoInventario;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.model.TipoMovimiento;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import com.makila.domain.port.out.MovimientoRepositoryPort;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GestionMovimientosTest {

    private ReactivoRepositoryPort reactivoRepo;
    private BiomaterialRepositoryPort biomaterialRepo;
    private MovimientoRepositoryPort movimientoRepo;
    private SecurityGuardrails guardrails;
    private GestionMovimientosService service;

    @BeforeEach
    void setUp() {
        reactivoRepo = Mockito.mock(ReactivoRepositoryPort.class);
        biomaterialRepo = Mockito.mock(BiomaterialRepositoryPort.class);
        movimientoRepo = Mockito.mock(MovimientoRepositoryPort.class);
        guardrails = new SecurityGuardrails();
        service = new GestionMovimientosService(reactivoRepo, biomaterialRepo, movimientoRepo, guardrails);
    }

    @Test
    @DisplayName("Registrar entrada válida incrementa stock")
    void testEntradaValidaIncrementaStock() {
        ReactivoQuimico r = new ReactivoQuimico(1L, "Reactivo Test", "H2O", 10.0, "mL", "Lab", "2027-01-01", "Seguro", null, false, null);
        when(reactivoRepo.findById(1L)).thenReturn(Optional.of(r));
        when(movimientoRepo.save(any(MovimientoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovimientoInventario mov = new MovimientoInventario();
        mov.setTipoMovimiento(TipoMovimiento.ENTRADA);
        mov.setCategoria(CategoriaMaterial.REACTIVO);
        mov.setItemId(1L);
        mov.setCantidad(5.0);
        mov.setExperimentoOLote("EXP-01");
        mov.setUsuarioResponsable("Investigador");

        MovimientoInventario result = service.registrarMovimiento(mov);

        assertNotNull(result);
        verify(reactivoRepo, times(1)).updateStock(1L, 15.0);
    }

    @Test
    @DisplayName("Guardrail P07: Rechazo de salida si cantidad > stock actual")
    void testSalidaMayorAlStockLanzaExcepcion() {
        ReactivoQuimico r = new ReactivoQuimico(1L, "Reactivo Escaso", "H2O", 2.0, "mL", "Lab", "2027-01-01", "Seguro", null, false, null);
        when(reactivoRepo.findById(1L)).thenReturn(Optional.of(r));

        MovimientoInventario mov = new MovimientoInventario();
        mov.setTipoMovimiento(TipoMovimiento.SALIDA);
        mov.setCategoria(CategoriaMaterial.REACTIVO);
        mov.setItemId(1L);
        mov.setCantidad(10.0);
        mov.setExperimentoOLote("EXP-OVERFLOW");
        mov.setUsuarioResponsable("Investigador");

        assertThrows(StockInsuficienteException.class, () -> service.registrarMovimiento(mov));
        verify(reactivoRepo, never()).updateStock(anyLong(), anyDouble());
    }
}

