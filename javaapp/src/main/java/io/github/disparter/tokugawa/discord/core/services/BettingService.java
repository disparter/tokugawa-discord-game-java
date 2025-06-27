package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Bet;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetType;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing betting-related operations.
 */
public interface BettingService {

    /**
     * Place a bet on a duel, event, or competition.
     *
     * @param playerId the player ID
     * @param betType the type of bet (DUEL, EVENT, COMPETITION)
     * @param amount the amount to bet
     * @param targetId the ID of the target (duel ID, event ID, etc.)
     * @return the created bet if successful, or empty if failed
     */
    Optional<Bet> placeBet(Long playerId, BetType betType, Integer amount, String targetId);

    /**
     * Get all active bets for a player.
     *
     * @param playerId the player ID
     * @return a list of active bets
     */
    List<Bet> getActiveBets(Long playerId);

    /**
     * Get betting history for a player.
     *
     * @param playerId the player ID
     * @return a list of all bets for the player
     */
    List<Bet> getBetHistory(Long playerId);

    /**
     * Get betting statistics for a player.
     *
     * @param playerId the player ID
     * @return a map containing betting statistics
     */
    Map<String, Object> getBettingStats(Long playerId);

    /**
     * Resolve a bet with the given result.
     *
     * @param betId the bet ID
     * @param result the result of the bet
     * @return the updated bet if successful, or empty if failed
     */
    Optional<Bet> resolveBet(Long betId, BetResult result);

    /**
     * Resolve all bets for a duel.
     *
     * @param duelId the duel ID
     * @param winnerId the ID of the winner
     * @return the number of bets resolved
     */
    int resolveDuelBets(String duelId, Long winnerId);

    /**
     * Resolve all bets for an event.
     *
     * @param eventId the event ID
     * @param winnerIds the IDs of the winners
     * @return the number of bets resolved
     */
    int resolveEventBets(String eventId, List<Long> winnerIds);

    /**
     * Resolve all bets for a competition.
     *
     * @param competitionId the competition ID
     * @param winnerIds the IDs of the winners
     * @return the number of bets resolved
     */
    int resolveCompetitionBets(String competitionId, List<Long> winnerIds);

    /**
     * Cancel a bet and refund the player.
     *
     * @param betId the bet ID
     * @return true if successful, false otherwise
     */
    boolean cancelBet(Long betId);

    /**
     * Get the betting ranking (players with the most bets or winnings).
     *
     * @param limit the maximum number of players to return
     * @return a list of players with their betting statistics
     */
    List<Map<String, Object>> getBettingRanking(int limit);
}