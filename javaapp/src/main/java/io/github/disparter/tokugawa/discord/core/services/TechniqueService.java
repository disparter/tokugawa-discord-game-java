package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Technique;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Service for managing techniques in the game.
 */
public interface TechniqueService {

    /**
     * Find a technique by its ID.
     *
     * @param id the technique ID
     * @return the technique if found
     */
    Optional<Technique> findById(Long id);

    /**
     * Find a technique by its unique technique ID.
     *
     * @param techniqueId the unique technique ID
     * @return the technique if found
     */
    Optional<Technique> findByTechniqueId(String techniqueId);

    /**
     * Find a technique by its name.
     *
     * @param name the technique name
     * @return the technique if found
     */
    Optional<Technique> findByName(String name);

    /**
     * Find all techniques of a specific type.
     *
     * @param type the technique type
     * @return a list of techniques of the specified type
     */
    List<Technique> findByType(Technique.TechniqueType type);

    /**
     * Get all techniques known by a player.
     *
     * @param player the player
     * @return a list of techniques known by the player
     */
    List<Technique> getPlayerTechniques(Player player);

    /**
     * Get all techniques known by an NPC.
     *
     * @param npc the NPC
     * @return a list of techniques known by the NPC
     */
    List<Technique> getNPCTechniques(NPC npc);

    /**
     * Teach a technique to a player.
     *
     * @param player the player
     * @param technique the technique to teach
     * @return the updated player
     * @throws IllegalArgumentException if the technique is not learnable or the player already knows it
     */
    Player teachTechniqueToPlayer(Player player, Technique technique);

    /**
     * Teach a technique to an NPC.
     *
     * @param npc the NPC
     * @param technique the technique to teach
     * @return the updated NPC
     */
    NPC teachTechniqueToNPC(NPC npc, Technique technique);

    /**
     * Check if a player knows a specific technique.
     *
     * @param player the player
     * @param technique the technique to check
     * @return true if the player knows the technique, false otherwise
     */
    boolean playerKnowsTechnique(Player player, Technique technique);

    /**
     * Check if an NPC knows a specific technique.
     *
     * @param npc the NPC
     * @param technique the technique to check
     * @return true if the NPC knows the technique, false otherwise
     */
    boolean npcKnowsTechnique(NPC npc, Technique technique);

    /**
     * Evolve a player's technique to a more powerful version.
     *
     * @param player the player
     * @param technique the technique to evolve
     * @param powerPoints the power points to invest in the evolution
     * @return the evolved technique
     * @throws IllegalArgumentException if the player doesn't know the technique or doesn't have enough power points
     */
    Technique evolveTechnique(Player player, Technique technique, int powerPoints);

    /**
     * Get the evolution requirements for a technique.
     *
     * @param technique the technique
     * @return a map of requirement names to values
     */
    Map<String, Integer> getTechniqueEvolutionRequirements(Technique technique);

    /**
     * Get a map of all possible technique effects and their descriptions.
     *
     * @return a map of effect names to descriptions
     */
    Map<String, String> getTechniqueEffectDescriptions();

    /**
     * Save a technique.
     *
     * @param technique the technique to save
     * @return the saved technique
     */
    Technique save(Technique technique);

    /**
     * Delete a technique.
     *
     * @param technique the technique to delete
     */
    void delete(Technique technique);
}