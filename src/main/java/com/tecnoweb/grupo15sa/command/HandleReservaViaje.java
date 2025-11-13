package com.tecnoweb.grupo15sa.command;

import com.tecnoweb.grupo15sa.business.BReservaViaje;
import java.util.List;

public class HandleReservaViaje {

    public static String execute(String command, String params) {
        BReservaViaje bReserva = new BReservaViaje();
        
        try {
            switch (command) {
                case "crear":
                    return crear(bReserva, params);
                case "confirmar":
                    return confirmar(bReserva, params);
                case "cancelar":
                    return cancelar(bReserva, params);
                case "completar":
                    return completar(bReserva, params);
                case "listarPorViaje":
                    return listarPorViaje(bReserva, params);
                case "listarPorCliente":
                    return listarPorCliente(bReserva, params);
                case "contarPersonas":
                    return contarPersonas(bReserva, params);
                case "listar":
                    return listar(bReserva);
                case "buscar":
                    return buscar(bReserva, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String crear(BReservaViaje bReserva, String params) {
        String[] parts = params.split(",");
        if (parts.length < 3) {
            return "Error: Faltan parámetros. Uso: crear (viajeId, ventaId, numeroPersonas)";
        }
        
        int viajeId = Integer.parseInt(parts[0].trim());
        int ventaId = Integer.parseInt(parts[1].trim());
        int numeroPersonas = Integer.parseInt(parts[2].trim());
        
        return bReserva.crearReserva(viajeId, ventaId, numeroPersonas);
    }

    private static String confirmar(BReservaViaje bReserva, String params) {
        int id = Integer.parseInt(params.trim());
        return bReserva.confirmarReserva(id);
    }

    private static String cancelar(BReservaViaje bReserva, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: cancelar (id, motivo)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String motivo = parts[1].trim();
        
        return bReserva.cancelarReserva(id, motivo);
    }

    private static String completar(BReservaViaje bReserva, String params) {
        int id = Integer.parseInt(params.trim());
        return bReserva.completarReserva(id);
    }

    private static String listarPorViaje(BReservaViaje bReserva, String params) {
        int viajeId = Integer.parseInt(params.trim());
        List<String[]> reservas = bReserva.listarPorViaje(viajeId);
        
        if (reservas.isEmpty()) {
            return "No hay reservas para este viaje";
        }
        
        StringBuilder sb = new StringBuilder("=== RESERVAS DEL VIAJE ===\n");
        for (String[] r : reservas) {
            sb.append("Reserva #").append(r[0])
              .append(" | Cliente: ").append(r[11])
              .append(" | Personas: ").append(r[3])
              .append(" | Estado: ").append(r[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String listarPorCliente(BReservaViaje bReserva, String params) {
        int clienteId = Integer.parseInt(params.trim());
        List<String[]> reservas = bReserva.listarPorCliente(clienteId);
        
        if (reservas.isEmpty()) {
            return "No hay reservas para este cliente";
        }
        
        StringBuilder sb = new StringBuilder("=== RESERVAS DEL CLIENTE ===\n");
        for (String[] r : reservas) {
            sb.append("Reserva #").append(r[0])
              .append(" | Viaje: ").append(r[9])
              .append(" | Personas: ").append(r[3])
              .append(" | Total: Bs. ").append(r[5])
              .append("\n");
        }
        return sb.toString();
    }

    private static String contarPersonas(BReservaViaje bReserva, String params) {
        int viajeId = Integer.parseInt(params.trim());
        return bReserva.contarPersonasPorViaje(viajeId);
    }

    private static String listar(BReservaViaje bReserva) {
        List<String[]> reservas = bReserva.listarReservas();
        
        if (reservas.isEmpty()) {
            return "No hay reservas registradas";
        }
        
        StringBuilder sb = new StringBuilder("=== TODAS LAS RESERVAS ===\n");
        for (String[] r : reservas) {
            sb.append("ID: ").append(r[0])
              .append(" | Viaje: ").append(r[9])
              .append(" | Cliente: ").append(r[11])
              .append(" | Estado: ").append(r[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BReservaViaje bReserva, String params) {
        int id = Integer.parseInt(params.trim());
        String[] reserva = bReserva.buscarPorId(id);
        
        if (reserva == null) {
            return "Reserva no encontrada";
        }
        
        return "Reserva #" + reserva[0] + "\n" +
               "Viaje: " + reserva[9] + "\n" +
               "Cliente: " + reserva[11] + "\n" +
               "Número de personas: " + reserva[3] + "\n" +
               "Precio total: Bs. " + reserva[5] + "\n" +
               "Estado: " + reserva[6];
    }
}
