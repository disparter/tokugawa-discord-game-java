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
    public String getDescription() {
        return "View your decision dashboard";
    }

    @Override
    public List<ApplicationCommandOption> getOptions() {
        return List.of();
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // Get the Discord user ID
        String discordId = event.getInteraction().getUser().getId().asString();
        
        // Find the player by Discord ID
        Player player = playerService.findByDiscordId(discordId);
        if (player == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("You need to register first. Use the /register command.");
        }
        
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
        embedBuilder.footer("Use /decisions to view your decision dashboard", null);
        
        // Reply with the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }
}