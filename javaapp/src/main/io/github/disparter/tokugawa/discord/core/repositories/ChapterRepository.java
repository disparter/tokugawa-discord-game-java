package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Chapter entity operations.
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    /**
     * Find a chapter by its chapter ID.
     *
     * @param chapterId the chapter ID
     * @return the chapter, if found
     */
    Optional<Chapter> findByChapterId(String chapterId);

    /**
     * Find chapters by arc ID.
     *
     * @param arcId the arc ID
     * @return the list of chapters in the arc
     */
    List<Chapter> findByArcId(String arcId);

    /**
     * Find chapters by type.
     *
     * @param type the chapter type
     * @return the list of chapters of the specified type
     */
    List<Chapter> findByType(Chapter.ChapterType type);

    /**
     * Find chapters by phase.
     *
     * @param phase the chapter phase
     * @return the list of chapters in the specified phase
     */
    List<Chapter> findByPhase(Integer phase);
}