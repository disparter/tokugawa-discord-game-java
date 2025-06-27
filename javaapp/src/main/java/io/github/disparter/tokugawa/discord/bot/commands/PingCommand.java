package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * A simple ping command that responds with "Pong!".
 * This command is useful for testing if the bot is responsive.
 */
@Component
public class PingCommand implements SlashCommand {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.reply()
                .withContent("Pong!")
                .withEphemeral(false);
    }
}
