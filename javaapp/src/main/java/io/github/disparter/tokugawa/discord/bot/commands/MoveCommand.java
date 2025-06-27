package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Location;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.services.LocationService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command for moving between locations in the game world.
 * This command allows players to move to a connected location.
 */
@Component
public class MoveCommand implements SlashCommand {

    private final PlayerService playerService;
    private final LocationService locationService;

    @Autowired
    public MoveCommand(PlayerService playerService, LocationService locationService) {
        this.playerService = playerService;
        this.locationService = locationService;
    }

    @Override
    public String getName() {
        return "mover";
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

                    // Get the destination option
                    Optional<ApplicationCommandInteractionOption> destinationOption = 
                            event.getOption("destino");

                    if (destinationOption.isEmpty() || destinationOption.get().getValue().isEmpty()) {
                        return showAvailableDestinations(event, player);
                    }

                    String destinationName = destinationOption.get().getValue().get().asString();
                    return moveToDestination(event, player, destinationName);
                });
    }

    private Mono<Void> showAvailableDestinations(ChatInputInteractionEvent event, Player player) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());

        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }

        List<Location> connectedLocations = locationService.getConnectedLocations(currentLocation.getId());

        if (connectedLocations.isEmpty()) {
            return event.reply()
                    .withContent("Não há locais conectados a " + currentLocation.getName() + ". Você não pode se mover daqui.")
                    .withEphemeral(true);
        }

        StringBuilder response = new StringBuilder("Você está em **")
                .append(currentLocation.getName())
                .append("**.\n\nVocê pode se mover para:\n\n");

        for (Location location : connectedLocations) {
            response.append("- **").append(location.getName()).append("**");

            if (location.isLocked()) {
                response.append(" (Bloqueado)");
            }

            response.append("\n");
        }

        response.append("\nPara se mover, use `/mover destino:nome_do_local`");

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }

    private Mono<Void> moveToDestination(ChatInputInteractionEvent event, Player player, String destinationName) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());

        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }

        // Find the destination location
        List<Location> connectedLocations = locationService.getConnectedLocations(currentLocation.getId());
        Optional<Location> destinationOpt = connectedLocations.stream()
                .filter(location -> location.getName().equalsIgnoreCase(destinationName))
                .findFirst();

        if (destinationOpt.isEmpty()) {
            return event.reply()
                    .withContent("Você não pode se mover para " + destinationName + " a partir de " + currentLocation.getName() + ".")
                    .withEphemeral(true);
        }

        Location destination = destinationOpt.get();

        // Check if the destination is locked
        if (destination.isLocked()) {
            // Check if the player meets the requirements to unlock it
            if (locationService.checkUnlockRequirements(player.getId(), destination.getId())) {
                locationService.unlockLocation(destination.getId());

                return event.reply()
                        .withContent("Você desbloqueou **" + destination.getName() + "**!")
                        .withEphemeral(false)
                        .then(movePlayerToDestination(event, player, destination));
            } else {
                return event.reply()
                        .withContent("**" + destination.getName() + "** está bloqueado. Você não atende aos requisitos para desbloquear este local.")
                        .withEphemeral(true);
            }
        }

        return movePlayerToDestination(event, player, destination);
    }

    private Mono<Void> movePlayerToDestination(ChatInputInteractionEvent event, Player player, Location destination) {
        // Move the player to the destination
        boolean moved = locationService.movePlayer(player.getId(), destination.getId());

        if (!moved) {
            return event.reply()
                    .withContent("Não foi possível se mover para " + destination.getName() + ". Por favor, tente novamente.")
                    .withEphemeral(true);
        }

        // Create an embed for the new location
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("Você chegou a: " + destination.getName())
                .description(destination.getDescription())
                .build();

        // Check if a random event is triggered
        Optional<Event> randomEvent = locationService.triggerRandomEvent(player.getId(), destination.getId());

        if (randomEvent.isPresent()) {
            Event event1 = randomEvent.get();

            // Add the event to the embed
            embed = EmbedCreateSpec.builder()
                    .from(embed)
                    .addField("Evento", event1.getName(), false)
                    .addField("Descrição", event1.getDescription(), false)
                    .build();
        }

        return event.reply()
                .withEmbeds(embed)
                .withEphemeral(false);
    }
}
