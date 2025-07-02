package io.github.disparter.tokugawa.discord.bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import io.github.disparter.tokugawa.discord.bot.listeners.SlashCommandListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Main Discord bot class that handles the connection to Discord using Discord4J.
 */
@Slf4j
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
        try {
            DiscordClient client = DiscordClient.create(token);

            gatewayClient = client.login().block();

            if (gatewayClient != null) {
                // Register the slash command listener
                gatewayClient.on(ChatInputInteractionEvent.class, slashCommandListener::handle)
                        .subscribe();

                log.info("Discord bot connected successfully!");
            } else {
                log.error("Failed to connect to Discord. Check your token.");
            }
        } catch (Exception e) {
            log.error("Error initializing Discord bot: {}", e.getMessage(), e);
        }
    }

    /**
     * Disconnects the bot from Discord when the application is shutting down.
     */
    @PreDestroy
    public void destroy() {
        try {
            if (gatewayClient != null) {
                gatewayClient.logout().block();
                log.info("Discord bot disconnected.");
            }
        } catch (Exception e) {
            log.error("Error disconnecting Discord bot: {}", e.getMessage(), e);
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
