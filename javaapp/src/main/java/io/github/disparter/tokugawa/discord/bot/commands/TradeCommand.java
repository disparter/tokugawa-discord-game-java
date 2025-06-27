package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.ItemService;
import io.github.disparter.tokugawa.discord.core.services.NPCService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command for trading items with NPCs.
 */
@Component
public class TradeCommand implements SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(TradeCommand.class);

    private final PlayerService playerService;
    private final NPCService npcService;
    private final ItemService itemService;
    private final TradeService tradeService;

    @Autowired
    public TradeCommand(
            PlayerService playerService,
            NPCService npcService,
            ItemService itemService,
            TradeService tradeService) {
        this.playerService = playerService;
        this.npcService = npcService;
        this.itemService = itemService;
        this.tradeService = tradeService;
    }

    @Override
    public String getName() {
        return "trade";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // Get the Discord user ID
        String discordId = event.getInteraction().getUser().getId().asString();
        
        // Find the player
        Optional<Player> playerOpt = playerService.findByDiscordId(discordId);
        
        if (playerOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("You need to register first! Use /register to create a character.");
        }
        
        Player player = playerOpt.get();
        
        // Get the subcommand
        String subcommand = event.getOptions().get(0).getName();
        
        switch (subcommand) {
            case "list_traders":
                return handleListTraders(event, player);
            case "view_trades":
                return handleViewTrades(event, player);
            case "trade":
                return handleTrade(event, player);
            default:
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Unknown subcommand: " + subcommand);
        }
    }

    /**
     * Handle the "list_traders" subcommand to display all NPCs that are willing to trade.
     */
    private Mono<Void> handleListTraders(ChatInputInteractionEvent event, Player player) {
        // Get the location ID from the command options (optional)
        Optional<ApplicationCommandInteractionOptionValue> locationIdOption = event.getOption("location_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        List<NPC> tradingNPCs;
        
        if (locationIdOption.isPresent()) {
            // Get trading NPCs at the specified location
            Long locationId = Long.parseLong(locationIdOption.get().asString());
            tradingNPCs = tradeService.getTradingNPCsAtLocation(locationId);
        } else {
            // Get all trading NPCs
            tradingNPCs = tradeService.getTradingNPCs();
        }
        
        // Create an embed to display the trading NPCs
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Trading NPCs")
                .description("Here are the NPCs that are willing to trade:");
        
        if (tradingNPCs.isEmpty()) {
            embedBuilder.addField("No Traders", "There are no NPCs willing to trade at the moment.", false);
        } else {
            // Group NPCs by type
            Map<String, StringBuilder> npcsByType = new java.util.HashMap<>();
            
            for (NPC npc : tradingNPCs) {
                StringBuilder typeBuilder = npcsByType.computeIfAbsent(
                        npc.getType(), k -> new StringBuilder());
                
                typeBuilder.append("- **").append(npc.getName()).append("** (ID: ")
                        .append(npc.getId()).append(")\n");
            }
            
            // Add each type as a field
            for (Map.Entry<String, StringBuilder> entry : npcsByType.entrySet()) {
                embedBuilder.addField(entry.getKey(), entry.getValue().toString(), false);
            }
        }
        
        // Send the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handle the "view_trades" subcommand to display the trades offered by an NPC.
     */
    private Mono<Void> handleViewTrades(ChatInputInteractionEvent event, Player player) {
        // Get the NPC ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> npcIdOption = event.getOption("npc_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        if (npcIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify an NPC ID.");
        }
        
        Long npcId = Long.parseLong(npcIdOption.get().asString());
        
        // Find the NPC
        Optional<NPC> npcOpt = npcService.findById(npcId);
        
        if (npcOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("NPC not found with ID: " + npcId);
        }
        
        NPC npc = npcOpt.get();
        
        // Get the items the NPC is willing to accept and the rewards they offer
        Map<Item, Integer> tradeItems = tradeService.getNPCTradeItems(npc);
        Map<Item, Integer> tradeRewards = tradeService.getNPCTradeRewards(npc);
        
        // Create an embed to display the trades
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Trades with " + npc.getName())
                .description(npc.getName() + " is offering the following trades:");
        
        // Add the items the NPC is willing to accept
        if (tradeItems.isEmpty()) {
            embedBuilder.addField("Items Accepted", "This NPC doesn't accept any items.", false);
        } else {
            StringBuilder itemsBuilder = new StringBuilder();
            
            for (Map.Entry<Item, Integer> entry : tradeItems.entrySet()) {
                Item item = entry.getKey();
                Integer value = entry.getValue();
                
                itemsBuilder.append("- **").append(item.getName()).append("** (Value: ")
                        .append(value).append(")\n");
            }
            
            embedBuilder.addField("Items Accepted", itemsBuilder.toString(), false);
        }
        
        // Add the rewards the NPC is offering
        if (tradeRewards.isEmpty()) {
            embedBuilder.addField("Rewards Offered", "This NPC doesn't offer any rewards.", false);
        } else {
            StringBuilder rewardsBuilder = new StringBuilder();
            
            for (Map.Entry<Item, Integer> entry : tradeRewards.entrySet()) {
                Item reward = entry.getKey();
                Integer cost = entry.getValue();
                
                rewardsBuilder.append("- **").append(reward.getName()).append("** (Cost: ")
                        .append(cost).append(")\n");
            }
            
            embedBuilder.addField("Rewards Offered", rewardsBuilder.toString(), false);
        }
        
        // Send the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handle the "trade" subcommand to execute a trade with an NPC.
     */
    private Mono<Void> handleTrade(ChatInputInteractionEvent event, Player player) {
        // Get the NPC ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> npcIdOption = event.getOption("npc_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        if (npcIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify an NPC ID.");
        }
        
        Long npcId = Long.parseLong(npcIdOption.get().asString());
        
        // Get the player item ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> playerItemIdOption = event.getOption("player_item_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        if (playerItemIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify a player item ID.");
        }
        
        String playerItemId = playerItemIdOption.get().asString();
        
        // Get the NPC reward ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> npcRewardIdOption = event.getOption("npc_reward_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        if (npcRewardIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify an NPC reward ID.");
        }
        
        String npcRewardId = npcRewardIdOption.get().asString();
        
        // Get the quantity from the command options
        Optional<ApplicationCommandInteractionOptionValue> quantityOption = event.getOption("quantity")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        int quantity = quantityOption.map(value -> (int) value.asLong()).orElse(1);
        
        // Find the NPC
        Optional<NPC> npcOpt = npcService.findById(npcId);
        
        if (npcOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("NPC not found with ID: " + npcId);
        }
        
        NPC npc = npcOpt.get();
        
        // Find the player item
        Optional<Item> playerItemOpt = itemService.findByItemId(playerItemId);
        
        if (playerItemOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Player item not found with ID: " + playerItemId);
        }
        
        Item playerItem = playerItemOpt.get();
        
        // Find the NPC reward
        Optional<Item> npcRewardOpt = itemService.findByItemId(npcRewardId);
        
        if (npcRewardOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("NPC reward not found with ID: " + npcRewardId);
        }
        
        Item npcReward = npcRewardOpt.get();
        
        try {
            // Execute the trade
            Player updatedPlayer = tradeService.tradeItemForReward(player, npc, playerItem, npcReward, quantity);
            
            // Create an embed to display the result
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("Trade Successful")
                    .description("You traded " + quantity + "x " + playerItem.getName() + 
                            " to " + npc.getName() + " for " + npcReward.getName() + ".")
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