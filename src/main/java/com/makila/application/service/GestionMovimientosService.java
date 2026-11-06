package com.makila.application.service;

import com.makila.application.guardrail.SecurityGuardrails;
import com.makila.domain.exception.OperacionInvalidaException;
import com.makila.domain.exception.RegistroNoEncontradoException;
import com.makila.domain.model.AleacionBiomaterial;
import com.makila.domain.model.CategoriaMaterial;
import com.makila.domain.model.MovimientoInventario;
import com.makila.domain.model.ReactivoQuimico;
import com.makila.domain.model.TipoMovimiento;
import com.makila.domain.port.in.GestionMovimientosPort;
import com.makila.domain.port.out.BiomaterialRepositoryPort;
import com.makila.domain.port.out.MovimientoRepositoryPort;
import com.makila.domain.port.out.ReactivoRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GestionMovimientosService implements GestionMovimientosPort {

    private final ReactivoRepositoryPort reactivoRepo;
    private final BiomaterialRepositoryPort biomaterialRepo;
    private final MovimientoRepositoryPort movimientoRepo;
    private final SecurityGuardrails guardrails;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GestionMovimientosService(ReactivoRepositoryPort reactivoRepo, 
                                     BiomaterialRepositoryPort biomaterialRepo, 
                                     MovimientoRepositoryPort movimientoRepo, 
                                     SecurityGuardrails guardrails) {
        this.reactivoRepo = reactivoRepo;
        this.biomaterialRepo = biomaterialRepo;
        this.movimientoRepo = movimientoRepo;
        this.guardrails = guardrails;
    }

    @Override
    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento) {
        // Validaciones generales de seguridad (P07)
        guardrails.validarSeguridadEntrada(movimiento.getExperimentoOLote());
        guardrails.validarSeguridadEntrada(movimiento.getUsuarioResponsable());
        guardrails.validarSeguridadEntrada(movimiento.getNotas());

        if (movimiento.getCantidad() == null || movimiento.getCantidad() <= 0) {
            throw new OperacionInvalidaException("La cantidad debe ser un valor positivo mayor a 0.");
        }

        String itemNombre = "";
        String unidad = "";

        if (movimiento.getCategoria() == CategoriaMaterial.REACTIVO) {
            ReactivoQuimico reactivo = reactivoRepo.findById(movimiento.getItemId())
                    .orElseThrow(() -> new RegistroNoEncontradoException("Reactivo Químico", movimiento.getItemId()));

            itemNombre = reactivo.getNombre();
            unidad = reactivo.getUnidad();

            double nuevoStock;
            if (movimiento.getTipoMovimiento() == TipoMovimiento.SALIDA || movimiento.getTipoMovimiento() == TipoMovimiento.CONSUMO) {
                // Requisito P07: Impedir salida superior al stock actual
                guardrails.validarStockSuficiente(reactivo.getNombre(), reactivo.getCantidad(), movimiento.getCantidad());
                nuevoStock = reactivo.getCantidad() - movimiento.getCantidad();
            } else {
                nuevoStock = reactivo.getCantidad() + movimiento.getCantidad();
            }
            reactivoRepo.updateStock(reactivo.getId(), nuevoStock);

        } else if (movimiento.getCategoria() == CategoriaMaterial.BIOMATERIAL) {
            AleacionBiomaterial biomaterial = biomaterialRepo.findById(movimiento.getItemId())
                    .orElseThrow(() -> new RegistroNoEncontradoException("Aleación / Biomaterial", movimiento.getItemId()));

            itemNombre = biomaterial.getNombre();
            unidad = biomaterial.getUnidad();

            double nuevoStock;
            if (movimiento.getTipoMovimiento() == TipoMovimiento.SALIDA || movimiento.getTipoMovimiento() == TipoMovimiento.CONSUMO) {
                // Requisito P07: Impedir salida superior al stock actual
                guardrails.validarStockSuficiente(biomaterial.getNombre(), biomaterial.getCantidadDisponible(), movimiento.getCantidad());
                nuevoStock = biomaterial.getCantidadDisponible() - movimiento.getCantidad();
            } else {
                nuevoStock = biomaterial.getCantidadDisponible() + movimiento.getCantidad();
            }
            biomaterialRepo.updateStock(biomaterial.getId(), nuevoStock);

        } else {
            throw new OperacionInvalidaException("La categoría especificada no admite movimientos de inventario directos.");
        }

        movimiento.setItemNombre(itemNombre);
        if (movimiento.getUnidad() == null || movimiento.getUnidad().isBlank()) {
            movimiento.setUnidad(unidad);
        }
        if (movimiento.getFechaHora() == null || movimiento.getFechaHora().isBlank()) {
            movimiento.setFechaHora(LocalDateTime.now().format(FORMATTER));
        }

        return movimientoRepo.save(movimiento);
    }

    @Override
    public List<MovimientoInventario> listarHistorial() {
        return movimientoRepo.findAll();
    }

    @Override
    public List<MovimientoInventario> listarPorCategoriaYItem(CategoriaMaterial categoria, Long itemId) {
        return movimientoRepo.findByCategoriaAndItemId(categoria, itemId);
    }
}

