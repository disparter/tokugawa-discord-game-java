package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;

/**
 * Service interface for managing player reputation.
 */
public interface ReputationService {
    
    /**
     * Get the current reputation of a player.
     *
     * @param playerId the player ID
     * @return the player's reputation
     */
    int getReputation(Long playerId);
    
    /**
     * Increase a player's reputation.
     *
     * @param playerId the player ID
     * @param amount the amount to increase
     * @return the updated player
     */
    Player increaseReputation(Long playerId, int amount);
    
    /**
     * Decrease a player's reputation.
     *
     * @param playerId the player ID
     * @param amount the amount to decrease
     * @return the updated player
     */
    Player decreaseReputation(Long playerId, int amount);
    
    /**
     * Update reputation based on a story decision.
     *
     * @param playerId the player ID
     * @param decisionId the decision ID
     * @return the updated player
     */
    Player updateReputationByDecision(Long playerId, String decisionId);
    
    /**
     * Update reputation based on a duel outcome.
     *
     * @param playerId the player ID
     * @param won whether the player won the duel
     * @param opponentLevel the level of the opponent
     * @return the updated player
     */
    Player updateReputationByDuel(Long playerId, boolean won, int opponentLevel);
    
    /**
     * Update reputation based on a social interaction.
     *
     * @param playerId the player ID
     * @param interactionType the type of interaction
     * @param targetId the ID of the interaction target (NPC or club)
     * @return the updated player
     */
    Player updateReputationBySocialInteraction(Long playerId, String interactionType, Long targetId);
    
    /**
     * Get players ranked by reputation.
     *
     * @return list of players sorted by reputation
     */
    java.util.List<Player> getRankedPlayers();
    
    /**
     * Get the reputation rank of a player.
     *
     * @param playerId the player ID
     * @return the player's rank
     */
    int getReputationRank(Long playerId);
    
    /**
     * Check if a player has reached a specific reputation threshold.
     *
     * @param playerId the player ID
     * @param threshold the reputation threshold
     * @return true if the player's reputation is at or above the threshold
     */
    boolean hasReachedReputationThreshold(Long playerId, int threshold);
}