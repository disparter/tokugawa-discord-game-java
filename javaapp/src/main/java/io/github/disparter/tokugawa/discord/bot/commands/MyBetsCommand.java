package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Bet;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.BettingService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Command for viewing active bets.
 */
@Component
public class MyBetsCommand implements SlashCommand {

    private final BettingService bettingService;
    private final PlayerService playerService;

    @Autowired
    public MyBetsCommand(BettingService bettingService, PlayerService playerService) {
        this.bettingService = bettingService;
        this.playerService = playerService;
    }

    @Override
    public String getName() {
        return "minhas_apostas";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        // Get the Discord user
        User user = event.getInteraction().getUser();

        try {
            // Find the player
            Player player = playerService.findByDiscordId(user.getId().asString());
            if (player == null) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Você precisa se registrar primeiro usando /register");
            }

            // Get the player's active bets
            List<Bet> activeBets = bettingService.getActiveBets(player.getId());
            
            if (activeBets.isEmpty()) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Você não tem apostas ativas.");
            }

            // Create an embed with the active bets information
            EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                    .color(Color.BLUE)
                    .title("Suas Apostas Ativas")
                    .description("Aqui estão suas apostas ativas:");

            // Add each bet to the embed
            for (Bet bet : activeBets) {
                String betType = "";
                switch (bet.getType()) {
                    case DUEL:
                        betType = "Duelo";
                        break;
                    case EVENT:
                        betType = "Evento";
                        break;
                    case COMPETITION:
                        betType = "Competição";
                        break;
                }
                
                embedBuilder.addField(
                    "Aposta em " + betType + " " + bet.getTargetId(),
                    "Valor: " + bet.getAmount() + " moedas",
                    false
                );
            }

            return event.reply().withEmbeds(embedBuilder.build());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Erro ao buscar apostas: " + e.getMessage());
        }
    }
}