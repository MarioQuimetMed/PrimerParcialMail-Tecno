package com.tecnoweb.grupo7sa.data;

import com.tecnoweb.grupo7sa.ConfigDB.DatabaseConection;
import com.tecnoweb.grupo7sa.ConfigDB.ConfigDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DReservaViaje {
    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DReservaViaje() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    /**
     * Guarda una nueva reserva de viaje
     */
    public String[] save(int viajeId, int ventaId, int numeroPersonas, 
                        BigDecimal precioTotal, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "INSERT INTO RESERVA_VIAJE (viaje_id, venta_id, numero_personas, " +
                        "precio_total, estado) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, viajeId);
            statement.setInt(2, ventaId);
            statement.setInt(3, numeroPersonas);
            statement.setBigDecimal(4, precioTotal);
            statement.setString(5, estado);
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("id"));
            } else {
                result[0] = "-1";
                result[1] = "Error al guardar la reserva";
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            result[0] = "-1";
            result[1] = "Error SQL: " + e.getMessage();
        }
        return result;
    }

    /**
     * Actualiza una reserva existente
     */
    public String[] update(int id, int viajeId, int ventaId, int numeroPersonas,
                          BigDecimal precioTotal, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE RESERVA_VIAJE SET viaje_id = ?, venta_id = ?, numero_personas = ?, " +
                        "precio_total = ?, estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, viajeId);
            statement.setInt(2, ventaId);
            statement.setInt(3, numeroPersonas);
            statement.setBigDecimal(4, precioTotal);
            statement.setString(5, estado);
            statement.setInt(6, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Reserva actualizada correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró la reserva";
            }
            
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            result[0] = "-1";
            result[1] = "Error SQL: " + e.getMessage();
        }
        return result;
    }

    /**
     * Actualiza el estado de una reserva
     */
    public String[] updateEstado(int id, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE RESERVA_VIAJE SET estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            statement.setInt(2, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Estado actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró la reserva";
            }
            
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            result[0] = "-1";
            result[1] = "Error SQL: " + e.getMessage();
        }
        return result;
    }

    /**
     * Obtiene todas las reservas con información completa
     */
    public List<String[]> findAll() {
        List<String[]> reservas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT rv.*, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "pv.nombre as plan_nombre, d.ciudad, d.pais, " +
                        "ven.fecha_venta, u.nombre as cliente_nombre, u.email as cliente_email " +
                        "FROM RESERVA_VIAJE rv " +
                        "INNER JOIN VIAJE v ON rv.viaje_id = v.id " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "INNER JOIN VENTA ven ON rv.venta_id = ven.id " +
                        "INNER JOIN USUARIO u ON ven.cliente_id = u.id " +
                        "ORDER BY rv.fecha_reserva DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] reserva = new String[17];
                reserva[0] = String.valueOf(rs.getInt("id"));
                reserva[1] = String.valueOf(rs.getInt("viaje_id"));
                reserva[2] = String.valueOf(rs.getInt("venta_id"));
                reserva[3] = String.valueOf(rs.getInt("numero_personas"));
                reserva[4] = rs.getBigDecimal("precio_total").toString();
                reserva[5] = rs.getString("estado");
                reserva[6] = rs.getTimestamp("fecha_reserva").toString();
                reserva[7] = rs.getString("codigo_viaje");
                reserva[8] = rs.getDate("fecha_salida").toString();
                reserva[9] = rs.getDate("fecha_retorno").toString();
                reserva[10] = rs.getString("plan_nombre");
                reserva[11] = rs.getString("ciudad");
                reserva[12] = rs.getString("pais");
                reserva[13] = rs.getDate("fecha_venta").toString();
                reserva[14] = rs.getString("cliente_nombre");
                reserva[15] = rs.getString("cliente_email");
                reserva[16] = String.valueOf(rs.getInt("viaje_id"));
                reservas.add(reserva);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas: " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Obtiene una reserva por ID
     */
    public String[] findOneById(int id) {
        String[] reserva = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT rv.*, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "v.precio_final as viaje_precio, pv.nombre as plan_nombre, " +
                        "pv.descripcion as plan_descripcion, d.ciudad, d.pais, d.nombre as destino_nombre, " +
                        "ven.fecha_venta, ven.tipo_venta, ven.estado as venta_estado, " +
                        "u.nombre as cliente_nombre, u.email as cliente_email, u.telefono as cliente_telefono " +
                        "FROM RESERVA_VIAJE rv " +
                        "INNER JOIN VIAJE v ON rv.viaje_id = v.id " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "INNER JOIN VENTA ven ON rv.venta_id = ven.id " +
                        "INNER JOIN USUARIO u ON ven.cliente_id = u.id " +
                        "WHERE rv.id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                reserva = new String[22];
                reserva[0] = String.valueOf(rs.getInt("id"));
                reserva[1] = String.valueOf(rs.getInt("viaje_id"));
                reserva[2] = String.valueOf(rs.getInt("venta_id"));
                reserva[3] = String.valueOf(rs.getInt("numero_personas"));
                reserva[4] = rs.getBigDecimal("precio_total").toString();
                reserva[5] = rs.getString("estado");
                reserva[6] = rs.getTimestamp("fecha_reserva").toString();
                reserva[7] = rs.getString("codigo_viaje");
                reserva[8] = rs.getDate("fecha_salida").toString();
                reserva[9] = rs.getDate("fecha_retorno").toString();
                reserva[10] = rs.getBigDecimal("viaje_precio").toString();
                reserva[11] = rs.getString("plan_nombre");
                reserva[12] = rs.getString("plan_descripcion");
                reserva[13] = rs.getString("ciudad");
                reserva[14] = rs.getString("pais");
                reserva[15] = rs.getString("destino_nombre");
                reserva[16] = rs.getDate("fecha_venta").toString();
                reserva[17] = rs.getString("tipo_venta");
                reserva[18] = rs.getString("venta_estado");
                reserva[19] = rs.getString("cliente_nombre");
                reserva[20] = rs.getString("cliente_email");
                reserva[21] = rs.getString("cliente_telefono");
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener reserva: " + e.getMessage());
        }
        return reserva;
    }

    /**
     * Obtiene todas las reservas de un viaje específico
     */
    public List<String[]> findByViaje(int viajeId) {
        List<String[]> reservas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT rv.*, ven.fecha_venta, u.nombre as cliente_nombre, u.email as cliente_email " +
                        "FROM RESERVA_VIAJE rv " +
                        "INNER JOIN VENTA ven ON rv.venta_id = ven.id " +
                        "INNER JOIN USUARIO u ON ven.cliente_id = u.id " +
                        "WHERE rv.viaje_id = ? " +
                        "ORDER BY rv.fecha_reserva DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, viajeId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] reserva = new String[11];
                reserva[0] = String.valueOf(rs.getInt("id"));
                reserva[1] = String.valueOf(rs.getInt("viaje_id"));
                reserva[2] = String.valueOf(rs.getInt("venta_id"));
                reserva[3] = String.valueOf(rs.getInt("numero_personas"));
                reserva[4] = rs.getBigDecimal("precio_total").toString();
                reserva[5] = rs.getString("estado");
                reserva[6] = rs.getTimestamp("fecha_reserva").toString();
                reserva[7] = rs.getDate("fecha_venta").toString();
                reserva[8] = rs.getString("cliente_nombre");
                reserva[9] = rs.getString("cliente_email");
                reserva[10] = String.valueOf(rs.getInt("viaje_id"));
                reservas.add(reserva);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por viaje: " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Obtiene todas las reservas de una venta específica
     */
    public List<String[]> findByVenta(int ventaId) {
        List<String[]> reservas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT rv.*, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "pv.nombre as plan_nombre, d.ciudad, d.pais " +
                        "FROM RESERVA_VIAJE rv " +
                        "INNER JOIN VIAJE v ON rv.viaje_id = v.id " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE rv.venta_id = ? " +
                        "ORDER BY rv.fecha_reserva DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] reserva = new String[14];
                reserva[0] = String.valueOf(rs.getInt("id"));
                reserva[1] = String.valueOf(rs.getInt("viaje_id"));
                reserva[2] = String.valueOf(rs.getInt("venta_id"));
                reserva[3] = String.valueOf(rs.getInt("numero_personas"));
                reserva[4] = rs.getBigDecimal("precio_total").toString();
                reserva[5] = rs.getString("estado");
                reserva[6] = rs.getTimestamp("fecha_reserva").toString();
                reserva[7] = rs.getString("codigo_viaje");
                reserva[8] = rs.getDate("fecha_salida").toString();
                reserva[9] = rs.getDate("fecha_retorno").toString();
                reserva[10] = rs.getString("plan_nombre");
                reserva[11] = rs.getString("ciudad");
                reserva[12] = rs.getString("pais");
                reserva[13] = String.valueOf(rs.getInt("viaje_id"));
                reservas.add(reserva);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por venta: " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Obtiene reservas por estado
     */
    public List<String[]> findByEstado(String estado) {
        List<String[]> reservas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT rv.*, v.codigo_viaje, v.fecha_salida, pv.nombre as plan_nombre, " +
                        "d.ciudad, d.pais, u.nombre as cliente_nombre " +
                        "FROM RESERVA_VIAJE rv " +
                        "INNER JOIN VIAJE v ON rv.viaje_id = v.id " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "INNER JOIN VENTA ven ON rv.venta_id = ven.id " +
                        "INNER JOIN USUARIO u ON ven.cliente_id = u.id " +
                        "WHERE rv.estado = ? " +
                        "ORDER BY v.fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] reserva = new String[14];
                reserva[0] = String.valueOf(rs.getInt("id"));
                reserva[1] = String.valueOf(rs.getInt("viaje_id"));
                reserva[2] = String.valueOf(rs.getInt("venta_id"));
                reserva[3] = String.valueOf(rs.getInt("numero_personas"));
                reserva[4] = rs.getBigDecimal("precio_total").toString();
                reserva[5] = rs.getString("estado");
                reserva[6] = rs.getTimestamp("fecha_reserva").toString();
                reserva[7] = rs.getString("codigo_viaje");
                reserva[8] = rs.getDate("fecha_salida").toString();
                reserva[9] = rs.getString("plan_nombre");
                reserva[10] = rs.getString("ciudad");
                reserva[11] = rs.getString("pais");
                reserva[12] = rs.getString("cliente_nombre");
                reserva[13] = String.valueOf(rs.getInt("viaje_id"));
                reservas.add(reserva);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por estado: " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Obtiene reservas de un cliente específico
     */
    public List<String[]> findByCliente(int clienteId) {
        List<String[]> reservas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT rv.*, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "pv.nombre as plan_nombre, d.ciudad, d.pais, d.nombre as destino_nombre " +
                        "FROM RESERVA_VIAJE rv " +
                        "INNER JOIN VIAJE v ON rv.viaje_id = v.id " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "INNER JOIN VENTA ven ON rv.venta_id = ven.id " +
                        "WHERE ven.cliente_id = ? " +
                        "ORDER BY rv.fecha_reserva DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, clienteId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] reserva = new String[15];
                reserva[0] = String.valueOf(rs.getInt("id"));
                reserva[1] = String.valueOf(rs.getInt("viaje_id"));
                reserva[2] = String.valueOf(rs.getInt("venta_id"));
                reserva[3] = String.valueOf(rs.getInt("numero_personas"));
                reserva[4] = rs.getBigDecimal("precio_total").toString();
                reserva[5] = rs.getString("estado");
                reserva[6] = rs.getTimestamp("fecha_reserva").toString();
                reserva[7] = rs.getString("codigo_viaje");
                reserva[8] = rs.getDate("fecha_salida").toString();
                reserva[9] = rs.getDate("fecha_retorno").toString();
                reserva[10] = rs.getString("plan_nombre");
                reserva[11] = rs.getString("ciudad");
                reserva[12] = rs.getString("pais");
                reserva[13] = rs.getString("destino_nombre");
                reserva[14] = String.valueOf(rs.getInt("viaje_id"));
                reservas.add(reserva);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por cliente: " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Cuenta el total de personas reservadas para un viaje
     */
    public String[] contarPersonasPorViaje(int viajeId) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT COALESCE(SUM(numero_personas), 0) as total " +
                        "FROM RESERVA_VIAJE " +
                        "WHERE viaje_id = ? AND estado IN ('CONFIRMADA', 'PAGADA')";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, viajeId);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("total"));
            } else {
                result[0] = "1";
                result[1] = "0";
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            result[0] = "-1";
            result[1] = "Error SQL: " + e.getMessage();
        }
        return result;
    }
}
