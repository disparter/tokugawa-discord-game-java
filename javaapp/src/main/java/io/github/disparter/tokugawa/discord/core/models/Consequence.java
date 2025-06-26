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
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a consequence of a player's decision.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String consequenceId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ConsequenceType type;

    @ManyToOne
    private Player player;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consequence_effects", joinColumns = @JoinColumn(name = "consequence_id"))
    private List<String> effects = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consequence_related_choices", joinColumns = @JoinColumn(name = "consequence_id"))
    private List<String> relatedChoices = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consequence_affected_npcs", joinColumns = @JoinColumn(name = "consequence_id"))
    private List<Long> affectedNpcs = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consequence_ethical_reflections", joinColumns = @JoinColumn(name = "consequence_id"))
    @Column(name = "reflection", length = 1000)
    private List<String> ethicalReflections = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consequence_alternative_paths", joinColumns = @JoinColumn(name = "consequence_id"))
    @Column(name = "path", length = 1000)
    private List<String> alternativePaths = new ArrayList<>();

    @Column(name = "community_choice_percentage")
    private Double communityChoicePercentage;

    @Column(name = "decision_context", length = 1000)
    private String decisionContext;

    @Column(name = "choice_made", length = 500)
    private String choiceMade;

    @Column(name = "chapter_id")
    private String chapterId;

    @Column(name = "scene_id")
    private String sceneId;

    /**
     * Enum representing the type of consequence.
     */
    public enum ConsequenceType {
        IMMEDIATE,
        SHORT_TERM,
        LONG_TERM,
        PERMANENT
    }
}
