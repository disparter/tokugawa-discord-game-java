package io.github.disparter.tokugawa.discord.bot.commands;

import lombok.extern.slf4j.Slf4j;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Technique;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.TechniqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command for managing a player's techniques.
 */
@Component
@Slf4j
public class TechniqueCommand implements SlashCommand {


    private final PlayerService playerService;
    private final TechniqueService techniqueService;

    @Autowired
    public TechniqueCommand(
            PlayerService playerService,
            TechniqueService techniqueService) {
        this.playerService = playerService;
        this.techniqueService = techniqueService;
    }

    @Override
    public String getName() {
        return "technique";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        // Get the Discord user ID
        String discordId = event.getInteraction().getUser().getId().asString();

        // Find the player
        Player player = playerService.findByDiscordId(discordId);

        if (player == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("You need to register first! Use /register to create a character.");
        }

        // Get the subcommand
        String subcommand = event.getOptions().get(0).getName();

        switch (subcommand) {
            case "list":
                return handleListTechniques(event, player);
            case "info":
                return handleTechniqueInfo(event, player);
            case "evolve":
                return handleEvolveTechnique(event, player);
            default:
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Unknown subcommand: " + subcommand);
        }
    }

    /**
     * Handle the "list" subcommand to display the player's techniques.
     */
    private Mono<Void> handleListTechniques(ChatInputInteractionEvent event, Player player) {
        // Get the player's techniques
        List<Technique> techniques = techniqueService.getPlayerTechniques(player);

        // Create an embed to display the techniques
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title(player.getUsername() + "'s Techniques")
                .description("Here are the techniques you know:");

        if (techniques.isEmpty()) {
            embedBuilder.addField("No Techniques", "You don't know any techniques yet.", false);
        } else {
            // Group techniques by type
            Map<Technique.TechniqueType, StringBuilder> techniquesByType = new java.util.HashMap<>();

            for (Technique technique : techniques) {
                StringBuilder typeBuilder = techniquesByType.computeIfAbsent(
                        technique.getType(), k -> new StringBuilder());

                typeBuilder.append("- **").append(technique.getName()).append("**");

                // Add power points if the technique has been evolved
                if (technique.getPowerPoints() != null && technique.getPowerPoints() > 0) {
                    typeBuilder.append(" (Power: ").append(technique.getPowerPoints()).append(")");
                }

                typeBuilder.append("\n");
            }

            // Add each type as a field
            for (Map.Entry<Technique.TechniqueType, StringBuilder> entry : techniquesByType.entrySet()) {
                embedBuilder.addField(entry.getKey().toString(), entry.getValue().toString(), false);
            }
        }

        // Add player's power points
        embedBuilder.addField("Power Points", 
                String.format("You have %d power points available for technique evolution.", 
                        player.getPowerPoints()), 
                false);

        // Send the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handle the "info" subcommand to display information about a technique.
     */
    private Mono<Void> handleTechniqueInfo(ChatInputInteractionEvent event, Player player) {
        // Get the technique ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> techniqueIdOption = event.getOption("technique_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (techniqueIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify a technique ID.");
        }

        String techniqueId = techniqueIdOption.get().asString();

        // Find the technique
        Optional<Technique> techniqueOpt = techniqueService.findByTechniqueId(techniqueId);

        if (techniqueOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Technique not found with ID: " + techniqueId);
        }

        Technique technique = techniqueOpt.get();

        // Check if the player knows the technique
        boolean playerKnowsTechnique = techniqueService.playerKnowsTechnique(player, technique);

        // Create an embed to display the technique information
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(playerKnowsTechnique ? Color.GREEN : Color.RED)
                .title(technique.getName())
                .description(technique.getDescription())
                .addField("Type", technique.getType().toString(), true)
                .addField("Mana Cost", technique.getManaCost().toString(), true)
                .addField("Cooldown", technique.getCooldown().toString() + " turns", true)
                .addField("Base Damage", technique.getBaseDamage().toString(), true)
                .addField("Known", playerKnowsTechnique ? "Yes" : "No", true);

        // Add power points if the technique has been evolved
        if (technique.getPowerPoints() != null && technique.getPowerPoints() > 0) {
            embedBuilder.addField("Power Points", technique.getPowerPoints().toString(), true);
        }

        // Add effects if the technique has any
        if (!technique.getEffects().isEmpty()) {
            StringBuilder effectsBuilder = new StringBuilder();
            Map<String, String> effectDescriptions = techniqueService.getTechniqueEffectDescriptions();

            for (Map.Entry<String, Integer> effect : technique.getEffects().entrySet()) {
                String effectName = effect.getKey();
                Integer effectValue = effect.getValue();
                String description = effectDescriptions.getOrDefault(effectName, "Unknown effect");

                effectsBuilder.append("- **").append(effectName).append("**: ")
                        .append(effectValue).append(" (").append(description).append(")\n");
            }

            embedBuilder.addField("Effects", effectsBuilder.toString(), false);
        }

        // Add evolution requirements if the player knows the technique
        if (playerKnowsTechnique) {
            Map<String, Integer> requirements = techniqueService.getTechniqueEvolutionRequirements(technique);

            StringBuilder requirementsBuilder = new StringBuilder();
            for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
                requirementsBuilder.append("- **").append(requirement.getKey()).append("**: ")
                        .append(requirement.getValue()).append("\n");
            }

            embedBuilder.addField("Evolution Requirements", requirementsBuilder.toString(), false);
        }

        // Send the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handle the "evolve" subcommand to evolve a technique.
     */
    private Mono<Void> handleEvolveTechnique(ChatInputInteractionEvent event, Player player) {
        // Get the technique ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> techniqueIdOption = event.getOption("technique_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (techniqueIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify a technique ID.");
        }

        String techniqueId = techniqueIdOption.get().asString();

        // Get the power points from the command options
        Optional<ApplicationCommandInteractionOptionValue> powerPointsOption = event.getOption("power_points")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (powerPointsOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify the number of power points to invest.");
        }

        int powerPoints = (int) powerPointsOption.get().asLong();

        // Find the technique
        Optional<Technique> techniqueOpt = techniqueService.findByTechniqueId(techniqueId);

        if (techniqueOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Technique not found with ID: " + techniqueId);
        }

        Technique technique = techniqueOpt.get();

        try {
            // Evolve the technique
            Technique evolvedTechnique = techniqueService.evolveTechnique(player, technique, powerPoints);

            // Create an embed to display the result
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("Technique Evolved")
                    .description("You evolved " + evolvedTechnique.getName() + " by investing " + 
                            powerPoints + " power points.")
                    .addField("New Power Level", evolvedTechnique.getPowerPoints().toString(), true)
                    .addField("Remaining Power Points", Integer.toString(player.getPowerPoints()), true)
                    .build();

            return event.reply()
                    .withEmbeds(embed);
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error: " + e.getMessage());
        }
    }
}
