package com.tecnoweb.grupo15sa.data;

import com.tecnoweb.grupo15sa.ConfigDB.ConfigDB;
import com.tecnoweb.grupo15sa.ConfigDB.DatabaseConection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DPlanViaje {

    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DPlanViaje() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    // CU3 - GESTIÓN DE PLANES DE VIAJE

    /**
     * Crear nuevo plan de viaje
     */
    public String save(String nombre, String descripcion, int destinoId, int duracionDias, 
                       BigDecimal precioTotal, boolean incluyeHotel, boolean incluyeTransporte, 
                       boolean incluyeComidas, String categoria, Integer cupoMaximo) {
        String query = "INSERT INTO PLAN_VIAJE (nombre, descripcion, destino_id, duracion_dias, precio_total, " +
                "incluye_hotel, incluye_transporte, incluye_comidas, categoria, cupo_maximo, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setInt(3, destinoId);
            ps.setInt(4, duracionDias);
            ps.setBigDecimal(5, precioTotal);
            ps.setBoolean(6, incluyeHotel);
            ps.setBoolean(7, incluyeTransporte);
            ps.setBoolean(8, incluyeComidas);
            ps.setString(9, categoria);
            if (cupoMaximo != null) {
                ps.setInt(10, cupoMaximo);
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.setBoolean(11, true);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Plan de viaje creado exitosamente" : "Error: No se pudo crear el plan";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Actualizar plan de viaje existente
     */
    public String update(int id, String nombre, String descripcion, int destinoId, int duracionDias,
                         BigDecimal precioTotal, boolean incluyeHotel, boolean incluyeTransporte,
                         boolean incluyeComidas, String categoria, Integer cupoMaximo) {
        String query = "UPDATE PLAN_VIAJE SET nombre = ?, descripcion = ?, destino_id = ?, duracion_dias = ?, " +
                "precio_total = ?, incluye_hotel = ?, incluye_transporte = ?, incluye_comidas = ?, " +
                "categoria = ?, cupo_maximo = ? WHERE id = ? AND activo = TRUE";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setInt(3, destinoId);
            ps.setInt(4, duracionDias);
            ps.setBigDecimal(5, precioTotal);
            ps.setBoolean(6, incluyeHotel);
            ps.setBoolean(7, incluyeTransporte);
            ps.setBoolean(8, incluyeComidas);
            ps.setString(9, categoria);
            if (cupoMaximo != null) {
                ps.setInt(10, cupoMaximo);
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.setInt(11, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Plan de viaje actualizado exitosamente" : "Error: No se pudo actualizar el plan";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Desactivar plan de viaje
     */
    public String delete(int id) {
        String query = "UPDATE PLAN_VIAJE SET activo = false WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Plan desactivado exitosamente" : "Error: No se pudo desactivar el plan";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Reactivar plan de viaje
     */
    public String reactivate(int id) {
        String query = "UPDATE PLAN_VIAJE SET activo = true WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Plan reactivado exitosamente" : "Error: No se pudo reactivar el plan";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Listar todos los planes activos con información del destino
     */
    public List<String[]> findAll() {
        String query = "SELECT p.id, p.nombre, p.descripcion, p.destino_id, d.nombre as destino_nombre, " +
                "p.duracion_dias, p.precio_total, p.incluye_hotel, p.incluye_transporte, p.incluye_comidas, " +
                "p.categoria, p.cupo_maximo, p.activo, p.fecha_creacion " +
                "FROM PLAN_VIAJE p " +
                "INNER JOIN DESTINO d ON p.destino_id = d.id " +
                "WHERE p.activo = true ORDER BY p.categoria, p.precio_total";
        List<String[]> planes = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] plan = new String[14];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = rs.getString("nombre");
                plan[2] = rs.getString("descripcion");
                plan[3] = String.valueOf(rs.getInt("destino_id"));
                plan[4] = rs.getString("destino_nombre");
                plan[5] = String.valueOf(rs.getInt("duracion_dias"));
                plan[6] = rs.getBigDecimal("precio_total").toString();
                plan[7] = String.valueOf(rs.getBoolean("incluye_hotel"));
                plan[8] = String.valueOf(rs.getBoolean("incluye_transporte"));
                plan[9] = String.valueOf(rs.getBoolean("incluye_comidas"));
                plan[10] = rs.getString("categoria");
                plan[11] = rs.getObject("cupo_maximo") != null ? String.valueOf(rs.getInt("cupo_maximo")) : "null";
                plan[12] = String.valueOf(rs.getBoolean("activo"));
                plan[13] = rs.getTimestamp("fecha_creacion").toString();
                planes.add(plan);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return planes;
    }

    /**
     * Buscar plan por ID
     */
    public String[] findOneById(int id) {
        String query = "SELECT p.id, p.nombre, p.descripcion, p.destino_id, d.nombre as destino_nombre, " +
                "p.duracion_dias, p.precio_total, p.incluye_hotel, p.incluye_transporte, p.incluye_comidas, " +
                "p.categoria, p.cupo_maximo, p.activo, p.fecha_creacion " +
                "FROM PLAN_VIAJE p " +
                "INNER JOIN DESTINO d ON p.destino_id = d.id " +
                "WHERE p.id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] plan = new String[14];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = rs.getString("nombre");
                plan[2] = rs.getString("descripcion");
                plan[3] = String.valueOf(rs.getInt("destino_id"));
                plan[4] = rs.getString("destino_nombre");
                plan[5] = String.valueOf(rs.getInt("duracion_dias"));
                plan[6] = rs.getBigDecimal("precio_total").toString();
                plan[7] = String.valueOf(rs.getBoolean("incluye_hotel"));
                plan[8] = String.valueOf(rs.getBoolean("incluye_transporte"));
                plan[9] = String.valueOf(rs.getBoolean("incluye_comidas"));
                plan[10] = rs.getString("categoria");
                plan[11] = rs.getObject("cupo_maximo") != null ? String.valueOf(rs.getInt("cupo_maximo")) : "null";
                plan[12] = String.valueOf(rs.getBoolean("activo"));
                plan[13] = rs.getTimestamp("fecha_creacion").toString();

                rs.close();
                ps.close();
                return plan;
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
     * Buscar planes por destino
     */
    public List<String[]> findByDestino(int destinoId) {
        String query = "SELECT p.id, p.nombre, p.descripcion, p.destino_id, d.nombre as destino_nombre, " +
                "p.duracion_dias, p.precio_total, p.incluye_hotel, p.incluye_transporte, p.incluye_comidas, " +
                "p.categoria, p.cupo_maximo, p.activo, p.fecha_creacion " +
                "FROM PLAN_VIAJE p " +
                "INNER JOIN DESTINO d ON p.destino_id = d.id " +
                "WHERE p.destino_id = ? AND p.activo = true ORDER BY p.precio_total";
        List<String[]> planes = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, destinoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] plan = new String[14];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = rs.getString("nombre");
                plan[2] = rs.getString("descripcion");
                plan[3] = String.valueOf(rs.getInt("destino_id"));
                plan[4] = rs.getString("destino_nombre");
                plan[5] = String.valueOf(rs.getInt("duracion_dias"));
                plan[6] = rs.getBigDecimal("precio_total").toString();
                plan[7] = String.valueOf(rs.getBoolean("incluye_hotel"));
                plan[8] = String.valueOf(rs.getBoolean("incluye_transporte"));
                plan[9] = String.valueOf(rs.getBoolean("incluye_comidas"));
                plan[10] = rs.getString("categoria");
                plan[11] = rs.getObject("cupo_maximo") != null ? String.valueOf(rs.getInt("cupo_maximo")) : "null";
                plan[12] = String.valueOf(rs.getBoolean("activo"));
                plan[13] = rs.getTimestamp("fecha_creacion").toString();
                planes.add(plan);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return planes;
    }

    /**
     * Buscar planes por categoría
     */
    public List<String[]> findByCategoria(String categoria) {
        String query = "SELECT p.id, p.nombre, p.descripcion, p.destino_id, d.nombre as destino_nombre, " +
                "p.duracion_dias, p.precio_total, p.incluye_hotel, p.incluye_transporte, p.incluye_comidas, " +
                "p.categoria, p.cupo_maximo, p.activo, p.fecha_creacion " +
                "FROM PLAN_VIAJE p " +
                "INNER JOIN DESTINO d ON p.destino_id = d.id " +
                "WHERE p.categoria = ? AND p.activo = true ORDER BY p.precio_total";
        List<String[]> planes = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, categoria);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] plan = new String[14];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = rs.getString("nombre");
                plan[2] = rs.getString("descripcion");
                plan[3] = String.valueOf(rs.getInt("destino_id"));
                plan[4] = rs.getString("destino_nombre");
                plan[5] = String.valueOf(rs.getInt("duracion_dias"));
                plan[6] = rs.getBigDecimal("precio_total").toString();
                plan[7] = String.valueOf(rs.getBoolean("incluye_hotel"));
                plan[8] = String.valueOf(rs.getBoolean("incluye_transporte"));
                plan[9] = String.valueOf(rs.getBoolean("incluye_comidas"));
                plan[10] = rs.getString("categoria");
                plan[11] = rs.getObject("cupo_maximo") != null ? String.valueOf(rs.getInt("cupo_maximo")) : "null";
                plan[12] = String.valueOf(rs.getBoolean("activo"));
                plan[13] = rs.getTimestamp("fecha_creacion").toString();
                planes.add(plan);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return planes;
    }

    /**
     * Buscar planes por rango de precio
     */
    public List<String[]> findByPrecioRango(BigDecimal precioMin, BigDecimal precioMax) {
        String query = "SELECT p.id, p.nombre, p.descripcion, p.destino_id, d.nombre as destino_nombre, " +
                "p.duracion_dias, p.precio_total, p.incluye_hotel, p.incluye_transporte, p.incluye_comidas, " +
                "p.categoria, p.cupo_maximo, p.activo, p.fecha_creacion " +
                "FROM PLAN_VIAJE p " +
                "INNER JOIN DESTINO d ON p.destino_id = d.id " +
                "WHERE p.precio_total BETWEEN ? AND ? AND p.activo = true ORDER BY p.precio_total";
        List<String[]> planes = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setBigDecimal(1, precioMin);
            ps.setBigDecimal(2, precioMax);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] plan = new String[14];
                plan[0] = String.valueOf(rs.getInt("id"));
                plan[1] = rs.getString("nombre");
                plan[2] = rs.getString("descripcion");
                plan[3] = String.valueOf(rs.getInt("destino_id"));
                plan[4] = rs.getString("destino_nombre");
                plan[5] = String.valueOf(rs.getInt("duracion_dias"));
                plan[6] = rs.getBigDecimal("precio_total").toString();
                plan[7] = String.valueOf(rs.getBoolean("incluye_hotel"));
                plan[8] = String.valueOf(rs.getBoolean("incluye_transporte"));
                plan[9] = String.valueOf(rs.getBoolean("incluye_comidas"));
                plan[10] = rs.getString("categoria");
                plan[11] = rs.getObject("cupo_maximo") != null ? String.valueOf(rs.getInt("cupo_maximo")) : "null";
                plan[12] = String.valueOf(rs.getBoolean("activo"));
                plan[13] = rs.getTimestamp("fecha_creacion").toString();
                planes.add(plan);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return planes;
    }
}
