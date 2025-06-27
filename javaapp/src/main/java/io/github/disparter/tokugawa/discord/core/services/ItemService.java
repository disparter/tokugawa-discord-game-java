package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Service for managing items in the game.
 */
public interface ItemService {

    /**
     * Find an item by its ID.
     *
     * @param id the item ID
     * @return the item if found
     */
    Optional<Item> findById(Long id);

    /**
     * Find an item by its unique item ID.
     *
     * @param itemId the unique item ID
     * @return the item if found
     */
    Optional<Item> findByItemId(String itemId);

    /**
     * Find an item by its name.
     *
     * @param name the item name
     * @return the item if found
     */
    Optional<Item> findByName(String name);

    /**
     * Find all items of a specific type.
     *
     * @param type the item type
     * @return a list of items of the specified type
     */
    List<Item> findByType(Item.ItemType type);

    /**
     * Find all items with a price less than or equal to the specified value.
     *
     * @param maxPrice the maximum price
     * @return a list of items with a price less than or equal to the specified value
     */
    List<Item> findByPriceLessThanEqual(Integer maxPrice);

    /**
     * Save an item.
     *
     * @param item the item to save
     * @return the saved item
     */
    Item save(Item item);

    /**
     * Delete an item.
     *
     * @param item the item to delete
     */
    void delete(Item item);

    /**
     * Apply the effects of an item to a player.
     *
     * @param item the item to apply
     * @param player the player to apply the effects to
     * @return the updated player
     */
    Player applyItemEffects(Item item, Player player);

    /**
     * Get a map of all possible item effects and their descriptions.
     *
     * @return a map of effect names to descriptions
     */
    Map<String, String> getItemEffectDescriptions();
}