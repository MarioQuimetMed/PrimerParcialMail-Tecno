package com.tecnoweb.grupo15sa.command;

import com.tecnoweb.grupo15sa.business.BPlanPago;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class HandlePlanPago {

    public static String execute(String command, String params) {
        BPlanPago bPlanPago = new BPlanPago();
        
        try {
            switch (command) {
                case "crear":
                    return crear(bPlanPago, params);
                case "actualizarEstado":
                    return actualizarEstado(bPlanPago, params);
                case "cancelar":
                    return cancelar(bPlanPago, params);
                case "verificar":
                    return verificar(bPlanPago, params);
                case "listarPorVenta":
                    return listarPorVenta(bPlanPago, params);
                case "listarVencidos":
                    return listarVencidos(bPlanPago);
                case "listarActivos":
                    return listarActivos(bPlanPago);
                case "listar":
                    return listar(bPlanPago);
                case "buscar":
                    return buscar(bPlanPago, params);
                case "calcularResumen":
                    return calcularResumen(bPlanPago, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String crear(BPlanPago bPlanPago, String params) {
        String[] parts = params.split(",");
        if (parts.length < 4) {
            return "Error: Faltan parámetros. Uso: crear (ventaId, numeroCuotas, interesPorcentaje, fechaPrimerVencimiento)";
        }
        
        int ventaId = Integer.parseInt(parts[0].trim());
        int numeroCuotas = Integer.parseInt(parts[1].trim());
        BigDecimal interesPorcentaje = new BigDecimal(parts[2].trim());
        Date fechaPrimerVencimiento = Date.valueOf(parts[3].trim());
        
        return bPlanPago.crearPlanPago(ventaId, numeroCuotas, interesPorcentaje, fechaPrimerVencimiento);
    }

    private static String actualizarEstado(BPlanPago bPlanPago, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: actualizarEstado (id, nuevoEstado)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String nuevoEstado = parts[1].trim();
        
        return bPlanPago.actualizarEstado(id, nuevoEstado);
    }

    private static String cancelar(BPlanPago bPlanPago, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: cancelar (id, motivo)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String motivo = parts[1].trim();
        
        return bPlanPago.cancelarPlan(id, motivo);
    }

    private static String verificar(BPlanPago bPlanPago, String params) {
        int id = Integer.parseInt(params.trim());
        return bPlanPago.verificarEstadoPlan(id);
    }

    private static String listarPorVenta(BPlanPago bPlanPago, String params) {
        int ventaId = Integer.parseInt(params.trim());
        List<String[]> planes = bPlanPago.listarPorVenta(ventaId);
        
        if (planes.isEmpty()) {
            return "No hay planes de pago para esta venta";
        }
        
        StringBuilder sb = new StringBuilder("=== PLANES DE PAGO ===\n");
        for (String[] p : planes) {
            sb.append("ID: ").append(p[0])
              .append(" | Cuotas: ").append(p[2])
              .append(" | Monto cuota: Bs. ").append(p[3])
              .append(" | Estado: ").append(p[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String listarVencidos(BPlanPago bPlanPago) {
        List<String[]> planes = bPlanPago.listarPlanesVencidos();
        
        if (planes.isEmpty()) {
            return "No hay planes vencidos";
        }
        
        StringBuilder sb = new StringBuilder("=== PLANES VENCIDOS ===\n");
        for (String[] p : planes) {
            sb.append("Plan #").append(p[0])
              .append(" | Venta #").append(p[1])
              .append(" | Cliente: ").append(p[7])
              .append("\n");
        }
        return sb.toString();
    }

    private static String listarActivos(BPlanPago bPlanPago) {
        List<String[]> planes = bPlanPago.listarPorEstado("ACTIVO");
        
        if (planes.isEmpty()) {
            return "No hay planes activos";
        }
        
        StringBuilder sb = new StringBuilder("=== PLANES ACTIVOS ===\n");
        for (String[] p : planes) {
            sb.append("ID: ").append(p[0])
              .append(" | Cuotas: ").append(p[2])
              .append(" | Interés: ").append(p[4]).append("%")
              .append("\n");
        }
        return sb.toString();
    }

    private static String listar(BPlanPago bPlanPago) {
        List<String[]> planes = bPlanPago.listarPlanesPago();
        
        if (planes.isEmpty()) {
            return "No hay planes de pago registrados";
        }
        
        StringBuilder sb = new StringBuilder("=== TODOS LOS PLANES DE PAGO ===\n");
        for (String[] p : planes) {
            sb.append("ID: ").append(p[0])
              .append(" | Venta: ").append(p[1])
              .append(" | Cuotas: ").append(p[2])
              .append(" | Estado: ").append(p[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BPlanPago bPlanPago, String params) {
        int id = Integer.parseInt(params.trim());
        String[] plan = bPlanPago.buscarPorId(id);
        
        if (plan == null) {
            return "Plan de pago no encontrado";
        }
        
        return "Plan de pago #" + plan[0] + "\n" +
               "Venta ID: " + plan[1] + "\n" +
               "Número de cuotas: " + plan[2] + "\n" +
               "Monto por cuota: Bs. " + plan[3] + "\n" +
               "Interés: " + plan[4] + "%\n" +
               "Estado: " + plan[6];
    }

    private static String calcularResumen(BPlanPago bPlanPago, String params) {
        int id = Integer.parseInt(params.trim());
        String[] resumen = bPlanPago.calcularResumen(id);
        
        if (resumen == null) {
            return "Plan no encontrado";
        }
        
        return "=== RESUMEN DEL PLAN ===\n" +
               "Total cuotas: " + resumen[0] + "\n" +
               "Cuotas pagadas: " + resumen[1] + "\n" +
               "Cuotas pendientes: " + resumen[2] + "\n" +
               "Monto pagado: Bs. " + resumen[3] + "\n" +
               "Monto pendiente: Bs. " + resumen[4] + "\n" +
               "Estado: " + resumen[5];
    }
}
