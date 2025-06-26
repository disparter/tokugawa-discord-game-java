package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * Entity representing an item in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String itemId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType type;

    private Integer price = 0;

    private Boolean tradable = true;

    private Boolean consumable = false;

    private Integer durability = -1;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_effects", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "effect_value")
    private Map<String, Integer> effects = new HashMap<>();

    /**
     * Enum representing the type of item.
     */
    public enum ItemType {
        WEAPON,
        ARMOR,
        ACCESSORY,
        CONSUMABLE,
        KEY_ITEM,
        MATERIAL,
        QUEST_ITEM
    }
}