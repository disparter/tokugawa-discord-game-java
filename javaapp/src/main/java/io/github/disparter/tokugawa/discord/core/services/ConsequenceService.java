package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Consequence.ConsequenceType;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Tracks a player's decision with enhanced details.
     * 
     * @param playerId the player ID
     * @param chapterId the chapter ID
     * @param sceneId the scene ID
     * @param choiceMade the choice that was made
     * @param decisionContext the context of the decision
     * @param name the consequence name
     * @param description the consequence description
     * @param type the consequence type
     * @param effects the effects of the consequence
     * @param relatedChoices the related choices
     * @param affectedNpcs the affected NPCs
     * @return the created consequence
     */
    Consequence trackPlayerDecision(Long playerId, String chapterId, String sceneId, 
                                  String choiceMade, String decisionContext, String name, 
                                  String description, ConsequenceType type, List<String> effects, 
                                  List<String> relatedChoices, List<Long> affectedNpcs);

    /**
     * Gets the percentage of players who made the same choice.
     * 
     * @param chapterId the chapter ID
     * @param sceneId the scene ID
     * @param choiceMade the choice that was made
     * @return the percentage of players who made the same choice
     */
    double getCommunityChoicePercentage(String chapterId, String sceneId, String choiceMade);

    /**
     * Updates the community choice percentage for a consequence.
     * 
     * @param consequenceId the consequence ID
     * @return the updated consequence
     */
    Consequence updateCommunityChoicePercentage(Long consequenceId);

    /**
     * Adds ethical reflections to a consequence.
     * 
     * @param consequenceId the consequence ID
     * @param reflections the ethical reflections to add
     * @return the updated consequence
     */
    Consequence addEthicalReflections(Long consequenceId, List<String> reflections);

    /**
     * Gets ethical reflections for a player's decisions.
     * 
     * @param playerId the player ID
     * @return a map of consequence IDs to lists of ethical reflections
     */
    Map<Long, List<String>> getEthicalReflectionsForPlayer(Long playerId);

    /**
     * Adds alternative paths to a consequence.
     * 
     * @param consequenceId the consequence ID
     * @param alternativePaths the alternative paths to add
     * @return the updated consequence
     */
    Consequence addAlternativePaths(Long consequenceId, List<String> alternativePaths);

    /**
     * Gets alternative paths for a player's decisions.
     * 
     * @param playerId the player ID
     * @return a map of consequence IDs to lists of alternative paths
     */
    Map<Long, List<String>> getAlternativePathsForPlayer(Long playerId);

    /**
     * Gets consequences for a player by chapter and scene.
     * 
     * @param playerId the player ID
     * @param chapterId the chapter ID
     * @param sceneId the scene ID
     * @return list of consequences
     */
    List<Consequence> getConsequencesByChapterAndScene(Long playerId, String chapterId, String sceneId);

    /**
     * Gets a comprehensive decision dashboard for a player, including community comparisons,
     * ethical reflections, and alternative paths.
     * 
     * @param playerId the player ID
     * @return a map of decision categories to lists of consequences with enhanced information
     */
    Map<String, List<Consequence>> getEnhancedDecisionDashboard(Long playerId);
}
