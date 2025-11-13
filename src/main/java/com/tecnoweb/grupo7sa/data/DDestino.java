package com.tecnoweb.grupo7sa.data;

import com.tecnoweb.grupo7sa.ConfigDB.ConfigDB;
import com.tecnoweb.grupo7sa.ConfigDB.DatabaseConection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DDestino {

    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DDestino() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    // CU2 - GESTIÓN DE DESTINOS TURÍSTICOS

    /**
     * Crear nuevo destino
     */
    public String save(String nombre, String pais, String ciudad, String descripcion, String clima,
                       String idioma, String moneda, BigDecimal precioBase, String imagenUrl) {
        String query = "INSERT INTO DESTINO (nombre, pais, ciudad, descripcion, clima, idioma, moneda, precio_base, imagen_url, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, pais);
            ps.setString(3, ciudad);
            ps.setString(4, descripcion);
            ps.setString(5, clima);
            ps.setString(6, idioma);
            ps.setString(7, moneda);
            ps.setBigDecimal(8, precioBase);
            ps.setString(9, imagenUrl);
            ps.setBoolean(10, true);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Destino creado exitosamente" : "Error: No se pudo crear el destino";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Actualizar destino existente
     */
    public String update(int id, String nombre, String pais, String ciudad, String descripcion, String clima,
                         String idioma, String moneda, BigDecimal precioBase, String imagenUrl) {
        String query = "UPDATE DESTINO SET nombre = ?, pais = ?, ciudad = ?, descripcion = ?, clima = ?, " +
                "idioma = ?, moneda = ?, precio_base = ?, imagen_url = ? WHERE id = ? AND activo = TRUE";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, pais);
            ps.setString(3, ciudad);
            ps.setString(4, descripcion);
            ps.setString(5, clima);
            ps.setString(6, idioma);
            ps.setString(7, moneda);
            ps.setBigDecimal(8, precioBase);
            ps.setString(9, imagenUrl);
            ps.setInt(10, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Destino actualizado exitosamente" : "Error: No se pudo actualizar el destino";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Desactivar destino (soft delete)
     */
    public String delete(int id) {
        String query = "UPDATE DESTINO SET activo = false WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Destino desactivado exitosamente" : "Error: No se pudo desactivar el destino";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Reactivar destino
     */
    public String reactivate(int id) {
        String query = "UPDATE DESTINO SET activo = true WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);

            int result = ps.executeUpdate();
            ps.close();

            return result > 0 ? "Destino reactivado exitosamente" : "Error: No se pudo reactivar el destino";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Listar todos los destinos activos
     */
    public List<String[]> findAll() {
        String query = "SELECT id, nombre, pais, ciudad, descripcion, clima, idioma, moneda, precio_base, imagen_url, activo " +
                "FROM DESTINO WHERE activo = true ORDER BY pais, ciudad";
        List<String[]> destinos = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] destino = new String[11];
                destino[0] = String.valueOf(rs.getInt("id"));
                destino[1] = rs.getString("nombre");
                destino[2] = rs.getString("pais");
                destino[3] = rs.getString("ciudad");
                destino[4] = rs.getString("descripcion");
                destino[5] = rs.getString("clima");
                destino[6] = rs.getString("idioma");
                destino[7] = rs.getString("moneda");
                destino[8] = rs.getBigDecimal("precio_base").toString();
                destino[9] = rs.getString("imagen_url");
                destino[10] = String.valueOf(rs.getBoolean("activo"));
                destinos.add(destino);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return destinos;
    }

    /**
     * Buscar destino por ID
     */
    public String[] findOneById(int id) {
        String query = "SELECT id, nombre, pais, ciudad, descripcion, clima, idioma, moneda, precio_base, imagen_url, activo " +
                "FROM DESTINO WHERE id = ?";

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String[] destino = new String[11];
                destino[0] = String.valueOf(rs.getInt("id"));
                destino[1] = rs.getString("nombre");
                destino[2] = rs.getString("pais");
                destino[3] = rs.getString("ciudad");
                destino[4] = rs.getString("descripcion");
                destino[5] = rs.getString("clima");
                destino[6] = rs.getString("idioma");
                destino[7] = rs.getString("moneda");
                destino[8] = rs.getBigDecimal("precio_base").toString();
                destino[9] = rs.getString("imagen_url");
                destino[10] = String.valueOf(rs.getBoolean("activo"));

                rs.close();
                ps.close();
                return destino;
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
     * Buscar destinos por país
     */
    public List<String[]> findByPais(String pais) {
        String query = "SELECT id, nombre, pais, ciudad, descripcion, clima, idioma, moneda, precio_base, imagen_url, activo " +
                "FROM DESTINO WHERE pais = ? AND activo = true ORDER BY ciudad";
        List<String[]> destinos = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, pais);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] destino = new String[11];
                destino[0] = String.valueOf(rs.getInt("id"));
                destino[1] = rs.getString("nombre");
                destino[2] = rs.getString("pais");
                destino[3] = rs.getString("ciudad");
                destino[4] = rs.getString("descripcion");
                destino[5] = rs.getString("clima");
                destino[6] = rs.getString("idioma");
                destino[7] = rs.getString("moneda");
                destino[8] = rs.getBigDecimal("precio_base").toString();
                destino[9] = rs.getString("imagen_url");
                destino[10] = String.valueOf(rs.getBoolean("activo"));
                destinos.add(destino);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return destinos;
    }

    /**
     * Buscar destinos por ciudad
     */
    public List<String[]> findByCiudad(String ciudad) {
        String query = "SELECT id, nombre, pais, ciudad, descripcion, clima, idioma, moneda, precio_base, imagen_url, activo " +
                "FROM DESTINO WHERE ciudad = ? AND activo = true ORDER BY nombre";
        List<String[]> destinos = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setString(1, ciudad);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] destino = new String[11];
                destino[0] = String.valueOf(rs.getInt("id"));
                destino[1] = rs.getString("nombre");
                destino[2] = rs.getString("pais");
                destino[3] = rs.getString("ciudad");
                destino[4] = rs.getString("descripcion");
                destino[5] = rs.getString("clima");
                destino[6] = rs.getString("idioma");
                destino[7] = rs.getString("moneda");
                destino[8] = rs.getBigDecimal("precio_base").toString();
                destino[9] = rs.getString("imagen_url");
                destino[10] = String.valueOf(rs.getBoolean("activo"));
                destinos.add(destino);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return destinos;
    }

    /**
     * Buscar destinos por rango de precio
     */
    public List<String[]> findByPrecioRango(BigDecimal precioMin, BigDecimal precioMax) {
        String query = "SELECT id, nombre, pais, ciudad, descripcion, clima, idioma, moneda, precio_base, imagen_url, activo " +
                "FROM DESTINO WHERE precio_base BETWEEN ? AND ? AND activo = true ORDER BY precio_base";
        List<String[]> destinos = new ArrayList<>();

        try {
            PreparedStatement ps = databaseConection.openConnection().prepareStatement(query);
            ps.setBigDecimal(1, precioMin);
            ps.setBigDecimal(2, precioMax);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] destino = new String[11];
                destino[0] = String.valueOf(rs.getInt("id"));
                destino[1] = rs.getString("nombre");
                destino[2] = rs.getString("pais");
                destino[3] = rs.getString("ciudad");
                destino[4] = rs.getString("descripcion");
                destino[5] = rs.getString("clima");
                destino[6] = rs.getString("idioma");
                destino[7] = rs.getString("moneda");
                destino[8] = rs.getBigDecimal("precio_base").toString();
                destino[9] = rs.getString("imagen_url");
                destino[10] = String.valueOf(rs.getBoolean("activo"));
                destinos.add(destino);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return destinos;
    }
}
