package com.vivo.crm.casemanagement.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI/Swagger para documentação da API
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Motor de Orquestração de Casos - VIVO")
                        .version("1.0.0")
                        .description("""
                                API de gerenciamento de tickets de atendimento seguindo o padrão TMF621 (Trouble Ticket).
                                
                                Este motor de orquestração centraliza o ciclo de vida de casos, integrando:
                                - Canais digitais (App VIVO, URA, WhatsApp)
                                - CRM Salesforce
                                - Sistema de roteamento Genesys
                                
                                **Fluxo Principal:**
                                1. Canal digital envia requisição TMF621
                                2. Motor valida e persiste o caso
                                3. Motor sincroniza com Salesforce
                                4. Motor retorna resposta ao canal
                                """)
                        .contact(new Contact()
                                .name("Time de Integração VIVO")
                                .email("integracao@vivo.com.br"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.vivo.com.br")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.vivo.com.br")
                                .description("Servidor de Produção")
                ));
    }
}
