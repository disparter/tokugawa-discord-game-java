package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import io.github.disparter.tokugawa.discord.core.models.Bet;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetType;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetStatus;
import io.github.disparter.tokugawa.discord.core.models.Bet.BetResult;
import io.github.disparter.tokugawa.discord.core.repositories.BetRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the BettingService interface.
 */
@Service
@Slf4j
public class BettingServiceImpl implements BettingService {


    private final BetRepository betRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public BettingServiceImpl(BetRepository betRepository, PlayerRepository playerRepository) {
        this.betRepository = betRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional
    public Optional<Bet> placeBet(Long playerId, BetType betType, Integer amount, String targetId) {
        try {
            // Check if player exists
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                log.error("Player not found: {}", playerId);
                return Optional.empty();
            }

            Player player = playerOpt.get();

            // Check if player has enough currency
            if (player.getCurrency() < amount) {
                log.error("Player {} does not have enough currency for bet: {}", playerId, amount);
                return Optional.empty();
            }

            // Create bet
            Bet bet = new Bet();
            bet.setPlayer(player);
            bet.setType(betType);
            bet.setAmount(amount);
            bet.setTargetId(targetId);
            bet.setStatus(BetStatus.ACTIVE);

            // Deduct currency from player
            player.setCurrency(player.getCurrency() - amount);
            playerRepository.save(player);

            // Save bet
            return Optional.of(betRepository.save(bet));
        } catch (Exception e) {
            log.error("Error placing bet for player {}: {}", playerId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Bet> getActiveBets(Long playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                log.error("Player not found: {}", playerId);
                return new ArrayList<>();
            }

            return betRepository.findByPlayerAndStatus(playerOpt.get(), BetStatus.ACTIVE);
        } catch (Exception e) {
            log.error("Error getting active bets for player {}: {}", playerId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Bet> getBetHistory(Long playerId) {
        try {
            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isEmpty()) {
                log.error("Player not found: {}", playerId);
                return new ArrayList<>();
            }

            return betRepository.findByPlayer(playerOpt.get());
        } catch (Exception e) {
            log.error("Error getting bet history for player {}: {}", playerId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getBettingStats(Long playerId) {
        try {
            List<Bet> bets = getBetHistory(playerId);

            int totalBets = bets.size();
            int totalAmount = bets.stream().mapToInt(Bet::getAmount).sum();
            int wins = (int) bets.stream().filter(b -> BetResult.WIN.equals(b.getResult())).count();
            int losses = (int) bets.stream().filter(b -> BetResult.LOSE.equals(b.getResult())).count();
            int totalWinnings = bets.stream()
                .filter(b -> b.getWinnings() != null)
                .mapToInt(Bet::getWinnings)
                .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBets", totalBets);
            stats.put("totalAmount", totalAmount);
            stats.put("wins", wins);
            stats.put("losses", losses);
            stats.put("totalWinnings", totalWinnings);

            return stats;
        } catch (Exception e) {
            log.error("Error getting betting stats for player {}: {}", playerId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional
    public Optional<Bet> resolveBet(Long betId, BetResult result) {
        try {
            Optional<Bet> betOpt = betRepository.findById(betId);
            if (betOpt.isEmpty()) {
                log.error("Bet not found: {}", betId);
                return Optional.empty();
            }

            Bet bet = betOpt.get();

            // Check if bet is already resolved
            if (BetStatus.COMPLETED.equals(bet.getStatus()) || BetStatus.CANCELLED.equals(bet.getStatus())) {
                log.error("Bet is already resolved: {}", betId);
                return Optional.empty();
            }

            // Calculate winnings
            int winnings = 0;
            if (BetResult.WIN.equals(result)) {
                // Simple 2x payout for now
                winnings = bet.getAmount() * 2;
            }

            // Update bet
            bet.setStatus(BetStatus.COMPLETED);
            bet.setResult(result);
            bet.setWinnings(winnings);

            // Add winnings to player if won
            if (winnings > 0) {
                Player player = bet.getPlayer();
                player.setCurrency(player.getCurrency() + winnings);
                playerRepository.save(player);
            }

            return Optional.of(betRepository.save(bet));
        } catch (Exception e) {
            log.error("Error resolving bet {}: {}", betId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public int resolveDuelBets(String duelId, Long winnerId) {
        try {
            List<Bet> bets = betRepository.findByTargetIdAndTypeAndStatus(duelId, BetType.DUEL, BetStatus.ACTIVE);

            int resolvedCount = 0;
            for (Bet bet : bets) {
                BetResult result = bet.getPlayer().getId().equals(winnerId) ? BetResult.WIN : BetResult.LOSE;
                if (resolveBet(bet.getId(), result).isPresent()) {
                    resolvedCount++;
                }
            }

            return resolvedCount;
        } catch (Exception e) {
            log.error("Error resolving duel bets for duel {}: {}", duelId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int resolveEventBets(String eventId, List<Long> winnerIds) {
        try {
            List<Bet> bets = betRepository.findByTargetIdAndTypeAndStatus(eventId, BetType.EVENT, BetStatus.ACTIVE);

            int resolvedCount = 0;
            for (Bet bet : bets) {
                BetResult result = winnerIds.contains(bet.getPlayer().getId()) ? BetResult.WIN : BetResult.LOSE;
                if (resolveBet(bet.getId(), result).isPresent()) {
                    resolvedCount++;
                }
            }

            return resolvedCount;
        } catch (Exception e) {
            log.error("Error resolving event bets for event {}: {}", eventId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int resolveCompetitionBets(String competitionId, List<Long> winnerIds) {
        try {
            List<Bet> bets = betRepository.findByTargetIdAndTypeAndStatus(competitionId, BetType.COMPETITION, BetStatus.ACTIVE);

            int resolvedCount = 0;
            for (Bet bet : bets) {
                BetResult result = winnerIds.contains(bet.getPlayer().getId()) ? BetResult.WIN : BetResult.LOSE;
                if (resolveBet(bet.getId(), result).isPresent()) {
                    resolvedCount++;
                }
            }

            return resolvedCount;
        } catch (Exception e) {
            log.error("Error resolving competition bets for competition {}: {}", competitionId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean cancelBet(Long betId) {
        try {
            Optional<Bet> betOpt = betRepository.findById(betId);
            if (betOpt.isEmpty()) {
                log.error("Bet not found: {}", betId);
                return false;
            }

            Bet bet = betOpt.get();

            // Check if bet is already resolved
            if (BetStatus.COMPLETED.equals(bet.getStatus()) || BetStatus.CANCELLED.equals(bet.getStatus())) {
                log.error("Bet is already resolved: {}", betId);
                return false;
            }

            // Update bet
            bet.setStatus(BetStatus.CANCELLED);

            // Refund player
            Player player = bet.getPlayer();
            player.setCurrency(player.getCurrency() + bet.getAmount());
            playerRepository.save(player);

            betRepository.save(bet);
            return true;
        } catch (Exception e) {
            log.error("Error cancelling bet {}: {}", betId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getBettingRanking(int limit) {
        try {
            // Get all players
            List<Player> players = playerRepository.findAll();

            // Calculate betting stats for each player
            List<Map<String, Object>> ranking = new ArrayList<>();
            for (Player player : players) {
                List<Bet> bets = betRepository.findByPlayer(player);
                if (bets.isEmpty()) {
                    continue;
                }

                int totalBets = bets.size();
                int totalAmount = bets.stream().mapToInt(Bet::getAmount).sum();
                int wins = (int) bets.stream().filter(b -> BetResult.WIN.equals(b.getResult())).count();
                int totalWinnings = bets.stream()
                    .filter(b -> b.getWinnings() != null)
                    .mapToInt(Bet::getWinnings)
                    .sum();

                Map<String, Object> playerStats = new HashMap<>();
                playerStats.put("playerId", player.getId());
                playerStats.put("playerName", player.getName());
                playerStats.put("totalBets", totalBets);
                playerStats.put("totalAmount", totalAmount);
                playerStats.put("wins", wins);
                playerStats.put("totalWinnings", totalWinnings);

                ranking.add(playerStats);
            }

            // Sort by total amount bet
            ranking.sort((a, b) -> Integer.compare((int) b.get("totalAmount"), (int) a.get("totalAmount")));

            // Limit results
            return ranking.stream().limit(limit).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting betting ranking: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public int cancelDuelBets(String duelId) {
        try {
            List<Bet> bets = betRepository.findByTargetIdAndTypeAndStatus(duelId, BetType.DUEL, BetStatus.ACTIVE);

            int cancelledCount = 0;
            for (Bet bet : bets) {
                if (cancelBet(bet.getId())) {
                    cancelledCount++;
                }
            }

            return cancelledCount;
        } catch (Exception e) {
            log.error("Error cancelling duel bets for duel {}: {}", duelId, e.getMessage(), e);
            return 0;
        }
    }
}
