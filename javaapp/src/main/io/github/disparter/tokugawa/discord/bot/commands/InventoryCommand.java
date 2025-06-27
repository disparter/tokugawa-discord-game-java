package io.github.disparter.tokugawa.discord.bot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.disparter.tokugawa.discord.core.models.Inventory;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.services.InventoryService;
import io.github.disparter.tokugawa.discord.core.services.ItemService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

/**
 * Command for managing a player's inventory and using items.
 */
@Component
public class InventoryCommand implements SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(InventoryCommand.class);

    private final PlayerService playerService;
    private final InventoryService inventoryService;
    private final ItemService itemService;

    @Autowired
    public InventoryCommand(
            PlayerService playerService,
            InventoryService inventoryService,
            ItemService itemService) {
        this.playerService = playerService;
        this.inventoryService = inventoryService;
        this.itemService = itemService;
    }

    @Override
    public String getName() {
        return "inventory";
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
            case "view":
                return handleViewInventory(event, player);
            case "use":
                return handleUseItem(event, player);
            case "info":
                return handleItemInfo(event, player);
            default:
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Unknown subcommand: " + subcommand);
        }
    }

    /**
     * Handle the "view" subcommand to display the player's inventory.
     */
    private Mono<Void> handleViewInventory(ChatInputInteractionEvent event, Player player) {
        // Get the player's inventory
        Map<Item, Integer> items = inventoryService.getPlayerItems(player);
        
        // Create an embed to display the inventory
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title(player.getUsername() + "'s Inventory")
                .description("Here are the items in your inventory:");
        
        if (items.isEmpty()) {
            embedBuilder.addField("Empty", "Your inventory is empty.", false);
        } else {
            // Group items by type
            Map<Item.ItemType, StringBuilder> itemsByType = new java.util.HashMap<>();
            
            for (Map.Entry<Item, Integer> entry : items.entrySet()) {
                Item item = entry.getKey();
                Integer quantity = entry.getValue();
                
                StringBuilder typeBuilder = itemsByType.computeIfAbsent(
                        item.getType(), k -> new StringBuilder());
                
                typeBuilder.append("- **").append(item.getName()).append("** (x").append(quantity).append(")\n");
            }
            
            // Add each type as a field
            for (Map.Entry<Item.ItemType, StringBuilder> entry : itemsByType.entrySet()) {
                embedBuilder.addField(entry.getKey().toString(), entry.getValue().toString(), false);
            }
        }
        
        // Add inventory stats
        int uniqueItems = inventoryService.getUniqueItemCount(player);
        int totalItems = inventoryService.getTotalItemCount(player);
        int maxCapacity = inventoryService.findByPlayer(player)
                .map(Inventory::getMaxCapacity)
                .orElse(50);
        
        embedBuilder.addField("Inventory Stats", 
                String.format("Unique Items: %d\nTotal Items: %d\nCapacity: %d/%d", 
                        uniqueItems, totalItems, uniqueItems, maxCapacity), 
                false);
        
        // Send the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }

    /**
     * Handle the "use" subcommand to use an item from the player's inventory.
     */
    private Mono<Void> handleUseItem(ChatInputInteractionEvent event, Player player) {
        // Get the item ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> itemIdOption = event.getOption("item_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        if (itemIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify an item ID.");
        }
        
        String itemId = itemIdOption.get().asString();
        
        // Find the item
        Optional<Item> itemOpt = itemService.findByItemId(itemId);
        
        if (itemOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Item not found with ID: " + itemId);
        }
        
        Item item = itemOpt.get();
        
        try {
            // Use the item
            Player updatedPlayer = inventoryService.useItem(player, item);
            
            // Create an embed to display the result
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("Item Used")
                    .description("You used " + item.getName() + ".")
                    .build();
            
            return event.reply()
                    .withEmbeds(embed);
        } catch (IllegalArgumentException e) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error: " + e.getMessage());
        }
    }

    /**
     * Handle the "info" subcommand to display information about an item.
     */
    private Mono<Void> handleItemInfo(ChatInputInteractionEvent event, Player player) {
        // Get the item ID from the command options
        Optional<ApplicationCommandInteractionOptionValue> itemIdOption = event.getOption("item_id")
                .flatMap(ApplicationCommandInteractionOption::getValue);
        
        if (itemIdOption.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please specify an item ID.");
        }
        
        String itemId = itemIdOption.get().asString();
        
        // Find the item
        Optional<Item> itemOpt = itemService.findByItemId(itemId);
        
        if (itemOpt.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Item not found with ID: " + itemId);
        }
        
        Item item = itemOpt.get();
        
        // Get the quantity of the item in the player's inventory
        int quantity = inventoryService.getItemQuantity(player, item);
        
        // Create an embed to display the item information
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title(item.getName())
                .description(item.getDescription())
                .addField("Type", item.getType().toString(), true)
                .addField("Price", item.getPrice().toString(), true)
                .addField("Quantity", Integer.toString(quantity), true)
                .addField("Tradable", item.getTradable() ? "Yes" : "No", true)
                .addField("Consumable", item.getConsumable() ? "Yes" : "No", true);
        
        // Add effects if the item has any
        if (!item.getEffects().isEmpty()) {
            StringBuilder effectsBuilder = new StringBuilder();
            Map<String, String> effectDescriptions = itemService.getItemEffectDescriptions();
            
            for (Map.Entry<String, Integer> effect : item.getEffects().entrySet()) {
                String effectName = effect.getKey();
                Integer effectValue = effect.getValue();
                String description = effectDescriptions.getOrDefault(effectName, "Unknown effect");
                
                effectsBuilder.append("- **").append(effectName).append("**: ")
                        .append(effectValue).append(" (").append(description).append(")\n");
            }
            
            embedBuilder.addField("Effects", effectsBuilder.toString(), false);
        }
        
        // Send the embed
        return event.reply()
                .withEmbeds(embedBuilder.build());
    }
}