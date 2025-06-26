package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.SlashCommandInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Location;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.LocationService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command for discovering hidden areas in the game world.
 * This command allows players to discover hidden areas at their current location.
 */
@Component
public class DiscoverCommand implements SlashCommand {

    private final PlayerService playerService;
    private final LocationService locationService;

    @Autowired
    public DiscoverCommand(PlayerService playerService, LocationService locationService) {
        this.playerService = playerService;
        this.locationService = locationService;
    }

    @Override
    public String getName() {
        return "descobrir";
    }

    @Override
    public Mono<Void> execute(SlashCommandInteractionEvent event) {
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
                    
                    // Get the area option
                    Optional<ApplicationCommandInteractionOption> areaOption = 
                            event.getOption("area");
                    
                    if (areaOption.isEmpty() || areaOption.get().getValue().isEmpty()) {
                        return showHiddenAreas(event, player);
                    }
                    
                    String areaName = areaOption.get().getValue().get().asString();
                    return discoverArea(event, player, areaName);
                });
    }
    
    private Mono<Void> showHiddenAreas(SlashCommandInteractionEvent event, Player player) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());
        
        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }
        
        List<String> hiddenAreas = locationService.getHiddenAreas(currentLocation.getId());
        
        if (hiddenAreas.isEmpty()) {
            return event.reply()
                    .withContent("Não há áreas ocultas para descobrir em " + currentLocation.getName() + ".")
                    .withEphemeral(true);
        }
        
        StringBuilder response = new StringBuilder("Você sente que há algo mais para descobrir em **")
                .append(currentLocation.getName())
                .append("**...\n\nVocê pode tentar descobrir:\n\n");
        
        for (String area : hiddenAreas) {
            response.append("- **").append(area).append("**\n");
        }
        
        response.append("\nPara tentar descobrir uma área, use `/descobrir area:nome_da_area`");
        
        return event.reply()
                .withContent(response.toString())
                .withEphemeral(true);
    }
    
    private Mono<Void> discoverArea(SlashCommandInteractionEvent event, Player player, String areaName) {
        Location currentLocation = locationService.getPlayerLocation(player.getId());
        
        if (currentLocation == null) {
            return event.reply()
                    .withContent("Você não está em nenhum local. Isso é estranho... Por favor, contate um administrador.")
                    .withEphemeral(true);
        }
        
        // Check if the area exists at the current location
        List<String> hiddenAreas = locationService.getHiddenAreas(currentLocation.getId());
        
        if (!hiddenAreas.contains(areaName)) {
            return event.reply()
                    .withContent("Não há uma área chamada '" + areaName + "' para descobrir em " + currentLocation.getName() + ".")
                    .withEphemeral(true);
        }
        
        // Check if the player meets the requirements to discover the area
        if (!locationService.checkDiscoveryRequirements(player.getId(), currentLocation.getId(), areaName)) {
            // Get the requirements for the area
            Map<String, String> requirements = locationService.getDiscoveryRequirements(currentLocation.getId());
            String requirement = requirements.get(areaName);
            
            return event.reply()
                    .withContent("Você tenta descobrir **" + areaName + "**, mas não consegue. " +
                            "Parece que você precisa de: " + formatRequirement(requirement))
                    .withEphemeral(true);
        }
        
        // Discover the area
        boolean discovered = locationService.discoverHiddenArea(player.getId(), currentLocation.getId(), areaName);
        
        if (!discovered) {
            return event.reply()
                    .withContent("Você tenta descobrir **" + areaName + "**, mas algo deu errado. Por favor, tente novamente.")
                    .withEphemeral(true);
        }
        
        // Create an embed for the discovery
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.YELLOW)
                .title("Descoberta: " + areaName)
                .description("Você descobriu **" + areaName + "** em " + currentLocation.getName() + "!")
                .build();
        
        return event.reply()
                .withEmbeds(embed)
                .withEphemeral(false);
    }
    
    private String formatRequirement(String requirement) {
        if (requirement == null) {
            return "requisitos desconhecidos";
        }
        
        String[] parts = requirement.split(":");
        
        if (parts.length < 2) {
            return requirement;
        }
        
        String requirementType = parts[0];
        
        switch (requirementType) {
            case "stat":
                if (parts.length < 3) return requirement;
                return parts[1] + " de nível " + parts[2] + " ou superior";
                
            case "skill":
                if (parts.length < 3) return requirement;
                return "habilidade " + parts[1] + " de nível " + parts[2] + " ou superior";
                
            case "level":
                if (parts.length < 2) return requirement;
                return "nível " + parts[1] + " ou superior";
                
            case "reputation":
                if (parts.length < 2) return requirement;
                return "reputação " + parts[1] + " ou superior";
                
            default:
                return requirement;
        }
    }
}