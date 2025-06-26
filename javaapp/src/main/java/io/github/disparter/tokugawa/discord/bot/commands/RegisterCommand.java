package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.SlashCommandInteractionEvent;
import discord4j.core.object.entity.User;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * A command that handles user registration in the game.
 * This implementation integrates with PlayerService to create or verify a player in the database.
 */
@Component
public class RegisterCommand implements SlashCommand {

    private final PlayerService playerService;

    @Autowired
    public RegisterCommand(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public Mono<Void> execute(SlashCommandInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getUser())
                .flatMap(user -> {
                    String userId = user.getId().asString();
                    String username = user.getUsername();

                    // Check if player already exists
                    Player player = playerService.findByDiscordId(userId);

                    String message;
                    if (player != null) {
                        message = "Olá, " + username + "! Você já está registrado no jogo Tokugawa. Bem-vindo de volta!";
                    } else {
                        // Create new player
                        player = new Player();
                        player.setUserId(userId);
                        player.setName(username);
                        player.setLevel(1);
                        player.setExp(0);
                        player.setPoints(0);

                        playerService.save(player);
                        message = "Olá, " + username + "! Você foi registrado no jogo Tokugawa. Bem-vindo!";
                    }

                    return event.reply()
                            .withContent(message)
                            .withEphemeral(true); // Only visible to the user who triggered the command
                });
    }
}
