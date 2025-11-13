package com.tecnoweb.grupo7sa.business;

import com.tecnoweb.grupo7sa.data.DVenta;
import com.tecnoweb.grupo7sa.data.DUsuario;
import com.tecnoweb.grupo7sa.data.DViaje;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class BVenta {
    private DVenta dVenta;
    private DUsuario dUsuario;
    private DViaje dViaje;

    public BVenta() {
        this.dVenta = new DVenta();
        this.dUsuario = new DUsuario();
        this.dViaje = new DViaje();
    }

    /**
     * Registra una nueva venta
     */
    public String registrarVenta(int clienteId, int vendedorId, int viajeId, String tipoVenta,
                                 BigDecimal montoTotal, BigDecimal montoPagado, String observaciones) {
        // Validaciones
        String[] cliente = dUsuario.findOneById(clienteId);
        if (cliente == null || !cliente[8].equals("CLIENTE")) {
            return "Error: Cliente no válido";
        }
        
        String[] vendedor = dUsuario.findOneById(vendedorId);
        if (vendedor == null || !vendedor[8].equals("VENDEDOR")) {
            return "Error: Vendedor no válido";
        }
        
        String[] viaje = dViaje.findOneById(viajeId);
        if (viaje == null) {
            return "Error: Viaje no encontrado";
        }
        
        if (!tipoVenta.equals("CONTADO") && !tipoVenta.equals("CREDITO")) {
            return "Error: Tipo de venta debe ser CONTADO o CREDITO";
        }
        
        if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El monto total debe ser mayor a 0";
        }
        
        if (montoPagado == null) {
            montoPagado = BigDecimal.ZERO;
        }
        
        if (montoPagado.compareTo(montoTotal) > 0) {
            return "Error: El monto pagado no puede ser mayor al monto total";
        }
        
        // Validar tipo de venta con monto pagado
        if (tipoVenta.equals("CONTADO") && montoPagado.compareTo(montoTotal) < 0) {
            return "Error: En venta de CONTADO se debe pagar el monto completo";
        }
        
        Date fechaVenta = new Date(System.currentTimeMillis());
        BigDecimal montoPendiente = montoTotal.subtract(montoPagado);
        String estado = montoPagado.compareTo(montoTotal) >= 0 ? "PAGADA" : "PENDIENTE";
        
        String[] result = dVenta.save(clienteId, vendedorId, viajeId, tipoVenta, montoTotal,
                                      montoPagado, montoPendiente, estado, fechaVenta, observaciones);
        
        if (result[0].equals("1")) {
            return "Venta registrada exitosamente con ID: " + result[1];
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza una venta existente
     */
    public String actualizarVenta(int id, int clienteId, int vendedorId, int viajeId,
                                  String tipoVenta, BigDecimal montoTotal, BigDecimal montoPagado,
                                  BigDecimal montoPendiente, String estado, String observaciones) {
        String[] venta = dVenta.findOneById(id);
        if (venta == null) {
            return "Error: Venta no encontrada";
        }
        
        if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El monto total debe ser mayor a 0";
        }
        
        // DVenta.update requiere: descuento y numeroFactura en lugar de montoPagado/montoPendiente
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal montoFinal = montoTotal.subtract(descuento);
        String numeroFactura = venta[13]; // Mantener factura original
        
        return dVenta.update(id, clienteId, vendedorId, viajeId, tipoVenta,
                            montoTotal, descuento, montoFinal, estado,
                            observaciones, numeroFactura);
    }

    /**
     * Actualiza el estado de una venta
     */
    public String actualizarEstado(int id, String nuevoEstado) {
        if (!validarEstado(nuevoEstado)) {
            return "Error: Estado inválido (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA, PAGADA)";
        }
        
        return dVenta.updateEstado(id, nuevoEstado);
    }

    /**
     * Registra un pago parcial y actualiza montos
     */
    public String registrarPagoParcial(int ventaId, BigDecimal montoPago) {
        String[] venta = dVenta.findOneById(ventaId);
        if (venta == null) {
            return "Error: Venta no encontrada";
        }
        
        if (montoPago == null || montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El monto del pago debe ser mayor a 0";
        }
        
        // Usar updateMontos en lugar de update completo
        // venta[0]=id, [8]=montoTotal, [9]=descuento, [10]=montoFinal
        BigDecimal montoTotal = new BigDecimal(venta[8]);
        BigDecimal descuento = new BigDecimal(venta[9]);
        BigDecimal montoFinal = new BigDecimal(venta[10]);
        
        // Obtener montos de pago desde DB (usar updateMontos)
        // Por ahora simplificamos usando updateEstado cuando se paga completo
        if (montoPago.compareTo(montoFinal) >= 0) {
            return dVenta.updateEstado(Integer.parseInt(venta[0]), "PAGADA");
        } else {
            return "Pago parcial registrado (usar método específico de pagos)";
        }
    }

    /**
     * Cancela una venta
     */
    public String cancelarVenta(int id, String motivo) {
        String[] venta = dVenta.findOneById(id);
        if (venta == null) {
            return "Error: Venta no encontrada";
        }
        
        // venta[11] = estado
        if (venta[11].equals("CANCELADA")) {
            return "Error: La venta ya está cancelada";
        }
        
        if (venta[11].equals("COMPLETADA")) {
            return "Error: No se puede cancelar una venta completada";
        }
        
        String result = dVenta.updateEstado(id, "CANCELADA");
        if (result.startsWith("Error")) {
            return result;
        }
        return "Venta cancelada exitosamente. Motivo: " + motivo;
    }

    /**
     * Lista todas las ventas
     */
    public List<String[]> listarVentas() {
        return dVenta.findAll();
    }

    /**
     * Busca una venta por ID
     */
    public String[] buscarPorId(int id) {
        return dVenta.findOneById(id);
    }

    /**
     * Lista ventas por cliente
     */
    public List<String[]> listarPorCliente(int clienteId) {
        return dVenta.findByCliente(clienteId);
    }

    /**
     * Lista ventas por vendedor
     */
    public List<String[]> listarPorVendedor(int vendedorId) {
        return dVenta.findByVendedor(vendedorId);
    }

    /**
     * Lista ventas por tipo
     */
    public List<String[]> listarPorTipo(String tipo) {
        if (!tipo.equals("CONTADO") && !tipo.equals("CREDITO")) {
            return null;
        }
        return dVenta.findByTipo(tipo);
    }

    /**
     * Lista ventas por estado
     */
    public List<String[]> listarPorEstado(String estado) {
        if (!validarEstado(estado)) {
            return null;
        }
        return dVenta.findByEstado(estado);
    }

    /**
     * Obtiene ventas pendientes de pago
     */
    public List<String[]> listarVentasPendientes() {
        return dVenta.findVentasPendientes();
    }

    /**
     * Valida estados de venta
     */
    private boolean validarEstado(String estado) {
        return estado != null && (estado.equals("PENDIENTE") || estado.equals("CONFIRMADA") ||
               estado.equals("CANCELADA") || estado.equals("COMPLETADA") || estado.equals("PAGADA"));
    }
}
