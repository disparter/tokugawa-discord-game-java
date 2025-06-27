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

import java.util.*;

/**
 * Handles daily events and announcements.
 */
@Component
public class DailyEvents extends BaseEvent {

    private final PlayerService playerService;
    private Map<String, Object> dailySubject = new HashMap<>();
    private Map<String, Map<String, Object>> playerProgress = new HashMap<>();

    /**
     * Constructor for DailyEvents.
     *
     * @param discordBot The Discord bot instance
     * @param playerService The player service
     * @param channelId The channel ID for announcements (optional)
     */
    @Autowired
    public DailyEvents(DiscordBot discordBot, PlayerService playerService, Long channelId) {
        super(discordBot, channelId);
        this.playerService = playerService;
        this.playerProgress.put("daily", new HashMap<>());
    }

    /**
     * Select subjects for daily events.
     *
     * @return A list of selected subjects
     */
    public List<String> selectSubjects() {
        List<String> subjects = Arrays.asList(
                "Matemática",
                "Português",
                "História",
                "Ciências",
                "Geografia",
                "Inglês",
                "Artes",
                "Educação Física"
        );

        // Randomly select 3 subjects
        List<String> selectedSubjects = new ArrayList<>(subjects);
        Collections.shuffle(selectedSubjects);
        return selectedSubjects.subList(0, 3);
    }

    /**
     * Create daily event embed.
     *
     * @param subjects The list of subjects
     * @return The embed specification
     */
    public EmbedCreateSpec createDailyEmbed(List<String> subjects) {
        StringBuilder description = new StringBuilder("Escolha uma das aulas abaixo para participar:\n\n");
        for (String subject : subjects) {
            description.append("• ").append(subject).append("\n");
        }

        return EmbedCreateSpec.builder()
                .title("📚 Aulas do Dia")
                .description(description.toString())
                .color(Color.BLUE)
                .build();
    }

    /**
     * Create daily event buttons.
     *
     * @param subjects The list of subjects
     * @return The action row with buttons
     */
    public ActionRow createDailyButtons(List<String> subjects) {
        List<Button> buttons = new ArrayList<>();
        for (String subject : subjects) {
            buttons.add(Button.primary("daily_" + subject, subject));
        }

        return ActionRow.of(buttons);
    }

    /**
     * Handle subject selection.
     *
     * @param userId The user ID
     * @param username The username
     * @param subject The selected subject
     * @return A Mono that completes when the operation is done
     */
    public Mono<Void> handleSubjectSelection(String userId, String username, String subject) {
        try {
            // Get player data
            return Mono.fromCallable(() -> playerService.findByDiscordId(userId))
                    .flatMap(player -> {
                        if (player == null) {
                            return Mono.error(new IllegalArgumentException("Player not found"));
                        }

                        // Update player progress
                        Map<String, Object> dailyProgress = player.getDailyProgress();
                        if (dailyProgress == null) {
                            dailyProgress = new HashMap<>();
                        }
                        dailyProgress.put(subject, true);
                        player.setDailyProgress(dailyProgress);

                        // Save player
                        playerService.save(player);

                        logger.info("Player {} participated in {} class", username, subject);
                        return Mono.empty().then();
                    })
                    .onErrorResume(e -> {
                        logger.error("Error handling subject selection: {}", e.getMessage());
                        return Mono.empty().then();
                    });
        } catch (Exception e) {
            logger.error("Error handling subject selection: {}", e.getMessage());
            return Mono.empty().then();
        }
    }

    /**
     * Send daily announcement.
     *
     * @return A Mono that emits the sent message
     */
    public Mono<Message> sendDailyAnnouncement() {
        try {
            List<String> subjects = selectSubjects();
            EmbedCreateSpec embed = createDailyEmbed(subjects);
            ActionRow buttons = createDailyButtons(subjects);

            if (channelId == null) {
                logger.error("No channel ID set for daily announcement");
                return Mono.empty();
            }

            return findChannel(channelId)
                    .flatMap(channel -> channel.createMessage(MessageCreateSpec.builder()
                            .addEmbed(embed)
                            .addComponent(buttons)
                            .build()))
                    .flatMap(message -> message.pin().thenReturn(message))
                    .doOnSuccess(message -> logger.info("Daily announcement sent"))
                    .doOnError(e -> logger.error("Error sending daily announcement: {}", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error sending daily announcement: {}", e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * Select daily subject.
     */
    public void selectDailySubject() {
        List<String> subjects = selectSubjects();
        dailySubject = new HashMap<>();
        dailySubject.put("subjects", subjects);
        logger.info("Daily subjects selected: {}", subjects);
    }

    /**
     * Announce daily subject.
     *
     * @return A Mono that completes when the announcement is sent
     */
    public Mono<Void> announceDailySubject() {
        @SuppressWarnings("unchecked")
        List<String> subjects = (List<String>) dailySubject.getOrDefault("subjects", Collections.emptyList());
        if (subjects.isEmpty()) {
            logger.error("No daily subjects selected");
            return Mono.empty();
        }

        return sendAnnouncement(
                "📚 Aulas do Dia",
                "As aulas de hoje são: " + String.join(", ", subjects),
                Color.BLUE
        );
    }

    /**
     * Reset daily progress.
     */
    public void resetDailyProgress() {
        playerProgress.put("daily", new HashMap<>());
        logger.info("Daily progress reset");
    }

    /**
     * Clean up resources.
     */
    @Override
    public void cleanup() {
        // Clean up any resources
        dailySubject.clear();
        playerProgress.get("daily").clear();
        logger.info("Daily events cleaned up");
    }
}
