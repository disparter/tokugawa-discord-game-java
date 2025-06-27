package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Location;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.LocationService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * Command for exploring the game world.
 * This command allows players to view their current location, see connected locations,
 * and view NPCs and items at their current location.
 */
@Component
public class ExploreCommand implements SlashCommand {

    private final PlayerService playerService;
    private final LocationService locationService;

    @Autowired
    public ExploreCommand(PlayerService playerService, LocationService locationService) {
        this.playerService = playerService;
        this.locationService = locationService;
    }

    @Override
    public String getName() {
        return "explorar";
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
                                .withContent("Comando inválido. Use /explorar local, /explorar conexoes, /explorar npcs, ou /explorar itens.")
                                .withEphemeral(true);
                    }

                    String subcommand = subcommandOption.get().getName();

                    switch (subcommand) {
                        case "local":
                            return handleViewLocation(event, player);
                        case "conexoes":
                            return handleViewConnections(event, player);
                        case "npcs":
                            return handleViewNPCs(event, player);
                        case "itens":
                            return handleViewItems(event, player);
                        default:
                            return event.reply()
                                    .withContent("Subcomando desconhecido: " + subcommand)
                                    .withEphemeral(true);
                    }
                });
    }

    private Mono<Void> handleViewLocation(ChatInputInteractionEvent event, Player player) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());

        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Localização Atual: " + currentLocation.getName())
                .description(currentLocation.getDescription())
                .addField("Coordenadas", "X: " + currentLocation.getX() + ", Y: " + currentLocation.getY(), true)
                .build();

        return event.reply()
                .withEmbeds(embed)
                .withEphemeral(false);
    }

    private Mono<Void> handleViewConnections(ChatInputInteractionEvent event, Player player) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());

        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }

        List<Location> connectedLocations = locationService.getConnectedLocations(currentLocation.getId());

        if (connectedLocations.isEmpty()) {
            return event.reply()
                    .withContent("Não há locais conectados a " + currentLocation.getName() + ".")
                    .withEphemeral(true);
        }

        StringBuilder response = new StringBuilder("Locais conectados a **")
                .append(currentLocation.getName())
                .append("**:\n\n");

        for (Location location : connectedLocations) {
            response.append("- **").append(location.getName()).append("**");

            if (location.isLocked()) {
                response.append(" (Bloqueado)");
            }

            response.append("\n");
        }

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(false);
    }

    private Mono<Void> handleViewNPCs(ChatInputInteractionEvent event, Player player) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());

        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }

        List<NPC> npcsAtLocation = locationService.getNPCsAtLocation(currentLocation.getId());

        if (npcsAtLocation.isEmpty()) {
            return event.reply()
                    .withContent("Não há NPCs em " + currentLocation.getName() + ".")
                    .withEphemeral(true);
        }

        StringBuilder response = new StringBuilder("NPCs em **")
                .append(currentLocation.getName())
                .append("**:\n\n");

        for (NPC npc : npcsAtLocation) {
            response.append("- **").append(npc.getName()).append("** (").append(npc.getType()).append(")\n")
                   .append("  ").append(npc.getBackground()).append("\n\n");
        }

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(false);
    }

    private Mono<Void> handleViewItems(ChatInputInteractionEvent event, Player player) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());

        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }

        List<Item> itemsAtLocation = locationService.getItemsAtLocation(currentLocation.getId());

        if (itemsAtLocation.isEmpty()) {
            return event.reply()
                    .withContent("Não há itens em " + currentLocation.getName() + ".")
                    .withEphemeral(true);
        }

        StringBuilder response = new StringBuilder("Itens em **")
                .append(currentLocation.getName())
                .append("**:\n\n");

        for (Item item : itemsAtLocation) {
            response.append("- **").append(item.getName()).append("** (").append(item.getType()).append(")\n")
                   .append("  ").append(item.getDescription()).append("\n\n");
        }

        return event.reply()
                .withContent(response.toString())
                .withEphemeral(false);
    }
}
