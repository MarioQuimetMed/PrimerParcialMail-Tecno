package com.tecnoweb.grupo7sa.business;

import com.tecnoweb.grupo7sa.data.DDestino;
import com.tecnoweb.grupo7sa.data.DPlanViaje;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BPlanViaje {
    private final DPlanViaje dPlanViaje;
    private final DDestino dDestino;

    public BPlanViaje() {
        this.dPlanViaje = new DPlanViaje();
        this.dDestino = new DDestino();
    }

    /**
     * Crea un nuevo plan de viaje.
     */
    public String crearPlanViaje(int destinoId, String nombre, String descripcion,
                                 int duracionDias, String categoria, BigDecimal precioBase,
                                 Integer cupoMaximo, boolean incluyeHotel, boolean incluyeTransporte,
                                 boolean incluyeComidas, boolean incluyeTours) {
        String[] destino = dDestino.findOneById(destinoId);
        if (destino == null) {
            return "Error: Destino no encontrado";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: El nombre del plan es obligatorio";
        }

        String categoriaNormalizada = categoria != null ? categoria.trim().toUpperCase() : null;
        if (!validarCategoria(categoriaNormalizada)) {
            return "Error: Categoria invalida (ECONOMICO, ESTANDAR, PREMIUM, LUJO)";
        }

        if (duracionDias <= 0) {
            return "Error: La duracion debe ser al menos 1 dia";
        }

        if (precioBase == null || precioBase.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El precio base debe ser mayor a 0";
        }

        if (cupoMaximo != null && cupoMaximo < 1) {
            return "Error: El cupo maximo debe ser al menos 1 persona";
        }

        BigDecimal precioCalculado = calcularPrecioSugerido(precioBase, incluyeHotel, incluyeTransporte,
                                                            incluyeComidas, incluyeTours);
        String descripcionLimpia = descripcion != null ? descripcion.trim() : "";

        return dPlanViaje.save(nombre.trim(), descripcionLimpia, destinoId, duracionDias,
                               precioCalculado, incluyeHotel, incluyeTransporte, incluyeComidas,
                               categoriaNormalizada, cupoMaximo);
    }

    /**
     * Actualiza un plan de viaje existente.
     */
    public String actualizarPlanViaje(int id, int destinoId, String nombre, String descripcion,
                                      int duracionDias, String categoria, BigDecimal precioBase,
                                      Integer cupoMaximo, boolean incluyeHotel, boolean incluyeTransporte,
                                      boolean incluyeComidas, boolean incluyeTours) {
        String[] plan = dPlanViaje.findOneById(id);
        if (plan == null) {
            return "Error: Plan de viaje no encontrado";
        }

        String[] destino = dDestino.findOneById(destinoId);
        if (destino == null) {
            return "Error: Destino no encontrado";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: El nombre del plan es obligatorio";
        }

        String categoriaNormalizada = categoria != null ? categoria.trim().toUpperCase() : null;
        if (!validarCategoria(categoriaNormalizada)) {
            return "Error: Categoria invalida";
        }

        if (duracionDias <= 0) {
            return "Error: La duracion debe ser al menos 1 dia";
        }

        if (precioBase == null || precioBase.compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: El precio base debe ser mayor a 0";
        }

        if (cupoMaximo != null && cupoMaximo < 1) {
            return "Error: El cupo maximo debe ser al menos 1 persona";
        }

        BigDecimal precioCalculado = calcularPrecioSugerido(precioBase, incluyeHotel, incluyeTransporte,
                                                            incluyeComidas, incluyeTours);
        String descripcionLimpia = descripcion != null ? descripcion.trim() : "";

        return dPlanViaje.update(id, nombre.trim(), descripcionLimpia, destinoId, duracionDias,
                                 precioCalculado, incluyeHotel, incluyeTransporte, incluyeComidas,
                                 categoriaNormalizada, cupoMaximo);
    }

    /**
     * Desactiva un plan de viaje.
     */
    public String desactivarPlan(int id) {
        String[] plan = dPlanViaje.findOneById(id);
        if (plan == null) {
            return "Error: Plan no encontrado";
        }
        return dPlanViaje.delete(id);
    }

    /**
     * Reactiva un plan de viaje.
     */
    public String reactivarPlan(int id) {
        return dPlanViaje.reactivate(id);
    }

    /**
     * Lista todos los planes (activos).
     */
    public List<String[]> listarPlanes() {
        return dPlanViaje.findAll();
    }

    /**
     * Lista los planes activos.
     */
    public List<String[]> listarPlanesActivos() {
        return dPlanViaje.findAll();
    }

    /**
     * Busca un plan por ID.
     */
    public String[] buscarPorId(int id) {
        return dPlanViaje.findOneById(id);
    }

    /**
     * Busca planes por destino.
     */
    public List<String[]> buscarPorDestino(int destinoId) {
        return dPlanViaje.findByDestino(destinoId);
    }

    /**
     * Busca planes por categoria.
     */
    public List<String[]> buscarPorCategoria(String categoria) {
        if (categoria == null) {
            return null;
        }
        String categoriaNormalizada = categoria.trim().toUpperCase();
        if (!validarCategoria(categoriaNormalizada)) {
            return null;
        }
        return dPlanViaje.findByCategoria(categoriaNormalizada);
    }

    /**
     * Busca planes por rango de precio.
     */
    public List<String[]> buscarPorRangoPrecio(BigDecimal min, BigDecimal max) {
        if (min == null || max == null || min.compareTo(max) > 0) {
            return null;
        }
        return dPlanViaje.findByPrecioRango(min, max);
    }

    /**
     * Valida la categoria del plan.
     */
    private boolean validarCategoria(String categoria) {
        if (categoria == null) {
            return false;
        }
        return categoria.equals("ECONOMICO") || categoria.equals("ESTANDAR") ||
               categoria.equals("PREMIUM") || categoria.equals("LUJO");
    }

    /**
     * Calcula un precio sugerido basado en los servicios incluidos.
     */
    public BigDecimal calcularPrecioSugerido(BigDecimal precioBase, boolean hotel, boolean transporte,
                                             boolean comidas, boolean tours) {
        BigDecimal precio = precioBase;
        if (hotel) {
            precio = precio.multiply(new BigDecimal("1.30"));
        }
        if (transporte) {
            precio = precio.multiply(new BigDecimal("1.15"));
        }
        if (comidas) {
            precio = precio.multiply(new BigDecimal("1.20"));
        }
        if (tours) {
            precio = precio.multiply(new BigDecimal("1.25"));
        }
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Obtiene el precio registrado para un plan especifico.
     */
    public String calcularPrecioSugerido(int planId) {
        String[] plan = dPlanViaje.findOneById(planId);
        if (plan == null) {
            return "Error: Plan de viaje no encontrado";
        }
        BigDecimal precio = new BigDecimal(plan[6]);
        BigDecimal precioRedondeado = precio.setScale(2, RoundingMode.HALF_UP);
        return "Precio del plan: Bs. " + precioRedondeado.toPlainString();
    }
}

