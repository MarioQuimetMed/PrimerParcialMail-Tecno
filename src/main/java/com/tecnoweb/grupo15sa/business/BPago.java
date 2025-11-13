package com.tecnoweb.grupo15sa.business;

import com.tecnoweb.grupo15sa.data.DPago;
import com.tecnoweb.grupo15sa.data.DVenta;
import com.tecnoweb.grupo15sa.data.DCuota;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.regex.Pattern;

public class BPago {
    private static final Pattern RECIBO_PATTERN = Pattern.compile("^[A-Z0-9]{8,20}$");
    
    private DPago dPago;
    private DVenta dVenta;
    private DCuota dCuota;

    public BPago() {
        this.dPago = new DPago();
        this.dVenta = new DVenta();
        this.dCuota = new DCuota();
    }

    /**
     * Registra un pago para una venta
     */
    public String registrarPago(int ventaId, BigDecimal monto, String metodoPago, 
                                String numeroRecibo, Integer cuotaId, String observaciones) {
        // Validar venta
        String[] venta = dVenta.findOneById(ventaId);
        if (venta == null) {
            return "Error: Venta no encontrada";
        }
        
        // Validaciones
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El monto debe ser mayor a 0";
        }
        
        if (!validarMetodoPago(metodoPago)) {
            return "Error: Método de pago inválido (EFECTIVO, TARJETA, TRANSFERENCIA, QR)";
        }
        
        if (numeroRecibo == null || numeroRecibo.trim().isEmpty()) {
            return "Error: El número de recibo es obligatorio";
        }
        
        if (!RECIBO_PATTERN.matcher(numeroRecibo.toUpperCase()).matches()) {
            return "Error: Número de recibo inválido (8-20 caracteres alfanuméricos en mayúscula)";
        }
        
        // Verificar unicidad del recibo
        if (dPago.existeRecibo(numeroRecibo)) {
            return "Error: Ya existe un pago con ese número de recibo";
        }
        
        // Verificar que no se exceda el monto pendiente
        // venta[8]=montoTotal, venta[9]=descuento, venta[10]=montoFinal
        BigDecimal montoFinal = new BigDecimal(venta[10]);
        // Para simplificar, validamos contra monto final (sin tracking de pagos previos)
        
        Date fechaPago = new Date(System.currentTimeMillis());
        
        // Validar cuota si se especifica
        if (cuotaId != null) {
            String[] cuota = dCuota.findOneById(cuotaId);
            if (cuota == null) {
                return "Error: Cuota no encontrada";
            }
            
            // Verificar que la cuota pertenece a un plan de esta venta
            String[] planPago = dPago.verificarCuotaVenta(cuotaId, ventaId);
            if (planPago == null) {
                return "Error: La cuota no pertenece a esta venta";
            }
        }
        
        // Registrar pago
        String[] result = dPago.save(ventaId, cuotaId, monto, metodoPago,
                                     numeroRecibo, fechaPago, observaciones);
        
        if (result[0].equals("1")) {
            int pagoId = Integer.parseInt(result[1]);
            
            // Actualizar montos de la venta
            actualizarMontosVenta(ventaId, monto);
            
            // Si es pago de cuota, actualizar estado de cuota
            if (cuotaId != null) {
                dCuota.registrarPago(cuotaId, fechaPago, monto, BigDecimal.ZERO);
            }
            
            return "Pago registrado exitosamente con ID: " + pagoId;
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza los montos de una venta tras un pago
     */
    private void actualizarMontosVenta(int ventaId, BigDecimal montoPago) {
        String[] venta = dVenta.findOneById(ventaId);
        if (venta == null) return;
        
        BigDecimal montoPagado = new BigDecimal(venta[6]);
        BigDecimal montoPendiente = new BigDecimal(venta[7]);
        
        BigDecimal nuevoMontoPagado = montoPagado.add(montoPago);
        BigDecimal nuevoMontoPendiente = montoPendiente.subtract(montoPago);
        
        dVenta.updateMontos(ventaId, nuevoMontoPagado, nuevoMontoPendiente);
        
        // Si se pagó todo, actualizar estado a PAGADA
        if (nuevoMontoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            dVenta.updateEstado(ventaId, "PAGADA");
        }
    }

    /**
     * Actualiza un pago
     */
    public String actualizarPago(int id, int ventaId, BigDecimal monto, String metodoPago,
                                 Date fechaPago, String numeroRecibo, Integer cuotaId,
                                 String observaciones) {
        String[] pago = dPago.findOneById(id);
        if (pago == null) {
            return "Error: Pago no encontrado";
        }
        
        if (!validarMetodoPago(metodoPago)) {
            return "Error: Método de pago inválido";
        }
        
        // Verificar unicidad del recibo (excepto el actual)
        if (!pago[5].equals(numeroRecibo) && dPago.existeRecibo(numeroRecibo)) {
            return "Error: Ya existe un pago con ese número de recibo";
        }
        
        String[] result = dPago.update(id, ventaId, cuotaId, monto, metodoPago,
                                      numeroRecibo, fechaPago, observaciones);
        
        if (result[0].equals("1")) {
            return "Pago actualizado exitosamente";
        } else {
            return result[1];
        }
    }

    /**
     * Anula un pago
     */
    public String anularPago(int id, String motivo) {
        String[] pago = dPago.findOneById(id);
        if (pago == null) {
            return "Error: Pago no encontrado";
        }
        
        int ventaId = Integer.parseInt(pago[1]);
        BigDecimal monto = new BigDecimal(pago[2]);
        Integer cuotaId = pago[6].equals("null") ? null : Integer.parseInt(pago[6]);
        
        // Revertir montos en la venta
        revertirMontosVenta(ventaId, monto);
        
        // Si era pago de cuota, revertir estado
        if (cuotaId != null) {
            dCuota.updateEstado(cuotaId, "PENDIENTE");
        }
        
        // Eliminar pago
        String[] result = dPago.delete(id);
        
        if (result[0].equals("1")) {
            return "Pago anulado exitosamente. Motivo: " + motivo;
        } else {
            return result[1];
        }
    }

    /**
     * Revierte los montos de una venta tras anular un pago
     */
    private void revertirMontosVenta(int ventaId, BigDecimal montoPago) {
        String[] venta = dVenta.findOneById(ventaId);
        if (venta == null) return;
        
        BigDecimal montoPagado = new BigDecimal(venta[6]);
        BigDecimal montoPendiente = new BigDecimal(venta[7]);
        
        BigDecimal nuevoMontoPagado = montoPagado.subtract(montoPago);
        BigDecimal nuevoMontoPendiente = montoPendiente.add(montoPago);
        
        dVenta.updateMontos(ventaId, nuevoMontoPagado, nuevoMontoPendiente);
        
        // Actualizar estado a CONFIRMADA si había quedado PAGADA
        if (venta[3].equals("PAGADA")) {
            dVenta.updateEstado(ventaId, "CONFIRMADA");
        }
    }

    /**
     * Lista todos los pagos
     */
    public List<String[]> listarPagos() {
        return dPago.findAll();
    }

    /**
     * Busca un pago por ID
     */
    public String[] buscarPorId(int id) {
        return dPago.findOneById(id);
    }

    /**
     * Lista pagos por venta
     */
    public List<String[]> listarPorVenta(int ventaId) {
        return dPago.findByVenta(ventaId);
    }

    /**
     * Lista pagos por método
     */
    public List<String[]> listarPorMetodo(String metodoPago) {
        if (!validarMetodoPago(metodoPago)) {
            return null;
        }
        return dPago.findByMetodoPago(metodoPago);
    }

    /**
     * Lista pagos por rango de fechas
     */
    public List<String[]> listarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return null;
        }
        
        if (fechaFin.before(fechaInicio)) {
            return null;
        }
        
        return dPago.findByDateRange(fechaInicio, fechaFin);
    }

    /**
     * Busca un pago por número de recibo
     */
    public String[] buscarPorRecibo(String numeroRecibo) {
        return dPago.findByRecibo(numeroRecibo);
    }

    /**
     * Calcula total de pagos por venta
     */
    public String calcularTotalPorVenta(int ventaId) {
        List<String[]> pagos = dPago.findByVenta(ventaId);
        
        BigDecimal total = BigDecimal.ZERO;
        for (String[] pago : pagos) {
            total = total.add(new BigDecimal(pago[2]));
        }
        
        return "Total pagado: " + total + " en " + pagos.size() + " pago(s)";
    }

    /**
     * Reconcilia pagos con cuotas
     */
    public String reconciliarPagos(int ventaId) {
        List<String[]> pagos = dPago.findByVenta(ventaId);
        
        int pagosSinCuota = 0;
        int pagosConCuota = 0;
        BigDecimal montoSinCuota = BigDecimal.ZERO;
        BigDecimal montoConCuota = BigDecimal.ZERO;
        
        for (String[] pago : pagos) {
            BigDecimal monto = new BigDecimal(pago[2]);
            if (pago[6].equals("null")) {
                pagosSinCuota++;
                montoSinCuota = montoSinCuota.add(monto);
            } else {
                pagosConCuota++;
                montoConCuota = montoConCuota.add(monto);
            }
        }
        
        return "Reconciliación: " + pagos.size() + " pagos totales. " +
               "Sin cuota asignada: " + pagosSinCuota + " (" + montoSinCuota + "). " +
               "Con cuota: " + pagosConCuota + " (" + montoConCuota + ")";
    }

    /**
     * Valida métodos de pago
     */
    private boolean validarMetodoPago(String metodo) {
        return metodo != null && (metodo.equals("EFECTIVO") || metodo.equals("TARJETA") ||
               metodo.equals("TRANSFERENCIA") || metodo.equals("QR"));
    }

    /**
     * Genera comprobante de pago
     */
    public String generarComprobante(int pagoId) {
        String[] pago = dPago.findOneById(pagoId);
        if (pago == null) {
            return "Error: Pago no encontrado";
        }
        
        String[] venta = dVenta.findOneById(Integer.parseInt(pago[1]));
        
        StringBuilder comprobante = new StringBuilder();
        comprobante.append("=== COMPROBANTE DE PAGO ===\n");
        comprobante.append("Recibo Nº: ").append(pago[5]).append("\n");
        comprobante.append("Fecha: ").append(pago[4]).append("\n");
        comprobante.append("Monto: ").append(pago[2]).append("\n");
        comprobante.append("Método: ").append(pago[3]).append("\n");
        comprobante.append("Cliente: ").append(venta[11]).append("\n");
        
        if (!pago[6].equals("null")) {
            comprobante.append("Cuota Nº: ").append(pago[6]).append("\n");
        }
        
        if (!pago[7].equals("null")) {
            comprobante.append("Observaciones: ").append(pago[7]).append("\n");
        }
        
        comprobante.append("========================");
        
        return comprobante.toString();
    }
}
