package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Entity representing a relationship between a player and an NPC.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player player;

    @ManyToOne
    private NPC npc;

    @Column(nullable = false)
    private Integer affinity = 0;

    @Enumerated(EnumType.STRING)
    private RelationshipStatus status = RelationshipStatus.STRANGER;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "relationship_events", joinColumns = @JoinColumn(name = "relationship_id"))
    private List<String> triggeredEvents = new ArrayList<>();

    /**
     * Enum representing the status of a relationship.
     */
    public enum RelationshipStatus {
        STRANGER,
        ACQUAINTANCE,
        FRIEND,
        CLOSE_FRIEND,
        BEST_FRIEND,
        CRUSH,
        DATING,
        COMMITTED,
        RIVAL,
        ENEMY
    }
}