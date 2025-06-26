package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Event entity operations.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find an event by its event ID.
     *
     * @param eventId the event ID
     * @return the event, if found
     */
    Optional<Event> findByEventId(String eventId);

    /**
     * Find events by type.
     *
     * @param type the event type
     * @return the list of events of the specified type
     */
    List<Event> findByType(Event.EventType type);

    /**
     * Find active events (current time is between start and end time).
     *
     * @param currentTime the current time
     * @return the list of active events
     */
    List<Event> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime currentTime, LocalDateTime currentTime2);

    /**
     * Find events by participant.
     *
     * @param player the participant
     * @return the list of events the player is participating in
     */
    List<Event> findByParticipantsContains(Player player);

    /**
     * Find events by name containing the given text.
     *
     * @param name the name to search for
     * @return the list of events with matching names
     */
    List<Event> findByNameContainingIgnoreCase(String name);
}