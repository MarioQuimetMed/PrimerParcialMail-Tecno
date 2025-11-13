package com.tecnoweb.grupo15sa.business;

import com.tecnoweb.grupo15sa.data.DUsuario;

import java.util.List;
import java.util.regex.Pattern;

public class BUsuario {
    private DUsuario dUsuario;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{7,15}$"
    );

    public BUsuario() {
        this.dUsuario = new DUsuario();
    }

    /**
     * Registra un nuevo usuario con validaciones
     */
    public String registrarUsuario(String nombre, String apellido, String cedula, String email, 
                                   String telefono, String password, String rol) {
        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: El nombre es obligatorio";
        }
        
        if (nombre.length() < 3) {
            return "Error: El nombre debe tener al menos 3 caracteres";
        }
        
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Error: Email inválido";
        }
        
        if (telefono != null && !telefono.isEmpty() && !PHONE_PATTERN.matcher(telefono).matches()) {
            return "Error: Teléfono inválido (debe contener solo números, 7-15 dígitos)";
        }
        
        if (cedula == null || cedula.trim().isEmpty()) {
            return "Error: La cédula es obligatoria";
        }
        
        if (password == null || password.length() < 6) {
            return "Error: La contraseña debe tener al menos 6 caracteres";
        }
        
        if (!validarRol(rol)) {
            return "Error: Rol inválido. Debe ser PROPIETARIO, VENDEDOR o CLIENTE";
        }
        
        // Verificar si ya existe el email
        String[] usuarioExistente = dUsuario.findByEmail(email);
        if (usuarioExistente != null) {
            return "Error: El email ya está registrado";
        }
        
        // Verificar si ya existe la cédula
        usuarioExistente = dUsuario.findByCedula(cedula);
        if (usuarioExistente != null) {
            return "Error: La cédula ya está registrada";
        }
        
        // Registrar usuario (DUsuario.save retorna String directamente)
        return dUsuario.save(nombre, apellido, cedula, email, telefono, password, rol);
    }

    /**
     * Actualiza información de un usuario
     */
    public String actualizarUsuario(int id, String nombre, String apellido, String cedula,
                                    String email, String telefono, String password, String rol) {
        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: El nombre es obligatorio";
        }
        
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Error: Email inválido";
        }
        
        if (telefono != null && !telefono.isEmpty() && !PHONE_PATTERN.matcher(telefono).matches()) {
            return "Error: Teléfono inválido";
        }
        
        if (!validarRol(rol)) {
            return "Error: Rol inválido";
        }
        
        // Verificar que el usuario existe
        String[] usuario = dUsuario.findOneById(id);
        if (usuario == null) {
            return "Error: Usuario no encontrado";
        }
        
        // Verificar email único (excepto el mismo usuario)
        String[] usuarioEmail = dUsuario.findByEmail(email);
        if (usuarioEmail != null && !usuarioEmail[0].equals(String.valueOf(id))) {
            return "Error: El email ya está en uso por otro usuario";
        }
        
        return dUsuario.update(id, nombre, apellido, cedula, email, telefono, password, rol);
    }

    /**
     * Desactiva un usuario (soft delete)
     */
    public String desactivarUsuario(int id) {
        String[] usuario = dUsuario.findOneById(id);
        if (usuario == null) {
            return "Error: Usuario no encontrado";
        }
        
        return dUsuario.delete(id);
    }

    /**
     * Reactiva un usuario
     */
    public String reactivarUsuario(int id) {
        return dUsuario.reactivate(id);
    }

    /**
     * Autentica un usuario
     */
    public String[] autenticarUsuario(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new String[]{"Error: Email requerido"};
        }
        
        if (password == null || password.trim().isEmpty()) {
            return new String[]{"Error: Contraseña requerida"};
        }
        
        String[] usuario = dUsuario.authenticateUser(email, password);
        
        if (usuario == null) {
            return new String[]{"Error: Email o contraseña incorrectos"};
        }
        
        // Verificar si está activo (índice 8)
        if (usuario[8].equals("false")) {
            return new String[]{"Error: Usuario inactivo. Contacte al administrador"};
        }
        
        return usuario; // Retorna datos del usuario autenticado
    }

    /**
     * Obtiene todos los usuarios
     */
    public List<String[]> listarUsuarios() {
        return dUsuario.findAllUsers();
    }

    /**
     * Obtiene usuarios por rol
     */
    public List<String[]> listarPorRol(String rol) {
        if (!validarRol(rol)) {
            return null;
        }
        return dUsuario.findByRole(rol);
    }

    /**
     * Busca un usuario por ID
     */
    public String[] buscarPorId(int id) {
        return dUsuario.findOneById(id);
    }

    /**
     * Busca un usuario por email
     */
    public String[] buscarPorEmail(String email) {
        return dUsuario.findByEmail(email);
    }

    /**
     * Busca un usuario por cédula
     */
    public String[] buscarPorCedula(String cedula) {
        return dUsuario.findByCedula(cedula);
    }

    /**
     * Cambia la contraseña de un usuario
     */
    public String cambiarPassword(int id, String passwordActual, String passwordNuevo) {
        String[] usuario = dUsuario.findOneById(id);
        
        if (usuario == null) {
            return "Error: Usuario no encontrado";
        }
        
        if (!usuario[6].equals(passwordActual)) {
            return "Error: Contraseña actual incorrecta";
        }
        
        if (passwordNuevo == null || passwordNuevo.length() < 6) {
            return "Error: La nueva contraseña debe tener al menos 6 caracteres";
        }
        
        return dUsuario.update(id, usuario[1], usuario[2], usuario[3], usuario[4], usuario[5], passwordNuevo, usuario[6]);
    }

    /**
     * Valida que el rol sea correcto
     */
    private boolean validarRol(String rol) {
        return rol != null && (rol.equals("PROPIETARIO") || rol.equals("VENDEDOR") || rol.equals("CLIENTE"));
    }

    /**
     * Obtiene estadísticas de usuarios
     */
    public String[] obtenerEstadisticas() {
        List<String[]> usuarios = dUsuario.findAllUsers();
        
        int totalPropietarios = 0;
        int totalVendedores = 0;
        int totalClientes = 0;
        int totalActivos = 0;
        int totalInactivos = 0;
        
        for (String[] usuario : usuarios) {
            String rol = usuario[7];
            boolean activo = usuario[8].equals("true");
            
            if (activo) totalActivos++;
            else totalInactivos++;
            
            switch (rol) {
                case "PROPIETARIO":
                    totalPropietarios++;
                    break;
                case "VENDEDOR":
                    totalVendedores++;
                    break;
                case "CLIENTE":
                    totalClientes++;
                    break;
            }
        }
        
        return new String[]{
            String.valueOf(usuarios.size()),
            String.valueOf(totalPropietarios),
            String.valueOf(totalVendedores),
            String.valueOf(totalClientes),
            String.valueOf(totalActivos),
            String.valueOf(totalInactivos)
        };
    }
}
