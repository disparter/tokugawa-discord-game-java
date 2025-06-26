package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Location;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing location-related operations.
 */
public interface LocationService {

    /**
     * Find a location by its ID.
     *
     * @param id the location ID
     * @return the location if found, or null
     */
    Location findById(Long id);

    /**
     * Find a location by its name.
     *
     * @param name the location name
     * @return the location if found, or null
     */
    Location findByName(String name);

    /**
     * Get all locations.
     *
     * @return list of all locations
     */
    List<Location> getAllLocations();

    /**
     * Get all unlocked locations.
     *
     * @return list of unlocked locations
     */
    List<Location> getUnlockedLocations();

    /**
     * Save a location.
     *
     * @param location the location to save
     * @return the saved location
     */
    Location save(Location location);

    /**
     * Delete a location.
     *
     * @param id the location ID
     */
    void delete(Long id);

    /**
     * Get connected locations for a given location.
     *
     * @param locationId the location ID
     * @return list of connected locations
     */
    List<Location> getConnectedLocations(Long locationId);

    /**
     * Move a player to a new location.
     *
     * @param playerId the player ID
     * @param locationId the destination location ID
     * @return true if the move was successful, false otherwise
     */
    boolean movePlayer(Long playerId, Long locationId);

    /**
     * Get NPCs present at a location.
     *
     * @param locationId the location ID
     * @return list of NPCs at the location
     */
    List<NPC> getNPCsAtLocation(Long locationId);

    /**
     * Get items available at a location.
     *
     * @param locationId the location ID
     * @return list of items at the location
     */
    List<Item> getItemsAtLocation(Long locationId);

    /**
     * Trigger a random event at a location.
     *
     * @param playerId the player ID
     * @param locationId the location ID
     * @return the triggered event, or empty if no event was triggered
     */
    Optional<Event> triggerRandomEvent(Long playerId, Long locationId);

    /**
     * Discover a hidden area at a location.
     *
     * @param playerId the player ID
     * @param locationId the location ID
     * @param areaName the name of the hidden area
     * @return true if the area was discovered, false otherwise
     */
    boolean discoverHiddenArea(Long playerId, Long locationId, String areaName);

    /**
     * Get all hidden areas at a location.
     *
     * @param locationId the location ID
     * @return list of hidden areas
     */
    List<String> getHiddenAreas(Long locationId);

    /**
     * Get discovery requirements for hidden areas at a location.
     *
     * @param locationId the location ID
     * @return map of area names to requirements
     */
    Map<String, String> getDiscoveryRequirements(Long locationId);

    /**
     * Check if a player meets the requirements to discover a hidden area.
     *
     * @param playerId the player ID
     * @param locationId the location ID
     * @param areaName the name of the hidden area
     * @return true if the player meets the requirements, false otherwise
     */
    boolean checkDiscoveryRequirements(Long playerId, Long locationId, String areaName);

    /**
     * Unlock a location.
     *
     * @param locationId the location ID
     * @return true if the location was unlocked, false otherwise
     */
    boolean unlockLocation(Long locationId);

    /**
     * Check if a player meets the requirements to unlock a location.
     *
     * @param playerId the player ID
     * @param locationId the location ID
     * @return true if the player meets the requirements, false otherwise
     */
    boolean checkUnlockRequirements(Long playerId, Long locationId);

    /**
     * Get the current location of a player.
     *
     * @param playerId the player ID
     * @return the player's current location
     */
    Location getPlayerLocation(Long playerId);

    /**
     * Set the current location of a player.
     *
     * @param playerId the player ID
     * @param locationId the location ID
     * @return the updated player
     */
    Player setPlayerLocation(Long playerId, Long locationId);
}