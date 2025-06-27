package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.GameCalendar;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;

import java.time.LocalDateTime;

/**
 * Service interface for managing the game calendar.
 */
public interface GameCalendarService {
    
    /**
     * Gets the current game calendar.
     * 
     * @return the current game calendar
     */
    GameCalendar getCurrentCalendar();
    
    /**
     * Gets the current season in the game.
     * 
     * @return the current season
     */
    Season getCurrentSeason();
    
    /**
     * Gets the current date in the game.
     * 
     * @return the current date
     */
    LocalDateTime getCurrentDate();
    
    /**
     * Advances the game calendar by the specified number of days.
     * 
     * @param days the number of days to advance
     * @return the updated game calendar
     */
    GameCalendar advanceCalendar(int days);
    
    /**
     * Checks if the current date is within the specified date range.
     * 
     * @param startMonth the start month (1-12)
     * @param startDay the start day (1-31)
     * @param endMonth the end month (1-12)
     * @param endDay the end day (1-31)
     * @return true if the current date is within the range, false otherwise
     */
    boolean isDateInRange(int startMonth, int startDay, int endMonth, int endDay);
    
    /**
     * Initializes the game calendar if it doesn't exist.
     * 
     * @return the initialized game calendar
     */
    GameCalendar initializeCalendar();
}