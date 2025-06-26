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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

    private String clubId;
}
