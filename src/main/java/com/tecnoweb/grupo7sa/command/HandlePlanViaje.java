package com.tecnoweb.grupo7sa.command;

import com.tecnoweb.grupo7sa.business.BPlanViaje;
import java.math.BigDecimal;
import java.util.List;

public class HandlePlanViaje {

    public static String execute(String command, String params) {
        BPlanViaje bPlan = new BPlanViaje();
        
        try {
            switch (command) {
                case "crear":
                    return crear(bPlan, params);
                case "actualizar":
                    return actualizar(bPlan, params);
                case "desactivar":
                    return desactivar(bPlan, params);
                case "reactivar":
                    return reactivar(bPlan, params);
                case "calcularPrecio":
                    return calcularPrecio(bPlan, params);
                case "listarActivos":
                    return listarActivos(bPlan);
                case "listar":
                    return listar(bPlan);
                case "buscar":
                    return buscar(bPlan, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String crear(BPlanViaje bPlan, String params) {
        String[] parts = params.split(",");
        if (parts.length < 11) {
            return "Error: Faltan parametros. Uso: crear(destinoId, nombre, descripcion, duracionDias, categoria, precioBase, cupoMaximo|null, incluyeHotel, incluyeTransporte, incluyeComidas, incluyeTours)";
        }

        int destinoId = Integer.parseInt(parts[0].trim());
        String nombre = parts[1].trim();
        String descripcion = parts[2].trim();
        int duracionDias = Integer.parseInt(parts[3].trim());
        String categoria = parts[4].trim();
        BigDecimal precioBase = new BigDecimal(parts[5].trim());
        Integer cupoMaximo = parseNullableInteger(parts[6]);
        boolean incluyeHotel = Boolean.parseBoolean(parts[7].trim());
        boolean incluyeTransporte = Boolean.parseBoolean(parts[8].trim());
        boolean incluyeComidas = Boolean.parseBoolean(parts[9].trim());
        boolean incluyeTours = Boolean.parseBoolean(parts[10].trim());

        return bPlan.crearPlanViaje(destinoId, nombre, descripcion, duracionDias, categoria,
                                    precioBase, cupoMaximo, incluyeHotel, incluyeTransporte,
                                    incluyeComidas, incluyeTours);
    }

    private static String actualizar(BPlanViaje bPlan, String params) {
        String[] parts = params.split(",");
        if (parts.length < 12) {
            return "Error: Faltan parametros. Uso: actualizar(id, destinoId, nombre, descripcion, duracionDias, categoria, precioBase, cupoMaximo|null, incluyeHotel, incluyeTransporte, incluyeComidas, incluyeTours)";
        }

        int id = Integer.parseInt(parts[0].trim());
        int destinoId = Integer.parseInt(parts[1].trim());
        String nombre = parts[2].trim();
        String descripcion = parts[3].trim();
        int duracionDias = Integer.parseInt(parts[4].trim());
        String categoria = parts[5].trim();
        BigDecimal precioBase = new BigDecimal(parts[6].trim());
        Integer cupoMaximo = parseNullableInteger(parts[7]);
        boolean incluyeHotel = Boolean.parseBoolean(parts[8].trim());
        boolean incluyeTransporte = Boolean.parseBoolean(parts[9].trim());
        boolean incluyeComidas = Boolean.parseBoolean(parts[10].trim());
        boolean incluyeTours = Boolean.parseBoolean(parts[11].trim());

        return bPlan.actualizarPlanViaje(id, destinoId, nombre, descripcion, duracionDias, categoria,
                                        precioBase, cupoMaximo, incluyeHotel, incluyeTransporte,
                                        incluyeComidas, incluyeTours);
    }

    private static String desactivar(BPlanViaje bPlan, String params) {
        int id = Integer.parseInt(params.trim());
        return bPlan.desactivarPlan(id);
    }

    private static String reactivar(BPlanViaje bPlan, String params) {
        int id = Integer.parseInt(params.trim());
        return bPlan.reactivarPlan(id);
    }

    private static String calcularPrecio(BPlanViaje bPlan, String params) {
        int id = Integer.parseInt(params.trim());
        return bPlan.calcularPrecioSugerido(id);
    }

    private static String listarActivos(BPlanViaje bPlan) {
        List<String[]> planes = bPlan.listarPlanesActivos();
        
        if (planes.isEmpty()) {
            return "No hay planes activos";
        }
        
        StringBuilder sb = new StringBuilder("=== PLANES DE VIAJE ACTIVOS ===\n");
        for (String[] p : planes) {
            sb.append("ID: ").append(p[0])
              .append(" | ").append(p[1])
              .append(" | Categoria: ").append(p[10])
              .append(" | Precio: Bs. ").append(p[6])
              .append("\n");
        }
        return sb.toString();
    }

    private static String listar(BPlanViaje bPlan) {
        List<String[]> planes = bPlan.listarPlanes();
        
        if (planes.isEmpty()) {
            return "No hay planes registrados";
        }
        
        StringBuilder sb = new StringBuilder("=== TODOS LOS PLANES ===\n");
        for (String[] p : planes) {
            sb.append("ID: ").append(p[0])
              .append(" | ").append(p[1])
              .append(" | Dias: ").append(p[5])
              .append(" | Activo: ").append(p[12])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BPlanViaje bPlan, String params) {
        int id = Integer.parseInt(params.trim());
        String[] plan = bPlan.buscarPorId(id);
        
        if (plan == null) {
            return "Plan no encontrado";
        }
        
        return "Plan: " + plan[1] + "\n" +
               "Descripcion: " + plan[2] + "\n" +
               "Destino: " + plan[4] + "\n" +
               "Duracion: " + plan[5] + " dias\n" +
               "Categoria: " + plan[10] + "\n" +
               "Precio: Bs. " + plan[6] + "\n" +
               "Cupo maximo: " + ("null".equals(plan[11]) ? "sin limite" : plan[11]) + "\n" +
               "Incluye hotel: " + plan[7] + "\n" +
               "Incluye transporte: " + plan[8] + "\n" +
               "Incluye comidas: " + plan[9];
    }

    private static Integer parseNullableInteger(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("null")) {
            return null;
        }
        return Integer.valueOf(trimmed);
    }
}
