package io.github.disparter.tokugawa.discord.core.events;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.bot.DiscordBot;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles special events like seasonal events and competitions.
 */
@Component
public class SpecialEvents extends BaseEvent {

    private final PlayerService playerService;
    private Map<String, Object> currentEvent = null;
    private Map<String, Map<String, Object>> eventParticipants = new ConcurrentHashMap<>();
    private LocalDateTime eventEndTime = null;

    /**
     * Constructor for SpecialEvents.
     *
     * @param discordBot The Discord bot instance
     * @param playerService The player service
     * @param channelId The channel ID for announcements (optional)
     */
    @Autowired
    public SpecialEvents(DiscordBot discordBot, PlayerService playerService, Long channelId) {
        super(discordBot, channelId);
        this.playerService = playerService;
    }

    /**
     * Start a special event.
     *
     * @param eventType The type of event to start
     * @return A Mono that completes when the event is started
     */
    public Mono<Void> startSpecialEvent(String eventType) {
        try {
            // Check if there's already an event running
            if (currentEvent != null) {
                logger.info("A special event is already running");
                return Mono.empty();
            }

            // Create event data
            Map<String, Object> eventData = new HashMap<>();
            
            switch (eventType) {
                case "seasonal":
                    // Determine current season
                    int month = LocalDateTime.now().getMonthValue();
                    String season;
                    if (month >= 3 && month <= 5) {
                        season = "Primavera";
                    } else if (month >= 6 && month <= 8) {
                        season = "Verão";
                    } else if (month >= 9 && month <= 11) {
                        season = "Outono";
                    } else {
                        season = "Inverno";
                    }
                    
                    eventData.put("name", "Festival de " + season);
                    eventData.put("description", "Participe do Festival de " + season + " e ganhe prêmios especiais!");
                    eventData.put("duration_days", 14); // Two weeks
                    break;
                    
                case "competition":
                    String[] competitions = {
                            "Competição de Redação",
                            "Competição de Matemática",
                            "Competição de Ciências",
                            "Competição de Artes",
                            "Competição de Esportes"
                    };
                    String competition = competitions[new Random().nextInt(competitions.length)];
                    
                    eventData.put("name", competition);
                    eventData.put("description", "Participe da " + competition + " e mostre seu talento!");
                    eventData.put("duration_days", 7); // One week
                    break;
                    
                case "holiday":
                    eventData.put("name", "Evento de Feriado");
                    eventData.put("description", "Celebre o feriado com atividades especiais!");
                    eventData.put("duration_days", 3); // Three days
                    break;
                    
                default:
                    eventData.put("name", "Evento Especial");
                    eventData.put("description", "Um evento especial está acontecendo! Participe e ganhe recompensas.");
                    eventData.put("duration_days", 5); // Five days
                    break;
            }
            
            eventData.put("type", eventType);
            eventData.put("start_time", LocalDateTime.now());
            eventData.put("end_time", LocalDateTime.now().plusDays((int) eventData.get("duration_days")));
            
            // Set event data
            currentEvent = eventData;
            eventParticipants.clear();
            eventEndTime = (LocalDateTime) eventData.get("end_time");
            
            // Announce event
            return sendSpecialEventAnnouncement()
                    .then();
        } catch (Exception e) {
            logger.error("Error starting special event: {}", e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * Send special event announcement.
     *
     * @return A Mono that emits the sent message
     */
    public Mono<Message> sendSpecialEventAnnouncement() {
        if (currentEvent == null) {
            logger.error("No event data available for announcement");
            return Mono.empty();
        }
        
        EmbedCreateSpec embed = createSpecialEventEmbed(currentEvent);
        ActionRow buttons = createSpecialEventButtons();
        
        if (channelId == null) {
            logger.error("No channel ID set for special event announcement");
            return Mono.empty();
        }
        
        return findChannel(channelId)
                .flatMap(channel -> channel.createMessage(MessageCreateSpec.builder()
                        .addEmbed(embed)
                        .addComponent(buttons)
                        .build()))
                .flatMap(message -> message.pin().thenReturn(message))
                .doOnSuccess(message -> logger.info("Special event announcement sent"))
                .doOnError(e -> logger.error("Error sending special event announcement: {}", e.getMessage()));
    }

    /**
     * Create special event embed.
     *
     * @param eventData The event data
     * @return The embed specification
     */
    private EmbedCreateSpec createSpecialEventEmbed(Map<String, Object> eventData) {
        String name = (String) eventData.get("name");
        String description = (String) eventData.get("description");
        LocalDateTime endTime = (LocalDateTime) eventData.get("end_time");
        
        return EmbedCreateSpec.builder()
                .title("✨ " + name)
                .description(description + "\n\nO evento termina em: " + endTime.toString())
                .color(Color.MAGENTA)
                .build();
    }

    /**
     * Create special event buttons.
     *
     * @return The action row with buttons
     */
    private ActionRow createSpecialEventButtons() {
        return ActionRow.of(
                Button.primary("event_register", "Participar"),
                Button.secondary("event_info", "Informações"),
                Button.secondary("event_leaderboard", "Classificação")
        );
    }

    /**
     * Add a participant to the event.
     *
     * @param userId The user ID
     * @param username The username
     * @return A Mono that completes when the participant is added
     */
    public Mono<Void> addEventParticipant(String userId, String username) {
        if (currentEvent == null) {
            logger.error("No special event running");
            return Mono.empty();
        }
        
        // Check if player is already registered
        if (eventParticipants.containsKey(userId)) {
            logger.info("Player {} is already registered for the special event", username);
            return Mono.empty();
        }
        
        // Register player
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("username", username);
        participantData.put("score", 0);
        participantData.put("join_time", LocalDateTime.now());
        
        eventParticipants.put(userId, participantData);
        logger.info("Player {} registered for the special event", username);
        
        return Mono.empty();
    }

    /**
     * Update a participant's score.
     *
     * @param userId The user ID
     * @param score The score to add
     * @return A Mono that completes when the score is updated
     */
    public Mono<Void> updateEventScore(String userId, int score) {
        if (currentEvent == null) {
            logger.error("No special event running");
            return Mono.empty();
        }
        
        // Check if player is registered
        if (!eventParticipants.containsKey(userId)) {
            logger.error("Player {} is not registered for the special event", userId);
            return Mono.empty();
        }
        
        // Update score
        Map<String, Object> participantData = eventParticipants.get(userId);
        int currentScore = (int) participantData.getOrDefault("score", 0);
        participantData.put("score", currentScore + score);
        
        logger.info("Player {} score updated to {}", userId, currentScore + score);
        return Mono.empty();
    }

    /**
     * End the current special event.
     *
     * @return A Mono that completes when the event is ended
     */
    public Mono<Void> endSpecialEvent() {
        if (currentEvent == null) {
            logger.error("No special event running");
            return Mono.empty();
        }
        
        // Get event results
        List<Map.Entry<String, Map<String, Object>>> sortedParticipants = new ArrayList<>(eventParticipants.entrySet());
        sortedParticipants.sort((a, b) -> {
            int scoreA = (int) a.getValue().getOrDefault("score", 0);
            int scoreB = (int) b.getValue().getOrDefault("score", 0);
            return Integer.compare(scoreB, scoreA); // Sort in descending order
        });
        
        // Create results embed
        StringBuilder results = new StringBuilder("✨ Resultados do " + currentEvent.get("name") + "\n\n");
        
        for (int i = 0; i < Math.min(10, sortedParticipants.size()); i++) {
            Map.Entry<String, Map<String, Object>> entry = sortedParticipants.get(i);
            String username = (String) entry.getValue().get("username");
            int score = (int) entry.getValue().getOrDefault("score", 0);
            
            results.append(i + 1).append(". ").append(username).append(" - ").append(score).append(" pontos\n");
        }
        
        // Award prizes to top participants
        for (int i = 0; i < Math.min(5, sortedParticipants.size()); i++) {
            Map.Entry<String, Map<String, Object>> entry = sortedParticipants.get(i);
            String userId = entry.getKey();
            String username = (String) entry.getValue().get("username");
            
            // Award points based on placement
            int points = switch (i) {
                case 0 -> 200; // 1st place
                case 1 -> 100; // 2nd place
                case 2 -> 50;  // 3rd place
                case 3 -> 25;  // 4th place
                case 4 -> 10;  // 5th place
                default -> 0;
            };
            
            // Update player
            try {
                playerService.increaseReputation(Long.parseLong(userId), points);
                logger.info("Awarded {} points to player {} for special event placement", points, username);
            } catch (Exception e) {
                logger.error("Error awarding points to player {}: {}", username, e.getMessage());
            }
        }
        
        // Reset event data
        currentEvent = null;
        eventParticipants.clear();
        eventEndTime = null;
        
        // Send announcement
        return sendAnnouncement("✨ Evento Especial Encerrado", results.toString(), Color.MAGENTA);
    }

    /**
     * Check if the special event has ended.
     *
     * @return A Mono that completes when the check is done
     */
    public Mono<Void> checkForEndingSpecialEvent() {
        if (currentEvent != null && eventEndTime != null && LocalDateTime.now().isAfter(eventEndTime)) {
            logger.info("Special event has ended, processing results");
            return endSpecialEvent();
        }
        return Mono.empty();
    }

    /**
     * Check for special events that should be started.
     *
     * @return A Mono that completes when the check is done
     */
    public Mono<Void> checkForSpecialEvents() {
        // If there's already an event running, do nothing
        if (currentEvent != null) {
            return Mono.empty();
        }
        
        // Check for seasonal events
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        
        // Start seasonal events at the beginning of each season
        if ((month == 3 && day == 21) || // Spring
            (month == 6 && day == 21) || // Summer
            (month == 9 && day == 21) || // Fall
            (month == 12 && day == 21))  // Winter
        {
            logger.info("Starting seasonal event");
            return startSpecialEvent("seasonal");
        }
        
        // Start holiday events on specific dates
        if ((month == 1 && day == 1) ||   // New Year
            (month == 12 && day == 25) || // Christmas
            (month == 10 && day == 31))   // Halloween
        {
            logger.info("Starting holiday event");
            return startSpecialEvent("holiday");
        }
        
        // Randomly start competition events (5% chance each day if no event is running)
        if (new Random().nextDouble() < 0.05) {
            logger.info("Starting random competition event");
            return startSpecialEvent("competition");
        }
        
        return Mono.empty();
    }

    /**
     * Clean up resources.
     */
    @Override
    public void cleanup() {
        // Clean up any resources
        currentEvent = null;
        eventParticipants.clear();
        eventEndTime = null;
        logger.info("Special events cleaned up");
    }
}