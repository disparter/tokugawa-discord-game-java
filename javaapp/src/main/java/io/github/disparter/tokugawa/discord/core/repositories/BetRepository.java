package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Bet;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetStatus;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing bet data.
 */
@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {

    /**
     * Find all bets for a player.
     *
     * @param player the player
     * @return a list of bets
     */
    List<Bet> findByPlayer(Player player);

    /**
     * Find all active bets for a player.
     *
     * @param player the player
     * @param status the bet status
     * @return a list of active bets
     */
    List<Bet> findByPlayerAndStatus(Player player, BetStatus status);

    /**
     * Find a bet by its ID.
     *
     * @param id the bet ID
     * @return the bet if found
     */
    Optional<Bet> findById(Long id);

    /**
     * Find all bets for a specific target.
     *
     * @param targetId the target ID
     * @param type the bet type
     * @return a list of bets
     */
    List<Bet> findByTargetIdAndType(String targetId, BetType type);

    /**
     * Find all active bets for a specific target.
     *
     * @param targetId the target ID
     * @param type the bet type
     * @param status the bet status
     * @return a list of active bets
     */
    List<Bet> findByTargetIdAndTypeAndStatus(String targetId, BetType type, BetStatus status);
}