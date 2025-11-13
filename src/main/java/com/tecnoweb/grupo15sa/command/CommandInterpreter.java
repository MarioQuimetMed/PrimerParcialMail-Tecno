package com.tecnoweb.grupo15sa.command;

import java.util.HashMap;
import java.util.Map;

public class CommandInterpreter {

    private static final Map<String, String[]> COMMANDS = new HashMap<>();

    static {
        // CU1 - Gestión de Usuarios
        COMMANDS.put("usuario", new String[]{"registrar", "autenticar", "actualizar", "desactivar", "reactivar", "cambiarPassword", "listar", "buscar", "estadisticas"});
        
        // CU2 - Gestión de Destinos Turísticos
        COMMANDS.put("destino", new String[]{"registrar", "actualizar", "desactivar", "reactivar", "listarActivos", "buscarPorPais", "buscarPorCiudad", "buscarPorRango", "listar"});
        
        // CU3 - Gestión de Planes de Viaje
        COMMANDS.put("planviaje", new String[]{"crear", "actualizar", "desactivar", "reactivar", "calcularPrecio", "listarActivos", "listar", "buscar"});
        
        // CU4 - Gestión de Ventas
        COMMANDS.put("venta", new String[]{"registrar", "actualizar", "actualizarEstado", "registrarPago", "cancelar", "listar", "buscarPorCliente", "buscar"});
        
        // CU5 - Gestión de Planes de Pago
        COMMANDS.put("planpago", new String[]{"crear", "actualizarEstado", "cancelar", "verificar", "listarPorVenta", "listarVencidos", "listarActivos", "listar", "buscar", "calcularResumen"});
        
        // CU6 - Gestión de Viajes
        COMMANDS.put("viaje", new String[]{"programar", "actualizar", "actualizarEstado", "verificarDisponibilidad", "cancelar", "listarProgramados", "calcularOcupacion", "listar", "buscar"});
        
        // CU7 - Gestión de Reservas
        COMMANDS.put("reserva", new String[]{"crear", "confirmar", "cancelar", "completar", "listarPorViaje", "listarPorCliente", "contarPersonas", "listar", "buscar"});
        
        // CU8 - Gestión de Cuotas
        COMMANDS.put("cuota", new String[]{"pagar", "listarPorPlan", "listarVencidas", "listarProximas", "calcularTotal", "actualizarVencidas", "listar", "buscar", "generarRecordatorio"});
        
        // CU9 - Gestión de Pagos
        COMMANDS.put("pago", new String[]{"registrar", "anular", "listarPorVenta", "buscarPorRecibo", "calcularTotal", "reconciliar", "listarPorMetodo", "listar", "buscar", "generarComprobante"});
        
        // CU10 - Reportes
        COMMANDS.put("reporte", new String[]{"ventas", "pagosPorMetodo", "viajesProgramados", "reservasPorEstado", "cuotasVencidas", "destinosMasVendidos", "dashboard", "ingresosMensuales"});
    }

    public static String interpret(String subject) {
        subject = subject.replaceAll("[^\\p{L}\\p{N}\\s\\(\\),./@_-]", "");
        subject = subject.replaceAll("\\s+", " ").trim();

        System.out.println("Subject luego de formatear: " + subject);

        if (subject.equalsIgnoreCase("help")) {
            return getHelpMessage();
        }

        String pattern = "([a-zA-Z]+)\\s+([a-zA-Z]+)\\s*\\((.*)\\)";
        java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = regex.matcher(subject);

        if (!matcher.matches()) {
            return "Comando no reconocido. Por favor, asegúrate de seguir la estructura: {entidad} {comando} (parametros)";
        }

        String entity = matcher.group(1).trim().toLowerCase();
        String command = matcher.group(2).trim();
        String params = matcher.group(3).trim();

        if (!COMMANDS.containsKey(entity)) {
            return "Entidad '" + entity + "' no reconocida. Usa 'help' para ver entidades disponibles.";
        }

        boolean commandExists = false;
        for (String validCommand : COMMANDS.get(entity)) {
            if (validCommand.equals(command)) {
                commandExists = true;
                break;
            }
        }

        if (!commandExists) {
            return "Comando '" + command + "' no reconocido para '" + entity + "'. Usa 'help' para ver comandos disponibles.";
        }

        // Ejecutar comandos según entidad
        switch (entity) {
            case "usuario":
                return HandleUsuario.execute(command, params);
            case "destino":
                return HandleDestino.execute(command, params);
            case "planviaje":
                return HandlePlanViaje.execute(command, params);
            case "venta":
                return HandleVenta.execute(command, params);
            case "planpago":
                return HandlePlanPago.execute(command, params);
            case "viaje":
                return HandleViaje.execute(command, params);
            case "reserva":
                return HandleReservaViaje.execute(command, params);
            case "cuota":
                return HandleCuota.execute(command, params);
            case "pago":
                return HandlePago.execute(command, params);
            case "reporte":
                return HandleReporte.execute(command, params);
            default:
                return "Entidad no implementada: " + entity;
        }
    }

    private static String getHelpMessage() {
        return "**************** SISTEMA DE AGENCIA DE VIAJES ****************\r\n" +
                "\r\n" +
                "Formato general: {entidad} {comando} (parametros)\r\n" +
                "- Todo en minuscula.\r\n" +
                "- Usa solo el asunto del correo.\r\n" +
                "- Parametros separados por coma. Escribe 'null' para campos opcionales.\r\n" +
                "- Fechas: YYYY-MM-DD. Valores booleanos: true / false.\r\n" +
                "\r\n" +
                "=== USUARIO ===\r\n" +
                "registrar(nombre,apellido,cedula,email,telefono,password,rol)\r\n" +
                "autenticar(email,password)\r\n" +
                "actualizar(id,nombre,apellido,cedula,email,telefono,password,rol)\r\n" +
                "desactivar(id) | reactivar(id) | cambiarPassword(id,actual,nueva)\r\n" +
                "listar() | buscar(id) | estadisticas()\r\n" +
                "\r\n" +
                "=== DESTINO ===\r\n" +
                "registrar(nombre,pais,ciudad,descripcion,clima,precioReferencia)\r\n" +
                "actualizar(id,nombre,pais,ciudad,descripcion,clima,precioReferencia)\r\n" +
                "desactivar(id) | reactivar(id)\r\n" +
                "listarActivos() | listar() | buscar(id)\r\n" +
                "buscarPorPais(pais) | buscarPorCiudad(ciudad) | buscarPorRango(min,max)\r\n" +
                "\r\n" +
                "=== PLANVIAJE ===\r\n" +
                "crear(destinoId,nombre,descripcion,duracionDias,categoria,precioBase,cupoMaximo|null,incluyeHotel,incluyeTransporte,incluyeComidas,incluyeTours)\r\n" +
                "actualizar(id,destinoId,nombre,descripcion,duracionDias,categoria,precioBase,cupoMaximo|null,incluyeHotel,incluyeTransporte,incluyeComidas,incluyeTours)\r\n" +
                "desactivar(id) | reactivar(id) | calcularPrecio(id)\r\n" +
                "listarActivos() | listar() | buscar(id)\r\n" +
                "\r\n" +
                "=== VENTA ===\r\n" +
                "registrar(clienteId,vendedorId,viajeId,tipoVenta,montoTotal,montoPagado,observaciones)\r\n" +
                "actualizar(id,clienteId,vendedorId,viajeId,tipoVenta,montoTotal,montoPagado,montoPendiente,estado,observaciones)\r\n" +
                "actualizarEstado(id,nuevoEstado) | registrarPago(id,monto)\r\n" +
                "cancelar(id,motivo) | listar() | buscar(id) | buscarPorCliente(clienteId)\r\n" +
                "\r\n" +
                "=== PLANPAGO ===\r\n" +
                "crear(ventaId,numeroCuotas,interesPorcentaje,fechaPrimerVencimiento)\r\n" +
                "actualizarEstado(id,nuevoEstado) | cancelar(id,motivo) | verificar(id)\r\n" +
                "listarPorVenta(ventaId) | listarVencidos() | listarActivos() | listar() | buscar(id)\r\n" +
                "calcularResumen(id)\r\n" +
                "\r\n" +
                "=== VIAJE ===\r\n" +
                "programar(planViajeId,codigoViaje,fechaSalida,fechaRetorno,cuposTotales,precioFinal)\r\n" +
                "actualizar(id,planViajeId,codigoViaje,fechaSalida,fechaRetorno,cuposDisponibles,cuposTotales,precioFinal,estado)\r\n" +
                "actualizarEstado(id,nuevoEstado) | verificarDisponibilidad(id,cuposSolicitados)\r\n" +
                "cancelar(id,motivo) | listarProgramados() | listar() | buscar(id) | calcularOcupacion(id)\r\n" +
                "\r\n" +
                "=== RESERVA ===\r\n" +
                "crear(viajeId,ventaId,numeroPersonas)\r\n" +
                "confirmar(id) | cancelar(id,motivo) | completar(id)\r\n" +
                "listarPorViaje(viajeId) | listarPorCliente(clienteId) | contarPersonas(viajeId)\r\n" +
                "listar() | buscar(id)\r\n" +
                "\r\n" +
                "=== CUOTA ===\r\n" +
                "pagar(cuotaId,montoPagado)\r\n" +
                "listarPorPlan(planPagoId) | listarVencidas() | listarProximas(dias)\r\n" +
                "calcularTotal(cuotaId) | actualizarVencidas() | listar() | buscar(id)\r\n" +
                "generarRecordatorio(cuotaId)\r\n" +
                "\r\n" +
                "=== PAGO ===\r\n" +
                "registrar(ventaId,monto,metodoPago,numeroRecibo,cuotaId|null,observaciones)\r\n" +
                "anular(id,motivo)\r\n" +
                "listarPorVenta(ventaId) | buscarPorRecibo(numeroRecibo) | calcularTotal(ventaId)\r\n" +
                "reconciliar(ventaId) | listarPorMetodo(metodo) | listar() | buscar(id)\r\n" +
                "generarComprobante(id)\r\n" +
                "\r\n" +
                "=== REPORTE ===\r\n" +
                "ventas(fechaInicio,fechaFin)\r\n" +
                "pagosPorMetodo(fechaInicio,fechaFin)\r\n" +
                "viajesProgramados() | reservasPorEstado() | cuotasVencidas()\r\n" +
                "destinosMasVendidos(limite) | dashboard() | ingresosMensuales(anio,mes)\r\n" +
                "\r\n" +
                "Flujo sugerido:\r\n" +
                "1) Registrar usuarios y destinos.\r\n" +
                "2) Crear planes de viaje, viajes y ventas.\r\n" +
                "3) Gestionar reservas, planes de pago y cuotas.\r\n" +
                "4) Registrar pagos y consultar reportes.\r\n" +
                "\r\n" +
                "El sistema responde siempre en texto plano al remitente del correo.\r\n" +
                "***************************************************************";
    }
}


