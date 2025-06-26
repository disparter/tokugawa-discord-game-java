package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing the in-game calendar.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime currentDate;

    @Enumerated(EnumType.STRING)
    private Season currentSeason;

    @Column(nullable = false)
    private int dayOfSeason;

    @Column(nullable = false)
    private int year;

    /**
     * Enum representing the four seasons of the year.
     */
    public enum Season {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER;

        /**
         * Gets the current season based on the current date.
         *
         * @return the current season
         */
        public static Season getCurrentSeason() {
            LocalDateTime now = LocalDateTime.now();
            int month = now.getMonthValue();

            if (month >= 3 && month <= 5) {
                return SPRING;
            } else if (month >= 6 && month <= 8) {
                return SUMMER;
            } else if (month >= 9 && month <= 11) {
                return AUTUMN;
            } else {
                return WINTER;
            }
        }

        /**
         * Gets the next season in the cycle.
         *
         * @param season the current season
         * @return the next season
         */
        public static Season getNextSeason(Season season) {
            switch (season) {
                case SPRING:
                    return SUMMER;
                case SUMMER:
                    return AUTUMN;
                case AUTUMN:
                    return WINTER;
                case WINTER:
                    return SPRING;
                default:
                    throw new IllegalArgumentException("Unknown season: " + season);
            }
        }
    }
}