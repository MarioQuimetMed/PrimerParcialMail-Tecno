package com.tecnoweb.grupo7sa.command;

import com.tecnoweb.grupo7sa.business.BReporte;
import java.sql.Date;

public class HandleReporte {

    public static String execute(String command, String params) {
        BReporte bReporte = new BReporte();
        
        try {
            switch (command) {
                case "ventas":
                    return reporteVentas(bReporte, params);
                case "pagosPorMetodo":
                    return reportePagosPorMetodo(bReporte, params);
                case "viajesProgramados":
                    return reporteViajesProgramados(bReporte);
                case "reservasPorEstado":
                    return reporteReservasPorEstado(bReporte);
                case "cuotasVencidas":
                    return reporteCuotasVencidas(bReporte);
                case "destinosMasVendidos":
                    return reporteDestinosMasVendidos(bReporte, params);
                case "dashboard":
                    return dashboard(bReporte);
                case "ingresosMensuales":
                    return reporteIngresosMensuales(bReporte, params);
                default:
                    return "Comando no implementado: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String reporteVentas(BReporte bReporte, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: ventas (fechaInicio, fechaFin)";
        }
        
        Date fechaInicio = Date.valueOf(parts[0].trim());
        Date fechaFin = Date.valueOf(parts[1].trim());
        
        return bReporte.generarReporteVentas(fechaInicio, fechaFin);
    }

    private static String reportePagosPorMetodo(BReporte bReporte, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: pagosPorMetodo (fechaInicio, fechaFin)";
        }
        
        Date fechaInicio = Date.valueOf(parts[0].trim());
        Date fechaFin = Date.valueOf(parts[1].trim());
        
        return bReporte.generarReportePagosPorMetodo(fechaInicio, fechaFin);
    }

    private static String reporteViajesProgramados(BReporte bReporte) {
        return bReporte.generarReporteViajesProgramados();
    }

    private static String reporteReservasPorEstado(BReporte bReporte) {
        return bReporte.generarReporteReservasPorEstado();
    }

    private static String reporteCuotasVencidas(BReporte bReporte) {
        return bReporte.generarReporteCuotasVencidas();
    }

    private static String reporteDestinosMasVendidos(BReporte bReporte, String params) {
        int limite = params.trim().isEmpty() ? 10 : Integer.parseInt(params.trim());
        return bReporte.generarReporteDestinosMasVendidos(limite);
    }

    private static String dashboard(BReporte bReporte) {
        return bReporte.generarDashboard();
    }

    private static String reporteIngresosMensuales(BReporte bReporte, String params) {
        String[] parts = params.split(",");
        if (parts.length < 2) {
            return "Error: Faltan parámetros. Uso: ingresosMensuales (año, mes)";
        }
        
        int año = Integer.parseInt(parts[0].trim());
        int mes = Integer.parseInt(parts[1].trim());
        
        return bReporte.generarReporteIngresosMensuales(año, mes);
    }
}
