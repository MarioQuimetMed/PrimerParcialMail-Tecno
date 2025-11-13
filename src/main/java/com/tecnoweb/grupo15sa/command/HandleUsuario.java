package com.tecnoweb.grupo15sa.command;

import com.tecnoweb.grupo15sa.business.BUsuario;
import java.util.List;

public class HandleUsuario {

    public static String execute(String command, String params) {
        BUsuario bUsuario = new BUsuario();
        
        try {
            switch (command) {
                case "registrar":
                    return registrar(bUsuario, params);
                case "autenticar":
                    return autenticar(bUsuario, params);
                case "actualizar":
                    return actualizar(bUsuario, params);
                case "desactivar":
                    return desactivar(bUsuario, params);
                case "reactivar":
                    return reactivar(bUsuario, params);
                case "cambiarPassword":
                    return cambiarPassword(bUsuario, params);
                case "listar":
                    return listar(bUsuario);
                case "buscar":
                    return buscar(bUsuario, params);
                case "estadisticas":
                    return estadisticas(bUsuario);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String registrar(BUsuario bUsuario, String params) {
        String[] parts = params.split(",");
        if (parts.length < 7) {
            return "Error: Faltan parámetros. Uso: registrar (nombre, apellido, cedula, email, telefono, password, rol)";
        }
        
        String nombre = parts[0].trim();
        String apellido = parts[1].trim();
        String cedula = parts[2].trim();
        String email = parts[3].trim();
        String telefono = parts[4].trim();
        String password = parts[5].trim();
        String rol = parts[6].trim();
        
        return bUsuario.registrarUsuario(nombre, apellido, cedula, email, telefono, password, rol);
    }

    private static String autenticar(BUsuario bUsuario, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: autenticar (email, password)";
        }
        
        String email = parts[0].trim();
        String password = parts[1].trim();
        
        String[] resultado = bUsuario.autenticarUsuario(email, password);
        if (resultado == null || resultado.length == 0) {
            return "Error: No se pudo autenticar al usuario";
        }
        
        if (resultado.length == 1 && resultado[0].startsWith("Error")) {
            return resultado[0];
        }
        
        String activo = resultado[8].equalsIgnoreCase("true") ? "SI" : "NO";
        StringBuilder sb = new StringBuilder();
        sb.append("Autenticacion exitosa\n");
        sb.append("ID: ").append(resultado[0]).append("\n");
        sb.append("Nombre: ").append(resultado[1]).append(" ").append(resultado[2]).append("\n");
        sb.append("Cedula: ").append(resultado[3]).append("\n");
        sb.append("Email: ").append(resultado[4]).append("\n");
        sb.append("Telefono: ").append(resultado[5]).append("\n");
        sb.append("Rol: ").append(resultado[7]).append("\n");
        sb.append("Activo: ").append(activo).append("\n");
        sb.append("Fecha registro: ").append(resultado[9]);
        
        return sb.toString();
    }

    private static String actualizar(BUsuario bUsuario, String params) {
        String[] parts = params.split(",");
        if (parts.length < 8) {
            return "Error: Faltan parámetros. Uso: actualizar (id, nombre, apellido, cedula, email, telefono, password, rol)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String nombre = parts[1].trim();
        String apellido = parts[2].trim();
        String cedula = parts[3].trim();
        String email = parts[4].trim();
        String telefono = parts[5].trim();
        String password = parts[6].trim();
        String rol = parts[7].trim();
        
        return bUsuario.actualizarUsuario(id, nombre, apellido, cedula, email, telefono, password, rol);
    }

    private static String desactivar(BUsuario bUsuario, String params) {
        int id = Integer.parseInt(params.trim());
        return bUsuario.desactivarUsuario(id);
    }

    private static String reactivar(BUsuario bUsuario, String params) {
        int id = Integer.parseInt(params.trim());
        return bUsuario.reactivarUsuario(id);
    }

    private static String cambiarPassword(BUsuario bUsuario, String params) {
        String[] parts = params.split(",");
        if (parts.length < 3) {
            return "Error: Faltan parámetros. Uso: cambiarPassword (id, passwordActual, passwordNueva)";
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String passwordActual = parts[1].trim();
        String passwordNueva = parts[2].trim();
        
        return bUsuario.cambiarPassword(id, passwordActual, passwordNueva);
    }

    private static String listar(BUsuario bUsuario) {
        List<String[]> usuarios = bUsuario.listarUsuarios();
        
        if (usuarios.isEmpty()) {
            return "No hay usuarios registrados";
        }
        
        StringBuilder sb = new StringBuilder("=== USUARIOS REGISTRADOS ===\n");
        for (String[] u : usuarios) {
            sb.append("ID: ").append(u[0])
              .append(" | ").append(u[1]).append(" ").append(u[2])
              .append(" | Email: ").append(u[4])
              .append(" | Rol: ").append(u[8])
              .append(" | Activo: ").append(u[9])
              .append("\n");
        }
        return sb.toString();
    }

    private static String buscar(BUsuario bUsuario, String params) {
        int id = Integer.parseInt(params.trim());
        String[] usuario = bUsuario.buscarPorId(id);
        
        if (usuario == null) {
            return "Usuario no encontrado";
        }
        
        return "Usuario: " + usuario[1] + " " + usuario[2] + "\n" +
               "Email: " + usuario[4] + "\n" +
               "Teléfono: " + usuario[5] + "\n" +
               "Rol: " + usuario[8] + "\n" +
               "Activo: " + usuario[9];
    }

    private static String estadisticas(BUsuario bUsuario) {
        String[] datos = bUsuario.obtenerEstadisticas();
        if (datos == null || datos.length < 6) {
            return "No hay información de usuarios disponible";
        }
        
        StringBuilder sb = new StringBuilder("=== ESTADISTICAS DE USUARIOS ===\n");
        sb.append("Total usuarios: ").append(datos[0]).append("\n");
        sb.append("Propietarios: ").append(datos[1]).append("\n");
        sb.append("Vendedores: ").append(datos[2]).append("\n");
        sb.append("Clientes: ").append(datos[3]).append("\n");
        sb.append("Activos: ").append(datos[4]).append("\n");
        sb.append("Inactivos: ").append(datos[5]);
        return sb.toString();
    }
}
