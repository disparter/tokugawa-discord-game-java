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
        // In a real implementation, this would be stored in the database
        // For now, we'll use a simple approach where NPCs accept items based on their type
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
        // In a real implementation, this would be stored in the database
        // For now, we'll use a simple approach where NPCs offer rewards based on their type
        Map<Item, Integer> tradeRewards = new HashMap<>();

        // Get all items from the repository
        Iterable<Item> allItems = itemRepository.findAll();

        for (Item item : allItems) {
            // Calculate a trade cost based on the item's price and the NPC's preferences
            int tradeCost = calculateTradeCost(npc, item);
            if (tradeCost > 0) {
                tradeRewards.put(item, tradeCost);
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
        // In a real implementation, this would filter NPCs based on a "trader" flag
        // For now, we'll assume all NPCs can trade
        List<NPC> tradingNPCs = new ArrayList<>();
        npcRepository.findAll().forEach(tradingNPCs::add);
        return tradingNPCs;
    }

    @Override
    public List<NPC> getTradingNPCsAtLocation(Long locationId) {
        // TODO: Implement location-based NPC filtering
        // 1. Add locationId field to NPC entity
        // 2. Create NPCRepository.findByLocationIdAndIsTraderTrue(Long locationId) method
        // 3. Filter NPCs by location and trader status
        // For now, return all trading NPCs as placeholder
        return getTradingNPCs();
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

        // In a real implementation, this would be based on the NPC's preferences
        // For now, we'll use a simple approach where NPCs prefer certain item types
        double multiplier = 1.0;

        // Check NPC type
        NPC.NPCType npcType = npc.getType();
        if (npcType == null) {
            return baseValue; // Default value if NPC type is null
        }

        // Different multipliers based on NPC type
        switch (npcType) {
            case FACULTY:
                // Faculty NPCs might value educational or rare items more
                multiplier = 1.2;
                if (Item.ItemType.MATERIAL == item.getType()) {
                    multiplier = 1.5;
                }
                break;
            case STUDENT:
                // Students might value different items
                if (Item.ItemType.WEAPON == item.getType() || Item.ItemType.MATERIAL == item.getType()) {
                    multiplier = 1.3;
                } else {
                    multiplier = 0.8;
                }
                break;
            default:
                // Default: NPCs value items at their base price
                multiplier = 1.0;
                break;
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

        // In a real implementation, this would be based on the NPC's inventory
        // For now, we'll use a simple approach where NPCs offer certain item types
        double multiplier = 0.0; // Default: NPC doesn't offer the item

        // Check NPC type
        NPC.NPCType npcType = npc.getType();
        if (npcType == null) {
            return 0; // NPC doesn't offer items if type is null
        }

        // Different multipliers based on NPC type
        switch (npcType) {
            case FACULTY:
                // Faculty NPCs might offer various items at a markup
                multiplier = 1.5;
                break;
            case STUDENT:
                // Students might offer specific items
                if (Item.ItemType.WEAPON == reward.getType() || Item.ItemType.ARMOR == reward.getType()) {
                    multiplier = 1.3;
                }
                break;
            default:
                // Default: NPC doesn't offer items
                multiplier = 0.0;
                break;
        }

        return (int) (baseCost * multiplier);
    }

    // TODO: Implement proper trader flag system in NPC entity
    // Add a boolean "isTrader" field to the NPC entity and use it to filter trading NPCs
    // This would replace the current logic that assumes all NPCs can trade
}
