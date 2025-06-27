package io.github.disparter.tokugawa.discord.bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SlashCommandInteractionEvent;
import io.github.disparter.tokugawa.discord.bot.listeners.SlashCommandListener;
import io.github.disparter.tokugawa.discord.core.events.EventsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    @Value("${discord.token}")
    private String token;

    private GatewayDiscordClient gatewayClient;
    private final SlashCommandListener slashCommandListener;
    private final EventsManager eventsManager;

    public DiscordBot(SlashCommandListener slashCommandListener, EventsManager eventsManager) {
        this.slashCommandListener = slashCommandListener;
        this.eventsManager = eventsManager;
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
                gatewayClient.on(SlashCommandInteractionEvent.class, slashCommandListener::handle)
                        .subscribe();

                logger.info("Discord bot connected successfully!");
            } else {
                logger.error("Failed to connect to Discord. Check your token.");
            }
        } catch (Exception e) {
            logger.error("Error initializing Discord bot: {}", e.getMessage(), e);
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
                logger.info("Discord bot disconnected.");
            }
        } catch (Exception e) {
            logger.error("Error disconnecting Discord bot: {}", e.getMessage(), e);
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
