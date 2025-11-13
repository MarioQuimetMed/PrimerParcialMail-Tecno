package com.tecnoweb.grupo15sa.business;

import com.tecnoweb.grupo15sa.data.DPlanPago;
import com.tecnoweb.grupo15sa.data.DVenta;
import com.tecnoweb.grupo15sa.data.DCuota;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class BPlanPago {
    private DPlanPago dPlanPago;
    private DVenta dVenta;
    private DCuota dCuota;

    public BPlanPago() {
        this.dPlanPago = new DPlanPago();
        this.dVenta = new DVenta();
        this.dCuota = new DCuota();
    }

    /**
     * Crea un plan de pago para una venta a crédito
     */
    public String crearPlanPago(int ventaId, int numeroCuotas, BigDecimal interesporcentaje,
                                Date fechaPrimerVencimiento) {
        // Validar venta
        String[] venta = dVenta.findOneById(ventaId);
        if (venta == null) {
            return "Error: Venta no encontrada";
        }
        
        if (!venta[4].equals("CREDITO")) {
            return "Error: Solo se pueden crear planes de pago para ventas a CREDITO";
        }
        
        // Validaciones
        if (numeroCuotas <= 0) {
            return "Error: El número de cuotas debe ser mayor a 0";
        }
        
        if (numeroCuotas > 24) {
            return "Error: El número de cuotas no puede exceder 24 meses";
        }
        
        if (interesporcentaje == null || interesporcentaje.compareTo(BigDecimal.ZERO) < 0) {
            return "Error: El interés debe ser mayor o igual a 0";
        }
        
        if (interesporcentaje.compareTo(new BigDecimal("100")) > 0) {
            return "Error: El interés no puede superar el 100%";
        }
        
        if (fechaPrimerVencimiento == null) {
            return "Error: La fecha del primer vencimiento es obligatoria";
        }
        
        Date hoy = new Date(System.currentTimeMillis());
        if (fechaPrimerVencimiento.before(hoy)) {
            return "Error: La fecha de vencimiento no puede ser anterior a hoy";
        }
        
        // Calcular monto de cada cuota
        BigDecimal montoPendiente = new BigDecimal(venta[7]);
        BigDecimal interes = montoPendiente.multiply(interesporcentaje.divide(new BigDecimal("100")));
        BigDecimal montoTotal = montoPendiente.add(interes);
        BigDecimal montoCuota = montoTotal.divide(BigDecimal.valueOf(numeroCuotas), 2, RoundingMode.HALF_UP);
        
        String estado = "ACTIVO";
        
        // Crear plan de pago
        String[] result = dPlanPago.save(ventaId, numeroCuotas, montoCuota, interesporcentaje,
                                        fechaPrimerVencimiento, estado);
        
        if (result[0].equals("1")) {
            int planPagoId = Integer.parseInt(result[1]);
            
            // Generar cuotas automáticamente
            String resultCuotas = generarCuotas(planPagoId, numeroCuotas, montoCuota, fechaPrimerVencimiento);
            
            return "Plan de pago creado con ID: " + planPagoId + ". " + resultCuotas;
        } else {
            return result[1];
        }
    }

    /**
     * Genera las cuotas para un plan de pago
     */
    private String generarCuotas(int planPagoId, int numeroCuotas, BigDecimal montoCuota,
                                 Date fechaPrimerVencimiento) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaPrimerVencimiento);
        
        int cuotasCreadas = 0;
        
        for (int i = 1; i <= numeroCuotas; i++) {
            Date fechaVencimiento = new Date(calendar.getTimeInMillis());
            String estado = "PENDIENTE";
            
            String[] result = dCuota.save(planPagoId, i, montoCuota, fechaVencimiento, estado);
            
            if (result[0].equals("1")) {
                cuotasCreadas++;
            }
            
            // Avanzar al siguiente mes
            calendar.add(Calendar.MONTH, 1);
        }
        
        return cuotasCreadas + " cuotas generadas de " + numeroCuotas;
    }

    /**
     * Actualiza un plan de pago
     */
    public String actualizarPlanPago(int id, int ventaId, int numeroCuotas, BigDecimal montoCuota,
                                     BigDecimal interesporcentaje, Date fechaPrimerVencimiento,
                                     String estado) {
        String[] plan = dPlanPago.findOneById(id);
        if (plan == null) {
            return "Error: Plan de pago no encontrado";
        }
        
        if (!validarEstado(estado)) {
            return "Error: Estado inválido (ACTIVO, COMPLETADO, CANCELADO, VENCIDO)";
        }
        
        String[] result = dPlanPago.update(id, ventaId, numeroCuotas, montoCuota, interesporcentaje,
                                          fechaPrimerVencimiento, estado);
        
        if (result[0].equals("1")) {
            return "Plan de pago actualizado exitosamente";
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza el estado de un plan de pago
     */
    public String actualizarEstado(int id, String nuevoEstado) {
        if (!validarEstado(nuevoEstado)) {
            return "Error: Estado inválido";
        }
        
        String[] result = dPlanPago.updateEstado(id, nuevoEstado);
        
        if (result[0].equals("1")) {
            return "Estado actualizado a " + nuevoEstado;
        } else {
            return result[1];
        }
    }

    /**
     * Verifica y actualiza el estado de un plan según sus cuotas
     */
    public String verificarEstadoPlan(int id) {
        String[] plan = dPlanPago.findOneById(id);
        if (plan == null) {
            return "Error: Plan no encontrado";
        }
        
        List<String[]> cuotas = dCuota.findByPlanPago(id);
        
        int cuotasPagadas = 0;
        int cuotasVencidas = 0;
        int cuotasPendientes = 0;
        
        for (String[] cuota : cuotas) {
            String estadoCuota = cuota[8];
            switch (estadoCuota) {
                case "PAGADA":
                    cuotasPagadas++;
                    break;
                case "VENCIDA":
                    cuotasVencidas++;
                    break;
                case "PENDIENTE":
                    cuotasPendientes++;
                    break;
            }
        }
        
        String nuevoEstado;
        if (cuotasPagadas == cuotas.size()) {
            nuevoEstado = "COMPLETADO";
        } else if (cuotasVencidas > 0) {
            nuevoEstado = "VENCIDO";
        } else {
            nuevoEstado = "ACTIVO";
        }
        
        if (!plan[6].equals(nuevoEstado)) {
            dPlanPago.updateEstado(id, nuevoEstado);
            return "Estado actualizado a " + nuevoEstado;
        }
        
        return "Estado actual: " + nuevoEstado;
    }

    /**
     * Cancela un plan de pago
     */
    public String cancelarPlan(int id, String motivo) {
        String[] plan = dPlanPago.findOneById(id);
        if (plan == null) {
            return "Error: Plan no encontrado";
        }
        
        if (plan[6].equals("COMPLETADO")) {
            return "Error: No se puede cancelar un plan completado";
        }
        
        String[] result = dPlanPago.updateEstado(id, "CANCELADO");
        
        if (result[0].equals("1")) {
            return "Plan de pago cancelado. Motivo: " + motivo;
        } else {
            return result[1];
        }
    }

    /**
     * Lista todos los planes de pago
     */
    public List<String[]> listarPlanesPago() {
        return dPlanPago.findAll();
    }

    /**
     * Busca un plan por ID
     */
    public String[] buscarPorId(int id) {
        return dPlanPago.findOneById(id);
    }

    /**
     * Lista planes por venta
     */
    public List<String[]> listarPorVenta(int ventaId) {
        return dPlanPago.findByVenta(ventaId);
    }

    /**
     * Lista planes por estado
     */
    public List<String[]> listarPorEstado(String estado) {
        if (!validarEstado(estado)) {
            return null;
        }
        return dPlanPago.findByEstado(estado);
    }

    /**
     * Lista planes con cuotas vencidas
     */
    public List<String[]> listarPlanesVencidos() {
        return dPlanPago.findVencidos();
    }

    /**
     * Valida estados del plan de pago
     */
    private boolean validarEstado(String estado) {
        return estado != null && (estado.equals("ACTIVO") || estado.equals("COMPLETADO") ||
               estado.equals("CANCELADO") || estado.equals("VENCIDO"));
    }

    /**
     * Calcula resumen de un plan de pago
     */
    public String[] calcularResumen(int id) {
        String[] plan = dPlanPago.findOneById(id);
        if (plan == null) {
            return null;
        }
        
        List<String[]> cuotas = dCuota.findByPlanPago(id);
        
        int totalCuotas = cuotas.size();
        int cuotasPagadas = 0;
        BigDecimal montoPagado = BigDecimal.ZERO;
        BigDecimal montoPendiente = BigDecimal.ZERO;
        
        for (String[] cuota : cuotas) {
            if (cuota[8].equals("PAGADA")) {
                cuotasPagadas++;
                if (!cuota[6].equals("null")) {
                    montoPagado = montoPagado.add(new BigDecimal(cuota[6]));
                }
            } else {
                montoPendiente = montoPendiente.add(new BigDecimal(cuota[3]));
            }
        }
        
        return new String[]{
            String.valueOf(totalCuotas),
            String.valueOf(cuotasPagadas),
            String.valueOf(totalCuotas - cuotasPagadas),
            montoPagado.toString(),
            montoPendiente.toString(),
            plan[6] // estado
        };
    }
}
