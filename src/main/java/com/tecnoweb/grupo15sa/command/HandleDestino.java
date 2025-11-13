package com.tecnoweb.grupo15sa.command;

import com.tecnoweb.grupo15sa.business.BDestino;
import java.math.BigDecimal;
import java.util.List;

public class HandleDestino {

    public static String execute(String command, String params) {
        BDestino bDestino = new BDestino();
        
        try {
            switch (command) {
                case "registrar":
                    return registrar(bDestino, params);
                case "actualizar":
                    return actualizar(bDestino, params);
                case "desactivar":
                    return desactivar(bDestino, params);
                case "reactivar":
                    return reactivar(bDestino, params);
                case "listarActivos":
                    return listarActivos(bDestino);
                case "buscarPorPais":
                    return buscarPorPais(bDestino, params);
                case "buscarPorCiudad":
                    return buscarPorCiudad(bDestino, params);
                case "buscarPorRango":
                    return buscarPorRango(bDestino, params);
                case "listar":
                    return listar(bDestino);
                case "buscar":
                    return buscar(bDestino, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String registrar(BDestino bDestino, String params) {
        String[] parts = params.split(",");
        if (parts.length < 6) {
            return "Error: Faltan parámetros. Uso: registrar (nombre, pais, ciudad, descripcion, clima, precioReferencia)";
        }
        
        String nombre = parts[0].trim();
        String pais = parts[1].trim();
        String ciudad = parts[2].trim();
        String descripcion = parts[3].trim();
        String clima = parts[4].trim();
        BigDecimal precioReferencia = new BigDecimal(parts[5].trim());
        
        return bDestino.registrarDestino(nombre, pais, ciudad, descripcion, clima, precioReferencia);
    }

    private static String actualizar(BDestino bDestino, String params) {
        String[] parts = params.split(",");
        if (parts.length < 7) {
            return "Error: Faltan parámetros. Uso: actualizar (id, nombre, pais, ciudad, descripcion, clima, precioReferencia)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String nombre = parts[1].trim();
        String pais = parts[2].trim();
        String ciudad = parts[3].trim();
        String descripcion = parts[4].trim();
        String clima = parts[5].trim();
        BigDecimal precioReferencia = new BigDecimal(parts[6].trim());
        
        return bDestino.actualizarDestino(id, nombre, pais, ciudad, descripcion, clima, precioReferencia);
    }

    private static String desactivar(BDestino bDestino, String params) {
        int id = Integer.parseInt(params.trim());
        return bDestino.desactivarDestino(id);
    }

    private static String reactivar(BDestino bDestino, String params) {
        int id = Integer.parseInt(params.trim());
        return bDestino.reactivarDestino(id);
    }

    private static String listarActivos(BDestino bDestino) {
        List<String[]> destinos = bDestino.listarDestinosActivos();
        
        if (destinos.isEmpty()) {
            return "No hay destinos activos";
        }
        
        StringBuilder sb = new StringBuilder("=== DESTINOS ACTIVOS ===\n");
        for (String[] d : destinos) {
            sb.append("ID: ").append(d[0])
              .append(" | ").append(d[1]).append(", ").append(d[2])
              .append(" | Precio ref: Bs. ").append(d[5])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscarPorPais(BDestino bDestino, String params) {
        List<String[]> destinos = bDestino.buscarPorPais(params.trim());
        
        if (destinos.isEmpty()) {
            return "No se encontraron destinos en: " + params;
        }
        
        StringBuilder sb = new StringBuilder("=== DESTINOS EN " + params.toUpperCase() + " ===\n");
        for (String[] d : destinos) {
            sb.append(d[1]).append(", ").append(d[3])
              .append(" - Bs. ").append(d[5]).append("\n");
        }
        return sb.toString();
    }

    private static String buscarPorCiudad(BDestino bDestino, String params) {
        List<String[]> destinos = bDestino.buscarPorCiudad(params.trim());
        
        if (destinos.isEmpty()) {
            return "No se encontraron destinos en ciudad: " + params;
        }
        
        StringBuilder sb = new StringBuilder("=== DESTINOS EN " + params.toUpperCase() + " ===\n");
        for (String[] d : destinos) {
            sb.append(d[1]).append(" - Bs. ").append(d[5]).append("\n");
        }
        return sb.toString();
    }

    private static String buscarPorRango(BDestino bDestino, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: buscarPorRango (precioMin, precioMax)";
        }
        
        BigDecimal min = new BigDecimal(parts[0].trim());
        BigDecimal max = new BigDecimal(parts[1].trim());
        
        List<String[]> destinos = bDestino.buscarPorRangoPrecio(min, max);
        
        if (destinos.isEmpty()) {
            return "No hay destinos en ese rango de precios";
        }
        
        StringBuilder sb = new StringBuilder("=== DESTINOS (Bs. " + min + " - " + max + ") ===\n");
        for (String[] d : destinos) {
            sb.append(d[1]).append(", ").append(d[2])
              .append(" - Bs. ").append(d[5]).append("\n");
        }
        return sb.toString();
    }

    private static String listar(BDestino bDestino) {
        List<String[]> destinos = bDestino.listarDestinos();
        
        if (destinos.isEmpty()) {
            return "No hay destinos registrados";
        }
        
        StringBuilder sb = new StringBuilder("=== TODOS LOS DESTINOS ===\n");
        for (String[] d : destinos) {
            sb.append("ID: ").append(d[0])
              .append(" | ").append(d[1]).append(", ").append(d[2])
              .append(" | Activo: ").append(d[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BDestino bDestino, String params) {
        int id = Integer.parseInt(params.trim());
        String[] destino = bDestino.buscarPorId(id);
        
        if (destino == null) {
            return "Destino no encontrado";
        }
        
        return "Destino: " + destino[1] + "\n" +
               "País: " + destino[2] + "\n" +
               "Ciudad: " + destino[3] + "\n" +
               "Descripción: " + destino[4] + "\n" +
               "Precio referencia: Bs. " + destino[5];
    }
}
