package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.GameCalendar;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the GameCalendar entity.
 */
@Repository
public interface GameCalendarRepository extends CrudRepository<GameCalendar, Long> {
    
    /**
     * Finds the most recent calendar entry.
     * 
     * @return the most recent calendar entry
     */
    GameCalendar findTopByOrderByIdDesc();
}