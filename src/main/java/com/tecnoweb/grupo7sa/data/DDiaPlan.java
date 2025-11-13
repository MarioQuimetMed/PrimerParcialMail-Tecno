package com.tecnoweb.grupo7sa.data;

import com.tecnoweb.grupo7sa.ConfigDB.ConfigDB;
import com.tecnoweb.grupo7sa.ConfigDB.DatabaseConection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DDiaPlan {

    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DDiaPlan() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    // CU3 - GESTIÓN DE DÍAS DEL PLAN DE VIAJE

    /**
     * Crear nuevo día en el plan
     */
    public String save(int planViajeId, int numeroDia, String titulo, String descripcion) {
        String query = "INSERT INTO DIA_PLAN (plan_viaje_id, numero_dia, titulo, descripcion) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, planViajeId);
            ps.setInt(2, numeroDia);
            ps.setString(3, titulo);
            ps.setString(4, descripcion);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Día agregado exitosamente al plan" : "Error: No se pudo agregar el día";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Actualizar día del plan
     */
    public String update(int id, int planViajeId, int numeroDia, String titulo, String descripcion) {
        String query = "UPDATE DIA_PLAN SET plan_viaje_id = ?, numero_dia = ?, titulo = ?, descripcion = ? WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, planViajeId);
            ps.setInt(2, numeroDia);
            ps.setString(3, titulo);
            ps.setString(4, descripcion);
            ps.setInt(5, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Día actualizado exitosamente" : "Error: No se pudo actualizar el día";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Eliminar día del plan
     */
    public String delete(int id) {
        String query = "DELETE FROM DIA_PLAN WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Día eliminado exitosamente" : "Error: No se pudo eliminar el día";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Listar todos los días de un plan
     */
    public List<String[]> findByPlan(int planViajeId) {
        String query = "SELECT id, plan_viaje_id, numero_dia, titulo, descripcion " +
                "FROM DIA_PLAN WHERE plan_viaje_id = ? ORDER BY numero_dia";
        List<String[]> dias = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, planViajeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] dia = new String[5];
                dia[0] = String.valueOf(rs.getInt("id"));
                dia[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                dia[2] = String.valueOf(rs.getInt("numero_dia"));
                dia[3] = rs.getString("titulo");
                dia[4] = rs.getString("descripcion");
                dias.add(dia);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return dias;
    }

    /**
     * Buscar día específico por ID
     */
    public String[] findOneById(int id) {
        String query = "SELECT id, plan_viaje_id, numero_dia, titulo, descripcion FROM DIA_PLAN WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] dia = new String[5];
                dia[0] = String.valueOf(rs.getInt("id"));
                dia[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                dia[2] = String.valueOf(rs.getInt("numero_dia"));
                dia[3] = rs.getString("titulo");
                dia[4] = rs.getString("descripcion");

                rs.close();
                ps.close();
                return dia;
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
     * Buscar un día específico por número de día
     */
    public String[] findByPlanAndDia(int planViajeId, int numeroDia) {
        String query = "SELECT id, plan_viaje_id, numero_dia, titulo, descripcion " +
                "FROM DIA_PLAN WHERE plan_viaje_id = ? AND numero_dia = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, planViajeId);
            ps.setInt(2, numeroDia);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] dia = new String[5];
                dia[0] = String.valueOf(rs.getInt("id"));
                dia[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                dia[2] = String.valueOf(rs.getInt("numero_dia"));
                dia[3] = rs.getString("titulo");
                dia[4] = rs.getString("descripcion");

                rs.close();
                ps.close();
                return dia;
            }

            rs.close();
            ps.close();
            return null;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}
