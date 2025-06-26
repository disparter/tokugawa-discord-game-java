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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Entity representing a chapter in the game's story.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String chapterId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ChapterType type;

    private String arcId;

    private Integer phase;

    private Integer completionExp;

    private Integer completionReward;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chapter_choices", joinColumns = @JoinColumn(name = "chapter_id"))
    private List<String> choices = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chapter_scenes", joinColumns = @JoinColumn(name = "chapter_id"))
    @Column(name = "scene_value", columnDefinition = "TEXT")
    private List<String> scenes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chapter_requirements", joinColumns = @JoinColumn(name = "chapter_id"))
    private List<String> requirements = new ArrayList<>();

    private String nextChapterId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chapter_dialogues", joinColumns = @JoinColumn(name = "chapter_id"))
    @Column(name = "dialogue_value", columnDefinition = "TEXT")
    private List<String> dialogues = new ArrayList();

    /**
     * Enum representing the type of chapter.
     */
    public enum ChapterType {
        STORY,
        CHALLENGE,
        BRANCHING
    }
}
