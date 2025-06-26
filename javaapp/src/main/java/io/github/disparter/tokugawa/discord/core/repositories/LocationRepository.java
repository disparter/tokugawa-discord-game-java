package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Location entity operations.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Find a location by its name.
     *
     * @param name the location name
     * @return the location, if found
     */
    Optional<Location> findByName(String name);

    /**
     * Find all unlocked locations.
     *
     * @return list of unlocked locations
     */
    List<Location> findByLockedFalse();

    /**
     * Find all locations containing a specific NPC.
     *
     * @param npcId the NPC ID
     * @return list of locations containing the NPC
     */
    List<Location> findByNpcsContaining(Long npcId);

    /**
     * Find all locations containing a specific item.
     *
     * @param itemId the item ID
     * @return list of locations containing the item
     */
    List<Location> findByItemsContaining(Long itemId);

    /**
     * Find all locations that can trigger a specific event.
     *
     * @param eventId the event ID
     * @return list of locations that can trigger the event
     */
    List<Location> findByPossibleEventsContaining(Long eventId);
}