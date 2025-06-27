package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity representing a bet in the game.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetType type;

    @Column(nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status;

    @Enumerated(EnumType.STRING)
    private BetResult result;

    private Integer winnings;

    /**
     * Enum representing the type of bet.
     */
    public enum BetType {
        DUEL,
        EVENT,
        COMPETITION
    }

    /**
     * Enum representing the status of a bet.
     */
    public enum BetStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    /**
     * Enum representing the result of a bet.
     */
    public enum BetResult {
        WIN,
        LOSE,
        DRAW
    }
}