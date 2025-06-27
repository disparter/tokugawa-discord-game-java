package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.ItemRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the ItemService interface.
 */
@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemRepository itemRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, PlayerRepository playerRepository) {
        this.itemRepository = itemRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public Optional<Item> findByItemId(String itemId) {
        return itemRepository.findByItemId(itemId);
    }

    @Override
    public Optional<Item> findByName(String name) {
        return itemRepository.findByName(name);
    }

    @Override
    public List<Item> findByType(Item.ItemType type) {
        return itemRepository.findByType(type);
    }

    @Override
    public List<Item> findByPriceLessThanEqual(Integer maxPrice) {
        return itemRepository.findByPriceLessThanEqual(maxPrice);
    }

    @Override
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public void delete(Item item) {
        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public Player applyItemEffects(Item item, Player player) {
        logger.info("Applying effects of item {} to player {}", item.getName(), player.getUsername());
        
        // Apply each effect from the item
        for (Map.Entry<String, Integer> effect : item.getEffects().entrySet()) {
            applyEffect(player, effect.getKey(), effect.getValue());
        }
        
        // Save and return the updated player
        return playerRepository.save(player);
    }

    /**
     * Apply a specific effect to a player.
     *
     * @param player the player to apply the effect to
     * @param effectType the type of effect
     * @param value the value of the effect
     */
    private void applyEffect(Player player, String effectType, Integer value) {
        switch (effectType) {
            case "health":
                int newHealth = player.getHealth() + value;
                player.setHealth(Math.min(newHealth, player.getMaxHealth()));
                logger.info("Applied health effect of {} to player {}, new health: {}", 
                        value, player.getUsername(), player.getHealth());
                break;
                
            case "mana":
                int newMana = player.getMana() + value;
                player.setMana(Math.min(newMana, player.getMaxMana()));
                logger.info("Applied mana effect of {} to player {}, new mana: {}", 
                        value, player.getUsername(), player.getMana());
                break;
                
            case "strength":
                player.setStrength(player.getStrength() + value);
                logger.info("Applied strength effect of {} to player {}, new strength: {}", 
                        value, player.getUsername(), player.getStrength());
                break;
                
            case "defense":
                player.setDefense(player.getDefense() + value);
                logger.info("Applied defense effect of {} to player {}, new defense: {}", 
                        value, player.getUsername(), player.getDefense());
                break;
                
            case "agility":
                player.setAgility(player.getAgility() + value);
                logger.info("Applied agility effect of {} to player {}, new agility: {}", 
                        value, player.getUsername(), player.getAgility());
                break;
                
            case "intelligence":
                player.setIntelligence(player.getIntelligence() + value);
                logger.info("Applied intelligence effect of {} to player {}, new intelligence: {}", 
                        value, player.getUsername(), player.getIntelligence());
                break;
                
            case "max_health":
                player.setMaxHealth(player.getMaxHealth() + value);
                logger.info("Applied max health effect of {} to player {}, new max health: {}", 
                        value, player.getUsername(), player.getMaxHealth());
                break;
                
            case "max_mana":
                player.setMaxMana(player.getMaxMana() + value);
                logger.info("Applied max mana effect of {} to player {}, new max mana: {}", 
                        value, player.getUsername(), player.getMaxMana());
                break;
                
            default:
                logger.warn("Unknown effect type: {}", effectType);
                break;
        }
    }

    @Override
    public Map<String, String> getItemEffectDescriptions() {
        Map<String, String> effectDescriptions = new HashMap<>();
        
        effectDescriptions.put("health", "Restores health points");
        effectDescriptions.put("mana", "Restores mana points");
        effectDescriptions.put("strength", "Increases strength attribute");
        effectDescriptions.put("defense", "Increases defense attribute");
        effectDescriptions.put("agility", "Increases agility attribute");
        effectDescriptions.put("intelligence", "Increases intelligence attribute");
        effectDescriptions.put("max_health", "Increases maximum health");
        effectDescriptions.put("max_mana", "Increases maximum mana");
        
        return effectDescriptions;
    }
}