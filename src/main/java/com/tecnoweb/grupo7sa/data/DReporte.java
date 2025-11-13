package com.tecnoweb.grupo7sa.data;

import com.tecnoweb.grupo7sa.ConfigDB.DatabaseConection;
import com.tecnoweb.grupo7sa.ConfigDB.ConfigDB;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DReporte {
    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DReporte() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    /**
     * Reporte de ventas totales por período
     */
    public String[] reporteVentasPorPeriodo(Date fechaInicio, Date fechaFin) {
        String[] reporte = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT COUNT(*) as total_ventas, " +
                        "SUM(monto_total) as ingresos_totales, " +
                        "AVG(monto_total) as promedio_venta, " +
                        "SUM(CASE WHEN tipo_venta = 'CONTADO' THEN 1 ELSE 0 END) as ventas_contado, " +
                        "SUM(CASE WHEN tipo_venta = 'CREDITO' THEN 1 ELSE 0 END) as ventas_credito " +
                        "FROM VENTA " +
                        "WHERE fecha_venta BETWEEN ? AND ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                reporte = new String[5];
                reporte[0] = String.valueOf(rs.getInt("total_ventas"));
                reporte[1] = rs.getBigDecimal("ingresos_totales").toString();
                reporte[2] = rs.getBigDecimal("promedio_venta").toString();
                reporte[3] = String.valueOf(rs.getInt("ventas_contado"));
                reporte[4] = String.valueOf(rs.getInt("ventas_credito"));
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de ventas: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de pagos recibidos por período
     */
    public String[] reportePagosPorPeriodo(Date fechaInicio, Date fechaFin) {
        String[] reporte = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT COUNT(*) as total_pagos, " +
                        "SUM(monto) as monto_total, " +
                        "SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN monto ELSE 0 END) as total_efectivo, " +
                        "SUM(CASE WHEN metodo_pago = 'TARJETA' THEN monto ELSE 0 END) as total_tarjeta, " +
                        "SUM(CASE WHEN metodo_pago = 'TRANSFERENCIA' THEN monto ELSE 0 END) as total_transferencia, " +
                        "SUM(CASE WHEN metodo_pago = 'QR' THEN monto ELSE 0 END) as total_qr " +
                        "FROM PAGO " +
                        "WHERE fecha_pago BETWEEN ? AND ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                reporte = new String[6];
                reporte[0] = String.valueOf(rs.getInt("total_pagos"));
                reporte[1] = rs.getBigDecimal("monto_total").toString();
                reporte[2] = rs.getBigDecimal("total_efectivo").toString();
                reporte[3] = rs.getBigDecimal("total_tarjeta").toString();
                reporte[4] = rs.getBigDecimal("total_transferencia").toString();
                reporte[5] = rs.getBigDecimal("total_qr").toString();
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de pagos: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de viajes más vendidos
     */
    public List<String[]> reporteViajesMasVendidos(int limite) {
        List<String[]> reporte = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.id, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "pv.nombre as plan_nombre, d.ciudad, d.pais, " +
                        "COUNT(rv.id) as total_reservas, " +
                        "SUM(rv.numero_personas) as total_personas, " +
                        "SUM(rv.precio_total) as ingresos_totales " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "LEFT JOIN RESERVA_VIAJE rv ON v.id = rv.viaje_id " +
                        "GROUP BY v.id, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "pv.nombre, d.ciudad, d.pais " +
                        "ORDER BY total_reservas DESC, ingresos_totales DESC " +
                        "LIMIT ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, limite);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] fila = new String[10];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("codigo_viaje");
                fila[2] = rs.getDate("fecha_salida").toString();
                fila[3] = rs.getDate("fecha_retorno").toString();
                fila[4] = rs.getString("plan_nombre");
                fila[5] = rs.getString("ciudad");
                fila[6] = rs.getString("pais");
                fila[7] = String.valueOf(rs.getInt("total_reservas"));
                fila[8] = String.valueOf(rs.getInt("total_personas"));
                fila[9] = rs.getBigDecimal("ingresos_totales").toString();
                reporte.add(fila);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de viajes más vendidos: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de destinos más populares
     */
    public List<String[]> reporteDestinosPopulares() {
        List<String[]> reporte = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT d.id, d.nombre, d.ciudad, d.pais, " +
                        "COUNT(DISTINCT pv.id) as total_planes, " +
                        "COUNT(DISTINCT v.id) as total_viajes, " +
                        "COUNT(rv.id) as total_reservas, " +
                        "SUM(rv.numero_personas) as total_viajeros " +
                        "FROM DESTINO d " +
                        "INNER JOIN PLAN_VIAJE pv ON d.id = pv.destino_id " +
                        "LEFT JOIN VIAJE v ON pv.id = v.plan_viaje_id " +
                        "LEFT JOIN RESERVA_VIAJE rv ON v.id = rv.viaje_id " +
                        "GROUP BY d.id, d.nombre, d.ciudad, d.pais " +
                        "ORDER BY total_reservas DESC, total_viajeros DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] fila = new String[8];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getString("ciudad");
                fila[3] = rs.getString("pais");
                fila[4] = String.valueOf(rs.getInt("total_planes"));
                fila[5] = String.valueOf(rs.getInt("total_viajes"));
                fila[6] = String.valueOf(rs.getInt("total_reservas"));
                fila[7] = String.valueOf(rs.getInt("total_viajeros"));
                reporte.add(fila);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de destinos populares: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de clientes con más compras
     */
    public List<String[]> reporteClientesFrecuentes(int limite) {
        List<String[]> reporte = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT u.id, u.nombre, u.email, u.telefono, " +
                        "COUNT(v.id) as total_compras, " +
                        "SUM(v.monto_total) as monto_total_compras, " +
                        "AVG(v.monto_total) as promedio_compra, " +
                        "MAX(v.fecha_venta) as ultima_compra " +
                        "FROM USUARIO u " +
                        "INNER JOIN VENTA v ON u.id = v.cliente_id " +
                        "WHERE u.rol = 'CLIENTE' AND v.estado != 'CANCELADA' " +
                        "GROUP BY u.id, u.nombre, u.email, u.telefono " +
                        "ORDER BY total_compras DESC, monto_total_compras DESC " +
                        "LIMIT ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, limite);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] fila = new String[8];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getString("email");
                fila[3] = rs.getString("telefono");
                fila[4] = String.valueOf(rs.getInt("total_compras"));
                fila[5] = rs.getBigDecimal("monto_total_compras").toString();
                fila[6] = rs.getBigDecimal("promedio_compra").toString();
                fila[7] = rs.getDate("ultima_compra").toString();
                reporte.add(fila);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de clientes frecuentes: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de cuotas pendientes y vencidas
     */
    public String[] reporteCuotasPendientes() {
        String[] reporte = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT " +
                        "COUNT(CASE WHEN estado = 'PENDIENTE' THEN 1 END) as cuotas_pendientes, " +
                        "SUM(CASE WHEN estado = 'PENDIENTE' THEN monto ELSE 0 END) as monto_pendiente, " +
                        "COUNT(CASE WHEN estado = 'VENCIDA' THEN 1 END) as cuotas_vencidas, " +
                        "SUM(CASE WHEN estado = 'VENCIDA' THEN monto ELSE 0 END) as monto_vencido, " +
                        "SUM(CASE WHEN estado = 'VENCIDA' THEN mora ELSE 0 END) as mora_total " +
                        "FROM CUOTA";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                reporte = new String[5];
                reporte[0] = String.valueOf(rs.getInt("cuotas_pendientes"));
                reporte[1] = rs.getBigDecimal("monto_pendiente").toString();
                reporte[2] = String.valueOf(rs.getInt("cuotas_vencidas"));
                reporte[3] = rs.getBigDecimal("monto_vencido").toString();
                reporte[4] = rs.getBigDecimal("mora_total").toString();
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de cuotas: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de ocupación de viajes
     */
    public List<String[]> reporteOcupacionViajes() {
        List<String[]> reporte = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.id, v.codigo_viaje, v.fecha_salida, v.fecha_retorno, " +
                        "pv.nombre as plan_nombre, d.ciudad, d.pais, " +
                        "v.cupos_totales, v.cupos_disponibles, " +
                        "(v.cupos_totales - v.cupos_disponibles) as cupos_vendidos, " +
                        "ROUND(((v.cupos_totales - v.cupos_disponibles)::numeric / v.cupos_totales * 100), 2) as porcentaje_ocupacion, " +
                        "v.estado " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE v.estado IN ('PROGRAMADO', 'EN_CURSO') " +
                        "ORDER BY v.fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] fila = new String[12];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("codigo_viaje");
                fila[2] = rs.getDate("fecha_salida").toString();
                fila[3] = rs.getDate("fecha_retorno").toString();
                fila[4] = rs.getString("plan_nombre");
                fila[5] = rs.getString("ciudad");
                fila[6] = rs.getString("pais");
                fila[7] = String.valueOf(rs.getInt("cupos_totales"));
                fila[8] = String.valueOf(rs.getInt("cupos_disponibles"));
                fila[9] = String.valueOf(rs.getInt("cupos_vendidos"));
                fila[10] = rs.getBigDecimal("porcentaje_ocupacion").toString();
                fila[11] = rs.getString("estado");
                reporte.add(fila);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de ocupación: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Reporte de rendimiento de vendedores
     */
    public List<String[]> reporteRendimientoVendedores(Date fechaInicio, Date fechaFin) {
        List<String[]> reporte = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT u.id, u.nombre, u.email, " +
                        "COUNT(v.id) as total_ventas, " +
                        "SUM(v.monto_total) as monto_total_vendido, " +
                        "AVG(v.monto_total) as promedio_venta, " +
                        "SUM(CASE WHEN v.tipo_venta = 'CONTADO' THEN 1 ELSE 0 END) as ventas_contado, " +
                        "SUM(CASE WHEN v.tipo_venta = 'CREDITO' THEN 1 ELSE 0 END) as ventas_credito " +
                        "FROM USUARIO u " +
                        "INNER JOIN VENTA v ON u.id = v.vendedor_id " +
                        "WHERE u.rol = 'VENDEDOR' " +
                        "AND v.fecha_venta BETWEEN ? AND ? " +
                        "AND v.estado != 'CANCELADA' " +
                        "GROUP BY u.id, u.nombre, u.email " +
                        "ORDER BY monto_total_vendido DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] fila = new String[8];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getString("email");
                fila[3] = String.valueOf(rs.getInt("total_ventas"));
                fila[4] = rs.getBigDecimal("monto_total_vendido").toString();
                fila[5] = rs.getBigDecimal("promedio_venta").toString();
                fila[6] = String.valueOf(rs.getInt("ventas_contado"));
                fila[7] = String.valueOf(rs.getInt("ventas_credito"));
                reporte.add(fila);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de vendedores: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Dashboard - Resumen general del negocio
     */
    public String[] dashboardResumenGeneral() {
        String[] dashboard = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT " +
                        "(SELECT COUNT(*) FROM USUARIO WHERE rol = 'CLIENTE' AND activo = true) as total_clientes, " +
                        "(SELECT COUNT(*) FROM VENTA WHERE estado != 'CANCELADA') as total_ventas, " +
                        "(SELECT SUM(monto_total) FROM VENTA WHERE estado != 'CANCELADA') as ingresos_totales, " +
                        "(SELECT COUNT(*) FROM VIAJE WHERE estado = 'PROGRAMADO') as viajes_programados, " +
                        "(SELECT COUNT(*) FROM RESERVA_VIAJE WHERE estado = 'CONFIRMADA') as reservas_activas, " +
                        "(SELECT SUM(monto) FROM PAGO WHERE fecha_pago >= CURRENT_DATE - INTERVAL '30 days') as ingresos_ultimo_mes, " +
                        "(SELECT COUNT(*) FROM CUOTA WHERE estado = 'VENCIDA') as cuotas_vencidas, " +
                        "(SELECT SUM(monto) FROM CUOTA WHERE estado = 'PENDIENTE') as monto_por_cobrar";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                dashboard = new String[8];
                dashboard[0] = String.valueOf(rs.getInt("total_clientes"));
                dashboard[1] = String.valueOf(rs.getInt("total_ventas"));
                dashboard[2] = rs.getBigDecimal("ingresos_totales").toString();
                dashboard[3] = String.valueOf(rs.getInt("viajes_programados"));
                dashboard[4] = String.valueOf(rs.getInt("reservas_activas"));
                dashboard[5] = rs.getBigDecimal("ingresos_ultimo_mes").toString();
                dashboard[6] = String.valueOf(rs.getInt("cuotas_vencidas"));
                dashboard[7] = rs.getBigDecimal("monto_por_cobrar").toString();
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en dashboard: " + e.getMessage());
        }
        return dashboard;
    }

    /**
     * Reporte de planes de viaje más rentables
     */
    public List<String[]> reportePlanesRentables() {
        List<String[]> reporte = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT pv.id, pv.nombre, pv.categoria, d.ciudad, d.pais, " +
                        "COUNT(DISTINCT v.id) as viajes_realizados, " +
                        "COUNT(rv.id) as total_reservas, " +
                        "SUM(rv.precio_total) as ingresos_totales, " +
                        "AVG(rv.precio_total) as precio_promedio " +
                        "FROM PLAN_VIAJE pv " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "LEFT JOIN VIAJE v ON pv.id = v.plan_viaje_id " +
                        "LEFT JOIN RESERVA_VIAJE rv ON v.id = rv.viaje_id " +
                        "WHERE pv.activo = true " +
                        "GROUP BY pv.id, pv.nombre, pv.categoria, d.ciudad, d.pais " +
                        "ORDER BY ingresos_totales DESC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] fila = new String[9];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getString("categoria");
                fila[3] = rs.getString("ciudad");
                fila[4] = rs.getString("pais");
                fila[5] = String.valueOf(rs.getInt("viajes_realizados"));
                fila[6] = String.valueOf(rs.getInt("total_reservas"));
                fila[7] = rs.getBigDecimal("ingresos_totales").toString();
                fila[8] = rs.getBigDecimal("precio_promedio").toString();
                reporte.add(fila);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error en reporte de planes rentables: " + e.getMessage());
        }
        return reporte;
    }

    /**
     * Obtiene el detalle de ventas dentro de un rango de fechas.
     */
    public List<String[]> obtenerVentasPorPeriodo(Date fechaInicio, Date fechaFin) {
        List<String[]> ventas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT v.id, v.fecha_venta, v.numero_factura, v.tipo_venta, " +
                         "v.monto_total, COALESCE(v.monto_pagado, 0) AS monto_pagado, " +
                         "COALESCE(v.monto_pendiente, v.monto_total - COALESCE(v.monto_pagado, 0)) AS monto_pendiente, " +
                         "(u.nombre || ' ' || u.apellido) AS cliente " +
                         "FROM VENTA v " +
                         "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                         "WHERE v.fecha_venta BETWEEN ? AND ? " +
                         "ORDER BY v.fecha_venta ASC";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[8];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getDate("fecha_venta").toString();
                fila[2] = rs.getString("cliente");
                fila[3] = rs.getString("numero_factura");
                fila[4] = rs.getString("tipo_venta");
                fila[5] = rs.getBigDecimal("monto_total").toString();
                fila[6] = rs.getBigDecimal("monto_pagado").toString();
                fila[7] = rs.getBigDecimal("monto_pendiente").toString();
                ventas.add(fila);
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por periodo: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Obtiene resumen de pagos agrupados por método dentro de un rango de fechas.
     */
    public List<String[]> obtenerPagosPorMetodo(Date fechaInicio, Date fechaFin) {
        List<String[]> resumen = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT metodo_pago, COUNT(*) AS cantidad, " +
                         "COALESCE(SUM(monto), 0) AS total " +
                         "FROM PAGO " +
                         "WHERE fecha_pago BETWEEN ? AND ? " +
                         "GROUP BY metodo_pago " +
                         "ORDER BY total DESC";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[3];
                fila[0] = rs.getString("metodo_pago");
                fila[1] = String.valueOf(rs.getInt("cantidad"));
                fila[2] = rs.getBigDecimal("total").toString();
                resumen.add(fila);
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por metodo: " + e.getMessage());
        }
        return resumen;
    }

    /**
     * Obtiene viajes programados o en curso junto con cupos y destino.
     */
    public List<String[]> obtenerViajesProgramados() {
        List<String[]> viajes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT v.id, v.codigo_viaje, v.cupos_totales, v.cupos_disponibles, " +
                         "v.fecha_salida, v.fecha_retorno, v.estado, v.precio_final, " +
                         "pv.nombre AS plan_nombre, d.nombre AS destino_nombre, d.ciudad, d.pais " +
                         "FROM VIAJE v " +
                         "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                         "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                         "WHERE v.estado IN ('PROGRAMADO', 'EN_CURSO') " +
                         "ORDER BY v.fecha_salida ASC";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[14];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = rs.getString("codigo_viaje");
                fila[2] = String.valueOf(rs.getInt("cupos_totales"));
                fila[3] = String.valueOf(rs.getInt("cupos_disponibles"));
                fila[4] = rs.getDate("fecha_salida").toString();
                fila[5] = rs.getDate("fecha_retorno") != null ? rs.getDate("fecha_retorno").toString() : "null";
                fila[6] = rs.getString("estado");
                fila[7] = rs.getString("plan_nombre");
                fila[8] = rs.getString("destino_nombre");
                fila[9] = rs.getString("ciudad");
                fila[10] = rs.getString("pais");
                fila[11] = rs.getBigDecimal("precio_final").toString();
                fila[12] = rs.getString("plan_nombre");
                fila[13] = rs.getString("destino_nombre") + " (" + rs.getString("ciudad") + ", " + rs.getString("pais") + ")";
                viajes.add(fila);
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener viajes programados: " + e.getMessage());
        }
        return viajes;
    }

    /**
     * Obtiene resumen de reservas agrupadas por estado.
     */
    public List<String[]> obtenerReservasPorEstado() {
        List<String[]> resumen = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT estado, COUNT(*) AS cantidad, " +
                         "COALESCE(SUM(numero_personas), 0) AS personas, " +
                         "COALESCE(SUM(precio_total), 0) AS monto " +
                         "FROM RESERVA_VIAJE " +
                         "GROUP BY estado " +
                         "ORDER BY cantidad DESC";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[4];
                fila[0] = rs.getString("estado");
                fila[1] = String.valueOf(rs.getInt("cantidad"));
                fila[2] = String.valueOf(rs.getInt("personas"));
                fila[3] = rs.getBigDecimal("monto").toString();
                resumen.add(fila);
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por estado: " + e.getMessage());
        }
        return resumen;
    }

    /**
     * Obtiene el detalle de cuotas vencidas junto con información del cliente.
     */
    public List<String[]> obtenerCuotasVencidas() {
        List<String[]> cuotas = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT c.id, c.plan_pago_id, c.numero_cuota, c.monto, c.fecha_vencimiento, " +
                         "c.estado, c.fecha_pago, c.monto_pagado, c.mora, " +
                         "(u.nombre || ' ' || u.apellido) AS cliente " +
                         "FROM CUOTA c " +
                         "INNER JOIN PLAN_PAGO pp ON c.plan_pago_id = pp.id " +
                         "INNER JOIN VENTA v ON pp.venta_id = v.id " +
                         "INNER JOIN USUARIO u ON v.cliente_id = u.id " +
                         "WHERE c.estado = 'VENCIDA' " +
                         "ORDER BY c.fecha_vencimiento ASC";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[10];
                fila[0] = String.valueOf(rs.getInt("id"));
                fila[1] = String.valueOf(rs.getInt("plan_pago_id"));
                fila[2] = String.valueOf(rs.getInt("numero_cuota"));
                fila[3] = rs.getBigDecimal("monto").toString();
                fila[4] = rs.getDate("fecha_vencimiento").toString();
                fila[5] = rs.getString("estado");
                fila[6] = rs.getDate("fecha_pago") != null ? rs.getDate("fecha_pago").toString() : "null";
                fila[7] = rs.getBigDecimal("monto_pagado") != null ? rs.getBigDecimal("monto_pagado").toString() : "0";
                fila[8] = rs.getBigDecimal("mora") != null ? rs.getBigDecimal("mora").toString() : "0";
                fila[9] = rs.getString("cliente");
                cuotas.add(fila);
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
     * Obtiene los destinos con mayor cantidad de reservas.
     */
    public List<String[]> obtenerDestinosMasVendidos(int limite) {
        List<String[]> destinos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT d.nombre, d.pais, " +
                         "COALESCE(COUNT(rv.id), 0) AS reservas, " +
                         "COALESCE(SUM(rv.numero_personas), 0) AS personas, " +
                         "COALESCE(SUM(rv.precio_total), 0) AS ingresos " +
                         "FROM DESTINO d " +
                         "INNER JOIN PLAN_VIAJE pv ON pv.destino_id = d.id " +
                         "INNER JOIN VIAJE v ON v.plan_viaje_id = pv.id " +
                         "LEFT JOIN RESERVA_VIAJE rv ON rv.viaje_id = v.id " +
                         "GROUP BY d.nombre, d.pais " +
                         "ORDER BY reservas DESC, ingresos DESC " +
                         "LIMIT ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, limite);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[5];
                fila[0] = rs.getString("nombre");
                fila[1] = rs.getString("pais");
                fila[2] = String.valueOf(rs.getInt("reservas"));
                fila[3] = String.valueOf(rs.getInt("personas"));
                fila[4] = rs.getBigDecimal("ingresos").toString();
                destinos.add(fila);
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener destinos mas vendidos: " + e.getMessage());
        }
        return destinos;
    }

    /**
     * Obtiene el total de ventas realizadas en la fecha actual.
     */
    public String[] obtenerVentasHoy() {
        String[] resultado = new String[]{"0", "0"};
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT COUNT(*) AS total, COALESCE(SUM(monto_total), 0) AS monto " +
                         "FROM VENTA WHERE fecha_venta = CURRENT_DATE";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                resultado[0] = String.valueOf(rs.getInt("total"));
                resultado[1] = rs.getBigDecimal("monto").toString();
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas de hoy: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Cuenta reservas activas y personas asociadas.
     */
    public String[] contarReservasActivas() {
        String[] resultado = new String[]{"0", "0"};
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT COUNT(*) AS total, COALESCE(SUM(numero_personas), 0) AS personas " +
                         "FROM RESERVA_VIAJE " +
                         "WHERE estado IN ('CONFIRMADA', 'PAGADA')";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                resultado[0] = String.valueOf(rs.getInt("total"));
                resultado[1] = String.valueOf(rs.getInt("personas"));
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al contar reservas activas: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Cuenta viajes programados dentro de los próximos N días.
     */
    public String[] contarViajesProximos(int dias) {
        String[] resultado = new String[]{"0"};
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT COUNT(*) AS total " +
                         "FROM VIAJE " +
                         "WHERE estado = 'PROGRAMADO' " +
                         "AND fecha_salida BETWEEN CURRENT_DATE AND ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            LocalDate limite = LocalDate.now().plusDays(dias);
            statement.setDate(1, Date.valueOf(limite));
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                resultado[0] = String.valueOf(rs.getInt("total"));
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al contar viajes proximos: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Cuenta cuotas pendientes cuya fecha de vencimiento se aproxima.
     */
    public String[] contarCuotasProximasVencer(int dias) {
        String[] resultado = new String[]{"0", "0"};
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT COUNT(*) AS total, COALESCE(SUM(monto), 0) AS monto " +
                         "FROM CUOTA " +
                         "WHERE estado = 'PENDIENTE' " +
                         "AND fecha_vencimiento BETWEEN CURRENT_DATE AND ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            LocalDate limite = LocalDate.now().plusDays(dias);
            statement.setDate(1, Date.valueOf(limite));
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                resultado[0] = String.valueOf(rs.getInt("total"));
                resultado[1] = rs.getBigDecimal("monto").toString();
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al contar cuotas proximas a vencer: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Cuenta cuotas vencidas y el monto asociado.
     */
    public String[] contarCuotasVencidas() {
        String[] resultado = new String[]{"0", "0"};
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT COUNT(*) AS total, COALESCE(SUM(monto), 0) AS monto " +
                         "FROM CUOTA WHERE estado = 'VENCIDA'";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                resultado[0] = String.valueOf(rs.getInt("total"));
                resultado[1] = rs.getBigDecimal("monto").toString();
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al contar cuotas vencidas: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene ingresos diarios para un mes específico.
     */
    public List<String[]> obtenerIngresosMensuales(int anio, int mes) {
        List<String[]> ingresos = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();

            String sql = "SELECT DATE(fecha_pago) AS fecha, " +
                         "COUNT(*) AS cantidad, COALESCE(SUM(monto), 0) AS total " +
                         "FROM PAGO " +
                         "WHERE EXTRACT(YEAR FROM fecha_pago) = ? " +
                         "AND EXTRACT(MONTH FROM fecha_pago) = ? " +
                         "GROUP BY DATE(fecha_pago) " +
                         "ORDER BY fecha ASC";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, anio);
            statement.setInt(2, mes);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String[] fila = new String[3];
                fila[0] = rs.getDate("fecha").toString();
                fila[1] = String.valueOf(rs.getInt("cantidad"));
                fila[2] = rs.getBigDecimal("total").toString();
                ingresos.add(fila);
            }

            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error al obtener ingresos mensuales: " + e.getMessage());
        }
        return ingresos;
    }
}
