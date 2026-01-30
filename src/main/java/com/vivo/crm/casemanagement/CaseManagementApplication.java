package com.vivo.crm.casemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Motor de Orquestração de Casos - VIVO
 * 
 * Aplicação responsável por gerenciar o ciclo de vida de tickets de atendimento,
 * integrando canais digitais (App, URA, WhatsApp) com sistemas de registro (Salesforce)
 * e roteamento (Genesys), seguindo o padrão TMF621 (Trouble Ticket).
 */
@SpringBootApplication
public class CaseManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseManagementApplication.class, args);
    }
}
