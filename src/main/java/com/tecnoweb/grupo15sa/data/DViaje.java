package com.tecnoweb.grupo15sa.data;

import com.tecnoweb.grupo15sa.ConfigDB.DatabaseConection;
import com.tecnoweb.grupo15sa.ConfigDB.ConfigDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DViaje {
    private final DatabaseConection databaseConection;
    ConfigDB configDB = new ConfigDB();

    public DViaje() {
        this.databaseConection = new DatabaseConection(configDB.getUser(), configDB.getPassword(),
                configDB.getHost(), configDB.getPort(), configDB.getDbName());
    }

    public void disconnect() {
        if (databaseConection != null) {
            databaseConection.closeConnection();
        }
    }

    /**
     * Guarda un nuevo viaje
     */
    public String[] save(int planViajeId, String codigoViaje, Date fechaSalida, 
                        Date fechaRetorno, int cuposDisponibles, int cuposTotales, 
                        BigDecimal precioFinal, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "INSERT INTO VIAJE (plan_viaje_id, codigo_viaje, fecha_salida, " +
                        "fecha_retorno, cupos_disponibles, cupos_totales, precio_final, estado) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, planViajeId);
            statement.setString(2, codigoViaje);
            statement.setDate(3, fechaSalida);
            statement.setDate(4, fechaRetorno);
            statement.setInt(5, cuposDisponibles);
            statement.setInt(6, cuposTotales);
            statement.setBigDecimal(7, precioFinal);
            statement.setString(8, estado);
            
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("id"));
            } else {
                result[0] = "-1";
                result[1] = "Error al guardar el viaje";
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
     * Actualiza un viaje existente
     */
    public String[] update(int id, int planViajeId, String codigoViaje, Date fechaSalida,
                          Date fechaRetorno, int cuposDisponibles, int cuposTotales,
                          BigDecimal precioFinal, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE VIAJE SET plan_viaje_id = ?, codigo_viaje = ?, fecha_salida = ?, " +
                        "fecha_retorno = ?, cupos_disponibles = ?, cupos_totales = ?, " +
                        "precio_final = ?, estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, planViajeId);
            statement.setString(2, codigoViaje);
            statement.setDate(3, fechaSalida);
            statement.setDate(4, fechaRetorno);
            statement.setInt(5, cuposDisponibles);
            statement.setInt(6, cuposTotales);
            statement.setBigDecimal(7, precioFinal);
            statement.setString(8, estado);
            statement.setInt(9, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Viaje actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el viaje";
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
     * Actualiza el estado de un viaje
     */
    public String[] updateEstado(int id, String estado) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE VIAJE SET estado = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            statement.setInt(2, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Estado actualizado correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el viaje";
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
     * Actualiza los cupos disponibles de un viaje
     */
    public String[] updateCupos(int id, int cuposDisponibles) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE VIAJE SET cupos_disponibles = ? WHERE id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, cuposDisponibles);
            statement.setInt(2, id);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                result[0] = "1";
                result[1] = "Cupos actualizados correctamente";
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el viaje";
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
     * Disminuye los cupos disponibles al hacer una reserva
     */
    public String[] decrementarCupos(int id, int cantidad) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE VIAJE SET cupos_disponibles = cupos_disponibles - ? " +
                        "WHERE id = ? AND cupos_disponibles >= ? RETURNING cupos_disponibles";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, cantidad);
            statement.setInt(2, id);
            statement.setInt(3, cantidad);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("cupos_disponibles"));
            } else {
                result[0] = "-1";
                result[1] = "No hay suficientes cupos disponibles";
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
     * Incrementa los cupos disponibles al cancelar una reserva
     */
    public String[] incrementarCupos(int id, int cantidad) {
        String[] result = new String[2];
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "UPDATE VIAJE SET cupos_disponibles = cupos_disponibles + ? " +
                        "WHERE id = ? RETURNING cupos_disponibles";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, cantidad);
            statement.setInt(2, id);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                result[0] = "1";
                result[1] = String.valueOf(rs.getInt("cupos_disponibles"));
            } else {
                result[0] = "-1";
                result[1] = "No se encontró el viaje";
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
     * Obtiene todos los viajes con información del plan y destino
     */
    public List<String[]> findAll() {
        List<String[]> viajes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.*, pv.nombre as plan_nombre, pv.categoria, " +
                        "d.ciudad, d.pais, d.nombre as destino_nombre " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "ORDER BY v.fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] viaje = new String[15];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viaje[10] = rs.getString("plan_nombre");
                viaje[11] = rs.getString("categoria");
                viaje[12] = rs.getString("ciudad");
                viaje[13] = rs.getString("pais");
                viaje[14] = rs.getString("destino_nombre");
                viajes.add(viaje);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener viajes: " + e.getMessage());
        }
        return viajes;
    }

    /**
     * Obtiene un viaje por ID
     */
    public String[] findOneById(int id) {
        String[] viaje = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.*, pv.nombre as plan_nombre, pv.descripcion as plan_descripcion, " +
                        "pv.categoria, pv.duracion_dias, d.ciudad, d.pais, d.nombre as destino_nombre " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE v.id = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                viaje = new String[18];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viaje[10] = rs.getString("plan_nombre");
                viaje[11] = rs.getString("plan_descripcion");
                viaje[12] = rs.getString("categoria");
                viaje[13] = String.valueOf(rs.getInt("duracion_dias"));
                viaje[14] = rs.getString("ciudad");
                viaje[15] = rs.getString("pais");
                viaje[16] = rs.getString("destino_nombre");
                viaje[17] = String.valueOf(rs.getInt("plan_viaje_id"));
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener viaje: " + e.getMessage());
        }
        return viaje;
    }

    /**
     * Busca un viaje por código único
     */
    public String[] findByCodigo(String codigoViaje) {
        String[] viaje = null;
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.*, pv.nombre as plan_nombre, d.ciudad, d.pais " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE v.codigo_viaje = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, codigoViaje);
            
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                viaje = new String[14];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viaje[10] = rs.getString("plan_nombre");
                viaje[11] = rs.getString("ciudad");
                viaje[12] = rs.getString("pais");
                viaje[13] = String.valueOf(rs.getInt("plan_viaje_id"));
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al buscar viaje por código: " + e.getMessage());
        }
        return viaje;
    }

    /**
     * Obtiene viajes por plan de viaje
     */
    public List<String[]> findByPlan(int planViajeId) {
        List<String[]> viajes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT * FROM VIAJE WHERE plan_viaje_id = ? ORDER BY fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, planViajeId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] viaje = new String[10];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viajes.add(viaje);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener viajes por plan: " + e.getMessage());
        }
        return viajes;
    }

    /**
     * Obtiene viajes por estado
     */
    public List<String[]> findByEstado(String estado) {
        List<String[]> viajes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.*, pv.nombre as plan_nombre, d.ciudad, d.pais " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE v.estado = ? " +
                        "ORDER BY v.fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, estado);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] viaje = new String[14];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viaje[10] = rs.getString("plan_nombre");
                viaje[11] = rs.getString("ciudad");
                viaje[12] = rs.getString("pais");
                viaje[13] = String.valueOf(rs.getInt("plan_viaje_id"));
                viajes.add(viaje);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener viajes por estado: " + e.getMessage());
        }
        return viajes;
    }

    /**
     * Obtiene viajes disponibles (con cupos y próximos a salir)
     */
    public List<String[]> findDisponibles() {
        List<String[]> viajes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.*, pv.nombre as plan_nombre, pv.categoria, " +
                        "d.ciudad, d.pais, d.nombre as destino_nombre " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE v.cupos_disponibles > 0 " +
                        "AND v.estado = 'PROGRAMADO' " +
                        "AND v.fecha_salida >= CURRENT_DATE " +
                        "ORDER BY v.fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] viaje = new String[15];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viaje[10] = rs.getString("plan_nombre");
                viaje[11] = rs.getString("categoria");
                viaje[12] = rs.getString("ciudad");
                viaje[13] = rs.getString("pais");
                viaje[14] = rs.getString("destino_nombre");
                viajes.add(viaje);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener viajes disponibles: " + e.getMessage());
        }
        return viajes;
    }

    /**
     * Obtiene viajes por rango de fechas
     */
    public List<String[]> findByFechas(Date fechaInicio, Date fechaFin) {
        List<String[]> viajes = new ArrayList<>();
        try {
            Connection connection = this.databaseConection.openConnection();
            
            String sql = "SELECT v.*, pv.nombre as plan_nombre, d.ciudad, d.pais " +
                        "FROM VIAJE v " +
                        "INNER JOIN PLAN_VIAJE pv ON v.plan_viaje_id = pv.id " +
                        "INNER JOIN DESTINO d ON pv.destino_id = d.id " +
                        "WHERE v.fecha_salida BETWEEN ? AND ? " +
                        "ORDER BY v.fecha_salida ASC";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String[] viaje = new String[14];
                viaje[0] = String.valueOf(rs.getInt("id"));
                viaje[1] = String.valueOf(rs.getInt("plan_viaje_id"));
                viaje[2] = rs.getString("codigo_viaje");
                viaje[3] = rs.getDate("fecha_salida").toString();
                viaje[4] = rs.getDate("fecha_retorno").toString();
                viaje[5] = String.valueOf(rs.getInt("cupos_disponibles"));
                viaje[6] = String.valueOf(rs.getInt("cupos_totales"));
                viaje[7] = rs.getBigDecimal("precio_final").toString();
                viaje[8] = rs.getString("estado");
                viaje[9] = rs.getTimestamp("fecha_creacion").toString();
                viaje[10] = rs.getString("plan_nombre");
                viaje[11] = rs.getString("ciudad");
                viaje[12] = rs.getString("pais");
                viaje[13] = String.valueOf(rs.getInt("plan_viaje_id"));
                viajes.add(viaje);
            }
            
            rs.close();
            statement.close();
            this.databaseConection.closeConnection();
            
        } catch (SQLException e) {
            System.err.println("Error al obtener viajes por fechas: " + e.getMessage());
        }
        return viajes;
    }
}
