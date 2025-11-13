package com.tecnoweb.grupo15sa.data;

import com.tecnoweb.grupo15sa.ConfigDB.DatabaseConection;
import com.tecnoweb.grupo15sa.ConfigDB.ConfigDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DPago {
    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DPago() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    /**
     * Guarda un nuevo pago
     */
    public String[] save(int ventaId, Integer cuotaId, BigDecimal monto, String metodoPago,
                        String numeroRecibo, Date fechaPago, String observaciones) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "INSERT INTO PAGO (venta_id, cuota_id, monto, metodo_pago, " +
                        "numero_recibo, fecha_pago, observaciones) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            
            if (cuotaId != null) {
                statement.setInt(2, cuotaId);
            } else {
                statement.setNull(2, Types.INTEGER);
            }
            
            statement.setBigDecimal(3, monto);
            statement.setString(4, metodoPago);
            statement.setString(5, numeroRecibo);
            statement.setDate(6, fechaPago);
            
            if (observaciones != null && !observaciones.isEmpty()) {
                statement.setString(7, observaciones);
            } else {
                statement.setNull(7, Types.VARCHAR);
            }
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("id"));
            } else {
                result[0] = "-1";
                result[1] = "Error al guardar el pago";
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
     * Actualiza un pago existente
     */
    public String[] update(int id, int ventaId, Integer cuotaId, BigDecimal monto,
                          String metodoPago, String numeroRecibo, Date fechaPago, String observaciones) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE PAGO SET venta_id = ?, cuota_id = ?, monto = ?, metodo_pago = ?, " +
                        "numero_recibo = ?, fecha_pago = ?, observaciones = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            
            if (cuotaId != null) {
                statement.setInt(2, cuotaId);
            } else {
                statement.setNull(2, Types.INTEGER);
            }
            
            statement.setBigDecimal(3, monto);
            statement.setString(4, metodoPago);
            statement.setString(5, numeroRecibo);
            statement.setDate(6, fechaPago);
            
            if (observaciones != null && !observaciones.isEmpty()) {
                statement.setString(7, observaciones);
            } else {
                statement.setNull(7, Types.VARCHAR);
            }
            
            statement.setInt(8, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Pago actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el pago";
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
     * Elimina un pago
     */
    public String[] delete(int id) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "DELETE FROM PAGO WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Pago eliminado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el pago";
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
     * Obtiene todos los pagos con información completa
     */
    public List<String[]> findAll() {
        List<String[]> pagos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.fecha_venta, v.tipo_venta, v.monto_total as venta_monto, " +
                        "u.nombre as cliente_nombre, u.email as cliente_email, " +
                        "c.numero_cuota, c.fecha_vencimiento " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "LEFT JOIN CUOTA c ON p.cuota_id = c.id " +
                        "ORDER BY p.fecha_pago DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] pago = new String[16];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getDate("fecha_venta").toString();
                pago[10] = rs.getString("tipo_venta");
                pago[11] = rs.getBigDecimal("venta_monto").toString();
                pago[12] = rs.getString("cliente_nombre");
                pago[13] = rs.getString("cliente_email");
                
                Integer numeroCuota = rs.getInt("numero_cuota");
                pago[14] = rs.wasNull() ? "null" : String.valueOf(numeroCuota);
                
                Date fechaVencimiento = rs.getDate("fecha_vencimiento");
                pago[15] = fechaVencimiento != null ? fechaVencimiento.toString() : "null";
                
                pagos.add(pago);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos: " + e.getMessage());
        }
        return pagos;
    }

    /**
     * Obtiene un pago por ID
     */
    public String[] findOneById(int id) {
        String[] pago = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.fecha_venta, v.tipo_venta, v.monto_total, v.monto_pendiente, " +
                        "u.nombre as cliente_nombre, u.email as cliente_email, u.telefono as cliente_telefono, " +
                        "c.numero_cuota, c.monto as cuota_monto, c.fecha_vencimiento " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "LEFT JOIN CUOTA c ON p.cuota_id = c.id " +
                        "WHERE p.id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                pago = new String[19];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getDate("fecha_venta").toString();
                pago[10] = rs.getString("tipo_venta");
                pago[11] = rs.getBigDecimal("monto_total").toString();
                pago[12] = rs.getBigDecimal("monto_pendiente").toString();
                pago[13] = rs.getString("cliente_nombre");
                pago[14] = rs.getString("cliente_email");
                pago[15] = rs.getString("cliente_telefono");
                
                Integer numeroCuota = rs.getInt("numero_cuota");
                pago[16] = rs.wasNull() ? "null" : String.valueOf(numeroCuota);
                
                BigDecimal cuotaMonto = rs.getBigDecimal("cuota_monto");
                pago[17] = cuotaMonto != null ? cuotaMonto.toString() : "null";
                
                Date fechaVencimiento = rs.getDate("fecha_vencimiento");
                pago[18] = fechaVencimiento != null ? fechaVencimiento.toString() : "null";
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pago: " + e.getMessage());
        }
        return pago;
    }

    /**
     * Verifica si existe un pago con el número de recibo
     */
    public boolean existeRecibo(String numeroRecibo) {
        String query = "SELECT COUNT(*) as total FROM PAGO WHERE numero_recibo = ?";
        
        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, numeroRecibo);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                ps.close();
                return total > 0;
            }
            
            rs.close();
            ps.close();
            return false;
        } catch (SQLException e) {
            System.err.println("Error al verificar recibo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un pago por número de recibo
     */
    public String[] findByRecibo(String numeroRecibo) {
        return findByNumeroRecibo(numeroRecibo);
    }

    /**
     * Obtiene pagos por rango de fechas
     */
    public List<String[]> findByDateRange(Date fechaInicio, Date fechaFin) {
        return findByFechas(fechaInicio, fechaFin);
    }

    /**
     * Verifica que una cuota pertenece a una venta específica
     */
    public String[] verificarCuotaVenta(int cuotaId, int ventaId) {
        String query = "SELECT pp.* FROM PLAN_PAGO pp " +
                      "INNER JOIN CUOTA c ON pp.id = c.plan_pago_id " +
                      "WHERE c.id = ? AND pp.venta_id = ?";
        
        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, cuotaId);
            ps.setInt(2, ventaId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String[] plan = new String[8];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = String.valueOf(rs.getInt("venta_id"));
                plan[2] = String.valueOf(rs.getInt("numero_cuotas"));
                plan[3] = rs.getBigDecimal("monto_cuota").toString();
                plan[4] = rs.getBigDecimal("interes_porcentaje").toString();
                plan[5] = rs.getDate("fecha_primer_vencimiento").toString();
                plan[6] = rs.getString("estado");
                plan[7] = rs.getTimestamp("fecha_creacion").toString();
                
                rs.close();
                ps.close();
                return plan;
            }
            
            rs.close();
            ps.close();
            return null;
        } catch (SQLException e) {
            System.err.println("Error al verificar cuota-venta: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca un pago por número de recibo
     */
    public String[] findByNumeroRecibo(String numeroRecibo) {
        String[] pago = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.monto_total, u.nombre as cliente_nombre " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE p.numero_recibo = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, numeroRecibo);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                pago = new String[12];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getBigDecimal("monto_total").toString();
                pago[10] = rs.getString("cliente_nombre");
                pago[11] = String.valueOf(rs.getInt("venta_id"));
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al buscar pago por número de recibo: " + e.getMessage());
        }
        return pago;
    }

    /**
     * Obtiene todos los pagos de una venta específica
     */
    public List<String[]> findByVenta(int ventaId) {
        List<String[]> pagos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, c.numero_cuota, c.fecha_vencimiento " +
                        "FROM PAGO p " +
                        "LEFT JOIN CUOTA c ON p.cuota_id = c.id " +
                        "WHERE p.venta_id = ? " +
                        "ORDER BY p.fecha_pago DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] pago = new String[11];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                
                Integer numeroCuota = rs.getInt("numero_cuota");
                pago[9] = rs.wasNull() ? "null" : String.valueOf(numeroCuota);
                
                Date fechaVencimiento = rs.getDate("fecha_vencimiento");
                pago[10] = fechaVencimiento != null ? fechaVencimiento.toString() : "null";
                
                pagos.add(pago);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por venta: " + e.getMessage());
        }
        return pagos;
    }

    /**
     * Obtiene todos los pagos de una cuota específica
     */
    public List<String[]> findByCuota(int cuotaId) {
        List<String[]> pagos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.fecha_venta, u.nombre as cliente_nombre " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE p.cuota_id = ? " +
                        "ORDER BY p.fecha_pago DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, cuotaId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] pago = new String[12];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                pago[2] = String.valueOf(rs.getInt("cuota_id"));
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getDate("fecha_venta").toString();
                pago[10] = rs.getString("cliente_nombre");
                pago[11] = String.valueOf(rs.getInt("venta_id"));
                pagos.add(pago);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por cuota: " + e.getMessage());
        }
        return pagos;
    }

    /**
     * Obtiene pagos por método de pago
     */
    public List<String[]> findByMetodoPago(String metodoPago) {
        List<String[]> pagos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.fecha_venta, v.tipo_venta, u.nombre as cliente_nombre " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE p.metodo_pago = ? " +
                        "ORDER BY p.fecha_pago DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, metodoPago);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] pago = new String[13];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getDate("fecha_venta").toString();
                pago[10] = rs.getString("tipo_venta");
                pago[11] = rs.getString("cliente_nombre");
                pago[12] = String.valueOf(rs.getInt("venta_id"));
                pagos.add(pago);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por método: " + e.getMessage());
        }
        return pagos;
    }

    /**
     * Obtiene pagos por rango de fechas
     */
    public List<String[]> findByFechas(Date fechaInicio, Date fechaFin) {
        List<String[]> pagos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.tipo_venta, u.nombre as cliente_nombre " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE p.fecha_pago BETWEEN ? AND ? " +
                        "ORDER BY p.fecha_pago DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] pago = new String[12];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getString("tipo_venta");
                pago[10] = rs.getString("cliente_nombre");
                pago[11] = String.valueOf(rs.getInt("venta_id"));
                pagos.add(pago);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por fechas: " + e.getMessage());
        }
        return pagos;
    }

    /**
     * Calcula el total de pagos realizados para una venta
     */
    public String[] calcularTotalPagadoVenta(int ventaId) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT COALESCE(SUM(monto), 0) as total_pagado FROM PAGO WHERE venta_id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                result[0] = "1";
                result[1] = rs.getBigDecimal("total_pagado").toString();
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

    /**
     * Obtiene resumen de pagos por cliente
     */
    public List<String[]> findByCliente(int clienteId) {
        List<String[]> pagos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT p.*, v.fecha_venta, v.tipo_venta, v.monto_total " +
                        "FROM PAGO p " +
                        "INNER JOIN VENTA v ON p.venta_id = v.id " +
                        "WHERE v.cliente_id = ? " +
                        "ORDER BY p.fecha_pago DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, clienteId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] pago = new String[13];
                pago[0] = String.valueOf(rs.getInt("id"));
                pago[1] = String.valueOf(rs.getInt("venta_id"));
                
                Integer cuotaId = rs.getInt("cuota_id");
                pago[2] = rs.wasNull() ? "null" : String.valueOf(cuotaId);
                
                pago[3] = rs.getBigDecimal("monto").toString();
                pago[4] = rs.getString("metodo_pago");
                pago[5] = rs.getString("numero_recibo");
                pago[6] = rs.getDate("fecha_pago").toString();
                
                String observaciones = rs.getString("observaciones");
                pago[7] = observaciones != null ? observaciones : "null";
                
                pago[8] = rs.getTimestamp("fecha_registro").toString();
                pago[9] = rs.getDate("fecha_venta").toString();
                pago[10] = rs.getString("tipo_venta");
                pago[11] = rs.getBigDecimal("monto_total").toString();
                pago[12] = String.valueOf(rs.getInt("venta_id"));
                pagos.add(pago);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por cliente: " + e.getMessage());
        }
        return pagos;
    }
}
