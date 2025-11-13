package com.tecnoweb.grupo7sa.business;

import com.tecnoweb.grupo7sa.data.DViaje;
import com.tecnoweb.grupo7sa.data.DReservaViaje;
import com.tecnoweb.grupo7sa.data.DPlanViaje;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class BViaje {
    private DViaje dViaje;
    private DReservaViaje dReservaViaje;
    private DPlanViaje dPlanViaje;

    public BViaje() {
        this.dViaje = new DViaje();
        this.dReservaViaje = new DReservaViaje();
        this.dPlanViaje = new DPlanViaje();
    }

    /**
     * Programa un nuevo viaje
     */
    public String programarViaje(int planViajeId, String codigoViaje, Date fechaSalida,
                                 Date fechaRetorno, int cuposTotales, BigDecimal precioFinal) {
        // Validaciones
        String[] plan = dPlanViaje.findOneById(planViajeId);
        if (plan == null) {
            return "Error: Plan de viaje no encontrado";
        }
        
        if (codigoViaje == null || codigoViaje.trim().isEmpty()) {
            return "Error: El código del viaje es obligatorio";
        }
        
        // Verificar código único
        String[] viajeExistente = dViaje.findByCodigo(codigoViaje);
        if (viajeExistente != null) {
            return "Error: El código de viaje ya existe";
        }
        
        if (fechaSalida == null || fechaRetorno == null) {
            return "Error: Las fechas de salida y retorno son obligatorias";
        }
        
        if (fechaRetorno.before(fechaSalida)) {
            return "Error: La fecha de retorno debe ser posterior a la fecha de salida";
        }
        
        if (cuposTotales <= 0) {
            return "Error: Los cupos totales deben ser mayor a 0";
        }
        
        // plan[11] = cupo_maximo (puede ser null)
        if (plan[11] != null && !plan[11].equals("null")) {
            int cupoMaximo = Integer.parseInt(plan[11]);
            if (cuposTotales > cupoMaximo) {
                return "Error: Los cupos totales no pueden exceder " + cupoMaximo + " (máximo del plan)";
            }
        }
        
        if (precioFinal == null || precioFinal.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El precio final debe ser mayor a 0";
        }
        
        String estado = "PROGRAMADO";
        int cuposDisponibles = cuposTotales;
        
        String[] result = dViaje.save(planViajeId, codigoViaje, fechaSalida, fechaRetorno,
                                     cuposDisponibles, cuposTotales, precioFinal, estado);
        
        if (result[0].equals("1")) {
            return "Viaje programado exitosamente con ID: " + result[1];
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza información de un viaje
     */
    public String actualizarViaje(int id, int planViajeId, String codigoViaje, Date fechaSalida,
                                  Date fechaRetorno, int cuposDisponibles, int cuposTotales,
                                  BigDecimal precioFinal, String estado) {
        String[] viaje = dViaje.findOneById(id);
        if (viaje == null) {
            return "Error: Viaje no encontrado";
        }
        
        if (!validarEstado(estado)) {
            return "Error: Estado inválido (PROGRAMADO, EN_CURSO, COMPLETADO, CANCELADO)";
        }
        
        if (cuposDisponibles > cuposTotales) {
            return "Error: Los cupos disponibles no pueden ser mayores a los cupos totales";
        }
        
        String[] result = dViaje.update(id, planViajeId, codigoViaje, fechaSalida, fechaRetorno,
                                       cuposDisponibles, cuposTotales, precioFinal, estado);
        
        if (result[0].equals("1")) {
            return "Viaje actualizado exitosamente";
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza el estado de un viaje
     */
    public String actualizarEstado(int id, String nuevoEstado) {
        if (!validarEstado(nuevoEstado)) {
            return "Error: Estado inválido";
        }
        
        String[] result = dViaje.updateEstado(id, nuevoEstado);
        
        if (result[0].equals("1")) {
            return "Estado actualizado a " + nuevoEstado;
        } else {
            return result[1];
        }
    }

    /**
     * Verifica disponibilidad de cupos
     */
    public String verificarDisponibilidad(int viajeId, int cuposSolicitados) {
        String[] viaje = dViaje.findOneById(viajeId);
        if (viaje == null) {
            return "Error: Viaje no encontrado";
        }
        
        int cuposDisponibles = Integer.parseInt(viaje[5]);
        
        if (cuposSolicitados > cuposDisponibles) {
            return "No disponible. Cupos solicitados: " + cuposSolicitados + 
                   ", disponibles: " + cuposDisponibles;
        }
        
        return "Disponible";
    }

    /**
     * Cancela un viaje
     */
    public String cancelarViaje(int id, String motivo) {
        String[] viaje = dViaje.findOneById(id);
        if (viaje == null) {
            return "Error: Viaje no encontrado";
        }
        
        if (viaje[8].equals("CANCELADO")) {
            return "Error: El viaje ya está cancelado";
        }
        
        if (viaje[8].equals("COMPLETADO")) {
            return "Error: No se puede cancelar un viaje completado";
        }
        
        if (viaje[8].equals("EN_CURSO")) {
            return "Error: No se puede cancelar un viaje en curso";
        }
        
        // Verificar si hay reservas
        List<String[]> reservas = dReservaViaje.findByViaje(id);
        if (!reservas.isEmpty()) {
            return "Advertencia: El viaje tiene " + reservas.size() + 
                   " reservas. Se deben gestionar las cancelaciones. Motivo: " + motivo;
        }
        
        String[] result = dViaje.updateEstado(id, "CANCELADO");
        
        if (result[0].equals("1")) {
            return "Viaje cancelado exitosamente. Motivo: " + motivo;
        } else {
            return result[1];
        }
    }

    /**
     * Lista todos los viajes
     */
    public List<String[]> listarViajes() {
        return dViaje.findAll();
    }

    /**
     * Busca un viaje por ID
     */
    public String[] buscarPorId(int id) {
        return dViaje.findOneById(id);
    }

    /**
     * Busca viaje por código
     */
    public String[] buscarPorCodigo(String codigo) {
        return dViaje.findByCodigo(codigo);
    }

    /**
     * Lista viajes por estado
     */
    public List<String[]> listarPorEstado(String estado) {
        if (!validarEstado(estado)) {
            return null;
        }
        return dViaje.findByEstado(estado);
    }

    /**
     * Lista viajes disponibles
     */
    public List<String[]> listarViajesDisponibles() {
        return dViaje.findDisponibles();
    }

    /**
     * Lista viajes por rango de fechas
     */
    public List<String[]> listarPorFechas(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null || fechaFin.before(fechaInicio)) {
            return null;
        }
        return dViaje.findByFechas(fechaInicio, fechaFin);
    }

    /**
     * Valida estados de viaje
     */
    private boolean validarEstado(String estado) {
        return estado != null && (estado.equals("PROGRAMADO") || estado.equals("EN_CURSO") ||
               estado.equals("COMPLETADO") || estado.equals("CANCELADO"));
    }

    /**
     * Calcula porcentaje de ocupación
     */
    public String calcularOcupacion(int viajeId) {
        String[] viaje = dViaje.findOneById(viajeId);
        if (viaje == null) {
            return "Error: Viaje no encontrado";
        }
        
        int cuposTotales = Integer.parseInt(viaje[6]);
        int cuposDisponibles = Integer.parseInt(viaje[5]);
        int cuposVendidos = cuposTotales - cuposDisponibles;
        
        double porcentaje = (cuposVendidos * 100.0) / cuposTotales;
        
        return String.format("Ocupación: %.2f%% (%d/%d cupos)", porcentaje, cuposVendidos, cuposTotales);
    }
}
