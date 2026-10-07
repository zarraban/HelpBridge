package com.example.help_bridge.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helpBridgeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HelpBridge API")
                        .version("v1")
                        .description("REST API платформи HelpBridge: фонди, запити на допомогу, збори та користувачі.")
                        .contact(new Contact().name("HelpBridge Team")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }
}