package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity representing a club in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String clubId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String leaderId;

    @Column(nullable = false)
    private Integer reputation = 0;

    @Column(nullable = false)
    private Integer ranking = 0;

    @Enumerated(EnumType.STRING)
    private ClubType type = ClubType.GENERAL;

    @OneToMany(mappedBy = "club")
    private List<Player> members = new ArrayList<>();

    @ManyToMany
    private List<Club> alliances = new ArrayList<>();

    @ManyToMany
    private List<Club> rivalries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "club_activities", joinColumns = @JoinColumn(name = "club_id"))
    private List<String> activities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "club_competition_results", joinColumns = @JoinColumn(name = "club_id"))
    private Map<String, Integer> competitionResults = new HashMap<>();

    /**
     * Enum representing the type of club.
     */
    public enum ClubType {
        GENERAL,
        SPORTS,
        ACADEMIC,
        ARTS,
        SOCIAL,
        SPECIAL
    }
}
