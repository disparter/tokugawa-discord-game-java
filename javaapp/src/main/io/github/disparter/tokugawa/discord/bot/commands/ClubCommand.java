package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import io.github.disparter.tokugawa.discord.core.models.Club;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.ClubService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A command that handles player interactions with clubs.
 * This implementation integrates with ClubService to manage club operations.
 */
@Component
public class ClubCommand implements SlashCommand {

    private final PlayerService playerService;
    private final ClubService clubService;

    @Autowired
    public ClubCommand(PlayerService playerService, ClubService clubService) {
        this.playerService = playerService;
        this.clubService = clubService;
    }

    @Override
    public String getName() {
        return "clube";
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
                        return event.reply()
                                .withContent("Comando inválido. Use /clube listar, /clube info, /clube entrar, /clube sair, /clube alianca, ou /clube rivalidade.")
                                .withEphemeral(true);
                    }

                    String subcommand = subcommandOption.get().getName();

                    switch (subcommand) {
                        case "listar":
                            return handleListClubs(event, player);
                        case "info":
                            return handleClubInfo(event, player);
                        case "entrar":
                            return handleJoinClub(event, player);
                        case "sair":
                            return handleLeaveClub(event, player);
                        case "alianca":
                            return handleAlliance(event, player);
                        case "rivalidade":
                            return handleRivalry(event, player);
                        case "competicao":
                            return handleCompetition(event, player);
                        default:
                            return event.reply()
                                    .withContent("Subcomando desconhecido: " + subcommand)
                                    .withEphemeral(true);
                    }
                });
    }

    private Mono<Void> handleListClubs(ChatInputInteractionEvent event, Player player) {
        List<Club> clubs = clubService.getAllClubs();

        if (clubs.isEmpty()) {
            return event.reply()
                    .withContent("Não há clubes disponíveis no momento.")
                    .withEphemeral(true);
        }

        StringBuilder response = new StringBuilder("Clubes disponíveis:\n\n");

        for (Club club : clubs) {
            response.append("**").append(club.getName()).append("** (").append(club.getType()).append(")\n")
                   .append("Reputação: ").append(club.getReputation()).append(" | ")
                   .append("Ranking: ").append(club.getRanking()).append("\n")
                   .append("Membros: ").append(club.getMembers().size()).append("\n\n");
        }

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }

    private Mono<Void> handleClubInfo(ChatInputInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> clubOption = 
                event.getOption("clube");

        // If no club specified, show player's club
        if (clubOption.isEmpty() || clubOption.get().getValue().isEmpty()) {
            List<Club> playerClubs = clubService.getPlayerClubs(player.getId());

            if (playerClubs.isEmpty()) {
                return event.reply()
                        .withContent("Você não é membro de nenhum clube. Use /clube entrar para se juntar a um clube.")
                        .withEphemeral(true);
            }

            Club club = playerClubs.get(0); // Assuming a player can only be in one club
            return showClubInfo(event, club);
        }

        // Show info for specified club
        String clubName = clubOption.get().getValue().get().asString();
        List<Club> allClubs = clubService.getAllClubs();
        Optional<Club> clubOpt = allClubs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(clubName))
                .findFirst();

        if (clubOpt.isEmpty()) {
            return event.reply()
                    .withContent("Clube não encontrado: " + clubName)
                    .withEphemeral(true);
        }

        return showClubInfo(event, clubOpt.get());
    }

    private Mono<Void> showClubInfo(ChatInputInteractionEvent event, Club club) {
        StringBuilder response = new StringBuilder("Informações do clube **")
                .append(club.getName()).append("**:\n\n")
                .append("Tipo: ").append(club.getType()).append("\n")
                .append("Descrição: ").append(club.getDescription()).append("\n")
                .append("Reputação: ").append(club.getReputation()).append("\n")
                .append("Ranking: ").append(club.getRanking()).append("\n")
                .append("Membros: ").append(club.getMembers().size()).append("\n");

        // Add alliances if any
        List<Club> alliances = clubService.getAlliances(club.getId());
        if (!alliances.isEmpty()) {
            response.append("\nAlianças:\n");
            for (Club ally : alliances) {
                response.append("- ").append(ally.getName()).append("\n");
            }
        }

        // Add rivalries if any
        List<Club> rivalries = clubService.getRivalries(club.getId());
        if (!rivalries.isEmpty()) {
            response.append("\nRivalidades:\n");
            for (Club rival : rivalries) {
                response.append("- ").append(rival.getName()).append("\n");
            }
        }

        // Add competition results if any
        Map<String, Integer> competitionResults = clubService.getCompetitionResults(club.getId());
        if (!competitionResults.isEmpty()) {
            response.append("\nResultados de competições:\n");
            for (Map.Entry<String, Integer> entry : competitionResults.entrySet()) {
                response.append("- ").append(entry.getKey()).append(": ")
                       .append(entry.getValue()).append(" pontos\n");
            }
        }

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }

    private Mono<Void> handleJoinClub(ChatInputInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> clubOption = 
                event.getOption("clube");

        if (clubOption.isEmpty() || clubOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar um clube para entrar.")
                    .withEphemeral(true);
        }

        String clubName = clubOption.get().getValue().get().asString();
        List<Club> allClubs = clubService.getAllClubs();
        Optional<Club> clubOpt = allClubs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(clubName))
                .findFirst();

        if (clubOpt.isEmpty()) {
            return event.reply()
                    .withContent("Clube não encontrado: " + clubName)
                    .withEphemeral(true);
        }

        Club club = clubOpt.get();

        // Check if player is already in a club
        List<Club> playerClubs = clubService.getPlayerClubs(player.getId());
        if (!playerClubs.isEmpty()) {
            return event.reply()
                    .withContent("Você já é membro do clube " + playerClubs.get(0).getName() + 
                                ". Use /clube sair para deixar seu clube atual antes de entrar em outro.")
                    .withEphemeral(true);
        }

        // Join the club
        clubService.joinClub(club.getId(), player.getId());

        return event.reply()
                .withContent("Você entrou no clube **" + club.getName() + "**!")
                .withEphemeral(true);
    }

    private Mono<Void> handleLeaveClub(ChatInputInteractionEvent event, Player player) {
        List<Club> playerClubs = clubService.getPlayerClubs(player.getId());

        if (playerClubs.isEmpty()) {
            return event.reply()
                    .withContent("Você não é membro de nenhum clube.")
                    .withEphemeral(true);
        }

        Club club = playerClubs.get(0); // Assuming a player can only be in one club

        // Leave the club
        clubService.leaveClub(club.getId(), player.getId());

        return event.reply()
                .withContent("Você saiu do clube **" + club.getName() + "**.")
                .withEphemeral(true);
    }

    private Mono<Void> handleAlliance(ChatInputInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> actionOption = 
                event.getOption("acao");
        Optional<ApplicationCommandInteractionOption> targetOption = 
                event.getOption("alvo");

        if (actionOption.isEmpty() || actionOption.get().getValue().isEmpty() ||
            targetOption.isEmpty() || targetOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar uma ação (formar/quebrar) e um clube alvo.")
                    .withEphemeral(true);
        }

        String action = actionOption.get().getValue().get().asString();
        String targetName = targetOption.get().getValue().get().asString();

        // Get player's club
        List<Club> playerClubs = clubService.getPlayerClubs(player.getId());
        if (playerClubs.isEmpty()) {
            return event.reply()
                    .withContent("Você não é membro de nenhum clube.")
                    .withEphemeral(true);
        }

        Club playerClub = playerClubs.get(0);

        // Check if player is the leader
        if (!playerClub.getLeaderId().equals(player.getUserId())) {
            return event.reply()
                    .withContent("Apenas o líder do clube pode gerenciar alianças.")
                    .withEphemeral(true);
        }

        // Find target club
        List<Club> allClubs = clubService.getAllClubs();
        Optional<Club> targetClubOpt = allClubs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(targetName))
                .findFirst();

        if (targetClubOpt.isEmpty()) {
            return event.reply()
                    .withContent("Clube alvo não encontrado: " + targetName)
                    .withEphemeral(true);
        }

        Club targetClub = targetClubOpt.get();

        // Can't form alliance with own club
        if (playerClub.getId().equals(targetClub.getId())) {
            return event.reply()
                    .withContent("Você não pode formar uma aliança com seu próprio clube.")
                    .withEphemeral(true);
        }

        if ("formar".equalsIgnoreCase(action)) {
            clubService.formAlliance(playerClub.getId(), targetClub.getId());
            return event.reply()
                    .withContent("Aliança formada entre **" + playerClub.getName() + 
                                "** e **" + targetClub.getName() + "**!")
                    .withEphemeral(true);
        } else if ("quebrar".equalsIgnoreCase(action)) {
            clubService.breakAlliance(playerClub.getId(), targetClub.getId());
            return event.reply()
                    .withContent("Aliança quebrada entre **" + playerClub.getName() + 
                                "** e **" + targetClub.getName() + "**.")
                    .withEphemeral(true);
        } else {
            return event.reply()
                    .withContent("Ação desconhecida: " + action + ". Use 'formar' ou 'quebrar'.")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> handleRivalry(ChatInputInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> actionOption = 
                event.getOption("acao");
        Optional<ApplicationCommandInteractionOption> targetOption = 
                event.getOption("alvo");

        if (actionOption.isEmpty() || actionOption.get().getValue().isEmpty() ||
            targetOption.isEmpty() || targetOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar uma ação (declarar/encerrar) e um clube alvo.")
                    .withEphemeral(true);
        }

        String action = actionOption.get().getValue().get().asString();
        String targetName = targetOption.get().getValue().get().asString();

        // Get player's club
        List<Club> playerClubs = clubService.getPlayerClubs(player.getId());
        if (playerClubs.isEmpty()) {
            return event.reply()
                    .withContent("Você não é membro de nenhum clube.")
                    .withEphemeral(true);
        }

        Club playerClub = playerClubs.get(0);

        // Check if player is the leader
        if (!playerClub.getLeaderId().equals(player.getUserId())) {
            return event.reply()
                    .withContent("Apenas o líder do clube pode gerenciar rivalidades.")
                    .withEphemeral(true);
        }

        // Find target club
        List<Club> allClubs = clubService.getAllClubs();
        Optional<Club> targetClubOpt = allClubs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(targetName))
                .findFirst();

        if (targetClubOpt.isEmpty()) {
            return event.reply()
                    .withContent("Clube alvo não encontrado: " + targetName)
                    .withEphemeral(true);
        }

        Club targetClub = targetClubOpt.get();

        // Can't declare rivalry with own club
        if (playerClub.getId().equals(targetClub.getId())) {
            return event.reply()
                    .withContent("Você não pode declarar rivalidade com seu próprio clube.")
                    .withEphemeral(true);
        }

        if ("declarar".equalsIgnoreCase(action)) {
            clubService.declareRivalry(playerClub.getId(), targetClub.getId());
            return event.reply()
                    .withContent("Rivalidade declarada entre **" + playerClub.getName() + 
                                "** e **" + targetClub.getName() + "**!")
                    .withEphemeral(true);
        } else if ("encerrar".equalsIgnoreCase(action)) {
            clubService.endRivalry(playerClub.getId(), targetClub.getId());
            return event.reply()
                    .withContent("Rivalidade encerrada entre **" + playerClub.getName() + 
                                "** e **" + targetClub.getName() + "**.")
                    .withEphemeral(true);
        } else {
            return event.reply()
                    .withContent("Ação desconhecida: " + action + ". Use 'declarar' ou 'encerrar'.")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> handleCompetition(ChatInputInteractionEvent event, Player player) {
        Optional<ApplicationCommandInteractionOption> actionOption = 
                event.getOption("acao");

        if (actionOption.isEmpty() || actionOption.get().getValue().isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar uma ação (iniciar/atualizar/encerrar).")
                    .withEphemeral(true);
        }

        String action = actionOption.get().getValue().get().asString();

        // Get player's club
        List<Club> playerClubs = clubService.getPlayerClubs(player.getId());
        if (playerClubs.isEmpty()) {
            return event.reply()
                    .withContent("Você não é membro de nenhum clube.")
                    .withEphemeral(true);
        }

        Club playerClub = playerClubs.get(0);

        // Check if player is the leader
        if (!playerClub.getLeaderId().equals(player.getUserId())) {
            return event.reply()
                    .withContent("Apenas o líder do clube pode gerenciar competições.")
                    .withEphemeral(true);
        }

        // Handle different competition actions
        if ("iniciar".equalsIgnoreCase(action)) {
            // Implementation for starting a competition would go here
            // This would require additional options like competition name and participants
            return event.reply()
                    .withContent("Funcionalidade de iniciar competição ainda não implementada.")
                    .withEphemeral(true);
        } else if ("atualizar".equalsIgnoreCase(action)) {
            // Implementation for updating competition scores would go here
            return event.reply()
                    .withContent("Funcionalidade de atualizar pontuação de competição ainda não implementada.")
                    .withEphemeral(true);
        } else if ("encerrar".equalsIgnoreCase(action)) {
            // Implementation for ending a competition would go here
            return event.reply()
                    .withContent("Funcionalidade de encerrar competição ainda não implementada.")
                    .withEphemeral(true);
        } else {
            return event.reply()
                    .withContent("Ação desconhecida: " + action + ". Use 'iniciar', 'atualizar' ou 'encerrar'.")
                    .withEphemeral(true);
        }
    }
}
