package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Duel;
import io.github.disparter.tokugawa.discord.core.models.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing Duel entities.
 */
@Repository
public interface DuelRepository extends CrudRepository<Duel, Long> {

    /**
     * Find a duel by its unique duel ID.
     *
     * @param duelId the duel ID
     * @return the duel if found
     */
    Optional<Duel> findByDuelId(String duelId);

    /**
     * Find all active duels for a player.
     *
     * @param player the player
     * @return a list of active duels
     */
    List<Duel> findByPlayerAndStatusNot(Player player, Duel.DuelStatus status);

    /**
     * Find all completed duels for a player.
     *
     * @param player the player
     * @return a list of completed duels
     */
    List<Duel> findByPlayerAndStatus(Player player, Duel.DuelStatus status);
}