package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration for romance routes in the game.
 * This replaces hardcoded romance route data with configurable database entities.
 */
@Entity
@Table(name = "romance_route_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RomanceRouteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The NPC ID this romance route is for.
     */
    @Column(name = "npc_id", nullable = false, unique = true)
    private Long npcId;

    /**
     * The minimum affinity level required to start this romance route.
     */
    @Column(name = "required_affinity", nullable = false)
    private Integer requiredAffinity;

    /**
     * The sequence of event IDs that make up this romance route, in order.
     * Stored as comma-separated values in the database.
     */
    @Column(name = "chapter_sequence", columnDefinition = "TEXT")
    private String chapterSequence;

    /**
     * Whether this romance route is currently active/enabled.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Optional description of this romance route.
     */
    @Column(name = "description")
    private String description;

    /**
     * Parse the chapter sequence string into a list of event IDs.
     * 
     * @return list of event IDs in order
     */
    public List<String> getChapterSequenceAsList() {
        if (chapterSequence == null || chapterSequence.trim().isEmpty()) {
            return List.of();
        }
        return List.of(chapterSequence.split(","));
    }

    /**
     * Set the chapter sequence from a list of event IDs.
     * 
     * @param chapters list of event IDs in order
     */
    public void setChapterSequenceFromList(List<String> chapters) {
        if (chapters == null || chapters.isEmpty()) {
            this.chapterSequence = "";
        } else {
            this.chapterSequence = String.join(",", chapters);
        }
    }
}