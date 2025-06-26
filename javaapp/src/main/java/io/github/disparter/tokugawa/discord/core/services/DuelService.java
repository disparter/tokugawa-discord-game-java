package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Technique;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing duel-related operations.
 */
public interface DuelService {

    /**
     * Initiates a duel between a player and an NPC.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return a map containing the initial duel state
     */
    Map<String, Object> initiateDuel(Long playerId, Long npcId);

    /**
     * Processes a player's technique selection in a duel.
     *
     * @param duelId the duel ID
     * @param techniqueId the technique ID
     * @return a map containing the updated duel state
     */
    Map<String, Object> processTechniqueSelection(String duelId, Long techniqueId);

    /**
     * Processes the result of a duel.
     *
     * @param duelId the duel ID
     * @return a map containing the final duel result
     */
    Map<String, Object> processDuelResult(String duelId);

    /**
     * Gets the available techniques for a player.
     *
     * @param playerId the player ID
     * @return a list of available techniques
     */
    List<Technique> getPlayerTechniques(Long playerId);

    /**
     * Gets the available techniques for an NPC.
     *
     * @param npcId the NPC ID
     * @return a list of available techniques
     */
    List<Technique> getNPCTechniques(Long npcId);

    /**
     * Teaches a technique to a player.
     *
     * @param playerId the player ID
     * @param techniqueId the technique ID
     * @return the updated player
     */
    Player teachTechniqueToPlayer(Long playerId, Long techniqueId);

    /**
     * Teaches a technique to an NPC.
     *
     * @param npcId the NPC ID
     * @param techniqueId the technique ID
     * @return the updated NPC
     */
    NPC teachTechniqueToNPC(Long npcId, Long techniqueId);

    /**
     * Gets the active duels for a player.
     *
     * @param playerId the player ID
     * @return a list of active duel IDs
     */
    List<String> getActivePlayerDuels(Long playerId);

    /**
     * Cancels an active duel.
     *
     * @param duelId the duel ID
     * @return true if the duel was successfully canceled, false otherwise
     */
    boolean cancelDuel(String duelId);
}