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

import java.util.ArrayList;
import java.util.HashMap;
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
        Optional<ApplicationCommandInteractionOption> nomeOption = 
                event.getOption("nome");
        Optional<ApplicationCommandInteractionOption> clubesOption = 
                event.getOption("clubes");
        Optional<ApplicationCommandInteractionOption> pontosOption = 
                event.getOption("pontos");

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
            // Check required parameters for starting a competition
            if (nomeOption.isEmpty() || nomeOption.get().getValue().isEmpty() ||
                clubesOption.isEmpty() || clubesOption.get().getValue().isEmpty()) {
                return event.reply()
                        .withContent("Para iniciar uma competição, você precisa especificar um nome e os clubes participantes.")
                        .withEphemeral(true);
            }

            String competitionName = nomeOption.get().getValue().get().asString();
            String clubsString = clubesOption.get().getValue().get().asString();
            String[] clubNames = clubsString.split(",");

            // Find club IDs from names
            List<Long> participatingClubIds = new ArrayList<>();
            List<String> participatingClubNames = new ArrayList<>();
            List<Club> allClubs = clubService.getAllClubs();

            // Always include the player's club
            participatingClubIds.add(playerClub.getId());
            participatingClubNames.add(playerClub.getName());

            // Add other clubs
            for (String clubName : clubNames) {
                String trimmedName = clubName.trim();
                if (!trimmedName.isEmpty()) {
                    Optional<Club> clubOpt = allClubs.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(trimmedName))
                            .findFirst();

                    if (clubOpt.isPresent() && !clubOpt.get().getId().equals(playerClub.getId())) {
                        participatingClubIds.add(clubOpt.get().getId());
                        participatingClubNames.add(clubOpt.get().getName());
                    }
                }
            }

            if (participatingClubIds.size() <= 1) {
                return event.reply()
                        .withContent("Você precisa incluir pelo menos um outro clube além do seu para iniciar uma competição.")
                        .withEphemeral(true);
            }

            // Start the competition
            Map<Long, Integer> initialScores = clubService.startCompetition(competitionName, participatingClubIds);

            // Format the response
            StringBuilder response = new StringBuilder();
            response.append("Competição **").append(competitionName).append("** iniciada!\n\n");
            response.append("Clubes participantes:\n");

            for (int i = 0; i < participatingClubIds.size(); i++) {
                Long clubId = participatingClubIds.get(i);
                String clubName = participatingClubNames.get(i);
                Integer score = initialScores.get(clubId);

                response.append("- **").append(clubName).append("**: ")
                        .append(score != null ? score : 0).append(" pontos\n");
            }

            return event.reply()
                    .withContent(response.toString());

        } else if ("atualizar".equalsIgnoreCase(action)) {
            // Check required parameters for updating competition scores
            if (nomeOption.isEmpty() || nomeOption.get().getValue().isEmpty() ||
                clubesOption.isEmpty() || clubesOption.get().getValue().isEmpty() ||
                pontosOption.isEmpty() || pontosOption.get().getValue().isEmpty()) {
                return event.reply()
                        .withContent("Para atualizar pontuações, você precisa especificar o nome da competição, os clubes e os pontos.")
                        .withEphemeral(true);
            }

            String competitionName = nomeOption.get().getValue().get().asString();
            String clubsString = clubesOption.get().getValue().get().asString();
            String pointsString = pontosOption.get().getValue().get().asString();

            String[] clubNames = clubsString.split(",");
            String[] pointsArray = pointsString.split(",");

            if (clubNames.length != pointsArray.length) {
                return event.reply()
                        .withContent("O número de clubes deve ser igual ao número de pontuações.")
                        .withEphemeral(true);
            }

            // Find club IDs from names and create scores map
            Map<Long, Integer> scores = new HashMap<>();
            List<String> updatedClubNames = new ArrayList<>();
            List<Club> allClubs = clubService.getAllClubs();

            for (int i = 0; i < clubNames.length; i++) {
                String trimmedName = clubNames[i].trim();
                if (!trimmedName.isEmpty()) {
                    Optional<Club> clubOpt = allClubs.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(trimmedName))
                            .findFirst();

                    if (clubOpt.isPresent()) {
                        try {
                            int points = Integer.parseInt(pointsArray[i].trim());
                            scores.put(clubOpt.get().getId(), points);
                            updatedClubNames.add(clubOpt.get().getName());
                        } catch (NumberFormatException e) {
                            return event.reply()
                                    .withContent("Pontuação inválida: " + pointsArray[i].trim())
                                    .withEphemeral(true);
                        }
                    }
                }
            }

            if (scores.isEmpty()) {
                return event.reply()
                        .withContent("Nenhum clube válido encontrado para atualizar pontuações.")
                        .withEphemeral(true);
            }

            // Update competition scores
            Map<Long, Integer> updatedScores = clubService.updateCompetitionScores(competitionName, scores);

            // Format the response
            StringBuilder response = new StringBuilder();
            response.append("Pontuações da competição **").append(competitionName).append("** atualizadas!\n\n");

            for (int i = 0; i < updatedClubNames.size(); i++) {
                Long clubId = allClubs.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(updatedClubNames.get(i)))
                        .findFirst()
                        .map(Club::getId)
                        .orElse(null);

                if (clubId != null) {
                    Integer score = updatedScores.get(clubId);
                    response.append("- **").append(updatedClubNames.get(i)).append("**: ")
                            .append(score != null ? score : 0).append(" pontos\n");
                }
            }

            return event.reply()
                    .withContent(response.toString());

        } else if ("encerrar".equalsIgnoreCase(action)) {
            // Check required parameters for ending a competition
            if (nomeOption.isEmpty() || nomeOption.get().getValue().isEmpty()) {
                return event.reply()
                        .withContent("Para encerrar uma competição, você precisa especificar o nome.")
                        .withEphemeral(true);
            }

            String competitionName = nomeOption.get().getValue().get().asString();

            // End the competition
            Map<Long, Integer> finalScores = clubService.endCompetition(competitionName);

            if (finalScores.isEmpty()) {
                return event.reply()
                        .withContent("Competição não encontrada ou já encerrada: " + competitionName)
                        .withEphemeral(true);
            }

            // Get club names and sort by score
            List<Map.Entry<Long, Integer>> sortedScores = new ArrayList<>(finalScores.entrySet());
            sortedScores.sort(Map.Entry.<Long, Integer>comparingByValue().reversed());

            List<Club> allClubs = clubService.getAllClubs();

            // Format the response
            StringBuilder response = new StringBuilder();
            response.append("Competição **").append(competitionName).append("** encerrada!\n\n");
            response.append("Resultados finais:\n");

            for (int i = 0; i < sortedScores.size(); i++) {
                Long clubId = sortedScores.get(i).getKey();
                Integer score = sortedScores.get(i).getValue();

                String clubName = allClubs.stream()
                        .filter(c -> c.getId().equals(clubId))
                        .findFirst()
                        .map(Club::getName)
                        .orElse("Clube Desconhecido");

                response.append(i + 1).append(". **").append(clubName).append("**: ")
                        .append(score).append(" pontos\n");
            }

            response.append("\nOs rankings dos clubes foram atualizados com base nos resultados.");

            return event.reply()
                    .withContent(response.toString());
        } else {
            return event.reply()
                    .withContent("Ação desconhecida: " + action + ". Use 'iniciar', 'atualizar' ou 'encerrar'.")
                    .withEphemeral(true);
        }
    }
}
