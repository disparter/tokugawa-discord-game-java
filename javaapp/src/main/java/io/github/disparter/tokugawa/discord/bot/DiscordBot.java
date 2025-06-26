package io.github.disparter.tokugawa.discord.bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SlashCommandInteractionEvent;
import io.github.disparter.tokugawa.discord.bot.listeners.SlashCommandListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Main Discord bot class that handles the connection to Discord using Discord4J.
 */
@Component
public class DiscordBot {

    @Value("${discord.token}")
    private String token;

    private GatewayDiscordClient gatewayClient;
    private final SlashCommandListener slashCommandListener;

    public DiscordBot(SlashCommandListener slashCommandListener) {
        this.slashCommandListener = slashCommandListener;
    }

    /**
     * Initializes the Discord bot and connects to Discord.
     * This method is called automatically by Spring after the bean is created.
     */
    @PostConstruct
    public void init() {
        DiscordClient client = DiscordClient.create(token);
        
        gatewayClient = client.login().block();
        
        if (gatewayClient != null) {
            // Register the slash command listener
            gatewayClient.on(SlashCommandInteractionEvent.class, slashCommandListener::handle)
                    .subscribe();
            
            System.out.println("Discord bot connected successfully!");
        } else {
            System.err.println("Failed to connect to Discord. Check your token.");
        }
    }

    /**
     * Disconnects the bot from Discord when the application is shutting down.
     */
    @PreDestroy
    public void destroy() {
        if (gatewayClient != null) {
            gatewayClient.logout().block();
            System.out.println("Discord bot disconnected.");
        }
    }

    /**
     * Gets the gateway client for direct access if needed.
     * 
     * @return The GatewayDiscordClient instance
     */
    public GatewayDiscordClient getGatewayClient() {
        return gatewayClient;
    }
}