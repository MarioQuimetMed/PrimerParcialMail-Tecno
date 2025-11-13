package com.tecnoweb.grupo15sa.data;

import com.tecnoweb.grupo15sa.ConfigDB.DatabaseConection;
import com.tecnoweb.grupo15sa.ConfigDB.ConfigDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DCuota {
    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DCuota() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    /**
     * Guarda una nueva cuota
     */
    public String[] save(int planPagoId, int numeroCuota, BigDecimal monto, 
                        Date fechaVencimiento, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "INSERT INTO CUOTA (plan_pago_id, numero_cuota, monto, " +
                        "fecha_vencimiento, estado) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING id";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, planPagoId);
            statement.setInt(2, numeroCuota);
            statement.setBigDecimal(3, monto);
            statement.setDate(4, fechaVencimiento);
            statement.setString(5, estado);
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("id"));
            } else {
                result[0] = "-1";
                result[1] = "Error al guardar la cuota";
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
     * Actualiza una cuota existente
     */
    public String[] update(int id, int planPagoId, int numeroCuota, BigDecimal monto,
                          Date fechaVencimiento, Date fechaPago, BigDecimal montoPagado,
                          BigDecimal mora, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE CUOTA SET plan_pago_id = ?, numero_cuota = ?, monto = ?, " +
                        "fecha_vencimiento = ?, fecha_pago = ?, monto_pagado = ?, " +
                        "mora = ?, estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, planPagoId);
            statement.setInt(2, numeroCuota);
            statement.setBigDecimal(3, monto);
            statement.setDate(4, fechaVencimiento);
            
            if (fechaPago != null) {
                statement.setDate(5, fechaPago);
            } else {
                statement.setNull(5, Types.DATE);
            }
            
            if (montoPagado != null) {
                statement.setBigDecimal(6, montoPagado);
            } else {
                statement.setNull(6, Types.DECIMAL);
            }
            
            if (mora != null) {
                statement.setBigDecimal(7, mora);
            } else {
                statement.setNull(7, Types.DECIMAL);
            }
            
            statement.setString(8, estado);
            statement.setInt(9, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Cuota actualizada correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró la cuota";
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
     * Registra el pago de una cuota
     */
    public String[] registrarPago(int id, Date fechaPago, BigDecimal montoPagado, BigDecimal mora) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE CUOTA SET fecha_pago = ?, monto_pagado = ?, mora = ?, " +
                        "estado = 'PAGADA' WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaPago);
            statement.setBigDecimal(2, montoPagado);
            
            if (mora != null && mora.compareTo(BigDecimal.ZERO) > 0) {
                statement.setBigDecimal(3, mora);
            } else {
                statement.setNull(3, Types.DECIMAL);
            }
            
            statement.setInt(4, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Pago registrado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró la cuota";
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
     * Actualiza el estado de una cuota
     */
    public String[] updateEstado(int id, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE CUOTA SET estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            statement.setInt(2, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Estado actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró la cuota";
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
     * Obtiene todas las cuotas
     */
    public List<String[]> findAll() {
        List<String[]> cuotas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT c.*, pp.venta_id, v.monto_total as venta_monto, " +
                        "u.nombre as cliente_nombre " +
                        "FROM CUOTA c " +
                        "INNER JOIN PLAN_PAGO pp ON c.plan_pago_id = pp.id " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "ORDER BY c.fecha_vencimiento ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] cuota = new String[13];
                cuota[0] = String.valueOf(rs.getInt("id"));
                cuota[1] = String.valueOf(rs.getInt("plan_pago_id"));
                cuota[2] = String.valueOf(rs.getInt("numero_cuota"));
                cuota[3] = rs.getBigDecimal("monto").toString();
                cuota[4] = rs.getDate("fecha_vencimiento").toString();
                
                Date fechaPago = rs.getDate("fecha_pago");
                cuota[5] = fechaPago != null ? fechaPago.toString() : "null";
                
                BigDecimal montoPagado = rs.getBigDecimal("monto_pagado");
                cuota[6] = montoPagado != null ? montoPagado.toString() : "null";
                
                BigDecimal mora = rs.getBigDecimal("mora");
                cuota[7] = mora != null ? mora.toString() : "null";
                
                cuota[8] = rs.getString("estado");
                cuota[9] = rs.getTimestamp("fecha_creacion").toString();
                cuota[10] = String.valueOf(rs.getInt("venta_id"));
                cuota[11] = rs.getBigDecimal("venta_monto").toString();
                cuota[12] = rs.getString("cliente_nombre");
                cuotas.add(cuota);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Obtiene una cuota por ID
     */
    public String[] findOneById(int id) {
        String[] cuota = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT c.*, pp.venta_id, pp.numero_cuotas, v.monto_total, " +
                        "u.nombre as cliente_nombre, u.email as cliente_email " +
                        "FROM CUOTA c " +
                        "INNER JOIN PLAN_PAGO pp ON c.plan_pago_id = pp.id " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE c.id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                cuota = new String[15];
                cuota[0] = String.valueOf(rs.getInt("id"));
                cuota[1] = String.valueOf(rs.getInt("plan_pago_id"));
                cuota[2] = String.valueOf(rs.getInt("numero_cuota"));
                cuota[3] = rs.getBigDecimal("monto").toString();
                cuota[4] = rs.getDate("fecha_vencimiento").toString();
                
                Date fechaPago = rs.getDate("fecha_pago");
                cuota[5] = fechaPago != null ? fechaPago.toString() : "null";
                
                BigDecimal montoPagado = rs.getBigDecimal("monto_pagado");
                cuota[6] = montoPagado != null ? montoPagado.toString() : "null";
                
                BigDecimal mora = rs.getBigDecimal("mora");
                cuota[7] = mora != null ? mora.toString() : "null";
                
                cuota[8] = rs.getString("estado");
                cuota[9] = rs.getTimestamp("fecha_creacion").toString();
                cuota[10] = String.valueOf(rs.getInt("venta_id"));
                cuota[11] = String.valueOf(rs.getInt("numero_cuotas"));
                cuota[12] = rs.getBigDecimal("monto_total").toString();
                cuota[13] = rs.getString("cliente_nombre");
                cuota[14] = rs.getString("cliente_email");
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cuota: " + e.getMessage());
        }
        return cuota;
    }

    /**
     * Obtiene todas las cuotas de un plan de pago específico
     */
    public List<String[]> findByPlanPago(int planPagoId) {
        List<String[]> cuotas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT * FROM CUOTA WHERE plan_pago_id = ? ORDER BY numero_cuota ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, planPagoId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] cuota = new String[10];
                cuota[0] = String.valueOf(rs.getInt("id"));
                cuota[1] = String.valueOf(rs.getInt("plan_pago_id"));
                cuota[2] = String.valueOf(rs.getInt("numero_cuota"));
                cuota[3] = rs.getBigDecimal("monto").toString();
                cuota[4] = rs.getDate("fecha_vencimiento").toString();
                
                Date fechaPago = rs.getDate("fecha_pago");
                cuota[5] = fechaPago != null ? fechaPago.toString() : "null";
                
                BigDecimal montoPagado = rs.getBigDecimal("monto_pagado");
                cuota[6] = montoPagado != null ? montoPagado.toString() : "null";
                
                BigDecimal mora = rs.getBigDecimal("mora");
                cuota[7] = mora != null ? mora.toString() : "null";
                
                cuota[8] = rs.getString("estado");
                cuota[9] = rs.getTimestamp("fecha_creacion").toString();
                cuotas.add(cuota);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas por plan de pago: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Obtiene todas las cuotas por estado
     */
    public List<String[]> findByEstado(String estado) {
        List<String[]> cuotas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT c.*, pp.venta_id, u.nombre as cliente_nombre, u.email as cliente_email " +
                        "FROM CUOTA c " +
                        "INNER JOIN PLAN_PAGO pp ON c.plan_pago_id = pp.id " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE c.estado = ? " +
                        "ORDER BY c.fecha_vencimiento ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] cuota = new String[13];
                cuota[0] = String.valueOf(rs.getInt("id"));
                cuota[1] = String.valueOf(rs.getInt("plan_pago_id"));
                cuota[2] = String.valueOf(rs.getInt("numero_cuota"));
                cuota[3] = rs.getBigDecimal("monto").toString();
                cuota[4] = rs.getDate("fecha_vencimiento").toString();
                
                Date fechaPago = rs.getDate("fecha_pago");
                cuota[5] = fechaPago != null ? fechaPago.toString() : "null";
                
                BigDecimal montoPagado = rs.getBigDecimal("monto_pagado");
                cuota[6] = montoPagado != null ? montoPagado.toString() : "null";
                
                BigDecimal mora = rs.getBigDecimal("mora");
                cuota[7] = mora != null ? mora.toString() : "null";
                
                cuota[8] = rs.getString("estado");
                cuota[9] = rs.getTimestamp("fecha_creacion").toString();
                cuota[10] = String.valueOf(rs.getInt("venta_id"));
                cuota[11] = rs.getString("cliente_nombre");
                cuota[12] = rs.getString("cliente_email");
                cuotas.add(cuota);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas por estado: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Obtiene cuotas vencidas que necesitan actualización de estado
     */
    public List<String[]> findCuotasVencidas() {
        List<String[]> cuotas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT c.*, pp.venta_id, u.nombre as cliente_nombre, " +
                        "u.email as cliente_email, u.telefono as cliente_telefono " +
                        "FROM CUOTA c " +
                        "INNER JOIN PLAN_PAGO pp ON c.plan_pago_id = pp.id " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE c.fecha_vencimiento < CURRENT_DATE " +
                        "AND c.estado = 'PENDIENTE' " +
                        "ORDER BY c.fecha_vencimiento ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] cuota = new String[14];
                cuota[0] = String.valueOf(rs.getInt("id"));
                cuota[1] = String.valueOf(rs.getInt("plan_pago_id"));
                cuota[2] = String.valueOf(rs.getInt("numero_cuota"));
                cuota[3] = rs.getBigDecimal("monto").toString();
                cuota[4] = rs.getDate("fecha_vencimiento").toString();
                cuota[5] = "null"; // fecha_pago
                cuota[6] = "null"; // monto_pagado
                cuota[7] = "null"; // mora
                cuota[8] = rs.getString("estado");
                cuota[9] = rs.getTimestamp("fecha_creacion").toString();
                cuota[10] = String.valueOf(rs.getInt("venta_id"));
                cuota[11] = rs.getString("cliente_nombre");
                cuota[12] = rs.getString("cliente_email");
                cuota[13] = rs.getString("cliente_telefono");
                cuotas.add(cuota);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas vencidas: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Obtiene cuotas próximas a vencer (en los próximos N días)
     */
    public List<String[]> findProximasVencer(int dias) {
        List<String[]> cuotas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT c.*, pp.venta_id, u.nombre as cliente_nombre, " +
                        "u.email as cliente_email " +
                        "FROM CUOTA c " +
                        "INNER JOIN PLAN_PAGO pp ON c.plan_pago_id = pp.id " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE c.fecha_vencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + ? " +
                        "AND c.estado = 'PENDIENTE' " +
                        "ORDER BY c.fecha_vencimiento ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, dias);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] cuota = new String[13];
                cuota[0] = String.valueOf(rs.getInt("id"));
                cuota[1] = String.valueOf(rs.getInt("plan_pago_id"));
                cuota[2] = String.valueOf(rs.getInt("numero_cuota"));
                cuota[3] = rs.getBigDecimal("monto").toString();
                cuota[4] = rs.getDate("fecha_vencimiento").toString();
                cuota[5] = "null"; // fecha_pago
                cuota[6] = "null"; // monto_pagado
                cuota[7] = "null"; // mora
                cuota[8] = rs.getString("estado");
                cuota[9] = rs.getTimestamp("fecha_creacion").toString();
                cuota[10] = String.valueOf(rs.getInt("venta_id"));
                cuota[11] = rs.getString("cliente_nombre");
                cuota[12] = rs.getString("cliente_email");
                cuotas.add(cuota);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cuotas próximas a vencer: " + e.getMessage());
        }
        return cuotas;
    }
}
