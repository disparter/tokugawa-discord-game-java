package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ItemRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the TradeService interface.
 */
@Service
@Slf4j
public class TradeServiceImpl implements TradeService {

    private final NPCRepository npcRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    @Autowired
    public TradeServiceImpl(
            NPCRepository npcRepository,
            ItemRepository itemRepository,
            InventoryService inventoryService) {
        this.npcRepository = npcRepository;
        this.itemRepository = itemRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public Map<Item, Integer> getNPCTradeItems(NPC npc) {
        // Get items that the NPC will accept for trade based on their preferences
        Map<Item, Integer> tradeItems = new HashMap<>();

        // Get all items from the repository
        Iterable<Item> allItems = itemRepository.findAll();

        for (Item item : allItems) {
            // Only tradable items can be traded
            if (item.getTradable()) {
                // Calculate a trade value based on the item's price and the NPC's preferences
                int tradeValue = calculateTradeValue(npc, item);
                if (tradeValue > 0) {
                    tradeItems.put(item, tradeValue);
                }
            }
        }

        return tradeItems;
    }

    @Override
    public Map<Item, Integer> getNPCTradeRewards(NPC npc) {
        // Get items that the NPC offers as rewards based on their inventory and type
        Map<Item, Integer> tradeRewards = new HashMap<>();

        // If NPC has a specific trade inventory, use that
        if (npc.getTradeInventory() != null && !npc.getTradeInventory().isEmpty()) {
            for (Long itemId : npc.getTradeInventory()) {
                itemRepository.findById(itemId).ifPresent(item -> {
                    int tradeCost = calculateTradeCost(npc, item);
                    if (tradeCost > 0) {
                        tradeRewards.put(item, tradeCost);
                    }
                });
            }
        } else {
            // Fall back to all items with cost calculation
            Iterable<Item> allItems = itemRepository.findAll();
            for (Item item : allItems) {
                int tradeCost = calculateTradeCost(npc, item);
                if (tradeCost > 0) {
                    tradeRewards.put(item, tradeCost);
                }
            }
        }

        return tradeRewards;
    }

    @Override
    @Transactional
    public Player tradeItemForReward(Player player, NPC npc, Item playerItem, Item npcReward, int quantity) {
        // Check if the trade is valid
        if (!isTradeValid(player, npc, playerItem, npcReward, quantity)) {
            throw new IllegalArgumentException("Invalid trade");
        }

        // Get the trade value and cost
        int tradeValue = getItemTradeValue(npc, playerItem) * quantity;
        int tradeCost = getRewardTradeCost(npc, npcReward);

        // Check if the player is offering enough value
        if (tradeValue < tradeCost) {
            throw new IllegalArgumentException(
                    String.format("Not enough value: %d < %d", tradeValue, tradeCost));
        }

        // Remove the item from the player's inventory
        inventoryService.removeItemFromInventory(player, playerItem, quantity);

        // Add the reward to the player's inventory
        inventoryService.addItemToInventory(player, npcReward, 1);

        log.info("Player {} traded {} x{} to NPC {} for {}", 
                player.getUsername(), playerItem.getName(), quantity, npc.getName(), npcReward.getName());

        return player;
    }

    @Override
    public boolean isTradeValid(Player player, NPC npc, Item playerItem, Item npcReward, int quantity) {
        // Check if the player has enough of the item
        if (inventoryService.getItemQuantity(player, playerItem) < quantity) {
            return false;
        }

        // Check if the item is tradable
        if (!playerItem.getTradable()) {
            return false;
        }

        // Check if the NPC accepts the item
        if (getItemTradeValue(npc, playerItem) <= 0) {
            return false;
        }

        // Check if the NPC offers the reward
        if (getRewardTradeCost(npc, npcReward) <= 0) {
            return false;
        }

        // Check if the player is offering enough value
        int tradeValue = getItemTradeValue(npc, playerItem) * quantity;
        int tradeCost = getRewardTradeCost(npc, npcReward);

        return tradeValue >= tradeCost;
    }

    @Override
    public int getItemTradeValue(NPC npc, Item item) {
        return calculateTradeValue(npc, item);
    }

    @Override
    public int getRewardTradeCost(NPC npc, Item reward) {
        return calculateTradeCost(npc, reward);
    }

    @Override
    public List<NPC> getTradingNPCs() {
        // Filter NPCs based on the trader flag
        return npcRepository.findByIsTraderTrue();
    }

    @Override
    public List<NPC> getTradingNPCsAtLocation(Long locationId) {
        // Filter NPCs by location and trader status using the repository method
        return npcRepository.findByLocationIdAndIsTraderTrue(locationId);
    }

    @Override
    public Map<Item, Map<NPC, Integer>> getPlayerTradeableItems(Player player) {
        Map<Item, Map<NPC, Integer>> tradeableItems = new HashMap<>();

        // Get all items in the player's inventory
        Map<Item, Integer> playerItems = inventoryService.getPlayerItems(player);

        // Get all trading NPCs
        List<NPC> tradingNPCs = getTradingNPCs();

        // For each item in the player's inventory
        for (Item item : playerItems.keySet()) {
            // Only include tradable items
            if (item.getTradable()) {
                Map<NPC, Integer> npcValues = new HashMap<>();

                // For each trading NPC
                for (NPC npc : tradingNPCs) {
                    // Get the trade value of the item for this NPC
                    int tradeValue = getItemTradeValue(npc, item);

                    // If the NPC accepts the item, add it to the map
                    if (tradeValue > 0) {
                        npcValues.put(npc, tradeValue);
                    }
                }

                // If at least one NPC accepts the item, add it to the map
                if (!npcValues.isEmpty()) {
                    tradeableItems.put(item, npcValues);
                }
            }
        }

        return tradeableItems;
    }

    @Override
    public Map<Item, Map<NPC, Integer>> getPlayerAvailableRewards(Player player) {
        Map<Item, Map<NPC, Integer>> availableRewards = new HashMap<>();

        // Get all items
        Iterable<Item> allItems = itemRepository.findAll();

        // Get all trading NPCs
        List<NPC> tradingNPCs = getTradingNPCs();

        // For each item
        for (Item item : allItems) {
            Map<NPC, Integer> npcCosts = new HashMap<>();

            // For each trading NPC
            for (NPC npc : tradingNPCs) {
                // Get the trade cost of the item for this NPC
                int tradeCost = getRewardTradeCost(npc, item);

                // If the NPC offers the item, add it to the map
                if (tradeCost > 0) {
                    npcCosts.put(npc, tradeCost);
                }
            }

            // If at least one NPC offers the item, add it to the map
            if (!npcCosts.isEmpty()) {
                availableRewards.put(item, npcCosts);
            }
        }

        return availableRewards;
    }

    /**
     * Calculate the trade value of an item for an NPC.
     *
     * @param npc the NPC
     * @param item the item
     * @return the trade value of the item
     */
    private int calculateTradeValue(NPC npc, Item item) {
        // Base value is the item's price
        int baseValue = item.getPrice();

        // Apply NPC preference multiplier
        double preferenceMultiplier = getPreferenceMultiplier(npc, item);
        double multiplier = preferenceMultiplier;

        // Check NPC type for additional modifiers
        NPC.NPCType npcType = npc.getType();
        if (npcType != null) {
            switch (npcType) {
                case FACULTY:
                    // Faculty NPCs might value educational or rare items more
                    multiplier *= 1.2;
                    if (Item.ItemType.MATERIAL == item.getType()) {
                        multiplier *= 1.2; // Additional bonus for materials
                    }
                    break;
                case STUDENT:
                    // Students might value different items
                    if (Item.ItemType.WEAPON == item.getType() || Item.ItemType.MATERIAL == item.getType()) {
                        multiplier *= 1.3;
                    } else {
                        multiplier *= 0.8;
                    }
                    break;
                default:
                    // Default: NPCs value items at their base price
                    break;
            }
        }

        return (int) (baseValue * multiplier);
    }

    /**
     * Calculate the trade cost of a reward for an NPC.
     *
     * @param npc the NPC
     * @param reward the reward
     * @return the trade cost of the reward
     */
    private int calculateTradeCost(NPC npc, Item reward) {
        // Base cost is the item's price
        int baseCost = reward.getPrice();

        // Check if item is in NPC's trade inventory (if they have one)
        if (npc.getTradeInventory() != null && !npc.getTradeInventory().isEmpty()) {
            if (!npc.getTradeInventory().contains(reward.getId())) {
                return 0; // NPC doesn't offer this item
            }
        }

        // Apply preference-based pricing
        double preferenceMultiplier = getPreferenceMultiplier(npc, reward);
        double multiplier = 1.0 / preferenceMultiplier; // Inverse for cost (preferred items cost less)

        // Check NPC type for base pricing strategy
        NPC.NPCType npcType = npc.getType();
        if (npcType != null) {
            switch (npcType) {
                case FACULTY:
                    // Faculty NPCs offer items at a markup
                    multiplier *= 1.5;
                    break;
                case STUDENT:
                    // Students offer specific items at moderate prices
                    if (Item.ItemType.WEAPON == reward.getType() || Item.ItemType.ARMOR == reward.getType()) {
                        multiplier *= 1.3;
                    } else {
                        multiplier *= 1.1;
                    }
                    break;
                default:
                    // Base NPCs offer items at standard rates
                    multiplier *= 1.2;
                    break;
            }
        } else {
            return 0; // NPC doesn't offer items if type is null
        }

        return (int) (baseCost * multiplier);
    }

    /**
     * Get the trade value multiplier based on NPC preferences and item types.
     * This method could be expanded to include more sophisticated trading logic.
     * 
     * @param npc the NPC
     * @param item the item
     * @return the preference multiplier
     */
    private double getPreferenceMultiplier(NPC npc, Item item) {
        // Check if the NPC has preferred item types
        if (npc.getPreferredItemTypes() != null && !npc.getPreferredItemTypes().isEmpty()) {
            String itemType = item.getType() != null ? item.getType().toString() : "";
            if (npc.getPreferredItemTypes().contains(itemType.toLowerCase())) {
                return 1.5; // 50% bonus for preferred items
            }
        }
        return 1.0; // No preference bonus
    }
}
