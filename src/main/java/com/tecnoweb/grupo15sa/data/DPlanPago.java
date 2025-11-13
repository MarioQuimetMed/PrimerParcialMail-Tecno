package com.tecnoweb.grupo15sa.data;

import com.tecnoweb.grupo15sa.ConfigDB.DatabaseConection;
import com.tecnoweb.grupo15sa.ConfigDB.ConfigDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DPlanPago {
    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DPlanPago() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    /**
     * Guarda un nuevo plan de pago
     */
    public String[] save(int ventaId, int numeroCuotas, BigDecimal montoCuota, 
                        BigDecimal interesporcentaje, Date fechaPrimerVencimiento, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "INSERT INTO PLAN_PAGO (venta_id, numero_cuotas, monto_cuota, " +
                        "interes_porcentaje, fecha_primer_vencimiento, estado) " +
                        "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            statement.setInt(2, numeroCuotas);
            statement.setBigDecimal(3, montoCuota);
            statement.setBigDecimal(4, interesporcentaje);
            statement.setDate(5, fechaPrimerVencimiento);
            statement.setString(6, estado);
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("id"));
            } else {
                result[0] = "-1";
                result[1] = "Error al guardar el plan de pago";
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
     * Actualiza un plan de pago existente
     */
    public String[] update(int id, int ventaId, int numeroCuotas, BigDecimal montoCuota,
                          BigDecimal interesporcentaje, Date fechaPrimerVencimiento, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE PLAN_PAGO SET venta_id = ?, numero_cuotas = ?, monto_cuota = ?, " +
                        "interes_porcentaje = ?, fecha_primer_vencimiento = ?, estado = ? " +
                        "WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            statement.setInt(2, numeroCuotas);
            statement.setBigDecimal(3, montoCuota);
            statement.setBigDecimal(4, interesporcentaje);
            statement.setDate(5, fechaPrimerVencimiento);
            statement.setString(6, estado);
            statement.setInt(7, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Plan de pago actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el plan de pago";
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
     * Actualiza el estado de un plan de pago
     */
    public String[] updateEstado(int id, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE PLAN_PAGO SET estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            statement.setInt(2, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Estado actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el plan de pago";
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
     * Obtiene todos los planes de pago con información de la venta
     */
    public List<String[]> findAll() {
        List<String[]> planes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT pp.*, v.tipo_venta, v.monto_total, " +
                        "v.fecha_venta, u.nombre as cliente_nombre " +
                        "FROM PLAN_PAGO pp " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "ORDER BY pp.fecha_creacion DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] plan = new String[12];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = String.valueOf(rs.getInt("venta_id"));
                plan[2] = String.valueOf(rs.getInt("numero_cuotas"));
                plan[3] = rs.getBigDecimal("monto_cuota").toString();
                plan[4] = rs.getBigDecimal("interes_porcentaje").toString();
                plan[5] = rs.getDate("fecha_primer_vencimiento").toString();
                plan[6] = rs.getString("estado");
                plan[7] = rs.getTimestamp("fecha_creacion").toString();
                plan[8] = rs.getString("tipo_venta");
                plan[9] = rs.getBigDecimal("monto_total").toString();
                plan[10] = rs.getDate("fecha_venta").toString();
                plan[11] = rs.getString("cliente_nombre");
                planes.add(plan);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener planes de pago: " + e.getMessage());
        }
        return planes;
    }

    /**
     * Obtiene un plan de pago por ID
     */
    public String[] findOneById(int id) {
        String[] plan = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT pp.*, v.tipo_venta, v.monto_total, v.fecha_venta, " +
                        "u.nombre as cliente_nombre, u.email as cliente_email " +
                        "FROM PLAN_PAGO pp " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE pp.id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                plan = new String[13];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = String.valueOf(rs.getInt("venta_id"));
                plan[2] = String.valueOf(rs.getInt("numero_cuotas"));
                plan[3] = rs.getBigDecimal("monto_cuota").toString();
                plan[4] = rs.getBigDecimal("interes_porcentaje").toString();
                plan[5] = rs.getDate("fecha_primer_vencimiento").toString();
                plan[6] = rs.getString("estado");
                plan[7] = rs.getTimestamp("fecha_creacion").toString();
                plan[8] = rs.getString("tipo_venta");
                plan[9] = rs.getBigDecimal("monto_total").toString();
                plan[10] = rs.getDate("fecha_venta").toString();
                plan[11] = rs.getString("cliente_nombre");
                plan[12] = rs.getString("cliente_email");
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener plan de pago: " + e.getMessage());
        }
        return plan;
    }

    /**
     * Obtiene todos los planes de pago de una venta específica
     */
    public List<String[]> findByVenta(int ventaId) {
        List<String[]> planes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT * FROM PLAN_PAGO WHERE venta_id = ? ORDER BY fecha_creacion DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ventaId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] plan = new String[8];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = String.valueOf(rs.getInt("venta_id"));
                plan[2] = String.valueOf(rs.getInt("numero_cuotas"));
                plan[3] = rs.getBigDecimal("monto_cuota").toString();
                plan[4] = rs.getBigDecimal("interes_porcentaje").toString();
                plan[5] = rs.getDate("fecha_primer_vencimiento").toString();
                plan[6] = rs.getString("estado");
                plan[7] = rs.getTimestamp("fecha_creacion").toString();
                planes.add(plan);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener planes de pago por venta: " + e.getMessage());
        }
        return planes;
    }

    /**
     * Obtiene todos los planes de pago por estado
     */
    public List<String[]> findByEstado(String estado) {
        List<String[]> planes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT pp.*, v.tipo_venta, v.monto_total, u.nombre as cliente_nombre " +
                        "FROM PLAN_PAGO pp " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "WHERE pp.estado = ? " +
                        "ORDER BY pp.fecha_primer_vencimiento ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] plan = new String[11];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = String.valueOf(rs.getInt("venta_id"));
                plan[2] = String.valueOf(rs.getInt("numero_cuotas"));
                plan[3] = rs.getBigDecimal("monto_cuota").toString();
                plan[4] = rs.getBigDecimal("interes_porcentaje").toString();
                plan[5] = rs.getDate("fecha_primer_vencimiento").toString();
                plan[6] = rs.getString("estado");
                plan[7] = rs.getTimestamp("fecha_creacion").toString();
                plan[8] = rs.getString("tipo_venta");
                plan[9] = rs.getBigDecimal("monto_total").toString();
                plan[10] = rs.getString("cliente_nombre");
                planes.add(plan);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener planes de pago por estado: " + e.getMessage());
        }
        return planes;
    }

    /**
     * Obtiene planes de pago con cuotas vencidas
     */
    public List<String[]> findVencidos() {
        List<String[]> planes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT DISTINCT pp.*, v.monto_total, u.nombre as cliente_nombre, " +
                        "u.email as cliente_email " +
                        "FROM PLAN_PAGO pp " +
                        "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                        "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                        "INNER JOIN CUOTA c ON pp.id = c.plan_pago_id " +
                        "WHERE c.estado = 'VENCIDA' AND pp.estado = 'ACTIVO' " +
                        "ORDER BY pp.fecha_primer_vencimiento ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] plan = new String[11];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = String.valueOf(rs.getInt("venta_id"));
                plan[2] = String.valueOf(rs.getInt("numero_cuotas"));
                plan[3] = rs.getBigDecimal("monto_cuota").toString();
                plan[4] = rs.getBigDecimal("interes_porcentaje").toString();
                plan[5] = rs.getDate("fecha_primer_vencimiento").toString();
                plan[6] = rs.getString("estado");
                plan[7] = rs.getTimestamp("fecha_creacion").toString();
                plan[8] = rs.getBigDecimal("monto_total").toString();
                plan[9] = rs.getString("cliente_nombre");
                plan[10] = rs.getString("cliente_email");
                planes.add(plan);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener planes de pago vencidos: " + e.getMessage());
        }
        return planes;
    }
}
