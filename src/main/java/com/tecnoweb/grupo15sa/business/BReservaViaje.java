package com.tecnoweb.grupo15sa.business;

import com.tecnoweb.grupo15sa.data.DReservaViaje;
import com.tecnoweb.grupo15sa.data.DViaje;
import com.tecnoweb.grupo15sa.data.DVenta;
import java.math.BigDecimal;
import java.util.List;

public class BReservaViaje {
    private DReservaViaje dReservaViaje;
    private DViaje dViaje;
    private DVenta dVenta;

    public BReservaViaje() {
        this.dReservaViaje = new DReservaViaje();
        this.dViaje = new DViaje();
        this.dVenta = new DVenta();
    }

    /**
     * Crea una nueva reserva de viaje
     */
    public String crearReserva(int viajeId, int ventaId, int numeroPersonas) {
        // Validar viaje
        String[] viaje = dViaje.findOneById(viajeId);
        if (viaje == null) {
            return "Error: Viaje no encontrado";
        }
        
        if (!viaje[8].equals("PROGRAMADO")) {
            return "Error: Solo se pueden reservar viajes en estado PROGRAMADO";
        }
        
        // Validar venta
        String[] venta = dVenta.findOneById(ventaId);
        if (venta == null) {
            return "Error: Venta no encontrada";
        }
        
        // Validar número de personas
        if (numeroPersonas <= 0) {
            return "Error: El número de personas debe ser mayor a 0";
        }
        
        int cuposDisponibles = Integer.parseInt(viaje[5]);
        if (numeroPersonas > cuposDisponibles) {
            return "Error: No hay suficientes cupos. Disponibles: " + cuposDisponibles;
        }
        
        // Calcular precio total
        BigDecimal precioViaje = new BigDecimal(viaje[7]);
        BigDecimal precioTotal = precioViaje.multiply(new BigDecimal(numeroPersonas));
        
        String estado = "PENDIENTE";
        
        // Crear reserva
        String[] result = dReservaViaje.save(viajeId, ventaId, numeroPersonas, precioTotal, estado);
        
        if (result[0].equals("1")) {
            // Decrementar cupos del viaje
            String[] resultCupos = dViaje.decrementarCupos(viajeId, numeroPersonas);
            
            if (resultCupos[0].equals("1")) {
                return "Reserva creada exitosamente con ID: " + result[1];
            } else {
                return "Reserva creada pero error al actualizar cupos: " + resultCupos[1];
            }
        } else {
            return result[1];
        }
    }

    /**
     * Actualiza una reserva existente
     */
    public String actualizarReserva(int id, int viajeId, int ventaId, int numeroPersonas,
                                    BigDecimal precioTotal, String estado) {
        String[] reserva = dReservaViaje.findOneById(id);
        if (reserva == null) {
            return "Error: Reserva no encontrada";
        }
        
        if (!validarEstado(estado)) {
            return "Error: Estado inválido (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)";
        }
        
        String[] result = dReservaViaje.update(id, viajeId, ventaId, numeroPersonas, precioTotal, estado);
        
        if (result != null && result[0].equals("1")) {
            return "Reserva actualizada exitosamente";
        } else {
            return result != null ? result[1] : "Error al actualizar reserva";
        }
    }

    /**
     * Confirma una reserva
     */
    public String confirmarReserva(int id) {
        String[] reserva = dReservaViaje.findOneById(id);
        if (reserva == null) {
            return "Error: Reserva no encontrada";
        }
        
        if (reserva[5].equals("CONFIRMADA")) {
            return "Error: La reserva ya está confirmada";
        }
        
        if (reserva[5].equals("CANCELADA")) {
            return "Error: No se puede confirmar una reserva cancelada";
        }
        
        String[] result = dReservaViaje.updateEstado(id, "CONFIRMADA");
        
        if (result != null && result[0].equals("1")) {
            return "Reserva confirmada exitosamente";
        } else {
            return result != null ? result[1] : "Error al confirmar reserva";
        }
    }

    /**
     * Cancela una reserva y libera cupos
     */
    public String cancelarReserva(int id, String motivo) {
        String[] reserva = dReservaViaje.findOneById(id);
        if (reserva == null) {
            return "Error: Reserva no encontrada";
        }
        
        if (reserva[5].equals("CANCELADA")) {
            return "Error: La reserva ya está cancelada";
        }
        
        if (reserva[5].equals("COMPLETADA")) {
            return "Error: No se puede cancelar una reserva completada";
        }
        
        int viajeId = Integer.parseInt(reserva[1]);
        int numeroPersonas = Integer.parseInt(reserva[3]);
        
        // Cancelar reserva
        String[] result = dReservaViaje.updateEstado(id, "CANCELADA");
        
        if (result[0].equals("1")) {
            // Incrementar cupos del viaje
            String[] resultCupos = dViaje.incrementarCupos(viajeId, numeroPersonas);
            
            if (resultCupos[0].equals("1")) {
                return "Reserva cancelada. Cupos liberados: " + numeroPersonas + 
                       ". Motivo: " + motivo;
            } else {
                return "Reserva cancelada pero error al liberar cupos: " + resultCupos[1];
            }
        } else {
            return result[1];
        }
    }

    /**
     * Completa una reserva (viaje realizado)
     */
    public String completarReserva(int id) {
        String[] reserva = dReservaViaje.findOneById(id);
        if (reserva == null) {
            return "Error: Reserva no encontrada";
        }
        
        if (!reserva[5].equals("CONFIRMADA")) {
            return "Error: Solo se pueden completar reservas confirmadas";
        }
        
        String[] result = dReservaViaje.updateEstado(id, "COMPLETADA");
        
        if (result[0].equals("1")) {
            return "Reserva completada exitosamente";
        } else {
            return result[1];
        }
    }

    /**
     * Lista todas las reservas
     */
    public List<String[]> listarReservas() {
        return dReservaViaje.findAll();
    }

    /**
     * Busca una reserva por ID
     */
    public String[] buscarPorId(int id) {
        return dReservaViaje.findOneById(id);
    }

    /**
     * Lista reservas por viaje
     */
    public List<String[]> listarPorViaje(int viajeId) {
        return dReservaViaje.findByViaje(viajeId);
    }

    /**
     * Lista reservas por venta
     */
    public List<String[]> listarPorVenta(int ventaId) {
        return dReservaViaje.findByVenta(ventaId);
    }

    /**
     * Lista reservas por estado
     */
    public List<String[]> listarPorEstado(String estado) {
        if (!validarEstado(estado)) {
            return null;
        }
        return dReservaViaje.findByEstado(estado);
    }

    /**
     * Lista reservas de un cliente
     */
    public List<String[]> listarPorCliente(int clienteId) {
        return dReservaViaje.findByCliente(clienteId);
    }

    /**
     * Valida estados de reserva
     */
    private boolean validarEstado(String estado) {
        return estado != null && (estado.equals("PENDIENTE") || estado.equals("CONFIRMADA") ||
               estado.equals("CANCELADA") || estado.equals("COMPLETADA") || estado.equals("PAGADA"));
    }

    /**
     * Calcula total de personas en un viaje
     */
    public String contarPersonasPorViaje(int viajeId) {
        String[] result = dReservaViaje.contarPersonasPorViaje(viajeId);
        
        if (result[0].equals("1")) {
            return "Total de personas reservadas: " + result[1];
        } else {
            return result[1];
        }
    }
}
