package io.github.disparter.tokugawa.discord.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Configuração principal para os testes funcionais.
 * Gerencia containers Docker e configurações de mock para o ambiente de teste.
 */
@TestConfiguration
@Testcontainers
public class FunctionalTestConfiguration {

    /**
     * Container PostgreSQL para os testes funcionais
     */
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("tokugawa_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    private WireMockServer wireMockServer;

    /**
     * Inicializa o WireMock server para mocking da API do Discord
     */
    @PostConstruct
    public void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(8089)
                .usingFilesUnderDirectory("src/functionalTest/resources/wiremock"));
        wireMockServer.start();
        
        System.setProperty("discord.api.base-url", "http://localhost:" + wireMockServer.port());
    }

    /**
     * Para o WireMock server após os testes
     */
    @PreDestroy
    public void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    /**
     * Bean do WireMock server para injeção de dependência
     */
    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

    /**
     * Configura as propriedades dinâmicas para os containers
     */
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}