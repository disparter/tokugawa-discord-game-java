package io.github.disparter.tokugawa.discord.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Test configuration class for Spring Boot tests.
 * This class provides the necessary beans and configuration for tests that need to load the Spring context.
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "io.github.disparter.tokugawa.discord.core.models")
@EnableJpaRepositories(basePackages = "io.github.disparter.tokugawa.discord.core.repositories")
@ComponentScan(basePackages = "io.github.disparter.tokugawa.discord")
public class TestConfig {

    /**
     * Creates an in-memory H2 database for testing.
     * 
     * @return the data source
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }
}