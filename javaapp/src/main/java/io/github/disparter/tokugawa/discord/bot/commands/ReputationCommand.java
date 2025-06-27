package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.ReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * A command that handles player reputation.
 * This implementation integrates with ReputationService to manage player reputation.
 */
@Component
public class ReputationCommand implements SlashCommand {

    private final PlayerService playerService;
    private final ReputationService reputationService;

    // Reputation thresholds and titles
    private static final int RESPECTED_THRESHOLD = 50;
    private static final int ADMIRED_THRESHOLD = 100;
    private static final int REVERED_THRESHOLD = 200;
    private static final int LEGENDARY_THRESHOLD = 500;

    @Autowired
    public ReputationCommand(PlayerService playerService, ReputationService reputationService) {
        this.playerService = playerService;
        this.reputationService = reputationService;
    }

    @Override
    public String getName() {
        return "reputacao";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getUser())
                .flatMap(user -> {
                    String userId = user.getId().asString();

                    // Check if player exists
                    Player player = playerService.findByDiscordId(userId);
                    if (player == null) {
                        return event.reply()
                                .withContent("Você precisa se registrar primeiro usando o comando /register.")
                                .withEphemeral(true);
                    }

                    // Get the subcommand
                    Optional<ApplicationCommandInteractionOption> subcommandOption = 
                            event.getOptions().stream().findFirst();

                    if (subcommandOption.isEmpty()) {
                        // Default to showing player's reputation
                        return handleViewReputation(event, player);
                    }

                    String subcommand = subcommandOption.get().getName();

                    switch (subcommand) {
                        case "ver":
                            return handleViewReputation(event, player);
                        case "ranking":
                            return handleViewRanking(event, player);
                        default:
                            return event.reply()
                                    .withContent("Subcomando desconhecido: " + subcommand)
                                    .withEphemeral(true);
                    }
                });
    }

    private Mono<Void> handleViewReputation(ChatInputInteractionEvent event, Player player) {
        int reputation = reputationService.getReputation(player.getId());
        int rank = reputationService.getReputationRank(player.getId());

        StringBuilder response = new StringBuilder("**Reputação de ")
                .append(player.getName()).append("**\n\n")
                .append("Pontos de Reputação: ").append(reputation).append("\n")
                .append("Ranking: ").append(rank).append("\n")
                .append("Título: ").append(getReputationTitle(reputation)).append("\n\n");

        // Add information about next threshold
        int nextThreshold = getNextReputationThreshold(reputation);
        if (nextThreshold > 0) {
            response.append("Próximo título em: ").append(nextThreshold).append(" pontos ")
                   .append("(faltam ").append(nextThreshold - reputation).append(" pontos)\n");
        } else {
            response.append("Você atingiu o título máximo de reputação!\n");
        }

        // Add information about how to gain reputation
        response.append("\nComo ganhar reputação:\n")
               .append("- Tomar decisões honrosas na história\n")
               .append("- Vencer duelos\n")
               .append("- Ajudar outros jogadores e NPCs\n")
               .append("- Participar de eventos do clube\n");

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }

    private Mono<Void> handleViewRanking(ChatInputInteractionEvent event, Player player) {
        List<Player> rankedPlayers = reputationService.getRankedPlayers();

        if (rankedPlayers.isEmpty()) {
            return event.reply()
                    .withContent("Não há jogadores registrados no ranking de reputação.")
                    .withEphemeral(true);
        }

        StringBuilder response = new StringBuilder("**Ranking de Reputação**\n\n");

        // Show top 10 players
        int limit = Math.min(10, rankedPlayers.size());
        for (int i = 0; i < limit; i++) {
            Player rankedPlayer = rankedPlayers.get(i);
            response.append(i + 1).append(". **")
                   .append(rankedPlayer.getName()).append("**: ")
                   .append(rankedPlayer.getReputation()).append(" pontos (")
                   .append(getReputationTitle(rankedPlayer.getReputation())).append(")\n");
        }

        // Show player's position if not in top 10
        int playerRank = reputationService.getReputationRank(player.getId());
        if (playerRank > 10) {
            response.append("\nSua posição:\n")
                   .append(playerRank).append(". **")
                   .append(player.getName()).append("**: ")
                   .append(player.getReputation()).append(" pontos (")
                   .append(getReputationTitle(player.getReputation())).append(")\n");
        }

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }

    private String getReputationTitle(int reputation) {
        if (reputation >= LEGENDARY_THRESHOLD) {
            return "Lendário";
        } else if (reputation >= REVERED_THRESHOLD) {
            return "Reverenciado";
        } else if (reputation >= ADMIRED_THRESHOLD) {
            return "Admirado";
        } else if (reputation >= RESPECTED_THRESHOLD) {
            return "Respeitado";
        } else {
            return "Novato";
        }
    }

    private int getNextReputationThreshold(int reputation) {
        if (reputation < RESPECTED_THRESHOLD) {
            return RESPECTED_THRESHOLD;
        } else if (reputation < ADMIRED_THRESHOLD) {
            return ADMIRED_THRESHOLD;
        } else if (reputation < REVERED_THRESHOLD) {
            return REVERED_THRESHOLD;
        } else if (reputation < LEGENDARY_THRESHOLD) {
            return LEGENDARY_THRESHOLD;
        } else {
            return -1; // No next threshold
        }
    }
}
