package com.tecnoweb.grupo7sa.business;

import com.tecnoweb.grupo7sa.data.DDestino;
import java.math.BigDecimal;
import java.util.List;

public class BDestino {
    private DDestino dDestino;

    public BDestino() {
        this.dDestino = new DDestino();
    }

    /**
     * Registra un nuevo destino turístico
     */
    public String registrarDestino(String nombre, String pais, String ciudad, String descripcion,
                                   String clima, BigDecimal precioReferencia) {
        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: El nombre del destino es obligatorio";
        }
        
        if (pais == null || pais.trim().isEmpty()) {
            return "Error: El país es obligatorio";
        }
        
        if (ciudad == null || ciudad.trim().isEmpty()) {
            return "Error: La ciudad es obligatoria";
        }
        
        if (precioReferencia == null || precioReferencia.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El precio de referencia debe ser mayor a 0";
        }
        
        // DDestino.save requiere: nombre, pais, ciudad, descripcion, clima, idioma, moneda, precioBase, imagenUrl
        return dDestino.save(nombre, pais, ciudad, descripcion != null ? descripcion : "", 
                            clima != null ? clima : "", "", "", precioReferencia, "");
    }

    /**
     * Actualiza un destino existente
     */
    public String actualizarDestino(int id, String nombre, String pais, String ciudad,
                                    String descripcion, String clima, BigDecimal precioReferencia) {
        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: El nombre es obligatorio";
        }
        
        if (precioReferencia == null || precioReferencia.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El precio debe ser mayor a 0";
        }
        
        String[] destino = dDestino.findOneById(id);
        if (destino == null) {
            return "Error: Destino no encontrado";
        }
        
        return dDestino.update(id, nombre, pais, ciudad, descripcion, clima, "", "", precioReferencia, "");
    }

    /**
     * Desactiva un destino
     */
    public String desactivarDestino(int id) {
        String[] destino = dDestino.findOneById(id);
        if (destino == null) {
            return "Error: Destino no encontrado";
        }
        
        return dDestino.delete(id);
    }

    /**
     * Reactiva un destino
     */
    public String reactivarDestino(int id) {
        return dDestino.reactivate(id);
    }

    /**
     * Lista todos los destinos
     */
    public List<String[]> listarDestinos() {
        return dDestino.findAll();
    }

    /**
     * Busca un destino por ID
     */
    public String[] buscarPorId(int id) {
        return dDestino.findOneById(id);
    }

    /**
     * Busca destinos por país
     */
    public List<String[]> buscarPorPais(String pais) {
        if (pais == null || pais.trim().isEmpty()) {
            return null;
        }
        return dDestino.findByPais(pais);
    }

    /**
     * Busca destinos por ciudad
     */
    public List<String[]> buscarPorCiudad(String ciudad) {
        if (ciudad == null || ciudad.trim().isEmpty()) {
            return null;
        }
        return dDestino.findByCiudad(ciudad);
    }

    /**
     * Busca destinos por rango de precio
     */
    public List<String[]> buscarPorRangoPrecio(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            return null;
        }
        
        if (min.compareTo(max) > 0) {
            return null; // min debe ser menor o igual que max
        }
        
        return dDestino.findByPrecioRango(min, max);
    }

    /**
     * Obtiene destinos activos
     */
    public List<String[]> listarDestinosActivos() {
        List<String[]> todos = dDestino.findAll();
        todos.removeIf(destino -> destino[7].equals("false")); // Filtrar inactivos
        return todos;
    }
}
