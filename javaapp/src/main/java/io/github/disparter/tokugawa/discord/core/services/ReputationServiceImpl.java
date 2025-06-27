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
 * Implementation of the ReputationService interface.
 */
@Service
public class ReputationServiceImpl implements ReputationService {

    private final PlayerRepository playerRepository;
    private final RelationshipService relationshipService;
    private final ClubService clubService;

    // Reputation changes for different decision types
    private static final Map<String, Integer> DECISION_REPUTATION_CHANGES = new HashMap<>();

    // Reputation changes for different social interaction types
    private static final Map<String, Integer> INTERACTION_REPUTATION_CHANGES = new HashMap<>();

    // Reputation thresholds
    private static final int RESPECTED_THRESHOLD = 50;
    private static final int ADMIRED_THRESHOLD = 100;
    private static final int REVERED_THRESHOLD = 200;
    private static final int LEGENDARY_THRESHOLD = 500;

    static {
        // Initialize decision reputation changes
        DECISION_REPUTATION_CHANGES.put("honorable_choice", 10);
        DECISION_REPUTATION_CHANGES.put("dishonest_choice", -10);
        DECISION_REPUTATION_CHANGES.put("heroic_action", 20);
        DECISION_REPUTATION_CHANGES.put("cowardly_action", -15);
        DECISION_REPUTATION_CHANGES.put("leadership_decision", 15);
        DECISION_REPUTATION_CHANGES.put("selfish_decision", -10);

        // Initialize interaction reputation changes
        INTERACTION_REPUTATION_CHANGES.put("public_praise", 5);
        INTERACTION_REPUTATION_CHANGES.put("public_insult", -5);
        INTERACTION_REPUTATION_CHANGES.put("help_club", 10);
        INTERACTION_REPUTATION_CHANGES.put("sabotage_club", -15);
        INTERACTION_REPUTATION_CHANGES.put("defend_npc", 8);
        INTERACTION_REPUTATION_CHANGES.put("betray_npc", -12);
    }

    @Autowired
    public ReputationServiceImpl(PlayerRepository playerRepository,
                                RelationshipService relationshipService,
                                ClubService clubService) {
        this.playerRepository = playerRepository;
        this.relationshipService = relationshipService;
        this.clubService = clubService;
    }

    @Override
    public int getReputation(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        return player.getReputation();
    }

    @Override
    @Transactional
    public Player increaseReputation(Long playerId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for increasing reputation");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        int newReputation = player.getReputation() + amount;
        player.setReputation(newReputation);

        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player decreaseReputation(Long playerId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for decreasing reputation");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        int newReputation = Math.max(0, player.getReputation() - amount);
        player.setReputation(newReputation);

        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player updateReputationByDecision(Long playerId, String decisionId) {
        if (!DECISION_REPUTATION_CHANGES.containsKey(decisionId)) {
            throw new IllegalArgumentException("Unknown decision ID: " + decisionId);
        }

        int reputationChange = DECISION_REPUTATION_CHANGES.get(decisionId);

        if (reputationChange > 0) {
            return increaseReputation(playerId, reputationChange);
        } else if (reputationChange < 0) {
            return decreaseReputation(playerId, Math.abs(reputationChange));
        } else {
            return playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        }
    }

    @Override
    @Transactional
    public Player updateReputationByDuel(Long playerId, boolean won, int opponentLevel) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        int playerLevel = player.getLevel();
        int levelDifference = opponentLevel - playerLevel;

        int reputationChange;
        if (won) {
            // Winning against a higher-level opponent gives more reputation
            reputationChange = 5 + Math.max(0, levelDifference * 2);
            return increaseReputation(playerId, reputationChange);
        } else {
            // Losing against a lower-level opponent loses more reputation
            reputationChange = 3 + Math.max(0, -levelDifference);
            return decreaseReputation(playerId, reputationChange);
        }
    }

    @Override
    @Transactional
    public Player updateReputationBySocialInteraction(Long playerId, String interactionType, Long targetId) {
        if (!INTERACTION_REPUTATION_CHANGES.containsKey(interactionType)) {
            throw new IllegalArgumentException("Unknown interaction type: " + interactionType);
        }

        int reputationChange = INTERACTION_REPUTATION_CHANGES.get(interactionType);

        if (reputationChange > 0) {
            return increaseReputation(playerId, reputationChange);
        } else if (reputationChange < 0) {
            return decreaseReputation(playerId, Math.abs(reputationChange));
        } else {
            return playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        }
    }

    @Override
    public List<Player> getRankedPlayers() {
        List<Player> allPlayers = new ArrayList<>();
        playerRepository.findAll().forEach(allPlayers::add);

        return allPlayers.stream()
                .sorted(Comparator.comparing(Player::getReputation).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public int getReputationRank(Long playerId) {
        List<Player> rankedPlayers = getRankedPlayers();

        for (int i = 0; i < rankedPlayers.size(); i++) {
            if (rankedPlayers.get(i).getId().equals(playerId)) {
                return i + 1; // +1 because index is 0-based
            }
        }

        throw new IllegalArgumentException("Player not found with ID: " + playerId);
    }

    @Override
    public boolean hasReachedReputationThreshold(Long playerId, int threshold) {
        int reputation = getReputation(playerId);
        return reputation >= threshold;
    }

    @Override
    @Transactional
    public Player updateFactionReputation(Long playerId, String factionId, int change) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // In a real implementation, this would update faction-specific reputation
        // For now, we'll just update the player's general reputation
        if (change > 0) {
            return increaseReputation(playerId, change);
        } else if (change < 0) {
            return decreaseReputation(playerId, Math.abs(change));
        } else {
            return player;
        }
    }
}
