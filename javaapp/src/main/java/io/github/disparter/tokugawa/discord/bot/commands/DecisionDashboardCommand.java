package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.ConsequenceService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Command for viewing the decision dashboard.
 */
@Component
public class DecisionDashboardCommand implements SlashCommand {

    private final ConsequenceService consequenceService;
    private final PlayerService playerService;

    @Autowired
    public DecisionDashboardCommand(ConsequenceService consequenceService, PlayerService playerService) {
        this.consequenceService = consequenceService;
        this.playerService = playerService;
    }

    @Override
    public String getName() {
        return "decisions";
    }


    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        // Get the Discord user ID
        String discordId = event.getInteraction().getUser().getId().asString();

        // Find the player by Discord ID
        Player player = playerService.findByDiscordId(discordId);
        if (player == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("You need to register first. Use the /register command.");
        }

        // Get the subcommand
        String subcommand = event.getOptions().get(0).getName();

        switch (subcommand) {
            case "view":
                return handleViewDashboard(event, player);
            case "enhanced":
                return handleEnhancedDashboard(event, player);
            case "reflections":
                return handleReflections(event, player);
            case "alternatives":
                return handleAlternatives(event, player);
            case "community":
                return handleCommunityComparison(event, player);
            default:
                return handleViewDashboard(event, player);
        }
    }

    /**
     * Handles the view subcommand.
     */
    private Mono<Void> handleViewDashboard(ChatInputInteractionEvent event, Player player) {
        // Get the decision dashboard
        Map<String, List<Consequence>> dashboard = consequenceService.getDecisionDashboard(player.getId());

        // Create the embed
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Decision Dashboard")
                .description("Here are the consequences of your decisions:");

        // Add active consequences
        List<Consequence> activeConsequences = dashboard.get("active");
        if (activeConsequences != null && !activeConsequences.isEmpty()) {
            StringBuilder activeText = new StringBuilder();
            for (Consequence consequence : activeConsequences) {
                activeText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
            }
            embedBuilder.addField("Active Consequences", activeText.toString(), false);
        } else {
            embedBuilder.addField("Active Consequences", "None", false);
        }

        // Add immediate consequences
        List<Consequence> immediateConsequences = dashboard.get("immediate");
        if (immediateConsequences != null && !immediateConsequences.isEmpty()) {
            StringBuilder immediateText = new StringBuilder();
            for (Consequence consequence : immediateConsequences) {
                immediateText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
            }
            embedBuilder.addField("Immediate Consequences", immediateText.toString(), false);
        }

        // Add short-term consequences
        List<Consequence> shortTermConsequences = dashboard.get("short_term");
        if (shortTermConsequences != null && !shortTermConsequences.isEmpty()) {
            StringBuilder shortTermText = new StringBuilder();
            for (Consequence consequence : shortTermConsequences) {
                shortTermText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
            }
            embedBuilder.addField("Short-Term Consequences", shortTermText.toString(), false);
        }

        // Add long-term consequences
        List<Consequence> longTermConsequences = dashboard.get("long_term");
        if (longTermConsequences != null && !longTermConsequences.isEmpty()) {
            StringBuilder longTermText = new StringBuilder();
            for (Consequence consequence : longTermConsequences) {
                longTermText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
            }
            embedBuilder.addField("Long-Term Consequences", longTermText.toString(), false);
        }

        // Add permanent consequences
        List<Consequence> permanentConsequences = dashboard.get("permanent");
        if (permanentConsequences != null && !permanentConsequences.isEmpty()) {
            StringBuilder permanentText = new StringBuilder();
            for (Consequence consequence : permanentConsequences) {
                permanentText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
            }
            embedBuilder.addField("Permanent Consequences", permanentText.toString(), false);
        }

        // Add footer
        embedBuilder.footer("Use /decisions view to view your decision dashboard", null);

        // Reply with the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handles the enhanced subcommand.
     */
    private Mono<Void> handleEnhancedDashboard(ChatInputInteractionEvent event, Player player) {
        // Get the enhanced decision dashboard
        Map<String, List<Consequence>> dashboard = consequenceService.getEnhancedDecisionDashboard(player.getId());

        // Create the embed
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Enhanced Decision Dashboard")
                .description("Here's a comprehensive view of your journey and decisions:");

        // Add consequences with reflections
        List<Consequence> withReflections = dashboard.get("with_reflections");
        if (withReflections != null && !withReflections.isEmpty()) {
            StringBuilder reflectionsText = new StringBuilder();
            for (Consequence consequence : withReflections) {
                reflectionsText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
                if (consequence.getEthicalReflections() != null && !consequence.getEthicalReflections().isEmpty()) {
                    reflectionsText.append("  *Reflection*: ").append(consequence.getEthicalReflections().get(0)).append("\n");
                }
            }
            embedBuilder.addField("Decisions with Ethical Reflections", reflectionsText.toString(), false);
        }

        // Add consequences with alternative paths
        List<Consequence> withAlternatives = dashboard.get("with_alternatives");
        if (withAlternatives != null && !withAlternatives.isEmpty()) {
            StringBuilder alternativesText = new StringBuilder();
            for (Consequence consequence : withAlternatives) {
                alternativesText.append("• **").append(consequence.getName()).append("**: ")
                        .append(consequence.getDescription()).append("\n");
                if (consequence.getAlternativePaths() != null && !consequence.getAlternativePaths().isEmpty()) {
                    alternativesText.append("  *Alternative Path*: ").append(consequence.getAlternativePaths().get(0)).append("\n");
                }
            }
            embedBuilder.addField("Decisions with Alternative Paths", alternativesText.toString(), false);
        }

        // Add community comparison
        StringBuilder communityText = new StringBuilder();
        for (Consequence consequence : dashboard.get("active")) {
            if (consequence.getCommunityChoicePercentage() != null) {
                communityText.append("• **").append(consequence.getName()).append("**: ")
                        .append(String.format("%.1f%% of players made the same choice", consequence.getCommunityChoicePercentage()))
                        .append("\n");
            }
        }
        if (communityText.length() > 0) {
            embedBuilder.addField("Community Comparison", communityText.toString(), false);
        }

        // Add footer
        embedBuilder.footer("Use /decisions enhanced to view your enhanced decision dashboard", null);

        // Reply with the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handles the reflections subcommand.
     */
    private Mono<Void> handleReflections(ChatInputInteractionEvent event, Player player) {
        // Get ethical reflections for the player
        Map<Long, List<String>> reflectionsMap = consequenceService.getEthicalReflectionsForPlayer(player.getId());
        List<Consequence> consequences = consequenceService.getConsequencesForPlayer(player.getId());

        // Create the embed
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Ethical Reflections")
                .description("Here are some reflections on your decisions:");

        // Add reflections
        if (!reflectionsMap.isEmpty()) {
            for (Map.Entry<Long, List<String>> entry : reflectionsMap.entrySet()) {
                Long consequenceId = entry.getKey();
                List<String> reflections = entry.getValue();

                // Find the consequence
                Consequence consequence = consequences.stream()
                        .filter(c -> c.getId().equals(consequenceId))
                        .findFirst()
                        .orElse(null);

                if (consequence != null && !reflections.isEmpty()) {
                    StringBuilder reflectionsText = new StringBuilder();
                    for (String reflection : reflections) {
                        reflectionsText.append("• ").append(reflection).append("\n");
                    }

                    embedBuilder.addField(consequence.getName(), reflectionsText.toString(), false);
                }
            }
        } else {
            embedBuilder.addField("No Reflections", "You don't have any ethical reflections yet.", false);
        }

        // Add footer
        embedBuilder.footer("Use /decisions reflections to view ethical reflections on your decisions", null);

        // Reply with the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handles the alternatives subcommand.
     */
    private Mono<Void> handleAlternatives(ChatInputInteractionEvent event, Player player) {
        // Get alternative paths for the player
        Map<Long, List<String>> pathsMap = consequenceService.getAlternativePathsForPlayer(player.getId());
        List<Consequence> consequences = consequenceService.getConsequencesForPlayer(player.getId());

        // Create the embed
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Alternative Paths")
                .description("Here are some paths you could have taken:");

        // Add alternative paths
        if (!pathsMap.isEmpty()) {
            for (Map.Entry<Long, List<String>> entry : pathsMap.entrySet()) {
                Long consequenceId = entry.getKey();
                List<String> paths = entry.getValue();

                // Find the consequence
                Consequence consequence = consequences.stream()
                        .filter(c -> c.getId().equals(consequenceId))
                        .findFirst()
                        .orElse(null);

                if (consequence != null && !paths.isEmpty()) {
                    StringBuilder pathsText = new StringBuilder();
                    for (String path : paths) {
                        pathsText.append("• ").append(path).append("\n");
                    }

                    embedBuilder.addField(consequence.getName(), pathsText.toString(), false);
                }
            }
        } else {
            embedBuilder.addField("No Alternative Paths", "You don't have any alternative paths yet.", false);
        }

        // Add footer
        embedBuilder.footer("Use /decisions alternatives to view alternative paths you could have taken", null);

        // Reply with the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handles the community subcommand.
     */
    private Mono<Void> handleCommunityComparison(ChatInputInteractionEvent event, Player player) {
        // Get the decision dashboard
        Map<String, List<Consequence>> dashboard = consequenceService.getDecisionDashboard(player.getId());

        // Create the embed
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Community Comparison")
                .description("Here's how your choices compare with the community:");

        // Add community comparison
        List<Consequence> allConsequences = consequenceService.getConsequencesForPlayer(player.getId());
        StringBuilder communityText = new StringBuilder();

        for (Consequence consequence : allConsequences) {
            if (consequence.getCommunityChoicePercentage() != null) {
                communityText.append("• **").append(consequence.getName()).append("**: ")
                        .append(String.format("%.1f%% of players made the same choice", consequence.getCommunityChoicePercentage()))
                        .append("\n");
                if (consequence.getChoiceMade() != null) {
                    communityText.append("  *Your choice*: ").append(consequence.getChoiceMade()).append("\n");
                }
            }
        }

        if (communityText.length() > 0) {
            embedBuilder.addField("Your Choices vs. Community", communityText.toString(), false);
        } else {
            embedBuilder.addField("No Community Data", "There's no community data available yet.", false);
        }

        // Add footer
        embedBuilder.footer("Use /decisions community to compare your choices with the community", null);

        // Reply with the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }
}
