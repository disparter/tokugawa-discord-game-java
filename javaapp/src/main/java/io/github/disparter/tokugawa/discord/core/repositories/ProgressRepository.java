package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Progress entity operations.
 */
@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    /**
     * Find progress by player.
     *
     * @param player the player
     * @return the progress, if found
     */
    Optional<Progress> findByPlayer(Player player);

    /**
     * Find progress by player ID.
     *
     * @param playerId the player ID
     * @return the progress, if found
     */
    @Query("SELECT p FROM Progress p WHERE p.player.id = :playerId")
    Optional<Progress> findByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find progress by current arc.
     *
     * @param currentArc the current arc
     * @return the list of progress with the specified current arc
     */
    List<Progress> findByCurrentArc(String currentArc);

    /**
     * Find progress by current chapter.
     *
     * @param currentChapter the current chapter
     * @return the list of progress with the specified current chapter
     */
    List<Progress> findByCurrentChapter(String currentChapter);

    /**
     * Find progress by completed chapter.
     *
     * @param chapterId the chapter ID
     * @return the list of progress that have completed the specified chapter
     */
    @Query("SELECT p FROM Progress p JOIN p.completedChapters c WHERE c = :chapterId")
    List<Progress> findByCompletedChaptersContains(@Param("chapterId") String chapterId);

    /**
     * Find progress by completed arc.
     *
     * @param arcId the arc ID
     * @return the list of progress that have completed the specified arc
     */
    @Query("SELECT p FROM Progress p JOIN p.completedArcs a WHERE a = :arcId")
    List<Progress> findByCompletedArcsContains(@Param("arcId") String arcId);

    /**
     * Find progress by triggered event.
     *
     * @param eventId the event ID
     * @return the list of progress that have triggered the specified event
     */
    @Query("SELECT p FROM Progress p JOIN p.triggeredEvents e WHERE KEY(e) = :eventId")
    List<Progress> findByTriggeredEventsContainsKey(@Param("eventId") String eventId);
}
