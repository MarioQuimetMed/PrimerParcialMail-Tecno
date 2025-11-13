package com.tecnoweb.grupo7sa.command;

import com.tecnoweb.grupo7sa.business.BCuota;
import java.math.BigDecimal;
import java.util.List;

public class HandleCuota {

    public static String execute(String command, String params) {
        BCuota bCuota = new BCuota();
        
        try {
            switch (command) {
                case "pagar":
                    return pagar(bCuota, params);
                case "listarPorPlan":
                    return listarPorPlan(bCuota, params);
                case "listarVencidas":
                    return listarVencidas(bCuota);
                case "listarProximas":
                    return listarProximas(bCuota, params);
                case "calcularTotal":
                    return calcularTotal(bCuota, params);
                case "actualizarVencidas":
                    return actualizarVencidas(bCuota);
                case "listar":
                    return listar(bCuota);
                case "buscar":
                    return buscar(bCuota, params);
                case "generarRecordatorio":
                    return generarRecordatorio(bCuota, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String pagar(BCuota bCuota, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: pagar (cuotaId, montoPagado)";
        }
        
        int cuotaId = Integer.parseInt(parts[0].trim());
        BigDecimal montoPagado = new BigDecimal(parts[1].trim());
        
        return bCuota.pagarCuota(cuotaId, montoPagado);
    }

    private static String listarPorPlan(BCuota bCuota, String params) {
        int planPagoId = Integer.parseInt(params.trim());
        List<String[]> cuotas = bCuota.listarPorPlan(planPagoId);
        
        if (cuotas.isEmpty()) {
            return "No hay cuotas para este plan";
        }
        
        StringBuilder sb = new StringBuilder("=== CUOTAS DEL PLAN ===\n");
        for (String[] c : cuotas) {
            sb.append("Cuota #").append(c[2])
              .append(" | Monto: Bs. ").append(c[3])
              .append(" | Vencimiento: ").append(c[4])
              .append(" | Estado: ").append(c[8])
              .append("\n");
        }
        return sb.toString();
    }

    private static String listarVencidas(BCuota bCuota) {
        List<String[]> cuotas = bCuota.listarCuotasVencidas();
        
        if (cuotas.isEmpty()) {
            return "No hay cuotas vencidas";
        }
        
        StringBuilder sb = new StringBuilder("=== CUOTAS VENCIDAS ===\n");
        for (String[] c : cuotas) {
            sb.append("Cuota #").append(c[2])
              .append(" | Cliente: ").append(c[11])
              .append(" | Monto: Bs. ").append(c[3])
              .append(" | Vencimiento: ").append(c[4])
              .append("\n");
        }
        return sb.toString();
    }

    private static String listarProximas(BCuota bCuota, String params) {
        int dias = Integer.parseInt(params.trim());
        List<String[]> cuotas = bCuota.listarProximasVencer(dias);
        
        if (cuotas.isEmpty()) {
            return "No hay cuotas próximas a vencer";
        }
        
        StringBuilder sb = new StringBuilder("=== CUOTAS PRÓXIMAS A VENCER ===\n");
        for (String[] c : cuotas) {
            sb.append("Cuota #").append(c[2])
              .append(" | Cliente: ").append(c[11])
              .append(" | Monto: Bs. ").append(c[3])
              .append(" | Vence: ").append(c[4])
              .append("\n");
        }
        return sb.toString();
    }

    private static String calcularTotal(BCuota bCuota, String params) {
        int cuotaId = Integer.parseInt(params.trim());
        return bCuota.calcularTotalAdeudado(cuotaId);
    }

    private static String actualizarVencidas(BCuota bCuota) {
        return bCuota.actualizarCuotasVencidas();
    }

    private static String listar(BCuota bCuota) {
        List<String[]> cuotas = bCuota.listarCuotas();
        
        if (cuotas.isEmpty()) {
            return "No hay cuotas registradas";
        }
        
        StringBuilder sb = new StringBuilder("=== TODAS LAS CUOTAS ===\n");
        for (String[] c : cuotas) {
            sb.append("ID: ").append(c[0])
              .append(" | Cuota #").append(c[2])
              .append(" | Monto: Bs. ").append(c[3])
              .append(" | Estado: ").append(c[8])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BCuota bCuota, String params) {
        int id = Integer.parseInt(params.trim());
        String[] cuota = bCuota.buscarPorId(id);
        
        if (cuota == null) {
            return "Cuota no encontrada";
        }
        
        return "Cuota #" + cuota[2] + "\n" +
               "Monto: Bs. " + cuota[3] + "\n" +
               "Fecha vencimiento: " + cuota[4] + "\n" +
               "Monto pagado: Bs. " + (cuota[6].equals("null") ? "0.00" : cuota[6]) + "\n" +
               "Mora: Bs. " + (cuota[7].equals("null") ? "0.00" : cuota[7]) + "\n" +
               "Estado: " + cuota[8];
    }

    private static String generarRecordatorio(BCuota bCuota, String params) {
        int cuotaId = Integer.parseInt(params.trim());
        return bCuota.generarRecordatorio(cuotaId);
    }
}
