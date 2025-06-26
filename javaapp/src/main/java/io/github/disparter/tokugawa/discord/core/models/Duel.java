package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity representing a duel between a player and an NPC.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Duel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String duelId;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "npc_id", nullable = false)
    private NPC npc;

    @Enumerated(EnumType.STRING)
    private DuelStatus status = DuelStatus.INITIATED;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    private Integer playerHealth = 100;
    private Integer npcHealth = 100;

    private Integer playerMana = 100;
    private Integer npcMana = 100;

    @ManyToOne
    @JoinColumn(name = "player_last_technique_id")
    private Technique playerLastTechnique;

    @ManyToOne
    @JoinColumn(name = "npc_last_technique_id")
    private Technique npcLastTechnique;

    private Integer round = 1;

    private Boolean playerWon;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "duel_logs", joinColumns = @JoinColumn(name = "duel_id"))
    @MapKeyColumn(name = "round")
    @Column(name = "log", columnDefinition = "TEXT")
    private Map<Integer, String> logs = new HashMap<>();

    /**
     * Enum representing the status of a duel.
     */
    public enum DuelStatus {
        INITIATED,
        IN_PROGRESS,
        PLAYER_TURN,
        NPC_TURN,
        COMPLETED,
        CANCELED
    }
}