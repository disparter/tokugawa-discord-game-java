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
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity representing a non-player character in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NPC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String npcId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String background;

    @Enumerated(EnumType.STRING)
    private NPCType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "npc_dialogues", joinColumns = @JoinColumn(name = "npc_id"))
    @Column(name = "dialogue", columnDefinition = "TEXT")
    private Map<String, String> dialogues = new HashMap<>();

    @OneToMany(mappedBy = "npc")
    private List<Relationship> relationships = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "npc_techniques",
        joinColumns = @JoinColumn(name = "npc_id"),
        inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    private Set<Technique> knownTechniques = new HashSet<>();

    // Location where this NPC is found
    @Column(name = "location_id")
    private Long locationId;

    // Flag to indicate if this NPC is a trader
    @Column(name = "is_trader", nullable = false)
    private Boolean isTrader = false;

    // Trading inventory and preferences for trader NPCs
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "npc_trade_items", joinColumns = @JoinColumn(name = "npc_id"))
    @Column(name = "item_id")
    private List<Long> tradeInventory = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "npc_preferred_items", joinColumns = @JoinColumn(name = "npc_id"))
    @Column(name = "item_type")
    private List<String> preferredItemTypes = new ArrayList<>();

    /**
     * Enum representing the type of NPC.
     */
    public enum NPCType {
        BASE,
        STUDENT,
        FACULTY
    }
}
