package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Event;

import java.util.List;

/**
 * Service interface for managing event-related operations.
 */
public interface EventService {
    
    /**
     * Find an event by its ID.
     *
     * @param id the event ID
     * @return the event if found, or null
     */
    Event findById(Long id);
    
    /**
     * Get all events.
     *
     * @return list of all events
     */
    List<Event> getAllEvents();
    
    /**
     * Get events available for a player.
     *
     * @param playerId the player ID
     * @return list of available events
     */
    List<Event> getAvailableEventsForPlayer(Long playerId);
    
    /**
     * Trigger an event for a player.
     *
     * @param eventId the event ID
     * @param playerId the player ID
     * @return the triggered event
     */
    Event triggerEvent(Long eventId, Long playerId);
    
    /**
     * Complete an event for a player.
     *
     * @param eventId the event ID
     * @param playerId the player ID
     * @return the completed event
     */
    Event completeEvent(Long eventId, Long playerId);
    
    /**
     * Save an event.
     *
     * @param event the event to save
     * @return the saved event
     */
    Event save(Event event);
}