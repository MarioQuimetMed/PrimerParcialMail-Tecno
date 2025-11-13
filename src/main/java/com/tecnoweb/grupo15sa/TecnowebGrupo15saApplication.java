package com.tecnoweb.grupo15sa;

import com.tecnoweb.grupo15sa.connection.ConnectionCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TecnowebGrupo15saApplication {

    public static void main(String[] args) {
        SpringApplication.run(TecnowebGrupo15saApplication.class, args);
        
        // Iniciar el sistema de gestión de agencia de viajes vía email
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║       SISTEMA DE GESTIÓN DE AGENCIA DE VIAJES               ║");
        System.out.println("║           Iniciando servicio de correo...                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        
        ConnectionCore.main(args);
    }

}
