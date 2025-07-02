package io.github.disparter.tokugawa.discord;

import io.github.disparter.tokugawa.discord.config.FunctionalTestConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Aplicação Spring Boot configurada especificamente para os testes funcionais.
 * Integra com Testcontainers para gerenciar o ambiente de teste.
 */
@SpringBootApplication
@SpringBootTest(
    classes = FunctionalTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(FunctionalTestConfiguration.class)
@Testcontainers
public class FunctionalTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunctionalTestApplication.class, args);
    }

    /**
     * Configura propriedades dinâmicas para os containers de teste
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        FunctionalTestConfiguration.configureProperties(registry);
    }
}