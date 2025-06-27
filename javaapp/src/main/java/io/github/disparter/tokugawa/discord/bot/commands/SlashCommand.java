package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * Interface for all slash commands in the bot.
 * Each command should implement this interface and provide its own execution logic.
 */
public interface SlashCommand {

    /**
     * Gets the name of the command.
     * This name is used to match incoming slash commands to their handlers.
     * 
     * @return The name of the command
     */
    String getName();

    /**
     * Executes the command logic when the command is triggered.
     * 
     * @param event The chat input interaction event
     * @return A Mono that completes when the command execution is done
     */
    Mono<Void> execute(ChatInputInteractionEvent event);
}
