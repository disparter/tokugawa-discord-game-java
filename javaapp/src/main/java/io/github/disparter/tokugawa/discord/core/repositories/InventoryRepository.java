package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Inventory;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Inventory entity operations.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find an inventory by player.
     *
     * @param player the player
     * @return the inventory, if found
     */
    Optional<Inventory> findByPlayer(Player player);

    /**
     * Find inventories containing the specified item.
     *
     * @param item the item
     * @return the list of inventories containing the item
     */
    @Query("SELECT i FROM Inventory i JOIN i.items items WHERE KEY(items) = :item")
    List<Inventory> findByItemsContainsKey(@Param("item") Item item);

    /**
     * Find inventories with max capacity greater than or equal to the specified value.
     *
     * @param capacity the minimum capacity
     * @return the list of inventories with max capacity >= the specified value
     */
    List<Inventory> findByMaxCapacityGreaterThanEqual(Integer capacity);

    /**
     * Count the number of items in an inventory.
     *
     * @param player the player
     * @return the number of items in the player's inventory
     */
    @Query("SELECT COUNT(KEY(items)) FROM Inventory i JOIN i.items items WHERE i.player = :player")
    Integer countItemsByPlayer(@Param("player") Player player);

    /**
     * Find the quantity of a specific item in a player's inventory.
     *
     * @param player the player
     * @param item the item
     * @return the quantity of the item in the player's inventory
     */
    @Query("SELECT items FROM Inventory i JOIN i.items items WHERE i.player = :player AND KEY(items) = :item")
    Integer findItemQuantityByPlayerAndItem(@Param("player") Player player, @Param("item") Item item);
}