package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the PlayerService interface.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    // Maps for reputation changes based on different factors
    private final Map<String, Integer> decisionReputationChanges = new HashMap<>();
    private final int duelWinReputationChange = 10;
    private final int duelLossReputationChange = -5;
    private final int socialPositiveReputationChange = 5;
    private final int socialNegativeReputationChange = -3;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
        initializeDecisionReputationChanges();
    }

    /**
     * Initialize the map of decision IDs to reputation changes.
     * This could be externalized to a configuration file or loaded from a database.
     */
    private void initializeDecisionReputationChanges() {
        // Positive moral decisions
        decisionReputationChanges.put("help_elderly", 15);
        decisionReputationChanges.put("donate_to_charity", 20);
        decisionReputationChanges.put("stand_up_to_bully", 10);
        decisionReputationChanges.put("help_injured_student", 12);
        decisionReputationChanges.put("return_lost_item", 8);
        decisionReputationChanges.put("share_knowledge", 6);
        decisionReputationChanges.put("volunteer_event", 18);
        decisionReputationChanges.put("defend_weak", 14);
        decisionReputationChanges.put("honest_confession", 16);
        decisionReputationChanges.put("sacrifice_for_others", 25);
        
        // Negative moral decisions
        decisionReputationChanges.put("cheat_on_test", -15);
        decisionReputationChanges.put("steal_from_store", -25);
        decisionReputationChanges.put("lie_to_friend", -10);
        decisionReputationChanges.put("abandon_teammate", -20);
        decisionReputationChanges.put("spread_rumors", -12);
        decisionReputationChanges.put("break_promise", -8);
        decisionReputationChanges.put("ignore_cry_for_help", -18);
        decisionReputationChanges.put("sabotage_competitor", -22);
        decisionReputationChanges.put("exploit_weakness", -16);
        decisionReputationChanges.put("betray_trust", -30);
        
        // Neutral decisions with context-dependent reputation
        decisionReputationChanges.put("neutral_choice_1", 0);
        decisionReputationChanges.put("neutral_choice_2", 0);
        decisionReputationChanges.put("pragmatic_decision", 2);
        decisionReputationChanges.put("strategic_withdrawal", 3);
        decisionReputationChanges.put("compromise_solution", 5);
    }

    @Override
    public Player findById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player findByDiscordId(String discordId) {
        return playerRepository.findByUserId(discordId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with Discord ID: " + discordId));
    }

    @Override
    public Player save(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Player increaseReputation(Long playerId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for increasing reputation");
        }

        Player player = findById(playerId);
        player.setReputation(player.getReputation() + amount);
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player decreaseReputation(Long playerId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for decreasing reputation");
        }

        Player player = findById(playerId);
        player.setReputation(player.getReputation() - amount);
        return playerRepository.save(player);
    }

    @Override
    public int getReputation(Long playerId) {
        Player player = findById(playerId);
        return player.getReputation();
    }

    @Override
    public List<Player> getPlayersByReputation() {
        List<Player> allPlayers = new ArrayList<>();
        playerRepository.findAll().forEach(allPlayers::add);

        return allPlayers.stream()
                .sorted(Comparator.comparing(Player::getReputation).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Player updateReputationFromDecision(Long playerId, String decisionId) {
        Player player = findById(playerId);

        Integer reputationChange = decisionReputationChanges.get(decisionId);
        if (reputationChange == null) {
            // Default to no change if decision not found
            return player;
        }

        if (reputationChange > 0) {
            return increaseReputation(playerId, reputationChange);
        } else if (reputationChange < 0) {
            return decreaseReputation(playerId, -reputationChange);
        } else {
            return player;
        }
    }

    @Override
    @Transactional
    public Player updateReputationFromDuel(Long playerId, boolean won) {
        if (won) {
            return increaseReputation(playerId, duelWinReputationChange);
        } else {
            return decreaseReputation(playerId, -duelLossReputationChange);
        }
    }

    @Override
    @Transactional
    public Player updateReputationFromSocialInteraction(Long playerId, Long npcId, boolean positive) {
        if (positive) {
            return increaseReputation(playerId, socialPositiveReputationChange);
        } else {
            return decreaseReputation(playerId, -socialNegativeReputationChange);
        }
    }

    @Override
    @Transactional
    public Player updatePlayerAttributes(Player player) {
        return playerRepository.save(player);
    }
}
