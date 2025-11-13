package com.tecnoweb.grupo7sa.business;

import com.tecnoweb.grupo7sa.data.DReporte;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;

public class BReporte {
    private DReporte dReporte;

    public BReporte() {
        this.dReporte = new DReporte();
    }

    /**
     * Genera reporte de ventas por período
     */
    public String generarReporteVentas(Date fechaInicio, Date fechaFin) {
        if (!validarRangoFechas(fechaInicio, fechaFin)) {
            return "Error: Rango de fechas inválido";
        }
        
        List<String[]> ventas = dReporte.obtenerVentasPorPeriodo(fechaInicio, fechaFin);
        
        if (ventas.isEmpty()) {
            return "No hay ventas en el período especificado";
        }
        
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal totalPendiente = BigDecimal.ZERO;
        int ventasContado = 0;
        int ventasCredito = 0;
        
        for (String[] venta : ventas) {
            BigDecimal montoTotal = new BigDecimal(venta[5]);
            BigDecimal montoPagado = new BigDecimal(venta[6]);
            BigDecimal montoPendiente = new BigDecimal(venta[7]);
            
            totalVentas = totalVentas.add(montoTotal);
            totalPagado = totalPagado.add(montoPagado);
            totalPendiente = totalPendiente.add(montoPendiente);
            
            if (venta[4].equals("CONTADO")) {
                ventasContado++;
            } else {
                ventasCredito++;
            }
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== REPORTE DE VENTAS ===\n");
        reporte.append("Período: ").append(fechaInicio).append(" a ").append(fechaFin).append("\n");
        reporte.append("Total ventas: ").append(ventas.size()).append("\n");
        reporte.append("  - Al contado: ").append(ventasContado).append("\n");
        reporte.append("  - A crédito: ").append(ventasCredito).append("\n");
        reporte.append("Monto total: ").append(formatearMoneda(totalVentas)).append("\n");
        reporte.append("Total pagado: ").append(formatearMoneda(totalPagado)).append("\n");
        reporte.append("Total pendiente: ").append(formatearMoneda(totalPendiente)).append("\n");
        reporte.append("===========================\n");
        
        return reporte.toString();
    }

    /**
     * Genera reporte de pagos por método
     */
    public String generarReportePagosPorMetodo(Date fechaInicio, Date fechaFin) {
        if (!validarRangoFechas(fechaInicio, fechaFin)) {
            return "Error: Rango de fechas inválido";
        }
        
        List<String[]> resumen = dReporte.obtenerPagosPorMetodo(fechaInicio, fechaFin);
        
        if (resumen.isEmpty()) {
            return "No hay pagos en el período especificado";
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== REPORTE DE PAGOS POR MÉTODO ===\n");
        reporte.append("Período: ").append(fechaInicio).append(" a ").append(fechaFin).append("\n\n");
        
        BigDecimal totalGeneral = BigDecimal.ZERO;
        int totalPagos = 0;
        
        for (String[] metodo : resumen) {
            String nombreMetodo = metodo[0];
            int cantidad = Integer.parseInt(metodo[1]);
            BigDecimal monto = new BigDecimal(metodo[2]);
            
            totalPagos += cantidad;
            totalGeneral = totalGeneral.add(monto);
            
            reporte.append(nombreMetodo).append(": ")
                   .append(cantidad).append(" pago(s) - ")
                   .append(formatearMoneda(monto)).append("\n");
        }
        
        reporte.append("\nTotal: ").append(totalPagos).append(" pago(s) - ")
               .append(formatearMoneda(totalGeneral)).append("\n");
        reporte.append("===================================\n");
        
        return reporte.toString();
    }

    /**
     * Genera reporte de viajes programados
     */
    public String generarReporteViajesProgramados() {
        List<String[]> viajes = dReporte.obtenerViajesProgramados();
        
        if (viajes.isEmpty()) {
            return "No hay viajes programados actualmente";
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== VIAJES PROGRAMADOS ===\n\n");
        
        int totalCuposOfrecidos = 0;
        int totalCuposVendidos = 0;
        
        for (String[] viaje : viajes) {
            String codigo = viaje[1];
            String destino = viaje[13];
            String fechaSalida = viaje[4];
            int cuposTotales = Integer.parseInt(viaje[2]);
            int cuposDisponibles = Integer.parseInt(viaje[3]);
            int cuposVendidos = cuposTotales - cuposDisponibles;
            
            totalCuposOfrecidos += cuposTotales;
            totalCuposVendidos += cuposVendidos;
            
            double ocupacion = (cuposVendidos * 100.0) / cuposTotales;
            
            reporte.append("Código: ").append(codigo).append("\n");
            reporte.append("Destino: ").append(destino).append("\n");
            reporte.append("Salida: ").append(fechaSalida).append("\n");
            reporte.append("Ocupación: ").append(String.format("%.1f%%", ocupacion))
                   .append(" (").append(cuposVendidos).append("/").append(cuposTotales).append(")\n");
            reporte.append("---\n");
        }
        
        double ocupacionGeneral = (totalCuposVendidos * 100.0) / totalCuposOfrecidos;
        
        reporte.append("\nTotal viajes: ").append(viajes.size()).append("\n");
        reporte.append("Ocupación general: ").append(String.format("%.1f%%", ocupacionGeneral)).append("\n");
        reporte.append("==========================\n");
        
        return reporte.toString();
    }

    /**
     * Genera reporte de reservas por estado
     */
    public String generarReporteReservasPorEstado() {
        List<String[]> resumen = dReporte.obtenerReservasPorEstado();
        
        if (resumen.isEmpty()) {
            return "No hay reservas registradas";
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== RESERVAS POR ESTADO ===\n\n");
        
        int totalReservas = 0;
        int totalPersonas = 0;
        BigDecimal ingresoTotal = BigDecimal.ZERO;
        
        for (String[] estado : resumen) {
            String nombreEstado = estado[0];
            int cantidad = Integer.parseInt(estado[1]);
            int personas = Integer.parseInt(estado[2]);
            BigDecimal ingreso = new BigDecimal(estado[3]);
            
            totalReservas += cantidad;
            totalPersonas += personas;
            ingresoTotal = ingresoTotal.add(ingreso);
            
            reporte.append(nombreEstado).append(": ")
                   .append(cantidad).append(" reserva(s), ")
                   .append(personas).append(" persona(s), ")
                   .append(formatearMoneda(ingreso)).append("\n");
        }
        
        reporte.append("\nTotal: ").append(totalReservas).append(" reserva(s), ")
               .append(totalPersonas).append(" persona(s), ")
               .append(formatearMoneda(ingresoTotal)).append("\n");
        reporte.append("===========================\n");
        
        return reporte.toString();
    }

    /**
     * Genera reporte de cuotas vencidas
     */
    public String generarReporteCuotasVencidas() {
        List<String[]> cuotas = dReporte.obtenerCuotasVencidas();
        
        if (cuotas.isEmpty()) {
            return "No hay cuotas vencidas";
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== CUOTAS VENCIDAS ===\n\n");
        
        BigDecimal totalAdeudado = BigDecimal.ZERO;
        
        for (String[] cuota : cuotas) {
            String cliente = cuota[9];
            String numeroCuota = cuota[2];
            String fechaVencimiento = cuota[4];
            BigDecimal monto = new BigDecimal(cuota[3]);
            
            totalAdeudado = totalAdeudado.add(monto);
            
            reporte.append("Cliente: ").append(cliente).append("\n");
            reporte.append("Cuota #").append(numeroCuota)
                   .append(" - Vencida desde: ").append(fechaVencimiento).append("\n");
            reporte.append("Monto: ").append(formatearMoneda(monto)).append("\n");
            reporte.append("---\n");
        }
        
        reporte.append("\nTotal cuotas vencidas: ").append(cuotas.size()).append("\n");
        reporte.append("Monto total adeudado: ").append(formatearMoneda(totalAdeudado)).append("\n");
        reporte.append("=======================\n");
        
        return reporte.toString();
    }

    /**
     * Genera reporte de destinos más vendidos
     */
    public String generarReporteDestinosMasVendidos(int limite) {
        if (limite <= 0 || limite > 20) {
            limite = 10; // Por defecto top 10
        }
        
        List<String[]> destinos = dReporte.obtenerDestinosMasVendidos(limite);
        
        if (destinos.isEmpty()) {
            return "No hay datos de ventas de destinos";
        }
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== TOP ").append(limite).append(" DESTINOS MÁS VENDIDOS ===\n\n");
        
        int posicion = 1;
        for (String[] destino : destinos) {
            String nombre = destino[0];
            String pais = destino[1];
            int reservas = Integer.parseInt(destino[2]);
            int personas = Integer.parseInt(destino[3]);
            BigDecimal ingresos = new BigDecimal(destino[4]);
            
            reporte.append(posicion).append(". ").append(nombre).append(", ").append(pais).append("\n");
            reporte.append("   Reservas: ").append(reservas)
                   .append(" | Personas: ").append(personas)
                   .append(" | Ingresos: ").append(formatearMoneda(ingresos)).append("\n\n");
            
            posicion++;
        }
        
        reporte.append("=====================================\n");
        
        return reporte.toString();
    }

    /**
     * Genera dashboard con indicadores clave
     */
    public String generarDashboard() {
        StringBuilder dashboard = new StringBuilder();
        dashboard.append("\n╔════════════════════════════════════════╗\n");
        dashboard.append("║      DASHBOARD - AGENCIA DE VIAJES     ║\n");
        dashboard.append("╚════════════════════════════════════════╝\n\n");
        
        // Indicadores de ventas
        String[] ventasHoy = dReporte.obtenerVentasHoy();
        dashboard.append("📊 VENTAS HOY:\n");
        dashboard.append("   Cantidad: ").append(ventasHoy[0]).append(" venta(s)\n");
        dashboard.append("   Monto: ").append(formatearMoneda(new BigDecimal(ventasHoy[1]))).append("\n\n");
        
        // Reservas activas
        String[] reservasActivas = dReporte.contarReservasActivas();
        dashboard.append("✈️  RESERVAS ACTIVAS:\n");
        dashboard.append("   Total: ").append(reservasActivas[0]).append(" reserva(s)\n");
        dashboard.append("   Personas: ").append(reservasActivas[1]).append("\n\n");
        
        // Viajes próximos
        String[] viajesProximos = dReporte.contarViajesProximos(7);
        dashboard.append("🗓️  VIAJES PRÓXIMOS (7 días):\n");
        dashboard.append("   Total: ").append(viajesProximos[0]).append(" viaje(s)\n\n");
        
        // Cuotas por vencer
        String[] cuotasProximas = dReporte.contarCuotasProximasVencer(7);
        dashboard.append("⏰ CUOTAS POR VENCER (7 días):\n");
        dashboard.append("   Cantidad: ").append(cuotasProximas[0]).append("\n");
        dashboard.append("   Monto: ").append(formatearMoneda(new BigDecimal(cuotasProximas[1]))).append("\n\n");
        
        // Cuotas vencidas
        String[] cuotasVencidas = dReporte.contarCuotasVencidas();
        dashboard.append("⚠️  CUOTAS VENCIDAS:\n");
        dashboard.append("   Cantidad: ").append(cuotasVencidas[0]).append("\n");
        dashboard.append("   Monto: ").append(formatearMoneda(new BigDecimal(cuotasVencidas[1]))).append("\n\n");
        
        dashboard.append("═══════════════════════════════════════════\n");
        
        return dashboard.toString();
    }

    /**
     * Genera reporte de ingresos mensuales
     */
    public String generarReporteIngresosMensuales(int año, int mes) {
        if (año < 2020 || año > 2100) {
            return "Error: Año inválido";
        }
        
        if (mes < 1 || mes > 12) {
            return "Error: Mes inválido (1-12)";
        }
        
        List<String[]> ingresos = dReporte.obtenerIngresosMensuales(año, mes);
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("\n=== INGRESOS MENSUALES ===\n");
        reporte.append("Mes: ").append(mes).append("/").append(año).append("\n\n");
        
        BigDecimal totalIngresos = BigDecimal.ZERO;
        
        for (String[] dia : ingresos) {
            String fecha = dia[0];
            int cantidad = Integer.parseInt(dia[1]);
            BigDecimal monto = new BigDecimal(dia[2]);
            
            totalIngresos = totalIngresos.add(monto);
            
            reporte.append(fecha).append(": ")
                   .append(cantidad).append(" pago(s) - ")
                   .append(formatearMoneda(monto)).append("\n");
        }
        
        reporte.append("\nTotal del mes: ").append(formatearMoneda(totalIngresos)).append("\n");
        reporte.append("==========================\n");
        
        return reporte.toString();
    }

    /**
     * Valida rango de fechas
     */
    private boolean validarRangoFechas(Date inicio, Date fin) {
        if (inicio == null || fin == null) {
            return false;
        }
        return !fin.before(inicio);
    }

    /**
     * Formatea valores monetarios
     */
    private String formatearMoneda(BigDecimal valor) {
        return "Bs. " + valor.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
