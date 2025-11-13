package com.tecnoweb.grupo7sa.data;

import com.tecnoweb.grupo7sa.ConfigDB.ConfigDB;
import com.tecnoweb.grupo7sa.ConfigDB.DatabaseConection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class DActividad {

    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DActividad() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    // CU3 - GESTIÓN DE ACTIVIDADES DEL DÍA

    /**
     * Crear nueva actividad
     */
    public String save(int diaPlanId, Time hora, String nombre, String descripcion, 
                       String lugar, BigDecimal costoExtra, boolean obligatoria) {
        String query = "INSERT INTO ACTIVIDAD (dia_plan_id, hora, nombre, descripcion, lugar, costo_extra, obligatoria) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, diaPlanId);
            if (hora != null) {
                ps.setTime(2, hora);
            } else {
                ps.setNull(2, java.sql.Types.TIME);
            }
            ps.setString(3, nombre);
            ps.setString(4, descripcion);
            ps.setString(5, lugar);
            ps.setBigDecimal(6, costoExtra != null ? costoExtra : BigDecimal.ZERO);
            ps.setBoolean(7, obligatoria);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Actividad creada exitosamente" : "Error: No se pudo crear la actividad";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Actualizar actividad existente
     */
    public String update(int id, int diaPlanId, Time hora, String nombre, String descripcion,
                         String lugar, BigDecimal costoExtra, boolean obligatoria) {
        String query = "UPDATE ACTIVIDAD SET dia_plan_id = ?, hora = ?, nombre = ?, descripcion = ?, " +
                "lugar = ?, costo_extra = ?, obligatoria = ? WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, diaPlanId);
            if (hora != null) {
                ps.setTime(2, hora);
            } else {
                ps.setNull(2, java.sql.Types.TIME);
            }
            ps.setString(3, nombre);
            ps.setString(4, descripcion);
            ps.setString(5, lugar);
            ps.setBigDecimal(6, costoExtra != null ? costoExtra : BigDecimal.ZERO);
            ps.setBoolean(7, obligatoria);
            ps.setInt(8, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Actividad actualizada exitosamente" : "Error: No se pudo actualizar la actividad";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Eliminar actividad
     */
    public String delete(int id) {
        String query = "DELETE FROM ACTIVIDAD WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Actividad eliminada exitosamente" : "Error: No se pudo eliminar la actividad";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Listar todas las actividades de un día
     */
    public List<String[]> findByDia(int diaPlanId) {
        String query = "SELECT id, dia_plan_id, hora, nombre, descripcion, lugar, costo_extra, obligatoria " +
                "FROM ACTIVIDAD WHERE dia_plan_id = ? ORDER BY hora";
        List<String[]> actividades = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, diaPlanId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] actividad = new String[8];
                actividad[0] = String.valueOf(rs.getInt("id"));
                actividad[1] = String.valueOf(rs.getInt("dia_plan_id"));
                Time hora = rs.getTime("hora");
                actividad[2] = hora != null ? hora.toString() : "null";
                actividad[3] = rs.getString("nombre");
                actividad[4] = rs.getString("descripcion");
                actividad[5] = rs.getString("lugar");
                actividad[6] = rs.getBigDecimal("costo_extra").toString();
                actividad[7] = String.valueOf(rs.getBoolean("obligatoria"));
                actividades.add(actividad);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return actividades;
    }

    /**
     * Buscar actividad por ID
     */
    public String[] findOneById(int id) {
        String query = "SELECT id, dia_plan_id, hora, nombre, descripcion, lugar, costo_extra, obligatoria " +
                "FROM ACTIVIDAD WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] actividad = new String[8];
                actividad[0] = String.valueOf(rs.getInt("id"));
                actividad[1] = String.valueOf(rs.getInt("dia_plan_id"));
                Time hora = rs.getTime("hora");
                actividad[2] = hora != null ? hora.toString() : "null";
                actividad[3] = rs.getString("nombre");
                actividad[4] = rs.getString("descripcion");
                actividad[5] = rs.getString("lugar");
                actividad[6] = rs.getBigDecimal("costo_extra").toString();
                actividad[7] = String.valueOf(rs.getBoolean("obligatoria"));

                rs.close();
                ps.close();
                return actividad;
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
     * Listar todas las actividades de un plan completo
     */
    public List<String[]> findByPlan(int planViajeId) {
        String query = "SELECT a.id, a.dia_plan_id, d.numero_dia, a.hora, a.nombre, a.descripcion, " +
                "a.lugar, a.costo_extra, a.obligatoria " +
                "FROM ACTIVIDAD a " +
                "INNER JOIN DIA_PLAN d ON a.dia_plan_id = d.id " +
                "WHERE d.plan_viaje_id = ? " +
                "ORDER BY d.numero_dia, a.hora";
        List<String[]> actividades = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, planViajeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] actividad = new String[9];
                actividad[0] = String.valueOf(rs.getInt("id"));
                actividad[1] = String.valueOf(rs.getInt("dia_plan_id"));
                actividad[2] = String.valueOf(rs.getInt("numero_dia"));
                Time hora = rs.getTime("hora");
                actividad[3] = hora != null ? hora.toString() : "null";
                actividad[4] = rs.getString("nombre");
                actividad[5] = rs.getString("descripcion");
                actividad[6] = rs.getString("lugar");
                actividad[7] = rs.getBigDecimal("costo_extra").toString();
                actividad[8] = String.valueOf(rs.getBoolean("obligatoria"));
                actividades.add(actividad);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return actividades;
    }
}
