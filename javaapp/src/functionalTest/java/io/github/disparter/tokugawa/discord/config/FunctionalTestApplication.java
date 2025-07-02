package io.github.disparter.tokugawa.discord.config;

import io.github.disparter.tokugawa.discord.context.TestContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Aplicação Spring Boot específica para testes funcionais.
 * Configurada para trabalhar com Testcontainers e ambiente de teste isolado.
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.disparter.tokugawa.discord")
public class FunctionalTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunctionalTestApplication.class, args);
    }

    /**
     * Bean para contexto de testes compartilhado
     */
    @Bean
    public TestContext testContext() {
        return new TestContext();
    }
}