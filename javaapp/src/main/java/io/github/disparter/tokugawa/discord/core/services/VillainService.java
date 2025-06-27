package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Villain;
import io.github.disparter.tokugawa.discord.core.models.Villain.VillainType;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Service interface for managing villain-related operations.
 */
public interface VillainService {

    /**
     * Find a villain by its ID.
     *
     * @param id the villain ID
     * @return the villain if found, or empty
     */
    Optional<Villain> findById(Long id);

    /**
     * Find a villain by its name.
     *
     * @param name the villain name
     * @return the villain if found, or empty
     */
    Optional<Villain> findByName(String name);

    /**
     * Get all active villains.
     *
     * @return a list of active villains
     */
    List<Villain> getActiveVillains();

    /**
     * Get all active villains of a specific type.
     *
     * @param type the villain type
     * @return a list of active villains of the specified type
     */
    List<Villain> getActiveVillainsByType(VillainType type);

    /**
     * Get all active villains at a specific location.
     *
     * @param location the spawn location
     * @return a list of active villains at the specified location
     */
    List<Villain> getActiveVillainsByLocation(String location);

    /**
     * Create a new villain.
     *
     * @param villain the villain to create
     * @return the created villain
     */
    Villain createVillain(Villain villain);

    /**
     * Update an existing villain.
     *
     * @param villain the villain to update
     * @return the updated villain
     */
    Villain updateVillain(Villain villain);

    /**
     * Delete a villain.
     *
     * @param id the villain ID
     */
    void deleteVillain(Long id);

    /**
     * Spawn a villain at a specific location.
     *
     * @param villainId the villain ID
     * @param location the location to spawn the villain
     * @return the spawned villain
     */
    Villain spawnVillain(Long villainId, String location);

    /**
     * Spawn a random villain of a specific type.
     *
     * @param type the villain type
     * @param location the location to spawn the villain
     * @return the spawned villain, or empty if no villain could be spawned
     */
    Optional<Villain> spawnRandomVillain(VillainType type, String location);

    /**
     * Spawn minions for a villain.
     *
     * @param villainId the villain ID
     * @param count the number of minions to spawn
     * @return a list of spawned minions
     */
    List<Villain> spawnMinions(Long villainId, int count);

    /**
     * Defeat a villain.
     *
     * @param villainId the villain ID
     * @param playerId the player ID who defeated the villain
     * @return a map containing the rewards for defeating the villain
     */
    Map<String, Integer> defeatVillain(Long villainId, Long playerId);

    /**
     * Check if a player can challenge a villain.
     *
     * @param villainId the villain ID
     * @param playerId the player ID
     * @return true if the player can challenge the villain, false otherwise
     */
    boolean canChallengeVillain(Long villainId, Long playerId);

    /**
     * Get all villains that a player can challenge.
     *
     * @param playerId the player ID
     * @return a list of villains that the player can challenge
     */
    List<Villain> getChallengableVillains(Long playerId);

    /**
     * Get the rewards for defeating a villain.
     *
     * @param villainId the villain ID
     * @return a map containing the rewards for defeating the villain
     */
    Map<String, Integer> getVillainRewards(Long villainId);
}