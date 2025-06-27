package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Chapter;

import java.util.List;
import java.util.Map;

/**
 * Service interface for narrative operations.
 * This service manages the narrative flow, including chapter progression,
 * choice processing, and narrative validation.
 */
public interface NarrativeService {

    /**
     * Find a chapter by its ID.
     *
     * @param id the chapter ID
     * @return the chapter, if found, or null
     */
    Chapter findChapterById(Long id);

    /**
     * Get all chapters.
     *
     * @return the list of all chapters
     */
    List<Chapter> getAllChapters();

    /**
     * Get chapters available for a player.
     *
     * @param playerId the player ID
     * @return the list of available chapters
     */
    List<Chapter> getAvailableChaptersForPlayer(Long playerId);

    /**
     * Start a chapter for a player.
     *
     * @param chapterId the chapter ID
     * @param playerId the player ID
     * @return the started chapter
     */
    Chapter startChapter(Long chapterId, Long playerId);

    /**
     * Complete a chapter for a player.
     *
     * @param chapterId the chapter ID
     * @param playerId the player ID
     * @return the completed chapter
     */
    Chapter completeChapter(Long chapterId, Long playerId);

    /**
     * Process a player's choice in the current chapter.
     *
     * @param playerId the player ID
     * @param choiceIndex the index of the chosen option
     * @return a map containing updated player data and chapter information
     */
    Map<String, Object> processChoice(Long playerId, int choiceIndex);

    /**
     * Validate the narrative flow of all chapters.
     * This checks for broken links, missing chapters, and other issues.
     *
     * @return a list of validation errors, or an empty list if no errors were found
     */
    List<String> validateNarrative();

    /**
     * Update player progress based on a duel result.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param playerWon whether the player won the duel
     * @return true if the progress was updated successfully, false otherwise
     */
    boolean updateProgressFromDuel(Long playerId, Long npcId, Boolean playerWon);
}