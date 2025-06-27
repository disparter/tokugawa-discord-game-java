package io.github.disparter.tokugawa.discord.core.events;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.bot.DiscordBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Base class for all events.
 * Provides common functionality for event handling.
 */
public abstract class BaseEvent {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final DiscordBot discordBot;
    protected Long channelId;
    
    /**
     * Constructor for BaseEvent.
     *
     * @param discordBot The Discord bot instance
     * @param channelId The channel ID for announcements (optional)
     */
    public BaseEvent(DiscordBot discordBot, Long channelId) {
        this.discordBot = discordBot;
        this.channelId = channelId;
    }
    
    /**
     * Find a channel by ID.
     *
     * @param channelId The channel ID
     * @return A Mono that emits the channel if found
     */
    protected Mono<MessageChannel> findChannel(Long channelId) {
        return discordBot.getGatewayClient()
                .getChannelById(discord4j.common.util.Snowflake.of(channelId))
                .cast(MessageChannel.class);
    }
    
    /**
     * Send an announcement to the event channel.
     *
     * @param title The title of the announcement
     * @param description The description of the announcement
     * @param color The color of the embed (default: green)
     * @return A Mono that completes when the message is sent
     */
    protected Mono<Void> sendAnnouncement(String title, String description, Color color) {
        if (channelId == null) {
            logger.error("No channel ID set for event announcement");
            return Mono.empty();
        }
        
        return findChannel(channelId)
                .flatMap(channel -> {
                    EmbedCreateSpec embed = EmbedCreateSpec.builder()
                            .title(title)
                            .description(description)
                            .color(color)
                            .build();
                    
                    return channel.createMessage(MessageCreateSpec.builder()
                            .addEmbed(embed)
                            .build())
                            .then();
                })
                .onErrorResume(e -> {
                    logger.error("Error sending announcement: {}", e.getMessage());
                    return Mono.empty();
                });
    }
    
    /**
     * Create a basic embed for announcements (default blue).
     *
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The embed specification
     */
    protected EmbedCreateSpec createBasicEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .title(title)
                .description(description)
                .color(Color.BLUE)
                .build();
    }
    
    /**
     * Create an error embed.
     *
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The embed specification
     */
    protected EmbedCreateSpec createErrorEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .title(title)
                .description(description)
                .color(Color.RED)
                .build();
    }
    
    /**
     * Create a success embed.
     *
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The embed specification
     */
    protected EmbedCreateSpec createSuccessEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .title(title)
                .description(description)
                .color(Color.GREEN)
                .build();
    }
    
    /**
     * Get a user by ID.
     *
     * @param userId The user ID
     * @return A Mono that emits the user if found
     */
    protected Mono<User> getUser(Long userId) {
        return discordBot.getGatewayClient()
                .getUserById(discord4j.common.util.Snowflake.of(userId));
    }
    
    /**
     * Get a guild by ID.
     *
     * @param guildId The guild ID
     * @return A Mono that emits the guild if found
     */
    protected Mono<Guild> getGuild(Long guildId) {
        return discordBot.getGatewayClient()
                .getGuildById(discord4j.common.util.Snowflake.of(guildId));
    }
    
    /**
     * Handle errors during event processing.
     *
     * @param error The error that occurred
     */
    protected void handleError(Throwable error) {
        logger.error("Error in event processing: {}", error.getMessage(), error);
    }
    
    /**
     * Clean up resources.
     */
    public void cleanup() {
        // Base implementation does nothing
        // Subclasses should override this method if they need to clean up resources
    }
}