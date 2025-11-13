package com.tecnoweb.grupo7sa.command;

import com.tecnoweb.grupo7sa.business.BViaje;
import java.sql.Date;
import java.util.List;

public class HandleViaje {

    public static String execute(String command, String params) {
        BViaje bViaje = new BViaje();
        
        try {
            switch (command) {
                case "programar":
                    return programar(bViaje, params);
                case "actualizar":
                    return actualizar(bViaje, params);
                case "actualizarEstado":
                    return actualizarEstado(bViaje, params);
                case "verificarDisponibilidad":
                    return verificarDisponibilidad(bViaje, params);
                case "cancelar":
                    return cancelar(bViaje, params);
                case "listarProgramados":
                    return listarProgramados(bViaje);
                case "calcularOcupacion":
                    return calcularOcupacion(bViaje, params);
                case "listar":
                    return listar(bViaje);
                case "buscar":
                    return buscar(bViaje, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String programar(BViaje bViaje, String params) {
        String[] parts = params.split(",");
        if (parts.length < 6) {
            return "Error: Faltan parámetros. Uso: programar (planViajeId, codigoViaje, fechaSalida, fechaRetorno, cuposTotales, precioFinal)";
        }
        
        int planViajeId = Integer.parseInt(parts[0].trim());
        String codigoViaje = parts[1].trim();
        Date fechaSalida = Date.valueOf(parts[2].trim());
        Date fechaRetorno = Date.valueOf(parts[3].trim());
        int cuposTotales = Integer.parseInt(parts[4].trim());
        java.math.BigDecimal precioFinal = new java.math.BigDecimal(parts[5].trim());
        
        return bViaje.programarViaje(planViajeId, codigoViaje, fechaSalida, fechaRetorno, cuposTotales, precioFinal);
    }

    private static String actualizar(BViaje bViaje, String params) {
        String[] parts = params.split(",");
        if (parts.length < 9) {
            return "Error: Faltan parámetros. Uso: actualizar (id, planViajeId, codigoViaje, fechaSalida, fechaRetorno, cuposDisponibles, cuposTotales, precioFinal, estado)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        int planViajeId = Integer.parseInt(parts[1].trim());
        String codigoViaje = parts[2].trim();
        Date fechaSalida = Date.valueOf(parts[3].trim());
        Date fechaRetorno = Date.valueOf(parts[4].trim());
        int cuposDisponibles = Integer.parseInt(parts[5].trim());
        int cuposTotales = Integer.parseInt(parts[6].trim());
        java.math.BigDecimal precioFinal = new java.math.BigDecimal(parts[7].trim());
        String estado = parts[8].trim();
        
        return bViaje.actualizarViaje(id, planViajeId, codigoViaje, fechaSalida, fechaRetorno, 
                                     cuposDisponibles, cuposTotales, precioFinal, estado);
    }

    private static String actualizarEstado(BViaje bViaje, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: actualizarEstado (id, nuevoEstado)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String nuevoEstado = parts[1].trim();
        
        return bViaje.actualizarEstado(id, nuevoEstado);
    }

    private static String verificarDisponibilidad(BViaje bViaje, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: verificarDisponibilidad (id, cuposSolicitados)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        int cuposSolicitados = Integer.parseInt(parts[1].trim());
        
        return bViaje.verificarDisponibilidad(id, cuposSolicitados);
    }

    private static String cancelar(BViaje bViaje, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: cancelar (id, motivo)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String motivo = parts[1].trim();
        
        return bViaje.cancelarViaje(id, motivo);
    }

    private static String listarProgramados(BViaje bViaje) {
        List<String[]> viajes = bViaje.listarPorEstado("PROGRAMADO");
        
        if (viajes == null || viajes.isEmpty()) {
            return "No hay viajes programados";
        }
        
        StringBuilder sb = new StringBuilder("=== VIAJES PROGRAMADOS ===\n");
        for (String[] v : viajes) {
            sb.append("Código: ").append(v[2])
              .append(" | Destino: ").append(v[10])
              .append(" | Salida: ").append(v[3])
              .append(" | Cupos: ").append(v[5]).append("/").append(v[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String calcularOcupacion(BViaje bViaje, String params) {
        int id = Integer.parseInt(params.trim());
        return bViaje.calcularOcupacion(id);
    }

    private static String listar(BViaje bViaje) {
        List<String[]> viajes = bViaje.listarViajes();
        
        if (viajes.isEmpty()) {
            return "No hay viajes registrados";
        }
        
        StringBuilder sb = new StringBuilder("=== TODOS LOS VIAJES ===\n");
        for (String[] v : viajes) {
            sb.append("ID: ").append(v[0])
              .append(" | Código: ").append(v[1])
              .append(" | Estado: ").append(v[9])
              .append(" | Salida: ").append(v[3])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BViaje bViaje, String params) {
        int id = Integer.parseInt(params.trim());
        String[] viaje = bViaje.buscarPorId(id);
        
        if (viaje == null) {
            return "Viaje no encontrado";
        }
        
        return "Viaje: " + viaje[1] + "\n" +
               "Destino: " + viaje[13] + "\n" +
               "Fecha salida: " + viaje[4] + "\n" +
               "Fecha retorno: " + viaje[5] + "\n" +
               "Cupos totales: " + viaje[2] + "\n" +
               "Cupos disponibles: " + viaje[3] + "\n" +
               "Estado: " + viaje[9];
    }
}
