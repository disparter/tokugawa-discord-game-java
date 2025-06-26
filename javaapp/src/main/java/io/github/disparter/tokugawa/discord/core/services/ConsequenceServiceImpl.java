package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Consequence.ConsequenceType;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.ConsequenceRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the ConsequenceService interface.
 */
@Service
public class ConsequenceServiceImpl implements ConsequenceService {

    private final ConsequenceRepository consequenceRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public ConsequenceServiceImpl(ConsequenceRepository consequenceRepository, PlayerRepository playerRepository) {
        this.consequenceRepository = consequenceRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional
    public Consequence createConsequence(Long playerId, String name, String description, 
                                        ConsequenceType type, List<String> effects, 
                                        List<String> relatedChoices, List<Long> affectedNpcs) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        Consequence consequence = new Consequence();
        consequence.setConsequenceId("consequence_" + UUID.randomUUID().toString().substring(0, 8));
        consequence.setName(name);
        consequence.setDescription(description);
        consequence.setType(type);
        consequence.setPlayer(player);
        consequence.setCreatedAt(LocalDateTime.now());
        consequence.setActive(true);
        
        if (effects != null) {
            consequence.setEffects(new ArrayList<>(effects));
        }
        
        if (relatedChoices != null) {
            consequence.setRelatedChoices(new ArrayList<>(relatedChoices));
        }
        
        if (affectedNpcs != null) {
            consequence.setAffectedNpcs(new ArrayList<>(affectedNpcs));
        }
        
        return consequenceRepository.save(consequence);
    }

    @Override
    public List<Consequence> getConsequencesForPlayer(Long playerId) {
        return consequenceRepository.findByPlayerId(playerId);
    }

    @Override
    public List<Consequence> getActiveConsequencesForPlayer(Long playerId) {
        return consequenceRepository.findByPlayerIdAndActiveTrue(playerId);
    }

    @Override
    public List<Consequence> getConsequencesByType(Long playerId, ConsequenceType type) {
        return consequenceRepository.findByPlayerIdAndType(playerId, type);
    }

    @Override
    public List<Consequence> getConsequencesForChoice(Long playerId, String choiceId) {
        return consequenceRepository.findByPlayerIdAndRelatedChoicesContaining(playerId, choiceId);
    }

    @Override
    public List<Consequence> getConsequencesAffectingNpc(Long playerId, Long npcId) {
        return consequenceRepository.findByPlayerIdAndAffectedNpcsContaining(playerId, npcId);
    }

    @Override
    @Transactional
    public Consequence deactivateConsequence(Long consequenceId) {
        Consequence consequence = consequenceRepository.findById(consequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Consequence not found with ID: " + consequenceId));
        
        consequence.setActive(false);
        return consequenceRepository.save(consequence);
    }

    @Override
    public Map<String, List<Consequence>> getDecisionDashboard(Long playerId) {
        List<Consequence> allConsequences = getConsequencesForPlayer(playerId);
        Map<String, List<Consequence>> dashboard = new HashMap<>();
        
        // Group by type
        dashboard.put("immediate", allConsequences.stream()
                .filter(c -> c.getType() == ConsequenceType.IMMEDIATE)
                .collect(Collectors.toList()));
        
        dashboard.put("short_term", allConsequences.stream()
                .filter(c -> c.getType() == ConsequenceType.SHORT_TERM)
                .collect(Collectors.toList()));
        
        dashboard.put("long_term", allConsequences.stream()
                .filter(c -> c.getType() == ConsequenceType.LONG_TERM)
                .collect(Collectors.toList()));
        
        dashboard.put("permanent", allConsequences.stream()
                .filter(c -> c.getType() == ConsequenceType.PERMANENT)
                .collect(Collectors.toList()));
        
        // Group by active status
        dashboard.put("active", allConsequences.stream()
                .filter(Consequence::isActive)
                .collect(Collectors.toList()));
        
        dashboard.put("inactive", allConsequences.stream()
                .filter(c -> !c.isActive())
                .collect(Collectors.toList()));
        
        return dashboard;
    }

    @Override
    @Transactional
    public Player applyConsequenceEffects(Player player) {
        List<Consequence> activeConsequences = getActiveConsequencesForPlayer(player.getId());
        
        // This is a simplified implementation
        // In a real implementation, you would parse the effects and apply them to the player
        // For example, effects could be strings like "reputation:+10" or "currency:-5"
        
        for (Consequence consequence : activeConsequences) {
            for (String effect : consequence.getEffects()) {
                applyEffect(player, effect);
            }
        }
        
        return playerRepository.save(player);
    }
    
    /**
     * Applies an effect to a player.
     * 
     * @param player the player
     * @param effect the effect to apply
     */
    private void applyEffect(Player player, String effect) {
        // Parse the effect string
        // Format: "attribute:value"
        String[] parts = effect.split(":");
        if (parts.length != 2) {
            return;
        }
        
        String attribute = parts[0];
        String valueStr = parts[1];
        
        try {
            // Handle different attributes
            switch (attribute) {
                case "reputation":
                    int reputationChange = Integer.parseInt(valueStr);
                    player.setReputation(player.getReputation() + reputationChange);
                    break;
                case "currency":
                    int currencyChange = Integer.parseInt(valueStr);
                    player.setCurrency(player.getCurrency() + currencyChange);
                    break;
                case "experience":
                    int experienceChange = Integer.parseInt(valueStr);
                    player.setExperience(player.getExperience() + experienceChange);
                    break;
                // Add more attributes as needed
            }
        } catch (NumberFormatException e) {
            // Ignore invalid values
        }
    }
}