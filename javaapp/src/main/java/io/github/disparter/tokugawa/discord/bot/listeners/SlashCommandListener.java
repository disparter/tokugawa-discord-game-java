package io.github.disparter.tokugawa.discord.bot.listeners;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import io.github.disparter.tokugawa.discord.bot.commands.SlashCommand;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listener for slash command interactions.
 * This class registers all available commands and dispatches incoming slash commands to the appropriate handler.
 */
@Component
public class SlashCommandListener {

    private final Map<String, SlashCommand> commands;

    /**
     * Creates a new SlashCommandListener with the given commands.
     * The commands are automatically injected by Spring and mapped by their names.
     * 
     * @param slashCommands The collection of available slash commands
     */
    public SlashCommandListener(Collection<SlashCommand> slashCommands) {
        this.commands = slashCommands.stream()
                .collect(Collectors.toMap(
                        SlashCommand::getName,
                        Function.identity()
                ));

        System.out.println("Registered slash commands: " + commands.keySet());
    }

    /**
     * Handles an incoming chat input interaction event.
     * This method is called by the Discord bot when a slash command is received.
     * 
     * @param event The chat input interaction event
     * @return A Mono that completes when the command execution is done
     */
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String commandName = event.getCommandName();

        return Mono.justOrEmpty(commands.get(commandName))
                .flatMap(command -> command.execute(event))
                .onErrorResume(error -> {
                    System.err.println("Error executing command '" + commandName + "': " + error.getMessage());
                    error.printStackTrace();

                    return event.reply()
                            .withContent("Ocorreu um erro ao executar o comando. Por favor, tente novamente mais tarde.")
                            .withEphemeral(true);
                })
                .then();
    }
}
