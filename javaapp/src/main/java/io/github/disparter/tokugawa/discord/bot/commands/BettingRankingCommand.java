package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.services.BettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Command for viewing betting ranking.
 */
@Component
public class BettingRankingCommand implements SlashCommand {

    private final BettingService bettingService;

    @Autowired
    public BettingRankingCommand(BettingService bettingService) {
        this.bettingService = bettingService;
    }

    @Override
    public String getName() {
        return "ranking_apostas";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        try {
            // Get the betting ranking
            List<Map<String, Object>> ranking = bettingService.getBettingRanking(10);
            
            if (ranking.isEmpty()) {
                return event.reply()
                        .withContent("Não há apostas registradas ainda.");
            }

            // Create an embed with the ranking information
            EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                    .color(Color.ORANGE)
                    .title("Ranking de Apostadores")
                    .description("Os maiores apostadores da Academia Tokugawa:");

            // Add each player to the embed
            for (int i = 0; i < ranking.size(); i++) {
                Map<String, Object> stats = ranking.get(i);
                embedBuilder.addField(
                    (i + 1) + ". " + stats.get("playerName"),
                    "Total apostado: " + stats.get("totalAmount") + " moedas\n" +
                    "Apostas: " + stats.get("totalBets") + "\n" +
                    "Vitórias: " + stats.get("wins"),
                    false
                );
            }

            return event.reply().withEmbeds(embedBuilder.build());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Erro ao buscar ranking: " + e.getMessage());
        }
    }
}