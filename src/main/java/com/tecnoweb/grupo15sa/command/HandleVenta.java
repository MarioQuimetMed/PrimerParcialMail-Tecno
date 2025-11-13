package com.tecnoweb.grupo15sa.command;

import com.tecnoweb.grupo15sa.business.BVenta;
import java.math.BigDecimal;
import java.util.List;

public class HandleVenta {

    public static String execute(String command, String params) {
        BVenta bVenta = new BVenta();
        
        try {
            switch (command) {
                case "registrar":
                    return registrar(bVenta, params);
                case "actualizar":
                    return actualizar(bVenta, params);
                case "actualizarEstado":
                    return actualizarEstado(bVenta, params);
                case "registrarPago":
                    return registrarPago(bVenta, params);
                case "cancelar":
                    return cancelar(bVenta, params);
                case "listar":
                    return listar(bVenta);
                case "buscarPorCliente":
                    return buscarPorCliente(bVenta, params);
                case "buscar":
                    return buscar(bVenta, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String registrar(BVenta bVenta, String params) {
        String[] parts = params.split(",");
        if (parts.length < 7) {
            return "Error: Faltan parámetros. Uso: registrar (clienteId, vendedorId, viajeId, tipoVenta, montoTotal, montoPagado, observaciones)";
        }
        
        int clienteId = Integer.parseInt(parts[0].trim());
        int vendedorId = Integer.parseInt(parts[1].trim());
        int viajeId = Integer.parseInt(parts[2].trim());
        String tipoVenta = parts[3].trim();
        BigDecimal montoTotal = new BigDecimal(parts[4].trim());
        BigDecimal montoPagado = new BigDecimal(parts[5].trim());
        String observaciones = parts[6].trim();
        
        return bVenta.registrarVenta(clienteId, vendedorId, viajeId, tipoVenta, montoTotal, montoPagado, observaciones);
    }

    private static String actualizar(BVenta bVenta, String params) {
        String[] parts = params.split(",");
        if (parts.length < 10) {
            return "Error: Faltan parámetros. Uso: actualizar (id, clienteId, vendedorId, viajeId, tipoVenta, montoTotal, montoPagado, montoPendiente, estado, observaciones)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        int clienteId = Integer.parseInt(parts[1].trim());
        int vendedorId = Integer.parseInt(parts[2].trim());
        int viajeId = Integer.parseInt(parts[3].trim());
        String tipoVenta = parts[4].trim();
        BigDecimal montoTotal = new BigDecimal(parts[5].trim());
        BigDecimal montoPagado = new BigDecimal(parts[6].trim());
        BigDecimal montoPendiente = new BigDecimal(parts[7].trim());
        String estado = parts[8].trim();
        String observaciones = parts[9].trim();
        
        return bVenta.actualizarVenta(id, clienteId, vendedorId, viajeId, tipoVenta, 
                                     montoTotal, montoPagado, montoPendiente, estado, observaciones);
    }

    private static String actualizarEstado(BVenta bVenta, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: actualizarEstado (id, nuevoEstado)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String nuevoEstado = parts[1].trim();
        
        return bVenta.actualizarEstado(id, nuevoEstado);
    }

    private static String registrarPago(BVenta bVenta, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: registrarPago (id, monto)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        BigDecimal monto = new BigDecimal(parts[1].trim());
        
        return bVenta.registrarPagoParcial(id, monto);
    }

    private static String cancelar(BVenta bVenta, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: cancelar (id, motivo)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String motivo = parts[1].trim();
        
        return bVenta.cancelarVenta(id, motivo);
    }

    private static String listar(BVenta bVenta) {
        List<String[]> ventas = bVenta.listarVentas();
        
        if (ventas.isEmpty()) {
            return "No hay ventas registradas";
        }
        
        StringBuilder sb = new StringBuilder("=== VENTAS REGISTRADAS ===\n");
        for (String[] v : ventas) {
            sb.append("ID: ").append(v[0])
              .append(" | Cliente: ").append(v[11])
              .append(" | Tipo: ").append(v[4])
              .append(" | Total: Bs. ").append(v[5])
              .append(" | Estado: ").append(v[3])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscarPorCliente(BVenta bVenta, String params) {
        int clienteId = Integer.parseInt(params.trim());
        List<String[]> ventas = bVenta.listarPorCliente(clienteId);
        
        if (ventas.isEmpty()) {
            return "No hay ventas para este cliente";
        }
        
        StringBuilder sb = new StringBuilder("=== VENTAS DEL CLIENTE ===\n");
        for (String[] v : ventas) {
            sb.append("Venta #").append(v[0])
              .append(" | Fecha: ").append(v[2])
              .append(" | Total: Bs. ").append(v[5])
              .append(" | Pendiente: Bs. ").append(v[7])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BVenta bVenta, String params) {
        int id = Integer.parseInt(params.trim());
        String[] venta = bVenta.buscarPorId(id);
        
        if (venta == null) {
            return "Venta no encontrada";
        }
        
        return "Venta #" + venta[0] + "\n" +
               "Cliente: " + venta[11] + "\n" +
               "Tipo: " + venta[4] + "\n" +
               "Monto total: Bs. " + venta[5] + "\n" +
               "Monto pagado: Bs. " + venta[6] + "\n" +
               "Monto pendiente: Bs. " + venta[7] + "\n" +
               "Estado: " + venta[3];
    }
}
