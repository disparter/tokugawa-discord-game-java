package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Relationship;

import java.util.List;

/**
 * Service interface for managing relationship-related operations.
 */
public interface RelationshipService {
    
    /**
     * Find a relationship by its ID.
     *
     * @param id the relationship ID
     * @return the relationship if found, or null
     */
    Relationship findById(Long id);
    
    /**
     * Get all relationships for a player.
     *
     * @param playerId the player ID
     * @return list of relationships
     */
    List<Relationship> getRelationshipsForPlayer(Long playerId);
    
    /**
     * Get relationship between a player and an NPC.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return the relationship if exists, or null
     */
    Relationship getRelationship(Long playerId, Long npcId);
    
    /**
     * Improve relationship between a player and an NPC.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param amount the amount to improve
     * @return the updated relationship
     */
    Relationship improveRelationship(Long playerId, Long npcId, int amount);
    
    /**
     * Worsen relationship between a player and an NPC.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param amount the amount to worsen
     * @return the updated relationship
     */
    Relationship worsenRelationship(Long playerId, Long npcId, int amount);
    
    /**
     * Get NPCs with high relationship values for a player.
     *
     * @param playerId the player ID
     * @return list of NPCs with high relationship
     */
    List<NPC> getFriendlyNPCs(Long playerId);
    
    /**
     * Get NPCs with low relationship values for a player.
     *
     * @param playerId the player ID
     * @return list of NPCs with low relationship
     */
    List<NPC> getHostileNPCs(Long playerId);
    
    /**
     * Save a relationship.
     *
     * @param relationship the relationship to save
     * @return the saved relationship
     */
    Relationship save(Relationship relationship);
}