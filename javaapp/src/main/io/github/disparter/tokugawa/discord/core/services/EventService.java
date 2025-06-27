package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Event.EventType;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Map;

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
     * Get events of a specific type available for a player.
     *
     * @param playerId the player ID
     * @param type the event type
     * @return list of available events of the specified type
     */
    List<Event> getAvailableEventsForPlayerByType(Long playerId, EventType type);

    /**
     * Get events available for a player in the current season.
     *
     * @param playerId the player ID
     * @return list of available seasonal events
     */
    List<Event> getAvailableSeasonalEventsForPlayer(Long playerId);

    /**
     * Get events available for a player in a specific season.
     *
     * @param playerId the player ID
     * @param season the season
     * @return list of available seasonal events for the specified season
     */
    List<Event> getAvailableSeasonalEventsForPlayer(Long playerId, Season season);

    /**
     * Check for random events that should be triggered for a player.
     *
     * @param playerId the player ID
     * @return list of random events that should be triggered
     */
    List<Event> checkForRandomEvents(Long playerId);

    /**
     * Check for events that should be triggered based on player actions.
     *
     * @param playerId the player ID
     * @param actionType the type of action performed
     * @param actionData additional data about the action
     * @return list of events that should be triggered
     */
    List<Event> checkForActionTriggeredEvents(Long playerId, String actionType, Map<String, Object> actionData);

    /**
     * Check for events that should be triggered based on story progress.
     *
     * @param playerId the player ID
     * @return list of events that should be triggered
     */
    List<Event> checkForStoryTriggeredEvents(Long playerId);

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
     * Apply the consequences of an event to a player.
     *
     * @param eventId the event ID
     * @param playerId the player ID
     * @return the updated player
     */
    Player applyEventConsequences(Long eventId, Long playerId);

    /**
     * Create a new event.
     *
     * @param event the event to create
     * @return the created event
     */
    Event createEvent(Event event);

    /**
     * Update an existing event.
     *
     * @param event the event to update
     * @return the updated event
     */
    Event updateEvent(Event event);

    /**
     * Delete an event.
     *
     * @param eventId the event ID
     */
    void deleteEvent(Long eventId);

    /**
     * Save an event.
     *
     * @param event the event to save
     * @return the saved event
     */
    Event save(Event event);
}
