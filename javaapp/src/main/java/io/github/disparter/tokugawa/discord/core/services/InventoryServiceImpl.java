package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import io.github.disparter.tokugawa.discord.core.models.Inventory;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.InventoryRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the InventoryService interface.
 */
@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {


    private final InventoryRepository inventoryRepository;
    private final PlayerRepository playerRepository;
    private final ItemService itemService;

    @Autowired
    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            PlayerRepository playerRepository,
            ItemService itemService) {
        this.inventoryRepository = inventoryRepository;
        this.playerRepository = playerRepository;
        this.itemService = itemService;
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Optional<Inventory> findByPlayer(Player player) {
        return inventoryRepository.findByPlayer(player);
    }

    @Override
    public Map<Item, Integer> getPlayerItems(Player player) {
        Optional<Inventory> inventoryOpt = findByPlayer(player);
        return inventoryOpt.map(Inventory::getItems).orElse(new HashMap<>());
    }

    @Override
    @Transactional
    public Inventory addItemToInventory(Player player, Item item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Get or create the player's inventory
        Inventory inventory = findByPlayer(player)
                .orElseGet(() -> createInventory(player));

        // Check if the inventory is full
        if (isInventoryFull(player) && !inventory.getItems().containsKey(item)) {
            throw new IllegalStateException("Inventory is full");
        }

        // Add the item to the inventory
        Map<Item, Integer> items = inventory.getItems();
        int currentQuantity = items.getOrDefault(item, 0);
        items.put(item, currentQuantity + quantity);

        // Update the inventory
        inventory.setLastUpdated(LocalDateTime.now());
        
        log.info("Added {} x{} to player {}'s inventory", 
                item.getName(), quantity, player.getUsername());
        
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory removeItemFromInventory(Player player, Item item, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Get the player's inventory
        Inventory inventory = findByPlayer(player)
                .orElseThrow(() -> new IllegalArgumentException("Player has no inventory"));

        // Check if the player has enough of the item
        Map<Item, Integer> items = inventory.getItems();
        int currentQuantity = items.getOrDefault(item, 0);
        
        if (currentQuantity < quantity) {
            throw new IllegalArgumentException(
                    String.format("Player only has %d of item %s", currentQuantity, item.getName()));
        }

        // Remove the item from the inventory
        if (currentQuantity == quantity) {
            items.remove(item);
        } else {
            items.put(item, currentQuantity - quantity);
        }

        // Update the inventory
        inventory.setLastUpdated(LocalDateTime.now());
        
        log.info("Removed {} x{} from player {}'s inventory", 
                item.getName(), quantity, player.getUsername());
        
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Player useItem(Player player, Item item) {
        // Check if the item is consumable
        if (!item.getConsumable()) {
            throw new IllegalArgumentException("Item is not consumable: " + item.getName());
        }

        // Check if the player has the item
        if (!hasItem(player, item)) {
            throw new IllegalArgumentException("Player does not have item: " + item.getName());
        }

        // Remove one of the item from the inventory
        removeItemFromInventory(player, item, 1);

        // Apply the item's effects to the player
        Player updatedPlayer = itemService.applyItemEffects(item, player);
        
        log.info("Player {} used item {}", player.getUsername(), item.getName());
        
        return updatedPlayer;
    }

    @Override
    public boolean hasItem(Player player, Item item) {
        Map<Item, Integer> items = getPlayerItems(player);
        return items.containsKey(item) && items.get(item) > 0;
    }

    @Override
    public int getItemQuantity(Player player, Item item) {
        Map<Item, Integer> items = getPlayerItems(player);
        return items.getOrDefault(item, 0);
    }

    @Override
    public boolean isInventoryFull(Player player) {
        Optional<Inventory> inventoryOpt = findByPlayer(player);
        
        if (inventoryOpt.isEmpty()) {
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        return inventory.getItems().size() >= inventory.getMaxCapacity();
    }

    @Override
    public int getUniqueItemCount(Player player) {
        Map<Item, Integer> items = getPlayerItems(player);
        return items.size();
    }

    @Override
    public int getTotalItemCount(Player player) {
        Map<Item, Integer> items = getPlayerItems(player);
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    @Transactional
    public Inventory createInventory(Player player) {
        // Check if the player already has an inventory
        Optional<Inventory> existingInventory = findByPlayer(player);
        
        if (existingInventory.isPresent()) {
            return existingInventory.get();
        }

        // Create a new inventory
        Inventory inventory = new Inventory();
        inventory.setPlayer(player);
        inventory.setItems(new HashMap<>());
        inventory.setMaxCapacity(50); // Default capacity
        inventory.setCreatedAt(LocalDateTime.now());
        inventory.setLastUpdated(LocalDateTime.now());
        
        log.info("Created new inventory for player {}", player.getUsername());
        
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public void delete(Inventory inventory) {
        inventoryRepository.delete(inventory);
    }
}