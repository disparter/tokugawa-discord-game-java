package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Consequence.ConsequenceType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the Consequence entity.
 */
@Repository
public interface ConsequenceRepository extends CrudRepository<Consequence, Long> {
    
    /**
     * Finds consequences by player ID.
     * 
     * @param playerId the player ID
     * @return list of consequences
     */
    List<Consequence> findByPlayerId(Long playerId);
    
    /**
     * Finds active consequences by player ID.
     * 
     * @param playerId the player ID
     * @return list of active consequences
     */
    List<Consequence> findByPlayerIdAndActiveTrue(Long playerId);
    
    /**
     * Finds consequences by player ID and type.
     * 
     * @param playerId the player ID
     * @param type the consequence type
     * @return list of consequences
     */
    List<Consequence> findByPlayerIdAndType(Long playerId, ConsequenceType type);
    
    /**
     * Finds consequences by player ID and related choice.
     * 
     * @param playerId the player ID
     * @param choiceId the choice ID
     * @return list of consequences
     */
    List<Consequence> findByPlayerIdAndRelatedChoicesContaining(Long playerId, String choiceId);
    
    /**
     * Finds consequences by player ID and affected NPC.
     * 
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return list of consequences
     */
    List<Consequence> findByPlayerIdAndAffectedNpcsContaining(Long playerId, Long npcId);
}