package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Technique;
import io.github.disparter.tokugawa.discord.core.services.DuelService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.NPCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command for managing duels in the game.
 */
@Component
public class DuelCommand implements SlashCommand {

    private final PlayerService playerService;
    private final NPCService npcService;
    private final DuelService duelService;

    @Autowired
    public DuelCommand(PlayerService playerService, NPCService npcService, DuelService duelService) {
        this.playerService = playerService;
        this.npcService = npcService;
        this.duelService = duelService;
    }

    @Override
    public String getName() {
        return "duel";
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        // Get the Discord user
        User user = event.getInteraction().getUser();

        try {
            // Find the player
            Player player = playerService.findByDiscordId(user.getId().asString());

            // Get the subcommand
            String subcommand = event.getOptions().get(0).getName();

            switch (subcommand) {
                case "challenge":
                    return handleChallenge(event, player);
                case "use_technique":
                    return handleUseTechnique(event, player);
                case "view":
                    return handleViewDuel(event, player);
                case "cancel":
                    return handleCancelDuel(event, player);
                case "techniques":
                    return handleViewTechniques(event, player);
                default:
                    return event.reply()
                            .withEphemeral(true)
                            .withContent("Unknown subcommand: " + subcommand);
            }
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("You need to register first. Use /register to create your character.");
        }
    }

    private Mono<Void> handleChallenge(ChatInputInteractionEvent event, Player player) {
        // Get the NPC ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> npcIdOption = event.getOption("npc_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (npcIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify an NPC to challenge.");
        }

        try {
            // Get the NPC
            Long npcId = Long.parseLong(npcIdOption.get().asString());
            NPC npc = npcService.findById(npcId);

            // Initiate the duel
            Map<String, Object> duelState = duelService.initiateDuel(player.getId(), npcId);

            // Create an embed with the duel information
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.BLUE)
                    .title("Duel Initiated")
                    .description("You have challenged " + npc.getName() + " to a duel!")
                    .addField("Duel ID", (String) duelState.get("duelId"), false)
                    .addField("Your Health", duelState.get("playerHealth").toString(), true)
                    .addField("Opponent Health", duelState.get("npcHealth").toString(), true)
                    .addField("Your Mana", duelState.get("playerMana").toString(), true)
                    .addField("Opponent Mana", duelState.get("npcMana").toString(), true)
                    .addField("Round", duelState.get("round").toString(), false)
                    .addField("Status", (String) duelState.get("status"), false)
                    .addField("Log", (String) duelState.get("log"), false)
                    .footer("Use /duel use_technique to select a technique to use.", null)
                    .build();

            return event.reply().withEmbeds(embed);
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("NPC not found: " + e.getMessage());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error initiating duel: " + e.getMessage());
        }
    }

    private Mono<Void> handleUseTechnique(ChatInputInteractionEvent event, Player player) {
        // Get the duel ID and technique ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> duelIdOption = event.getOption("duel_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        Optional<ApplicationCommandInteractionOptionValue> techniqueIdOption = event.getOption("technique_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (duelIdOption.isEmpty() || techniqueIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify both a duel ID and a technique ID.");
        }

        try {
            // Get the duel ID and technique ID
            String duelId = duelIdOption.get().asString();
            Long techniqueId = Long.parseLong(techniqueIdOption.get().asString());

            // Process the technique selection
            Map<String, Object> duelState = duelService.processTechniqueSelection(duelId, techniqueId);

            // Create an embed with the updated duel information
            EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                    .color(Color.BLUE)
                    .title("Duel Update")
                    .addField("Duel ID", (String) duelState.get("duelId"), false)
                    .addField("Your Health", duelState.get("playerHealth").toString(), true)
                    .addField("Opponent Health", duelState.get("npcHealth").toString(), true)
                    .addField("Your Mana", duelState.get("playerMana").toString(), true)
                    .addField("Opponent Mana", duelState.get("npcMana").toString(), true)
                    .addField("Round", duelState.get("round").toString(), false)
                    .addField("Status", (String) duelState.get("status"), false)
                    .addField("Log", (String) duelState.get("log"), false);

            // Add result information if the duel is completed
            if ("COMPLETED".equals(duelState.get("status"))) {
                boolean playerWon = (boolean) duelState.get("playerWon");
                embedBuilder.description(playerWon ? "You won the duel!" : "You lost the duel!")
                        .addField("Reputation Change", duelState.get("reputationChange").toString(), true)
                        .addField("Relationship Change", duelState.get("relationshipChange").toString(), true)
                        .color(playerWon ? Color.GREEN : Color.RED);
            } else {
                embedBuilder.description("The duel continues!")
                        .footer("Use /duel use_technique to select your next technique.", null);
            }

            return event.reply().withEmbeds(embedBuilder.build());
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Invalid duel or technique: " + e.getMessage());
        } catch (IllegalStateException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error processing technique: " + e.getMessage());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error processing technique: " + e.getMessage());
        }
    }

    private Mono<Void> handleViewDuel(ChatInputInteractionEvent event, Player player) {
        // Get the duel ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> duelIdOption = event.getOption("duel_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (duelIdOption.isEmpty()) {
            // If no duel ID is provided, list all active duels
            List<String> activeDuels = duelService.getActivePlayerDuels(player.getId());

            if (activeDuels.isEmpty()) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("You have no active duels.");
            }

            String duelsList = activeDuels.stream()
                    .collect(Collectors.joining("\n"));

            return event.reply()
                    .withEphemeral(true)
                    .withContent("Your active duels:\n" + duelsList);
        }

        try {
            // Get the duel ID
            String duelId = duelIdOption.get().asString();

            // Get the duel state
            Map<String, Object> duelState = duelService.processDuelResult(duelId);

            // Create an embed with the duel information
            EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                    .color(Color.BLUE)
                    .title("Duel Information")
                    .addField("Duel ID", (String) duelState.get("duelId"), false)
                    .addField("Your Health", duelState.get("playerHealth").toString(), true)
                    .addField("Opponent Health", duelState.get("npcHealth").toString(), true)
                    .addField("Your Mana", duelState.get("playerMana").toString(), true)
                    .addField("Opponent Mana", duelState.get("npcMana").toString(), true)
                    .addField("Round", duelState.get("round").toString(), false)
                    .addField("Status", (String) duelState.get("status"), false)
                    .addField("Log", (String) duelState.get("log"), false);

            // Add result information if the duel is completed
            if ("COMPLETED".equals(duelState.get("status"))) {
                boolean playerWon = (boolean) duelState.get("playerWon");
                embedBuilder.description(playerWon ? "You won the duel!" : "You lost the duel!")
                        .addField("Reputation Change", duelState.get("reputationChange").toString(), true)
                        .addField("Relationship Change", duelState.get("relationshipChange").toString(), true)
                        .color(playerWon ? Color.GREEN : Color.RED);
            } else {
                embedBuilder.description("The duel is in progress.")
                        .footer("Use /duel use_technique to select a technique to use.", null);
            }

            return event.reply().withEmbeds(embedBuilder.build());
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Duel not found: " + e.getMessage());
        } catch (IllegalStateException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error viewing duel: " + e.getMessage());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error viewing duel: " + e.getMessage());
        }
    }

    private Mono<Void> handleCancelDuel(ChatInputInteractionEvent event, Player player) {
        // Get the duel ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> duelIdOption = event.getOption("duel_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);

        if (duelIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify a duel ID to cancel.");
        }

        try {
            // Get the duel ID
            String duelId = duelIdOption.get().asString();

            // Cancel the duel
            boolean canceled = duelService.cancelDuel(duelId);

            if (canceled) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Duel canceled successfully.");
            } else {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Could not cancel the duel. It may already be completed.");
            }
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Duel not found: " + e.getMessage());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error canceling duel: " + e.getMessage());
        }
    }

    private Mono<Void> handleViewTechniques(ChatInputInteractionEvent event, Player player) {
        try {
            // Get the player's techniques
            List<Technique> techniques = duelService.getPlayerTechniques(player.getId());

            if (techniques.isEmpty()) {
                return event.reply()
                        .withEphemeral(true)
                        .withContent("You don't know any techniques yet.");
            }

            // Create an embed with the techniques information
            EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                    .color(Color.BLUE)
                    .title("Your Techniques")
                    .description("Here are the techniques you know:");

            // Add each technique to the embed
            for (Technique technique : techniques) {
                embedBuilder.addField(
                        technique.getName() + " (ID: " + technique.getId() + ")",
                        "Type: " + technique.getType() + "\n" +
                        "Description: " + technique.getDescription() + "\n" +
                        "Damage: " + technique.getBaseDamage() + "\n" +
                        "Mana Cost: " + technique.getManaCost(),
                        false
                );
            }

            return event.reply().withEmbeds(embedBuilder.build());
        } catch (Exception e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error viewing techniques: " + e.getMessage());
        }
    }
}
