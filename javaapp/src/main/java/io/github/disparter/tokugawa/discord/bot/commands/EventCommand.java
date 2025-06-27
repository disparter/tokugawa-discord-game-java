package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Event.EventType;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.EventService;
import io.github.disparter.tokugawa.discord.core.services.GameCalendarService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * A command that allows players to view and interact with events.
 */
@Slf4j
@Component
public class EventCommand implements SlashCommand {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EventService eventService;
    private final PlayerService playerService;
    private final GameCalendarService gameCalendarService;

    @Autowired
    public EventCommand(EventService eventService, PlayerService playerService, GameCalendarService gameCalendarService) {
        this.eventService = eventService;
        this.playerService = playerService;
        this.gameCalendarService = gameCalendarService;
    }

    @Override
    public String getName() {
        return "event";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getUser())
                .flatMap(user -> {
                    String userId = user.getId().asString();

                    // Get the player
                    Player player = playerService.findByDiscordId(userId);
                    if (player == null) {
                        return event.reply()
                                .withContent("Você precisa se registrar primeiro! Use o comando /register.")
                                .withEphemeral(true);
                    }

                    // Get the subcommand
                    Optional<String> subcommandOpt = event.getOption("subcommand")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString);

                    if (subcommandOpt.isEmpty()) {
                        return showAvailableEvents(event, player);
                    }

                    String subcommand = subcommandOpt.get();
                    switch (subcommand) {
                        case "list":
                            return showAvailableEvents(event, player);
                        case "seasonal":
                            return showSeasonalEvents(event, player);
                        case "random":
                            return showRandomEvents(event, player);
                        case "participate":
                            return participateInEvent(event, player);
                        case "calendar":
                            return showCalendar(event, player);
                        default:
                            return event.reply()
                                    .withContent("Subcomando desconhecido: " + subcommand)
                                    .withEphemeral(true);
                    }
                });
    }

    /**
     * Shows the available events for a player.
     *
     * @param event the slash command event
     * @param player the player
     * @return a Mono that completes when the command execution is done
     */
    private Mono<Void> showAvailableEvents(ChatInputInteractionEvent event, Player player) {
        List<Event> availableEvents = eventService.getAvailableEventsForPlayer(player.getId());

        if (availableEvents.isEmpty()) {
            return event.reply()
                    .withContent("Não há eventos disponíveis no momento.")
                    .withEphemeral(true);
        }

        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("Eventos Disponíveis")
                .color(Color.BLUE)
                .description("Aqui estão os eventos disponíveis para você:");

        for (Event availableEvent : availableEvents) {
            embedBuilder.addField(
                    availableEvent.getName(),
                    availableEvent.getDescription() + "\nTipo: " + formatEventType(availableEvent.getType()),
                    false
            );
        }

        return event.reply()
                .withEmbeds(embedBuilder.build())
                .withEphemeral(true);
    }

    /**
     * Shows the seasonal events for a player.
     *
     * @param event the slash command event
     * @param player the player
     * @return a Mono that completes when the command execution is done
     */
    private Mono<Void> showSeasonalEvents(ChatInputInteractionEvent event, Player player) {
        List<Event> seasonalEvents = eventService.getAvailableSeasonalEventsForPlayer(player.getId());

        if (seasonalEvents.isEmpty()) {
            return event.reply()
                    .withContent("Não há eventos sazonais disponíveis no momento.")
                    .withEphemeral(true);
        }

        Season currentSeason = gameCalendarService.getCurrentSeason();
        String seasonName = formatSeason(currentSeason);

        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("Eventos Sazonais - " + seasonName)
                .color(getSeasonColor(currentSeason))
                .description("Aqui estão os eventos sazonais disponíveis para você:");

        for (Event seasonalEvent : seasonalEvents) {
            String period = "";
            if (seasonalEvent.getStartMonth() != null && seasonalEvent.getStartDay() != null &&
                seasonalEvent.getEndMonth() != null && seasonalEvent.getEndDay() != null) {
                period = "\nPeríodo: " + seasonalEvent.getStartDay() + "/" + seasonalEvent.getStartMonth() +
                        " a " + seasonalEvent.getEndDay() + "/" + seasonalEvent.getEndMonth();
            }

            embedBuilder.addField(
                    seasonalEvent.getName(),
                    seasonalEvent.getDescription() + period,
                    false
            );
        }

        return event.reply()
                .withEmbeds(embedBuilder.build())
                .withEphemeral(true);
    }

    /**
     * Shows the random events for a player.
     *
     * @param event the slash command event
     * @param player the player
     * @return a Mono that completes when the command execution is done
     */
    private Mono<Void> showRandomEvents(ChatInputInteractionEvent event, Player player) {
        List<Event> randomEvents = eventService.checkForRandomEvents(player.getId());

        if (randomEvents.isEmpty()) {
            return event.reply()
                    .withContent("Não há eventos aleatórios disponíveis no momento.")
                    .withEphemeral(true);
        }

        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("Eventos Aleatórios")
                .color(Color.ORANGE)
                .description("Aqui estão os eventos aleatórios disponíveis para você:");

        for (Event randomEvent : randomEvents) {
            embedBuilder.addField(
                    randomEvent.getName(),
                    randomEvent.getDescription(),
                    false
            );
        }

        return event.reply()
                .withEmbeds(embedBuilder.build())
                .withEphemeral(true);
    }

    /**
     * Allows a player to participate in an event.
     *
     * @param event the slash command event
     * @param player the player
     * @return a Mono that completes when the command execution is done
     */
    private Mono<Void> participateInEvent(ChatInputInteractionEvent event, Player player) {
        Optional<String> eventIdOpt = event.getOption("event_id")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);

        if (eventIdOpt.isEmpty()) {
            return event.reply()
                    .withContent("Você precisa especificar o ID do evento.")
                    .withEphemeral(true);
        }

        String eventIdStr = eventIdOpt.get();
        Long eventId;
        try {
            eventId = Long.parseLong(eventIdStr);
        } catch (NumberFormatException e) {
            return event.reply()
                    .withContent("ID de evento inválido: " + eventIdStr)
                    .withEphemeral(true);
        }

        try {
            Event triggeredEvent = eventService.triggerEvent(eventId, player.getId());
            Player updatedPlayer = eventService.applyEventConsequences(eventId, player.getId());
            Event completedEvent = eventService.completeEvent(eventId, player.getId());

            return event.reply()
                    .withContent("Você participou do evento: " + completedEvent.getName() + "\n" +
                            "Recompensas aplicadas!")
                    .withEphemeral(true);
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withContent("Erro ao participar do evento: " + e.getMessage())
                    .withEphemeral(true);
        }
    }

    /**
     * Shows the game calendar.
     *
     * @param event the slash command event
     * @param player the player
     * @return a Mono that completes when the command execution is done
     */
    private Mono<Void> showCalendar(ChatInputInteractionEvent event, Player player) {
        Season currentSeason = gameCalendarService.getCurrentSeason();
        String seasonName = formatSeason(currentSeason);
        String currentDate = gameCalendarService.getCurrentDate().format(DATE_FORMATTER);

        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .title("Calendário do Jogo")
                .color(getSeasonColor(currentSeason))
                .description("Informações sobre o calendário atual do jogo:")
                .addField("Data Atual", currentDate, true)
                .addField("Estação", seasonName, true)
                .addField("Próxima Estação", formatSeason(Season.getNextSeason(currentSeason)), true);

        return event.reply()
                .withEmbeds(embedBuilder.build())
                .withEphemeral(true);
    }

    /**
     * Formats an event type for display.
     *
     * @param type the event type
     * @return the formatted event type
     */
    private String formatEventType(EventType type) {
        switch (type) {
            case SEASONAL:
                return "Sazonal";
            case RANDOM:
                return "Aleatório";
            case CLIMACTIC:
                return "Climático";
            case ROMANCE:
                return "Romance";
            case CHOICE_TRIGGERED:
                return "Baseado em Escolhas";
            default:
                return "Básico";
        }
    }

    /**
     * Formats a season for display.
     *
     * @param season the season
     * @return the formatted season
     */
    private String formatSeason(Season season) {
        switch (season) {
            case SPRING:
                return "Primavera";
            case SUMMER:
                return "Verão";
            case AUTUMN:
                return "Outono";
            case WINTER:
                return "Inverno";
            default:
                return "Desconhecida";
        }
    }

    /**
     * Gets the color for a season.
     *
     * @param season the season
     * @return the color
     */
    private Color getSeasonColor(Season season) {
        switch (season) {
            case SPRING:
                return Color.of(0x77DD77); // Light green
            case SUMMER:
                return Color.of(0xFFD700); // Gold
            case AUTUMN:
                return Color.of(0xD2691E); // Chocolate
            case WINTER:
                return Color.of(0x87CEEB); // Sky blue
            default:
                return Color.BLUE;
        }
    }
}
