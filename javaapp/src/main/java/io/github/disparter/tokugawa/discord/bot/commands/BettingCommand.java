package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Bet;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetType;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.BettingService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command for handling betting functionality.
 */
@Component
public class BettingCommand implements SlashCommand {

    private final BettingService bettingService;
    private final PlayerService playerService;

    @Autowired
    public BettingCommand(BettingService bettingService, PlayerService playerService) {
        this.bettingService = bettingService;
        this.playerService = playerService;
    }

    @Override
    public String getName() {
        return "apostar";
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

            // Get the subcommand
            String subcommand = event.getOptions().get(0).getName();

            switch (subcommand) {
                case "colocar":
                    return handlePlaceBet(event, player);
                case "minhas":
                    return handleMyBets(event, player);
                case "ranking":
                    return handleBettingRanking(event, player);
                default:
                    return event.reply()
                            .withEphemeral(true)
                            .withContent("Subcomando desconhecido: " + subcommand);
            }
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Erro ao processar comando: " + e.getMessage());
        }
    }

    private Mono<Void> handlePlaceBet(ChatInputInteractionEvent event, Player player) {
        // Get the bet type, target ID, and amount from the command options
        Optional<ApplicationCommandInteractionOptionValue> tipoOption = event.getOption("tipo")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        Optional<ApplicationCommandInteractionOptionValue> idOption = event.getOption("id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        Optional<ApplicationCommandInteractionOptionValue> valorOption = event.getOption("valor")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (tipoOption.isEmpty() || idOption.isEmpty() || valorOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Por favor, especifique o tipo, ID e valor da aposta.");
        }

        try {
            // Get the bet type, target ID, and amount
            String tipo = tipoOption.get().asString();
            String id = idOption.get().asString();
            Integer valor = (int) valorOption.get().asLong();

            // Validate the bet type
            BetType betType;
            switch (tipo.toLowerCase()) {
                case "duelo":
                    betType = BetType.DUEL;
                    break;
                case "evento":
                    betType = BetType.EVENT;
                    break;
                case "competicao":
                    betType = BetType.COMPETITION;
                    break;
                default:
                    return event.reply()
                            .withEphemeral(true)
                            .withContent("Tipo de aposta inválido. Use 'duelo', 'evento' ou 'competicao'");
            }

            // Validate the amount
            if (valor <= 0) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("O valor da aposta deve ser maior que zero");
            }

            // Place the bet
            Optional<Bet> betOpt = bettingService.placeBet(player.getId(), betType, valor, id);

            if (betOpt.isEmpty()) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Não foi possível realizar a aposta. Verifique se você tem moedas suficientes.");
            }

            // Create an embed with the bet information
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("Aposta Realizada")
                    .description("Aposta de " + valor + " moedas realizada com sucesso!")
                    .addField("Tipo", tipo, true)
                    .addField("ID do Alvo", id, true)
                    .addField("Valor", valor.toString(), true)
                    .build();

            return event.reply().withEmbeds(embed);
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Erro ao realizar aposta: " + e.getMessage());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Erro ao realizar aposta: " + e.getMessage());
        }
    }

    private Mono<Void> handleMyBets(ChatInputInteractionEvent event, Player player) {
        try {
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

    private Mono<Void> handleBettingRanking(ChatInputInteractionEvent event, Player player) {
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
