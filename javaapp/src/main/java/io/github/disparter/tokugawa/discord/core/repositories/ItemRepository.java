package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Item entity operations.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find an item by its item ID.
     *
     * @param itemId the item ID
     * @return the item, if found
     */
    Optional<Item> findByItemId(String itemId);

    /**
     * Find an item by name.
     *
     * @param name the item name
     * @return the item, if found
     */
    Optional<Item> findByName(String name);

    /**
     * Find items by type.
     *
     * @param type the item type
     * @return the list of items of the specified type
     */
    List<Item> findByType(Item.ItemType type);

    /**
     * Find items by price less than or equal to the specified value.
     *
     * @param price the maximum price
     * @return the list of items with price <= the specified value
     */
    List<Item> findByPriceLessThanEqual(Integer price);

    /**
     * Find items by tradable flag.
     *
     * @param tradable the tradable flag
     * @return the list of items with the specified tradable flag
     */
    List<Item> findByTradable(Boolean tradable);

    /**
     * Find items by consumable flag.
     *
     * @param consumable the consumable flag
     * @return the list of items with the specified consumable flag
     */
    List<Item> findByConsumable(Boolean consumable);

    /**
     * Find items by name containing the given text.
     *
     * @param name the name to search for
     * @return the list of items with matching names
     */
    List<Item> findByNameContainingIgnoreCase(String name);
}