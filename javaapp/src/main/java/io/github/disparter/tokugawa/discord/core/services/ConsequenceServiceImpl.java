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

        // Parse the effects and apply them to the player
        // Effects are strings like "reputation:+10", "currency:-5", "stat:strength:+2", etc.
        for (Consequence consequence : activeConsequences) {
            if (consequence.getEffects() != null) {
                for (String effect : consequence.getEffects()) {
                    applyEffect(player, effect);
                }
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
        // Format: "attribute:value" or "stat:statName:value"
        String[] parts = effect.split(":");
        if (parts.length < 2) {
            return;
        }

        String attribute = parts[0];

        try {
            // Handle different attributes
            switch (attribute) {
                case "reputation":
                    int reputationChange = Integer.parseInt(parts[1]);
                    player.setReputation(player.getReputation() + reputationChange);
                    break;
                case "currency":
                    int currencyChange = Integer.parseInt(parts[1]);
                    player.setCurrency(player.getCurrency() + currencyChange);
                    break;
                case "experience":
                    int experienceChange = Integer.parseInt(parts[1]);
                    player.setExperience(player.getExperience() + experienceChange);
                    break;
                case "level":
                    int levelChange = Integer.parseInt(parts[1]);
                    player.setLevel(Math.max(1, player.getLevel() + levelChange));
                    break;
                case "exp":
                    int expChange = Integer.parseInt(parts[1]);
                    player.setExp(Math.max(0, player.getExp() + expChange));
                    break;
                case "points":
                    int pointsChange = Integer.parseInt(parts[1]);
                    player.setPoints(Math.max(0, player.getPoints() + pointsChange));
                    break;
                case "stat":
                    if (parts.length >= 3) {
                        String statName = parts[1];
                        int statChange = Integer.parseInt(parts[2]);
                        if (player.getStats() == null) {
                            player.setStats(new HashMap<>());
                        }
                        int currentValue = player.getStats().getOrDefault(statName, 0);
                        player.getStats().put(statName, Math.max(0, currentValue + statChange));
                    }
                    break;
                case "skill":
                    if (parts.length >= 3) {
                        String skillName = parts[1];
                        int skillChange = Integer.parseInt(parts[2]);
                        if (player.getSkills() == null) {
                            player.setSkills(new HashMap<>());
                        }
                        int currentValue = player.getSkills().getOrDefault(skillName, 0);
                        player.getSkills().put(skillName, Math.max(0, currentValue + skillChange));
                    }
                    break;
                // Add more attributes as needed
            }
        } catch (NumberFormatException e) {
            // Ignore invalid values
        }
    }

    @Override
    @Transactional
    public Consequence trackPlayerDecision(Long playerId, String chapterId, String sceneId, 
                                         String choiceMade, String decisionContext, String name, 
                                         String description, ConsequenceType type, List<String> effects, 
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
        consequence.setChapterId(chapterId);
        consequence.setSceneId(sceneId);
        consequence.setChoiceMade(choiceMade);
        consequence.setDecisionContext(decisionContext);

        if (effects != null) {
            consequence.setEffects(new ArrayList<>(effects));
        }

        if (relatedChoices != null) {
            consequence.setRelatedChoices(new ArrayList<>(relatedChoices));
        }

        if (affectedNpcs != null) {
            consequence.setAffectedNpcs(new ArrayList<>(affectedNpcs));
        }

        // Calculate and set the community choice percentage
        double percentage = getCommunityChoicePercentage(chapterId, sceneId, choiceMade);
        consequence.setCommunityChoicePercentage(percentage);

        return consequenceRepository.save(consequence);
    }

    @Override
    public double getCommunityChoicePercentage(String chapterId, String sceneId, String choiceMade) {
        long choiceCount = consequenceRepository.countByChapterIdAndSceneIdAndChoiceMade(chapterId, sceneId, choiceMade);
        long totalCount = consequenceRepository.countByChapterIdAndSceneId(chapterId, sceneId);

        if (totalCount == 0) {
            return 0.0;
        }

        return (double) choiceCount / totalCount * 100.0;
    }

    @Override
    @Transactional
    public Consequence updateCommunityChoicePercentage(Long consequenceId) {
        Consequence consequence = consequenceRepository.findById(consequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Consequence not found with ID: " + consequenceId));

        double percentage = getCommunityChoicePercentage(
                consequence.getChapterId(), 
                consequence.getSceneId(), 
                consequence.getChoiceMade());

        consequence.setCommunityChoicePercentage(percentage);
        return consequenceRepository.save(consequence);
    }

    @Override
    @Transactional
    public Consequence addEthicalReflections(Long consequenceId, List<String> reflections) {
        Consequence consequence = consequenceRepository.findById(consequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Consequence not found with ID: " + consequenceId));

        List<String> currentReflections = consequence.getEthicalReflections();
        if (currentReflections == null) {
            currentReflections = new ArrayList<>();
        }

        currentReflections.addAll(reflections);
        consequence.setEthicalReflections(currentReflections);

        return consequenceRepository.save(consequence);
    }

    @Override
    public Map<Long, List<String>> getEthicalReflectionsForPlayer(Long playerId) {
        List<Consequence> consequences = getConsequencesForPlayer(playerId);
        Map<Long, List<String>> reflectionsMap = new HashMap<>();

        for (Consequence consequence : consequences) {
            if (consequence.getEthicalReflections() != null && !consequence.getEthicalReflections().isEmpty()) {
                reflectionsMap.put(consequence.getId(), consequence.getEthicalReflections());
            }
        }

        return reflectionsMap;
    }

    @Override
    @Transactional
    public Consequence addAlternativePaths(Long consequenceId, List<String> alternativePaths) {
        Consequence consequence = consequenceRepository.findById(consequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Consequence not found with ID: " + consequenceId));

        List<String> currentPaths = consequence.getAlternativePaths();
        if (currentPaths == null) {
            currentPaths = new ArrayList<>();
        }

        currentPaths.addAll(alternativePaths);
        consequence.setAlternativePaths(currentPaths);

        return consequenceRepository.save(consequence);
    }

    @Override
    public Map<Long, List<String>> getAlternativePathsForPlayer(Long playerId) {
        List<Consequence> consequences = getConsequencesForPlayer(playerId);
        Map<Long, List<String>> pathsMap = new HashMap<>();

        for (Consequence consequence : consequences) {
            if (consequence.getAlternativePaths() != null && !consequence.getAlternativePaths().isEmpty()) {
                pathsMap.put(consequence.getId(), consequence.getAlternativePaths());
            }
        }

        return pathsMap;
    }

    @Override
    public List<Consequence> getConsequencesByChapterAndScene(Long playerId, String chapterId, String sceneId) {
        return consequenceRepository.findByPlayerIdAndChapterIdAndSceneId(playerId, chapterId, sceneId);
    }

    @Override
    public Map<String, List<Consequence>> getEnhancedDecisionDashboard(Long playerId) {
        Map<String, List<Consequence>> dashboard = getDecisionDashboard(playerId);
        List<Consequence> allConsequences = getConsequencesForPlayer(playerId);

        // Add sections for consequences with ethical reflections and alternative paths
        dashboard.put("with_reflections", allConsequences.stream()
                .filter(c -> c.getEthicalReflections() != null && !c.getEthicalReflections().isEmpty())
                .collect(Collectors.toList()));

        dashboard.put("with_alternatives", allConsequences.stream()
                .filter(c -> c.getAlternativePaths() != null && !c.getAlternativePaths().isEmpty())
                .collect(Collectors.toList()));

        // Group by chapter and scene
        Map<String, List<Consequence>> chapterSceneMap = new HashMap<>();
        for (Consequence consequence : allConsequences) {
            if (consequence.getChapterId() != null && consequence.getSceneId() != null) {
                String key = consequence.getChapterId() + ":" + consequence.getSceneId();
                chapterSceneMap.computeIfAbsent(key, k -> new ArrayList<>()).add(consequence);
            }
        }

        for (Map.Entry<String, List<Consequence>> entry : chapterSceneMap.entrySet()) {
            dashboard.put("chapter_scene_" + entry.getKey(), entry.getValue());
        }

        return dashboard;
    }
}
