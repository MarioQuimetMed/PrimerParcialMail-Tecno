package com.tecnoweb.grupo15sa.business;

import com.tecnoweb.grupo15sa.data.DCuota;
import com.tecnoweb.grupo15sa.data.DPlanPago;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BCuota {
    private DCuota dCuota;
    private DPlanPago dPlanPago;

    public BCuota() {
        this.dCuota = new DCuota();
        this.dPlanPago = new DPlanPago();
    }

    /**
     * Registra el pago de una cuota
     */
    public String pagarCuota(int cuotaId, BigDecimal montoPagado) {
        String[] cuota = dCuota.findOneById(cuotaId);
        if (cuota == null) {
            return "Error: Cuota no encontrada";
        }
        
        if (cuota[8].equals("PAGADA")) {
            return "Error: La cuota ya está pagada";
        }
        
        BigDecimal montoCuota = new BigDecimal(cuota[3]);
        Date fechaVencimiento = Date.valueOf(cuota[4]);
        Date fechaPago = new Date(System.currentTimeMillis());
        
        // Calcular mora si aplica
        BigDecimal mora = calcularMora(fechaVencimiento, fechaPago, montoCuota);
        BigDecimal montoTotal = montoCuota.add(mora);
        
        if (montoPagado.compareTo(montoTotal) < 0) {
            return "Error: El monto pagado (" + montoPagado + ") es menor al requerido (" + 
                   montoTotal + "). Mora: " + mora;
        }
        
        String[] result = dCuota.registrarPago(cuotaId, fechaPago, montoPagado, mora);
        
        if (result[0].equals("1")) {
            // Verificar si se completó el plan de pago
            int planPagoId = Integer.parseInt(cuota[1]);
            verificarCompletacionPlan(planPagoId);
            
            String mensaje = "Cuota pagada exitosamente";
            if (mora.compareTo(BigDecimal.ZERO) > 0) {
                mensaje += ". Mora aplicada: " + mora;
            }
            return mensaje;
        } else {
            return result[1];
        }
    }

    /**
     * Calcula la mora de una cuota vencida
     */
    private BigDecimal calcularMora(Date fechaVencimiento, Date fechaPago, BigDecimal montoCuota) {
        if (fechaPago.before(fechaVencimiento) || fechaPago.equals(fechaVencimiento)) {
            return BigDecimal.ZERO; // No hay mora
        }
        
        // Calcular días de retraso
        long diffMillis = fechaPago.getTime() - fechaVencimiento.getTime();
        long diasRetraso = TimeUnit.MILLISECONDS.toDays(diffMillis);
        
        if (diasRetraso <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Mora: 0.5% por día de retraso sobre el monto de la cuota, máximo 30%
        BigDecimal tasaMoraDiaria = new BigDecimal("0.005"); // 0.5%
        BigDecimal mora = montoCuota.multiply(tasaMoraDiaria).multiply(new BigDecimal(diasRetraso));
        
        BigDecimal moraMaxima = montoCuota.multiply(new BigDecimal("0.30")); // 30% máximo
        
        return mora.min(moraMaxima).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Verifica si el plan de pago se completó
     */
    private void verificarCompletacionPlan(int planPagoId) {
        List<String[]> cuotas = dCuota.findByPlanPago(planPagoId);
        
        boolean todasPagadas = true;
        for (String[] cuota : cuotas) {
            if (!cuota[8].equals("PAGADA")) {
                todasPagadas = false;
                break;
            }
        }
        
        if (todasPagadas) {
            dPlanPago.updateEstado(planPagoId, "COMPLETADO");
        }
    }

    /**
     * Actualiza una cuota
     */
    public String actualizarCuota(int id, int planPagoId, int numeroCuota, BigDecimal monto,
                                  Date fechaVencimiento, Date fechaPago, BigDecimal montoPagado,
                                  BigDecimal mora, String estado) {
        String[] cuota = dCuota.findOneById(id);
        if (cuota == null) {
            return "Error: Cuota no encontrada";
        }
        
        if (!validarEstado(estado)) {
            return "Error: Estado inválido (PENDIENTE, PAGADA, VENCIDA)";
        }
        
        String[] result = dCuota.update(id, planPagoId, numeroCuota, monto, fechaVencimiento,
                                       fechaPago, montoPagado, mora, estado);
        
        if (result[0].equals("1")) {
            return "Cuota actualizada exitosamente";
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza el estado de una cuota
     */
    public String actualizarEstado(int id, String nuevoEstado) {
        if (!validarEstado(nuevoEstado)) {
            return "Error: Estado inválido";
        }
        
        String[] result = dCuota.updateEstado(id, nuevoEstado);
        
        if (result[0].equals("1")) {
            return "Estado actualizado a " + nuevoEstado;
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza cuotas vencidas automáticamente
     */
    public String actualizarCuotasVencidas() {
        List<String[]> cuotasVencidas = dCuota.findCuotasVencidas();
        
        int actualizadas = 0;
        for (String[] cuota : cuotasVencidas) {
            int id = Integer.parseInt(cuota[0]);
            String[] result = dCuota.updateEstado(id, "VENCIDA");
            
            if (result[0].equals("1")) {
                actualizadas++;
                
                // Actualizar estado del plan de pago a VENCIDO
                int planPagoId = Integer.parseInt(cuota[1]);
                dPlanPago.updateEstado(planPagoId, "VENCIDO");
            }
        }
        
        return actualizadas + " cuotas actualizadas a VENCIDA";
    }

    /**
     * Lista todas las cuotas
     */
    public List<String[]> listarCuotas() {
        return dCuota.findAll();
    }

    /**
     * Busca una cuota por ID
     */
    public String[] buscarPorId(int id) {
        return dCuota.findOneById(id);
    }

    /**
     * Lista cuotas por plan de pago
     */
    public List<String[]> listarPorPlan(int planPagoId) {
        return dCuota.findByPlanPago(planPagoId);
    }

    /**
     * Lista cuotas por estado
     */
    public List<String[]> listarPorEstado(String estado) {
        if (!validarEstado(estado)) {
            return null;
        }
        return dCuota.findByEstado(estado);
    }

    /**
     * Lista cuotas vencidas
     */
    public List<String[]> listarCuotasVencidas() {
        return dCuota.findCuotasVencidas();
    }

    /**
     * Lista cuotas próximas a vencer
     */
    public List<String[]> listarProximasVencer(int dias) {
        if (dias <= 0 || dias > 90) {
            dias = 7; // Por defecto 7 días
        }
        return dCuota.findProximasVencer(dias);
    }

    /**
     * Valida estados de cuota
     */
    private boolean validarEstado(String estado) {
        return estado != null && (estado.equals("PENDIENTE") || 
               estado.equals("PAGADA") || estado.equals("VENCIDA"));
    }

    /**
     * Calcula el total adeudado incluyendo mora
     */
    public String calcularTotalAdeudado(int cuotaId) {
        String[] cuota = dCuota.findOneById(cuotaId);
        if (cuota == null) {
            return "Error: Cuota no encontrada";
        }
        
        if (cuota[8].equals("PAGADA")) {
            return "Cuota ya pagada";
        }
        
        BigDecimal montoCuota = new BigDecimal(cuota[3]);
        Date fechaVencimiento = Date.valueOf(cuota[4]);
        Date hoy = new Date(System.currentTimeMillis());
        
        BigDecimal mora = calcularMora(fechaVencimiento, hoy, montoCuota);
        BigDecimal total = montoCuota.add(mora);
        
        return "Total adeudado: " + total + " (Cuota: " + montoCuota + ", Mora: " + mora + ")";
    }

    /**
     * Genera recordatorio de cuota próxima
     */
    public String generarRecordatorio(int cuotaId) {
        String[] cuota = dCuota.findOneById(cuotaId);
        if (cuota == null) {
            return "Error: Cuota no encontrada";
        }
        
        if (cuota[8].equals("PAGADA")) {
            return "La cuota ya está pagada";
        }
        
        String numeroCuota = cuota[2];
        String monto = cuota[3];
        String fechaVencimiento = cuota[4];
        String clienteNombre = cuota[11];
        String clienteEmail = cuota[12];
        
        return "Recordatorio para " + clienteNombre + " (" + clienteEmail + "): " +
               "Cuota #" + numeroCuota + " de " + monto + " vence el " + fechaVencimiento;
    }
}
