package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import io.github.disparter.tokugawa.discord.core.models.Relationship.RelationshipStatus;

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

    /**
     * Update affinity based on interaction type.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param interactionType the type of interaction
     * @return the updated relationship
     */
    Relationship updateAffinityByInteraction(Long playerId, Long npcId, String interactionType);

    /**
     * Check if a relationship has reached a specific status.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param status the relationship status to check
     * @return true if the relationship has reached the status, false otherwise
     */
    boolean hasReachedStatus(Long playerId, Long npcId, RelationshipStatus status);

    /**
     * Get NPCs that have a specific relationship status with a player.
     *
     * @param playerId the player ID
     * @param status the relationship status to check
     * @return list of NPCs with the specified relationship status
     */
    List<NPC> getNPCsByStatus(Long playerId, RelationshipStatus status);

    /**
     * Trigger a romance event based on the current relationship status.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return the event ID that was triggered, or null if no event was triggered
     */
    String triggerRomanceEvent(Long playerId, Long npcId);

    /**
     * Record that a specific event has been triggered for a relationship.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param eventId the event ID
     * @return the updated relationship
     */
    Relationship recordTriggeredEvent(Long playerId, Long npcId, String eventId);

    /**
     * Check if a specific event has been triggered for a relationship.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param eventId the event ID
     * @return true if the event has been triggered, false otherwise
     */
    boolean hasTriggeredEvent(Long playerId, Long npcId, String eventId);
}
