package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Player entity operations.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Find a player by their user ID.
     *
     * @param userId the user ID
     * @return the player, if found
     */
    Optional<Player> findByUserId(String userId);

    /**
     * Find a player by their name.
     *
     * @param name the player name
     * @return the player, if found
     */
    Optional<Player> findByName(String name);

    /**
     * Find top players by points.
     *
     * @param limit the maximum number of players to return
     * @return the list of top players
     */
    List<Player> findTop10ByOrderByPointsDesc();

    /**
     * Find top players by level and exp.
     *
     * @param limit the maximum number of players to return
     * @return the list of top players
     */
    List<Player> findTop10ByOrderByLevelDescExpDesc();

    /**
     * Find players by club ID.
     *
     * @param clubId the club ID
     * @return the list of players in the club
     */
    List<Player> findByClubId(String clubId);
}