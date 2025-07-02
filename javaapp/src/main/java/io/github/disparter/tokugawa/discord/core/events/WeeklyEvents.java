package io.github.disparter.tokugawa.discord.core.events;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.bot.DiscordBot;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles weekly tournaments and events.
 */
@Slf4j
public class WeeklyEvents extends BaseEvent {

    private final PlayerService playerService;
    private Map<String, Object> currentTournament = null;
    private Map<String, Map<String, Object>> tournamentParticipants = new ConcurrentHashMap<>();
    private LocalDateTime tournamentEndTime = null;

    /**
     * Constructor for WeeklyEvents.
     *
     * @param discordBot The Discord bot instance
     * @param playerService The player service
     * @param channelId The channel ID for announcements (optional)
     */
    public WeeklyEvents(DiscordBot discordBot, PlayerService playerService, Long channelId) {
        super(discordBot, channelId);
        this.playerService = playerService;
    }

    /**
     * Start a weekly tournament.
     *
     * @return A Mono that completes when the tournament is started
     */
    public Mono<Void> startWeeklyTournament() {
        try {
            // Check if there's already a tournament running
            if (currentTournament != null) {
                log.info("A tournament is already running");
                return Mono.empty();
            }

            // Create tournament data
            String[] tournamentTypes = {
                    "Torneio de Matemática",
                    "Torneio de Português",
                    "Torneio de História",
                    "Torneio de Ciências",
                    "Torneio de Geografia",
                    "Torneio de Inglês",
                    "Torneio de Artes",
                    "Torneio de Educação Física"
            };

            String tournamentType = tournamentTypes[new Random().nextInt(tournamentTypes.length)];

            Map<String, Object> tournamentData = new HashMap<>();
            tournamentData.put("name", tournamentType);
            tournamentData.put("description", "Participe do " + tournamentType + " e ganhe pontos para sua turma!");
            tournamentData.put("start_time", LocalDateTime.now());
            tournamentData.put("end_time", LocalDateTime.now().plusDays(7));

            // Set tournament data
            currentTournament = tournamentData;
            tournamentParticipants.clear();
            tournamentEndTime = (LocalDateTime) tournamentData.get("end_time");

            // Announce tournament
            return sendTournamentAnnouncement()
                    .then();
        } catch (Exception e) {
            log.error("Error starting weekly tournament: {}", e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * Send tournament announcement.
     *
     * @return A Mono that emits the sent message
     */
    public Mono<Message> sendTournamentAnnouncement() {
        if (currentTournament == null) {
            log.error("No tournament data available for announcement");
            return Mono.empty();
        }

        EmbedCreateSpec embed = createTournamentEmbed(currentTournament);
        ActionRow buttons = createTournamentButtons();

        if (channelId == null) {
            log.error("No channel ID set for tournament announcement");
            return Mono.empty();
        }

        return findChannel(channelId)
                .flatMap(channel -> channel.createMessage(MessageCreateSpec.builder()
                        .addEmbed(embed)
                        .addComponent(buttons)
                        .build()))
                .flatMap(message -> message.pin().thenReturn(message))
                .doOnSuccess(message -> log.info("Tournament announcement sent"))
                .doOnError(e -> log.error("Error sending tournament announcement: {}", e.getMessage()));
    }

    /**
     * Create tournament embed.
     *
     * @param tournamentData The tournament data
     * @return The embed specification
     */
    private EmbedCreateSpec createTournamentEmbed(Map<String, Object> tournamentData) {
        String name = (String) tournamentData.get("name");
        String description = (String) tournamentData.get("description");
        LocalDateTime endTime = (LocalDateTime) tournamentData.get("end_time");

        return EmbedCreateSpec.builder()
                .title("🏆 " + name)
                .description(description + "\n\nO torneio termina em: " + endTime.toString())
                .color(Color.YELLOW)
                .build();
    }

    /**
     * Create tournament buttons.
     *
     * @return The action row with buttons
     */
    private ActionRow createTournamentButtons() {
        return ActionRow.of(
                Button.primary("tournament_register", "Participar"),
                Button.secondary("tournament_leaderboard", "Classificação")
        );
    }

    /**
     * Add a participant to the tournament.
     *
     * @param userId The user ID
     * @param username The username
     * @return A Mono that completes when the participant is added
     */
    public Mono<Void> addTournamentParticipant(String userId, String username) {
        if (currentTournament == null) {
            log.error("No tournament running");
            return Mono.empty();
        }

        // Check if player is already registered
        if (tournamentParticipants.containsKey(userId)) {
            log.info("Player {} is already registered for the tournament", username);
            return Mono.empty();
        }

        // Register player
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("username", username);
        participantData.put("score", 0);
        participantData.put("join_time", LocalDateTime.now());

        tournamentParticipants.put(userId, participantData);
        log.info("Player {} registered for the tournament", username);

        return Mono.empty();
    }

    /**
     * Update a participant's score.
     *
     * @param userId The user ID
     * @param score The score to add
     * @return A Mono that completes when the score is updated
     */
    public Mono<Void> updateTournamentScore(String userId, int score) {
        if (currentTournament == null) {
            log.error("No tournament running");
            return Mono.empty();
        }

        // Check if player is registered
        if (!tournamentParticipants.containsKey(userId)) {
            log.error("Player {} is not registered for the tournament", userId);
            return Mono.empty();
        }

        // Update score
        Map<String, Object> participantData = tournamentParticipants.get(userId);
        int currentScore = (int) participantData.getOrDefault("score", 0);
        participantData.put("score", currentScore + score);

        log.info("Player {} score updated to {}", userId, currentScore + score);
        return Mono.empty();
    }

    /**
     * End the current tournament.
     *
     * @return A Mono that completes when the tournament is ended
     */
    public Mono<Void> endTournament() {
        if (currentTournament == null) {
            log.error("No tournament running");
            return Mono.empty();
        }

        // Get tournament results
        List<Map.Entry<String, Map<String, Object>>> sortedParticipants = new ArrayList<>(tournamentParticipants.entrySet());
        sortedParticipants.sort((a, b) -> {
            int scoreA = (int) a.getValue().getOrDefault("score", 0);
            int scoreB = (int) b.getValue().getOrDefault("score", 0);
            return Integer.compare(scoreB, scoreA); // Sort in descending order
        });

        // Create results embed
        StringBuilder results = new StringBuilder("🏆 Resultados do " + currentTournament.get("name") + "\n\n");

        for (int i = 0; i < Math.min(10, sortedParticipants.size()); i++) {
            Map.Entry<String, Map<String, Object>> entry = sortedParticipants.get(i);
            String username = (String) entry.getValue().get("username");
            int score = (int) entry.getValue().getOrDefault("score", 0);

            results.append(i + 1).append(". ").append(username).append(" - ").append(score).append(" pontos\n");
        }

        // Award prizes to top participants
        for (int i = 0; i < Math.min(3, sortedParticipants.size()); i++) {
            Map.Entry<String, Map<String, Object>> entry = sortedParticipants.get(i);
            String userId = entry.getKey();
            String username = (String) entry.getValue().get("username");

            // Award points based on placement
            int points = switch (i) {
                case 0 -> 100; // 1st place
                case 1 -> 50;  // 2nd place
                case 2 -> 25;  // 3rd place
                default -> 0;
            };

            // Update player
            try {
                playerService.increaseReputation(Long.parseLong(userId), points);
                log.info("Awarded {} points to player {} for tournament placement", points, username);
            } catch (Exception e) {
                log.error("Error awarding points to player {}: {}", username, e.getMessage());
            }
        }

        // Send results
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("🏆 Torneio Encerrado")
                .description(results.toString())
                .color(Color.YELLOW)
                .build();

        // Reset tournament data
        currentTournament = null;
        tournamentParticipants.clear();
        tournamentEndTime = null;

        // Send announcement
        return sendAnnouncement("🏆 Torneio Encerrado", results.toString(), Color.YELLOW);
    }

    /**
     * Check if the tournament has ended.
     *
     * @return A Mono that completes when the check is done
     */
    public Mono<Void> checkForEndingTournament() {
        if (currentTournament != null && tournamentEndTime != null && LocalDateTime.now().isAfter(tournamentEndTime)) {
            log.info("Tournament has ended, processing results");
            return endTournament();
        }
        return Mono.empty();
    }

    /**
     * Get information about the current tournament.
     *
     * @return A map containing tournament information
     */
    public Map<String, Object> getCurrentTournamentInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (currentTournament == null) {
            info.put("name", "Nenhum torneio em andamento");
            info.put("participants", 0);
            info.put("end_time", null);
            info.put("running", false);
        } else {
            info.put("name", currentTournament.get("name"));
            info.put("participants", tournamentParticipants.size());
            info.put("end_time", tournamentEndTime);
            info.put("running", true);
            info.put("description", currentTournament.get("description"));
            info.put("start_time", currentTournament.get("start_time"));
        }
        
        return info;
    }

    /**
     * Clean up resources.
     */
    @Override
    public void cleanup() {
        // Clean up any resources
        currentTournament = null;
        tournamentParticipants.clear();
        tournamentEndTime = null;
        log.info("Weekly events cleaned up");
    }
}
