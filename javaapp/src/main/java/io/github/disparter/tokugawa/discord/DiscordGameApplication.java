package io.github.disparter.tokugawa.discord;

import io.github.disparter.tokugawa.discord.bot.DiscordBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the Tokugawa Discord Game.
 * This class starts the Spring Boot application and initializes all components,
 * including the Discord bot.
 */
@SpringBootApplication
@ComponentScan(basePackages = "io.github.disparter.tokugawa.discord")
public class DiscordGameApplication {

    /**
     * Main method that starts the application.
     * The Discord bot (DiscordBot) is automatically initialized by Spring
     * because it's annotated with @Component.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscordGameApplication.class, args);
    }
}
