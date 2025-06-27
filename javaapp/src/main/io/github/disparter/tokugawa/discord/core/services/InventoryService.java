package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Inventory;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.Map;
import java.util.Optional;

/**
 * Service for managing player inventories in the game.
 */
public interface InventoryService {

    /**
     * Find an inventory by its ID.
     *
     * @param id the inventory ID
     * @return the inventory if found
     */
    Optional<Inventory> findById(Long id);

    /**
     * Find a player's inventory.
     *
     * @param player the player
     * @return the player's inventory if found
     */
    Optional<Inventory> findByPlayer(Player player);

    /**
     * Get all items in a player's inventory.
     *
     * @param player the player
     * @return a map of items to their quantities
     */
    Map<Item, Integer> getPlayerItems(Player player);

    /**
     * Add an item to a player's inventory.
     *
     * @param player the player
     * @param item the item to add
     * @param quantity the quantity to add
     * @return the updated inventory
     * @throws IllegalStateException if the inventory is full
     */
    Inventory addItemToInventory(Player player, Item item, int quantity);

    /**
     * Remove an item from a player's inventory.
     *
     * @param player the player
     * @param item the item to remove
     * @param quantity the quantity to remove
     * @return the updated inventory
     * @throws IllegalArgumentException if the player doesn't have enough of the item
     */
    Inventory removeItemFromInventory(Player player, Item item, int quantity);

    /**
     * Use an item from a player's inventory.
     *
     * @param player the player
     * @param item the item to use
     * @return the updated player
     * @throws IllegalArgumentException if the player doesn't have the item or it can't be used
     */
    Player useItem(Player player, Item item);

    /**
     * Check if a player has a specific item.
     *
     * @param player the player
     * @param item the item to check
     * @return true if the player has the item, false otherwise
     */
    boolean hasItem(Player player, Item item);

    /**
     * Get the quantity of a specific item in a player's inventory.
     *
     * @param player the player
     * @param item the item to check
     * @return the quantity of the item
     */
    int getItemQuantity(Player player, Item item);

    /**
     * Check if a player's inventory is full.
     *
     * @param player the player
     * @return true if the inventory is full, false otherwise
     */
    boolean isInventoryFull(Player player);

    /**
     * Get the number of unique items in a player's inventory.
     *
     * @param player the player
     * @return the number of unique items
     */
    int getUniqueItemCount(Player player);

    /**
     * Get the total number of items in a player's inventory.
     *
     * @param player the player
     * @return the total number of items
     */
    int getTotalItemCount(Player player);

    /**
     * Create a new inventory for a player.
     *
     * @param player the player
     * @return the new inventory
     */
    Inventory createInventory(Player player);

    /**
     * Save an inventory.
     *
     * @param inventory the inventory to save
     * @return the saved inventory
     */
    Inventory save(Inventory inventory);

    /**
     * Delete an inventory.
     *
     * @param inventory the inventory to delete
     */
    void delete(Inventory inventory);
}