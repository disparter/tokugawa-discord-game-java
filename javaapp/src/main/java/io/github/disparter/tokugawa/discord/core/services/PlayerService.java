package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;

/**
 * Service interface for managing player-related operations.
 */
public interface PlayerService {

    /**
     * Find a player by their ID.
     *
     * @param id the player ID
     * @return the player if found, or null
     */
    Player findById(Long id);

    /**
     * Find a player by their Discord ID.
     *
     * @param discordId the Discord ID
     * @return the player if found, or null
     */
    Player findByDiscordId(String discordId);

    /**
     * Save a player.
     *
     * @param player the player to save
     * @return the saved player
     */
    Player save(Player player);

    /**
     * Delete a player.
     *
     * @param id the player ID
     */
    void delete(Long id);

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
     * Get a player's reputation.
     *
     * @param playerId the player ID
     * @return the player's reputation
     */
    int getReputation(Long playerId);

    /**
     * Get players ranked by reputation.
     *
     * @return list of players sorted by reputation
     */
    java.util.List<Player> getPlayersByReputation();

    /**
     * Update a player's reputation based on a story decision.
     *
     * @param playerId the player ID
     * @param decisionId the decision ID
     * @return the updated player
     */
    Player updateReputationFromDecision(Long playerId, String decisionId);

    /**
     * Update a player's reputation based on a duel result.
     *
     * @param playerId the player ID
     * @param won whether the player won the duel
     * @return the updated player
     */
    Player updateReputationFromDuel(Long playerId, boolean won);

    /**
     * Update a player's reputation based on a social interaction.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param positive whether the interaction was positive
     * @return the updated player
     */
    Player updateReputationFromSocialInteraction(Long playerId, Long npcId, boolean positive);
}
