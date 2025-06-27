package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;

import java.util.List;
import java.util.Map;

/**
 * Service for managing trades in the game.
 */
public interface TradeService {

    /**
     * Get all items that an NPC is willing to trade.
     *
     * @param npc the NPC
     * @return a map of items to their trade values
     */
    Map<Item, Integer> getNPCTradeItems(NPC npc);

    /**
     * Get all rewards that an NPC is offering for trades.
     *
     * @param npc the NPC
     * @return a map of reward items to their trade costs
     */
    Map<Item, Integer> getNPCTradeRewards(NPC npc);

    /**
     * Trade an item from a player to an NPC in exchange for a reward.
     *
     * @param player the player
     * @param npc the NPC
     * @param playerItem the item the player is trading
     * @param npcReward the reward the player is receiving
     * @param quantity the quantity of the item being traded
     * @return the updated player
     * @throws IllegalArgumentException if the trade is not valid
     */
    Player tradeItemForReward(Player player, NPC npc, Item playerItem, Item npcReward, int quantity);

    /**
     * Check if a trade is valid.
     *
     * @param player the player
     * @param npc the NPC
     * @param playerItem the item the player is trading
     * @param npcReward the reward the player is receiving
     * @param quantity the quantity of the item being traded
     * @return true if the trade is valid, false otherwise
     */
    boolean isTradeValid(Player player, NPC npc, Item playerItem, Item npcReward, int quantity);

    /**
     * Get the trade value of an item for an NPC.
     *
     * @param npc the NPC
     * @param item the item
     * @return the trade value of the item, or 0 if the NPC doesn't accept the item
     */
    int getItemTradeValue(NPC npc, Item item);

    /**
     * Get the trade cost of a reward from an NPC.
     *
     * @param npc the NPC
     * @param reward the reward
     * @return the trade cost of the reward, or -1 if the NPC doesn't offer the reward
     */
    int getRewardTradeCost(NPC npc, Item reward);

    /**
     * Get all NPCs that are willing to trade.
     *
     * @return a list of NPCs that are willing to trade
     */
    List<NPC> getTradingNPCs();

    /**
     * Get all NPCs at a specific location that are willing to trade.
     *
     * @param locationId the location ID
     * @return a list of NPCs at the location that are willing to trade
     */
    List<NPC> getTradingNPCsAtLocation(Long locationId);

    /**
     * Get all items that a player can trade to any NPC.
     *
     * @param player the player
     * @return a map of items to a map of NPCs to trade values
     */
    Map<Item, Map<NPC, Integer>> getPlayerTradeableItems(Player player);

    /**
     * Get all rewards that a player can receive from any NPC.
     *
     * @param player the player
     * @return a map of rewards to a map of NPCs to trade costs
     */
    Map<Item, Map<NPC, Integer>> getPlayerAvailableRewards(Player player);
}