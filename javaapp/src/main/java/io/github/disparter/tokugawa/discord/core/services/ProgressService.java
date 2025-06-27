package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Progress;

import java.util.List;

/**
 * Service interface for managing player progress-related operations.
 */
public interface ProgressService {

    /**
     * Find progress by its ID.
     *
     * @param id the progress ID
     * @return the progress if found, or null
     */
    Progress findById(Long id);

    /**
     * Get all progress records for a player.
     *
     * @param playerId the player ID
     * @return list of progress records
     */
    List<Progress> getPlayerProgress(Long playerId);

    /**
     * Get progress for a specific chapter or event.
     *
     * @param playerId the player ID
     * @param contentId the chapter or event ID
     * @param contentType the type of content (e.g., "CHAPTER", "EVENT")
     * @return the progress record if found, or null
     */
    Progress getSpecificProgress(Long playerId, Long contentId, String contentType);

    /**
     * Update progress for a player.
     *
     * @param playerId the player ID
     * @param contentId the chapter or event ID
     * @param contentType the type of content (e.g., "CHAPTER", "EVENT")
     * @param status the new status (e.g., "STARTED", "COMPLETED")
     * @return the updated progress
     */
    Progress updateProgress(Long playerId, Long contentId, String contentType, String status);

    /**
     * Get overall game completion percentage for a player.
     *
     * @param playerId the player ID
     * @return completion percentage (0-100)
     */
    double getCompletionPercentage(Long playerId);

    /**
     * Save progress.
     *
     * @param progress the progress to save
     * @return the saved progress
     */
    Progress save(Progress progress);

    /**
     * Get progress for a player.
     *
     * @param playerId the player ID
     * @return the progress record if found, or null
     */
    Progress getProgressByPlayerId(Long playerId);
}
