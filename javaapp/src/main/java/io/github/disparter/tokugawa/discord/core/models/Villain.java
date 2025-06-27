package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity representing a villain in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Villain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private Integer level = 1;
    private Integer health = 100;
    private Integer maxHealth = 100;
    private Integer mana = 100;
    private Integer maxMana = 100;
    private Integer power = 10;
    private Integer defense = 5;

    @Enumerated(EnumType.STRING)
    private VillainType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "villain_abilities", joinColumns = @JoinColumn(name = "villain_id"))
    private List<String> abilities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "villain_rewards", joinColumns = @JoinColumn(name = "villain_id"))
    @MapKeyColumn(name = "reward_type")
    @Column(name = "reward_value")
    private Map<String, Integer> rewards = new HashMap<>();

    @ManyToMany
    @JoinTable(
        name = "villain_techniques",
        joinColumns = @JoinColumn(name = "villain_id"),
        inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    private Set<Technique> knownTechniques = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "villain_minions", joinColumns = @JoinColumn(name = "villain_id"))
    private List<Long> minionIds = new ArrayList<>();

    private Boolean active = true;
    private String spawnLocation;
    private String spawnCondition;
    private String defeatCondition;

    /**
     * Enum representing the type of villain.
     */
    public enum VillainType {
        BOSS,
        ELITE,
        REGULAR,
        MINION
    }
}