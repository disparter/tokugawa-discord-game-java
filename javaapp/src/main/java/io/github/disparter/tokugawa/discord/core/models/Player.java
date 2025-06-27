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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
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
 * Entity representing a player in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    private Integer level = 1;
    private Integer exp = 0;
    private Integer points = 0;
    private Integer reputation = 0;
    private Integer health = 100;
    private Integer maxHealth = 100;
    private Integer mana = 100;
    private Integer maxMana = 100;
    private Integer currency = 0;
    private Integer powerPoints = 0;

    @ManyToOne
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_stats", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "stat_name")
    @Column(name = "stat_value")
    private Map<String, Integer> stats = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_skills", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "skill_name")
    @Column(name = "skill_value")
    private Map<String, Integer> skills = new HashMap<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_quests", joinColumns = @JoinColumn(name = "player_id"))
    private List<String> quests = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_achievements", joinColumns = @JoinColumn(name = "player_id"))
    private List<String> achievements = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_discovered_areas", joinColumns = @JoinColumn(name = "player_id"))
    private List<String> discoveredAreas = new ArrayList<>();

    private String clubId;

    @ManyToMany
    @JoinTable(
        name = "player_techniques",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "technique_id")
    )
    private Set<Technique> knownTechniques = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_daily_progress", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "progress_key")
    @Column(name = "progress_value")
    private Map<String, Object> dailyProgress = new HashMap<>();

    /**
     * Gets the username of the player.
     * This is an alias for getName() for compatibility with some parts of the code.
     * 
     * @return the player's name
     */
    public String getUsername() {
        return name;
    }

    /**
     * Gets a stat value.
     * 
     * @param statName the name of the stat
     * @return the value of the stat, or 0 if the stat doesn't exist
     */
    public Integer getStat(String statName) {
        return stats.getOrDefault(statName, 0);
    }

    /**
     * Sets a stat value.
     * 
     * @param statName the name of the stat
     * @param value the value to set
     */
    public void setStat(String statName, Integer value) {
        stats.put(statName, value);
    }

    /**
     * Gets the strength stat.
     * 
     * @return the strength value
     */
    public Integer getStrength() {
        return getStat("strength");
    }

    /**
     * Sets the strength stat.
     * 
     * @param value the value to set
     */
    public void setStrength(Integer value) {
        setStat("strength", value);
    }

    /**
     * Gets the defense stat.
     * 
     * @return the defense value
     */
    public Integer getDefense() {
        return getStat("defense");
    }

    /**
     * Sets the defense stat.
     * 
     * @param value the value to set
     */
    public void setDefense(Integer value) {
        setStat("defense", value);
    }

    /**
     * Gets the agility stat.
     * 
     * @return the agility value
     */
    public Integer getAgility() {
        return getStat("agility");
    }

    /**
     * Sets the agility stat.
     * 
     * @param value the value to set
     */
    public void setAgility(Integer value) {
        setStat("agility", value);
    }

    /**
     * Gets the intelligence stat.
     * 
     * @return the intelligence value
     */
    public Integer getIntelligence() {
        return getStat("intelligence");
    }

    /**
     * Sets the intelligence stat.
     * 
     * @param value the value to set
     */
    public void setIntelligence(Integer value) {
        setStat("intelligence", value);
    }

    /**
     * Gets the charisma stat.
     * 
     * @return the charisma value
     */
    public Integer getCharisma() {
        return getStat("charisma");
    }

    /**
     * Sets the charisma stat.
     * 
     * @param value the value to set
     */
    public void setCharisma(Integer value) {
        setStat("charisma", value);
    }

    /**
     * Gets the experience value.
     * This is an alias for getExp() for compatibility with some parts of the code.
     * 
     * @return the experience value
     */
    public Integer getExperience() {
        return exp;
    }

    /**
     * Sets the experience value.
     * This is an alias for setExp() for compatibility with some parts of the code.
     * 
     * @param value the value to set
     */
    public void setExperience(Integer value) {
        this.exp = value;
    }

    /**
     * Gets the currency value.
     * 
     * @return the currency value
     */
    public Integer getCurrency() {
        return currency;
    }

    /**
     * Sets the currency value.
     * 
     * @param value the value to set
     */
    public void setCurrency(Integer value) {
        this.currency = value;
    }
}
