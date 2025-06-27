package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity representing a player's story progress in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Player player;

    private String currentArc;

    private String currentChapterId;

    private Integer currentDialogueIndex = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "completed_chapters", joinColumns = @JoinColumn(name = "progress_id"))
    private List<String> completedChapters = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "completed_arcs", joinColumns = @JoinColumn(name = "progress_id"))
    private List<String> completedArcs = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "story_choices", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "choice_value", columnDefinition = "TEXT")
    private Map<String, String> choices = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "triggered_events", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "event_value", columnDefinition = "TEXT")
    private Map<String, String> triggeredEvents = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_relationships", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "relationship_value")
    private Map<String, Integer> relationships = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "faction_reputations", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "reputation_value")
    private Map<String, Integer> factionReputations = new HashMap<>();
}
