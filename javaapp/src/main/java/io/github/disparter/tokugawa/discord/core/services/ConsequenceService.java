package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Consequence.ConsequenceType;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing consequences of player decisions.
 */
public interface ConsequenceService {
    
    /**
     * Creates a consequence for a player's decision.
     * 
     * @param playerId the player ID
     * @param name the consequence name
     * @param description the consequence description
     * @param type the consequence type
     * @param effects the effects of the consequence
     * @param relatedChoices the related choices
     * @param affectedNpcs the affected NPCs
     * @return the created consequence
     */
    Consequence createConsequence(Long playerId, String name, String description, 
                                 ConsequenceType type, List<String> effects, 
                                 List<String> relatedChoices, List<Long> affectedNpcs);
    
    /**
     * Gets all consequences for a player.
     * 
     * @param playerId the player ID
     * @return list of consequences
     */
    List<Consequence> getConsequencesForPlayer(Long playerId);
    
    /**
     * Gets active consequences for a player.
     * 
     * @param playerId the player ID
     * @return list of active consequences
     */
    List<Consequence> getActiveConsequencesForPlayer(Long playerId);
    
    /**
     * Gets consequences for a player by type.
     * 
     * @param playerId the player ID
     * @param type the consequence type
     * @return list of consequences
     */
    List<Consequence> getConsequencesByType(Long playerId, ConsequenceType type);
    
    /**
     * Gets consequences for a player related to a choice.
     * 
     * @param playerId the player ID
     * @param choiceId the choice ID
     * @return list of consequences
     */
    List<Consequence> getConsequencesForChoice(Long playerId, String choiceId);
    
    /**
     * Gets consequences for a player affecting an NPC.
     * 
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return list of consequences
     */
    List<Consequence> getConsequencesAffectingNpc(Long playerId, Long npcId);
    
    /**
     * Deactivates a consequence.
     * 
     * @param consequenceId the consequence ID
     * @return the updated consequence
     */
    Consequence deactivateConsequence(Long consequenceId);
    
    /**
     * Gets a summary of a player's decisions and their consequences.
     * 
     * @param playerId the player ID
     * @return a map of decision categories to lists of consequences
     */
    Map<String, List<Consequence>> getDecisionDashboard(Long playerId);
    
    /**
     * Applies the effects of a player's active consequences.
     * 
     * @param player the player
     * @return the updated player
     */
    Player applyConsequenceEffects(Player player);
}