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

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity representing an event in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    // Seasonal event fields
    private Integer startMonth;
    private Integer startDay;
    private Integer endMonth;
    private Integer endDay;

    // Choice-triggered event fields
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_required_choices", joinColumns = @JoinColumn(name = "event_id"))
    private List<String> requiredChoices = new ArrayList<>();

    // Random event fields
    private Double triggerChance;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_rewards", joinColumns = @JoinColumn(name = "event_id"))
    private List<String> rewards = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_conditions", joinColumns = @JoinColumn(name = "event_id"))
    private List<String> triggerConditions = new ArrayList<>();

    @ManyToMany
    private List<Player> participants = new ArrayList<>();

    /**
     * Enum representing the type of event.
     */
    public enum EventType {
        BASE,
        CLIMACTIC,
        RANDOM,
        SEASONAL,
        ROMANCE,
        CHOICE_TRIGGERED
    }
}
