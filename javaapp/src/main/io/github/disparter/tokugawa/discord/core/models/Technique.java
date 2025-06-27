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
import jakarta.persistence.ManyToMany;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity representing a technique that can be used in duels.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Technique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String techniqueId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private TechniqueType type;

    private Integer powerPoints;

    private Integer cooldown = 0;

    private Integer manaCost = 0;

    private Integer baseDamage = 0;

    private Boolean learnable = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technique_effects", joinColumns = @JoinColumn(name = "technique_id"))
    @Column(name = "effect_value")
    private Map<String, Integer> effects = new HashMap<>();

    @ManyToMany(mappedBy = "knownTechniques")
    private Set<Player> players = new HashSet<>();

    @ManyToMany(mappedBy = "knownTechniques")
    private Set<NPC> npcs = new HashSet<>();

    /**
     * Enum representing the type of technique.
     */
    public enum TechniqueType {
        ATTACK,
        DEFENSE,
        SUPPORT,
        SPECIAL,
        ULTIMATE
    }
}