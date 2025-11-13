package com.tecnoweb.grupo7sa.data;

import com.tecnoweb.grupo7sa.ConfigDB.ConfigDB;
import com.tecnoweb.grupo7sa.ConfigDB.DatabaseConection;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DVenta {

    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DVenta() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    // CU4 - GESTIÓN DE VENTAS (CONTADO/CRÉDITO)

    /**
     * Crear nueva venta con montos de pago
     */
    public String[] save(int clienteId, int vendedorId, int viajeId, String tipoVenta,
                       BigDecimal montoTotal, BigDecimal montoPagado, BigDecimal montoPendiente,
                       String estado, Date fechaVenta, String observaciones) {
        String query = "INSERT INTO VENTA (cliente_id, vendedor_id, viaje_id, tipo_venta, monto_total, " +
                "descuento, monto_final, monto_pagado, monto_pendiente, estado, fecha_venta, observaciones, numero_factura) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, clienteId);
            ps.setInt(2, vendedorId);
            ps.setInt(3, viajeId);
            ps.setString(4, tipoVenta);
            ps.setBigDecimal(5, montoTotal);
            ps.setBigDecimal(6, BigDecimal.ZERO); // descuento
            ps.setBigDecimal(7, montoTotal); // monto_final
            ps.setBigDecimal(8, montoPagado != null ? montoPagado : BigDecimal.ZERO);
            ps.setBigDecimal(9, montoPendiente != null ? montoPendiente : montoTotal);
            ps.setString(10, estado != null ? estado : "PENDIENTE");
            ps.setDate(11, fechaVenta);
            ps.setString(12, observaciones);
            ps.setString(13, "F-" + System.currentTimeMillis()); // numero_factura generado

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String[] result = new String[2];
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("id"));
                rs.close();
                ps.close();
                return result;
            }
            
            ps.close();
            return new String[]{"0", "Error: No se pudo crear la venta"};
        } catch (SQLException e) {
            return new String[]{"0", "Error: " + e.getMessage()};
        }
    }

    /**
     * Crear nueva venta (versión original)
     */
    public String save(int clienteId, int vendedorId, int viajeId, String tipoVenta,
                       BigDecimal montoTotal, BigDecimal descuento, BigDecimal montoFinal,
                       String estado, String observaciones, String numeroFactura) {
        String query = "INSERT INTO VENTA (cliente_id, vendedor_id, viaje_id, tipo_venta, monto_total, " +
                "descuento, monto_final, estado, observaciones, numero_factura) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, clienteId);
            ps.setInt(2, vendedorId);
            ps.setInt(3, viajeId);
            ps.setString(4, tipoVenta);
            ps.setBigDecimal(5, montoTotal);
            ps.setBigDecimal(6, descuento != null ? descuento : BigDecimal.ZERO);
            ps.setBigDecimal(7, montoFinal);
            ps.setString(8, estado != null ? estado : "PENDIENTE");
            ps.setString(9, observaciones);
            ps.setString(10, numeroFactura);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Venta creada exitosamente" : "Error: No se pudo crear la venta";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Actualizar venta existente
     */
    public String update(int id, int clienteId, int vendedorId, int viajeId, String tipoVenta,
                         BigDecimal montoTotal, BigDecimal descuento, BigDecimal montoFinal,
                         String estado, String observaciones, String numeroFactura) {
        String query = "UPDATE VENTA SET cliente_id = ?, vendedor_id = ?, viaje_id = ?, tipo_venta = ?, " +
                "monto_total = ?, descuento = ?, monto_final = ?, estado = ?, observaciones = ?, numero_factura = ? " +
                "WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, clienteId);
            ps.setInt(2, vendedorId);
            ps.setInt(3, viajeId);
            ps.setString(4, tipoVenta);
            ps.setBigDecimal(5, montoTotal);
            ps.setBigDecimal(6, descuento != null ? descuento : BigDecimal.ZERO);
            ps.setBigDecimal(7, montoFinal);
            ps.setString(8, estado);
            ps.setString(9, observaciones);
            ps.setString(10, numeroFactura);
            ps.setInt(11, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Venta actualizada exitosamente" : "Error: No se pudo actualizar la venta";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Actualizar estado de la venta
     */
    public String updateEstado(int id, String nuevoEstado) {
        String query = "UPDATE VENTA SET estado = ? WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Estado actualizado exitosamente" : "Error: No se pudo actualizar el estado";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Listar todas las ventas
     */
    public List<String[]> findAll() {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "ORDER BY v.fecha_venta DESC";
        List<String[]> ventas = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");
                ventas.add(venta);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Buscar venta por ID
     */
    public String[] findOneById(int id) {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "WHERE v.id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");

                rs.close();
                ps.close();
                return venta;
            }

            rs.close();
            ps.close();
            return null;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Buscar ventas por cliente
     */
    public List<String[]> findByCliente(int clienteId) {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "WHERE v.cliente_id = ? ORDER BY v.fecha_venta DESC";
        List<String[]> ventas = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");
                ventas.add(venta);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Buscar ventas por vendedor
     */
    public List<String[]> findByVendedor(int vendedorId) {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "WHERE v.vendedor_id = ? ORDER BY v.fecha_venta DESC";
        List<String[]> ventas = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, vendedorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");
                ventas.add(venta);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Buscar ventas por tipo (CONTADO/CREDITO)
     */
    public List<String[]> findByTipo(String tipoVenta) {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "WHERE v.tipo_venta = ? ORDER BY v.fecha_venta DESC";
        List<String[]> ventas = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, tipoVenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");
                ventas.add(venta);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Buscar ventas pendientes de pago
     */
    public List<String[]> findVentasPendientes() {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "WHERE v.estado IN ('PENDIENTE', 'CONFIRMADA') " +
                "ORDER BY v.fecha_venta DESC";
        List<String[]> ventas = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");
                ventas.add(venta);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Actualizar montos de una venta (monto pagado y pendiente)
     */
    public String updateMontos(int id, BigDecimal montoPagado, BigDecimal montoPendiente) {
        String query = "UPDATE VENTA SET monto_pagado = ?, monto_pendiente = ? WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setBigDecimal(1, montoPagado);
            ps.setBigDecimal(2, montoPendiente);
            ps.setInt(3, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Montos actualizados exitosamente" : "Error: No se pudo actualizar los montos";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Buscar ventas por estado
     */
    public List<String[]> findByEstado(String estado) {
        String query = "SELECT v.id, v.cliente_id, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                "v.vendedor_id, ve.nombre as vendedor_nombre, ve.apellido as vendedor_apellido, v.viaje_id, " +
                "v.fecha_venta, v.tipo_venta, v.monto_total, v.descuento, v.monto_final, v.estado, " +
                "v.observaciones, v.numero_factura " +
                "FROM VENTA v " +
                "INNER JOIN USUARIO c ON v.cliente_id = c.id " +
                "INNER JOIN USUARIO ve ON v.vendedor_id = ve.id " +
                "WHERE v.estado = ? ORDER BY v.fecha_venta DESC";
        List<String[]> ventas = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] venta = new String[16];
                venta[0] = String.valueOf(rs.getInt("id"));
                venta[1] = String.valueOf(rs.getInt("cliente_id"));
                venta[2] = rs.getString("cliente_nombre") + " " + rs.getString("cliente_apellido");
                venta[3] = String.valueOf(rs.getInt("vendedor_id"));
                venta[4] = rs.getString("vendedor_nombre") + " " + rs.getString("vendedor_apellido");
                venta[5] = String.valueOf(rs.getInt("viaje_id"));
                venta[6] = rs.getTimestamp("fecha_venta").toString();
                venta[7] = rs.getString("tipo_venta");
                venta[8] = rs.getBigDecimal("monto_total").toString();
                venta[9] = rs.getBigDecimal("descuento").toString();
                venta[10] = rs.getBigDecimal("monto_final").toString();
                venta[11] = rs.getString("estado");
                venta[12] = rs.getString("observaciones");
                venta[13] = rs.getString("numero_factura");
                ventas.add(venta);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return ventas;
    }
}
