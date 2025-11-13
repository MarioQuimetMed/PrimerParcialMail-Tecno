package com.tecnoweb.grupo15sa.command;

import com.tecnoweb.grupo15sa.business.BPago;
import java.math.BigDecimal;
import java.util.List;

public class HandlePago {

    public static String execute(String command, String params) {
        BPago bPago = new BPago();
        
        try {
            switch (command) {
                case "registrar":
                    return registrar(bPago, params);
                case "anular":
                    return anular(bPago, params);
                case "listarPorVenta":
                    return listarPorVenta(bPago, params);
                case "buscarPorRecibo":
                    return buscarPorRecibo(bPago, params);
                case "calcularTotal":
                    return calcularTotal(bPago, params);
                case "reconciliar":
                    return reconciliar(bPago, params);
                case "listarPorMetodo":
                    return listarPorMetodo(bPago, params);
                case "listar":
                    return listar(bPago);
                case "buscar":
                    return buscar(bPago, params);
                case "generarComprobante":
                    return generarComprobante(bPago, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String registrar(BPago bPago, String params) {
        String[] parts = params.split(",");
        if (parts.length < 6) {
            return "Error: Faltan parámetros. Uso: registrar (ventaId, monto, metodoPago, numeroRecibo, cuotaId, observaciones)";
        }
        
        int ventaId = Integer.parseInt(parts[0].trim());
        BigDecimal monto = new BigDecimal(parts[1].trim());
        String metodoPago = parts[2].trim();
        String numeroRecibo = parts[3].trim();
        Integer cuotaId = parts[4].trim().equals("null") ? null : Integer.parseInt(parts[4].trim());
        String observaciones = parts[5].trim();
        
        return bPago.registrarPago(ventaId, monto, metodoPago, numeroRecibo, cuotaId, observaciones);
    }

    private static String anular(BPago bPago, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: anular (id, motivo)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String motivo = parts[1].trim();
        
        return bPago.anularPago(id, motivo);
    }

    private static String listarPorVenta(BPago bPago, String params) {
        int ventaId = Integer.parseInt(params.trim());
        List<String[]> pagos = bPago.listarPorVenta(ventaId);
        
        if (pagos.isEmpty()) {
            return "No hay pagos para esta venta";
        }
        
        StringBuilder sb = new StringBuilder("=== PAGOS DE LA VENTA ===\n");
        for (String[] p : pagos) {
            sb.append("Pago #").append(p[0])
              .append(" | Monto: Bs. ").append(p[2])
              .append(" | Método: ").append(p[3])
              .append(" | Fecha: ").append(p[4])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscarPorRecibo(BPago bPago, String params) {
        String numeroRecibo = params.trim();
        String[] pago = bPago.buscarPorRecibo(numeroRecibo);
        
        if (pago == null) {
            return "No se encontró pago con ese número de recibo";
        }
        
        return "Pago encontrado:\n" +
               "ID: " + pago[0] + "\n" +
               "Monto: Bs. " + pago[2] + "\n" +
               "Método: " + pago[3] + "\n" +
               "Fecha: " + pago[4];
    }

    private static String calcularTotal(BPago bPago, String params) {
        int ventaId = Integer.parseInt(params.trim());
        return bPago.calcularTotalPorVenta(ventaId);
    }

    private static String reconciliar(BPago bPago, String params) {
        int ventaId = Integer.parseInt(params.trim());
        return bPago.reconciliarPagos(ventaId);
    }

    private static String listarPorMetodo(BPago bPago, String params) {
        String metodoPago = params.trim();
        List<String[]> pagos = bPago.listarPorMetodo(metodoPago);
        
        if (pagos.isEmpty()) {
            return "No hay pagos con ese método";
        }
        
        StringBuilder sb = new StringBuilder("=== PAGOS CON " + metodoPago + " ===\n");
        BigDecimal total = BigDecimal.ZERO;
        for (String[] p : pagos) {
            sb.append("Pago #").append(p[0])
              .append(" | Monto: Bs. ").append(p[2])
              .append(" | Fecha: ").append(p[4])
              .append("\n");
            total = total.add(new BigDecimal(p[2]));
        }
        sb.append("\nTotal: Bs. ").append(total);
        return sb.toString();
    }

    private static String listar(BPago bPago) {
        List<String[]> pagos = bPago.listarPagos();
        
        if (pagos.isEmpty()) {
            return "No hay pagos registrados";
        }
        
        StringBuilder sb = new StringBuilder("=== TODOS LOS PAGOS ===\n");
        for (String[] p : pagos) {
            sb.append("ID: ").append(p[0])
              .append(" | Monto: Bs. ").append(p[2])
              .append(" | Método: ").append(p[3])
              .append(" | Recibo: ").append(p[5])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BPago bPago, String params) {
        int id = Integer.parseInt(params.trim());
        String[] pago = bPago.buscarPorId(id);
        
        if (pago == null) {
            return "Pago no encontrado";
        }
        
        return "Pago #" + pago[0] + "\n" +
               "Venta ID: " + pago[1] + "\n" +
               "Monto: Bs. " + pago[2] + "\n" +
               "Método: " + pago[3] + "\n" +
               "Fecha: " + pago[4] + "\n" +
               "Recibo: " + pago[5] + "\n" +
               "Observaciones: " + (pago[7].equals("null") ? "N/A" : pago[7]);
    }

    private static String generarComprobante(BPago bPago, String params) {
        int id = Integer.parseInt(params.trim());
        return bPago.generarComprobante(id);
    }
}
