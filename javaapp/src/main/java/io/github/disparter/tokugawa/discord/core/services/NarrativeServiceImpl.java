package io.github.disparter.tokugawa.discord.core.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the NarrativeService interface.
 * This service manages the narrative flow, including chapter progression,
 * choice processing, and narrative validation.
 */
@Service
@Slf4j
public class NarrativeServiceImpl implements NarrativeService {


    private final ChapterRepository chapterRepository;
    private final PlayerRepository playerRepository;
    private final ProgressRepository progressRepository;
    private final ChapterLoader chapterLoader;
    private final ObjectMapper objectMapper;
    private final NarrativeValidator narrativeValidator;
    private final ConsequenceService consequenceService;
    private final PlayerService playerService;
    private final ReputationService reputationService;

    @Autowired
    public NarrativeServiceImpl(ChapterRepository chapterRepository, 
                               PlayerRepository playerRepository,
                               ProgressRepository progressRepository,
                               ChapterLoader chapterLoader,
                               ObjectMapper objectMapper,
                               NarrativeValidator narrativeValidator,
                               ConsequenceService consequenceService,
                               PlayerService playerService,
                               ReputationService reputationService) {
        this.chapterRepository = chapterRepository;
        this.playerRepository = playerRepository;
        this.progressRepository = progressRepository;
        this.chapterLoader = chapterLoader;
        this.objectMapper = objectMapper;
        this.narrativeValidator = narrativeValidator;
        this.consequenceService = consequenceService;
        this.playerService = playerService;
        this.reputationService = reputationService;
    }

    @Override
    public Chapter findChapterById(Long id) {
        return chapterRepository.findById(id).orElse(null);
    }

    @Override
    public List<Chapter> getAllChapters() {
        List<Chapter> chapters = new ArrayList<>();
        chapterRepository.findAll().forEach(chapters::add);
        return chapters;
    }

    @Override
    public List<Chapter> getAvailableChaptersForPlayer(Long playerId) {
        // Get player progress
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            log.warn("Player not found: {}", playerId);
            return new ArrayList<>();
        }

        Progress progress = progressRepository.findByPlayer(player).orElse(null);
        if (progress == null) {
            log.info("No progress found for player {}, creating new progress", playerId);
            progress = new Progress();
            progress.setPlayer(player);
            progress.setCompletedChapters(new ArrayList<>());
            progressRepository.save(progress);
        }

        // Convert player data to map for compatibility with ChapterLoader
        Map<String, Object> playerData = new HashMap<>();
        Map<String, Object> storyProgress = new HashMap<>();
        storyProgress.put("completed_chapters", progress.getCompletedChapters());
        playerData.put("story_progress", storyProgress);

        // Get available chapters
        List<String> availableChapterIds = chapterLoader.getAvailableChapters(playerData);
        List<Chapter> availableChapters = new ArrayList<>();

        for (String chapterId : availableChapterIds) {
            Chapter chapter = chapterLoader.getChapter(chapterId);
            if (chapter != null) {
                availableChapters.add(chapter);
            }
        }

        return availableChapters;
    }

    @Override
    @Transactional
    public Chapter startChapter(Long chapterId, Long playerId) {
        Chapter chapter = findChapterById(chapterId);
        if (chapter == null) {
            log.warn("Chapter not found: {}", chapterId);
            return null;
        }

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            log.warn("Player not found: {}", playerId);
            return null;
        }

        Progress progress = progressRepository.findByPlayer(player).orElse(new Progress());
        if (progress.getId() == null) {
            progress.setPlayer(player);
            progress.setCompletedChapters(new ArrayList<>());
        }

        // Set current chapter
        progress.setCurrentChapterId(chapter.getChapterId());
        progress.setCurrentDialogueIndex(0);
        progressRepository.save(progress);

        log.info("Started chapter {} for player {}", chapter.getChapterId(), playerId);
        return chapter;
    }

    @Override
    @Transactional
    public Chapter completeChapter(Long chapterId, Long playerId) {
        Chapter chapter = findChapterById(chapterId);
        if (chapter == null) {
            log.warn("Chapter not found: {}", chapterId);
            return null;
        }

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            log.warn("Player not found: {}", playerId);
            return null;
        }

        Progress progress = progressRepository.findByPlayer(player).orElse(null);
        if (progress == null) {
            log.warn("No progress found for player {}", playerId);
            return null;
        }

        // Add chapter to completed chapters if not already completed
        if (!progress.getCompletedChapters().contains(chapter.getChapterId())) {
            progress.getCompletedChapters().add(chapter.getChapterId());
        }

        // Award completion rewards
        if (chapter.getCompletionExp() != null) {
            player.setExperience(player.getExperience() + chapter.getCompletionExp());
        }

        if (chapter.getCompletionReward() != null) {
            player.setCurrency(player.getCurrency() + chapter.getCompletionReward());
        }

        // Clear current chapter if it's the completed one
        if (chapter.getChapterId().equals(progress.getCurrentChapterId())) {
            progress.setCurrentChapterId(null);
            progress.setCurrentDialogueIndex(0);
        }

        // Save changes
        playerRepository.save(player);
        progressRepository.save(progress);

        log.info("Completed chapter {} for player {}", chapter.getChapterId(), playerId);
        return chapter;
    }

    /**
     * Process a player's choice in the current chapter.
     *
     * @param playerId the player ID
     * @param choiceIndex the index of the chosen option
     * @return a map containing updated player data and chapter information
     */
    @Override
    @Transactional
    public Map<String, Object> processChoice(Long playerId, int choiceIndex) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            log.warn("Player not found: {}", playerId);
            return createErrorResponse("Player not found");
        }

        Progress progress = progressRepository.findByPlayerId(playerId).orElse(null);
        if (progress == null) {
            log.warn("No progress found for player {}", playerId);
            return createErrorResponse("No progress found for player");
        }

        String currentChapterId = progress.getCurrentChapterId();
        if (currentChapterId == null) {
            log.warn("No current chapter for player {}", playerId);
            return createErrorResponse("No current chapter");
        }

        // Get the current chapter
        Optional<Chapter> chapterOpt = chapterRepository.findByChapterId(currentChapterId);
        if (chapterOpt.isEmpty()) {
            log.warn("Current chapter not found: {}", currentChapterId);
            return createErrorResponse("Current chapter not found");
        }

        Chapter chapter = chapterOpt.get();
        int currentDialogueIndex = progress.getCurrentDialogueIndex();

        // Get choices based on the current dialogue index
        List<String> choices;

        // Check if we should use dialogues or choices
        List<String> dialogues = chapter.getDialogues();
        if (dialogues != null && !dialogues.isEmpty() && currentDialogueIndex < dialogues.size()) {
            // We're in a dialogue, get choices from the current dialogue
            try {
                String dialogueJson = dialogues.get(currentDialogueIndex);
                Map<String, Object> dialogueData = objectMapper.readValue(dialogueJson, Map.class);
                Object dialogueChoices = dialogueData.get("choices");

                if (dialogueChoices instanceof List) {
                    choices = new ArrayList<>();
                    for (Object choice : (List) dialogueChoices) {
                        if (choice instanceof String) {
                            choices.add((String) choice);
                        } else if (choice instanceof Map) {
                            choices.add(objectMapper.writeValueAsString(choice));
                        }
                    }
                } else {
                    // No choices in this dialogue, use chapter choices
                    choices = chapter.getChoices();
                }
            } catch (Exception e) {
                log.error("Error parsing dialogue JSON: {}", e.getMessage(), e);
                choices = chapter.getChoices();
            }
        } else {
            // No dialogues or past all dialogues, use chapter choices
            choices = chapter.getChoices();
        }

        if (choices == null || choices.isEmpty()) {
            log.warn("No choices available for chapter {} at dialogue index {}", currentChapterId, currentDialogueIndex);
            return createErrorResponse("No choices available");
        }

        if (choiceIndex < 0 || choiceIndex >= choices.size()) {
            log.warn("Invalid choice index {} for chapter {}", choiceIndex, currentChapterId);
            return createErrorResponse("Invalid choice index");
        }

        // Process the choice
        String choice = choices.get(choiceIndex);
        Map<String, Object> choiceData;

        try {
            // If the choice is a JSON string, parse it
            if (choice.startsWith("{")) {
                choiceData = objectMapper.readValue(choice, Map.class);
            } else {
                // If it's a simple string, create a simple map
                choiceData = new HashMap<>();
                choiceData.put("text", choice);
                choiceData.put("next_dialogue", currentDialogueIndex + 1);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing choice JSON: {}", e.getMessage(), e);
            return createErrorResponse("Error parsing choice data");
        }

        // Record the choice in player progress
        Map<String, String> storyChoices = progress.getChoices();
        if (storyChoices == null) {
            storyChoices = new HashMap<>();
            progress.setChoices(storyChoices);
        }
        String choiceKey = currentChapterId + "_dialogue_" + currentDialogueIndex;
        storyChoices.put(choiceKey, String.valueOf(choiceIndex));

        // Register the choice and its consequences
        String choiceText = choiceData.containsKey("text") ? (String) choiceData.get("text") : choice;
        String decisionContext = "Chapter " + currentChapterId + ", Dialogue " + currentDialogueIndex;
        String consequenceName = "Choice in " + chapter.getTitle();
        String consequenceDescription = "You chose: " + choiceText;

        List<String> effectsList = new ArrayList<>();

        // Apply choice effects if they exist
        if (choiceData.containsKey("effects")) {
            Map<String, Object> effects = (Map<String, Object>) choiceData.get("effects");
            applyChoiceEffects(player, progress, effects);

            // Convert effects to a list of strings for the consequence
            for (Map.Entry<String, Object> entry : effects.entrySet()) {
                effectsList.add(entry.getKey() + ":" + entry.getValue());
            }

            // Update reputation if applicable
            if (effects.containsKey("faction_reputation")) {
                Map<String, Object> factionChanges = (Map<String, Object>) effects.get("faction_reputation");
                for (Map.Entry<String, Object> entry : factionChanges.entrySet()) {
                    String factionId = entry.getKey();
                    int change = ((Number) entry.getValue()).intValue();
                    reputationService.updateFactionReputation(playerId, factionId, change);
                }
            }
        }

        // Always update player attributes
        playerService.updatePlayerAttributes(player);

        // Use ConsequenceService to track the decision - always track even if no effects
        consequenceService.trackPlayerDecision(
            playerId,
            currentChapterId,
            "scene_" + currentDialogueIndex,
            choiceText,
            decisionContext,
            consequenceName,
            consequenceDescription,
            Consequence.ConsequenceType.IMMEDIATE,
            effectsList,
            List.of(choiceKey),
            new ArrayList<>()
        );

        // Update dialogue index
        if (choiceData.containsKey("next_dialogue")) {
            progress.setCurrentDialogueIndex(((Number) choiceData.get("next_dialogue")).intValue());
        } else {
            // Default to next dialogue
            progress.setCurrentDialogueIndex(currentDialogueIndex + 1);
        }

        // Check if this choice completes the chapter
        if (choiceData.containsKey("complete_chapter") && (Boolean) choiceData.get("complete_chapter")) {
            completeChapter(chapter.getId(), playerId);
        }

        // Check if this choice moves to a scene
        if (choiceData.containsKey("next_scene")) {
            String nextSceneId = (String) choiceData.get("next_scene");

            // Find the scene in the chapter's scenes
            List<String> scenes = chapter.getScenes();
            if (scenes != null && !scenes.isEmpty()) {
                for (int i = 0; i < scenes.size(); i++) {
                    try {
                        Map<String, Object> sceneData = objectMapper.readValue(scenes.get(i), Map.class);
                        String sceneId = (String) sceneData.get("scene_id");

                        if (nextSceneId.equals(sceneId)) {
                            // Found the scene, update progress
                            progress.setCurrentDialogueIndex(i);
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Error parsing scene JSON: {}", e.getMessage(), e);
                    }
                }
            }
        }

        // Save changes
        playerRepository.save(player);
        progressRepository.save(progress);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("player", player);
        response.put("progress", progress);
        response.put("next_dialogue_index", progress.getCurrentDialogueIndex());

        // Check if we need to move to the next chapter
        if (choiceData.containsKey("next_chapter")) {
            String nextChapterId = (String) choiceData.get("next_chapter");
            Optional<Chapter> nextChapterOpt = chapterRepository.findByChapterId(nextChapterId);
            if (nextChapterOpt.isPresent()) {
                response.put("next_chapter", nextChapterOpt.get());

                // Update progress to point to the next chapter
                progress.setCurrentChapterId(nextChapterId);
                progress.setCurrentDialogueIndex(0);
                progressRepository.save(progress);
            }
        } else if (progress.getCurrentDialogueIndex() >= dialogues.size()) {
            // We've reached the end of dialogues, check if there's a next chapter
            String nextChapterId = chapter.getNextChapterId();
            if (nextChapterId != null && !nextChapterId.isEmpty()) {
                Optional<Chapter> nextChapterOpt = chapterRepository.findByChapterId(nextChapterId);
                if (nextChapterOpt.isPresent()) {
                    response.put("next_chapter", nextChapterOpt.get());

                    // Update progress to point to the next chapter
                    progress.setCurrentChapterId(nextChapterId);
                    progress.setCurrentDialogueIndex(0);
                    progressRepository.save(progress);
                }
            }
        }

        log.info("Processed choice {} for player {} in chapter {}", 
                choiceIndex, playerId, currentChapterId);
        return response;
    }

    /**
     * Apply effects from a choice to player data.
     *
     * @param player the player
     * @param progress the player's progress
     * @param effects the effects to apply
     */
    private void applyChoiceEffects(Player player, Progress progress, Map<String, Object> effects) {
        // Apply attribute changes
        if (effects.containsKey("attributes")) {
            Map<String, Object> attributes = (Map<String, Object>) effects.get("attributes");
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                String attribute = entry.getKey();
                int change = ((Number) entry.getValue()).intValue();

                switch (attribute) {
                    case "strength":
                        player.setStrength(player.getStrength() + change);
                        break;
                    case "intelligence":
                        player.setIntelligence(player.getIntelligence() + change);
                        break;
                    case "charisma":
                        player.setCharisma(player.getCharisma() + change);
                        break;
                    case "agility":
                        player.setAgility(player.getAgility() + change);
                        break;
                    default:
                        log.warn("Unknown attribute: {}", attribute);
                }
            }
        }

        // Apply relationship changes
        if (effects.containsKey("relationships")) {
            Map<String, Object> relationships = (Map<String, Object>) effects.get("relationships");
            Map<String, Integer> playerRelationships = progress.getRelationships();
            if (playerRelationships == null) {
                playerRelationships = new HashMap<>();
                progress.setRelationships(playerRelationships);
            }

            for (Map.Entry<String, Object> entry : relationships.entrySet()) {
                String npcId = entry.getKey();
                int change = ((Number) entry.getValue()).intValue();

                int currentValue = playerRelationships.getOrDefault(npcId, 0);
                playerRelationships.put(npcId, Math.max(-100, Math.min(100, currentValue + change)));
            }
        }

        // Apply faction reputation changes
        if (effects.containsKey("faction_reputation")) {
            Map<String, Object> factionChanges = (Map<String, Object>) effects.get("faction_reputation");
            Map<String, Integer> factionReputations = progress.getFactionReputations();
            if (factionReputations == null) {
                factionReputations = new HashMap<>();
                progress.setFactionReputations(factionReputations);
            }

            for (Map.Entry<String, Object> entry : factionChanges.entrySet()) {
                String factionId = entry.getKey();
                int change = ((Number) entry.getValue()).intValue();

                int currentValue = factionReputations.getOrDefault(factionId, 0);
                factionReputations.put(factionId, Math.max(-100, Math.min(100, currentValue + change)));
            }
        }

        // Apply currency changes
        if (effects.containsKey("currency")) {
            int change = ((Number) effects.get("currency")).intValue();
            player.setCurrency(player.getCurrency() + change);
        }

        // Apply experience changes
        if (effects.containsKey("experience")) {
            int change = ((Number) effects.get("experience")).intValue();
            player.setExperience(player.getExperience() + change);
        }
    }

    /**
     * Create an error response map.
     *
     * @param message the error message
     * @return a map containing the error message
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }

    /**
     * Validate the narrative flow of all chapters.
     * This checks for broken links, missing chapters, and other issues.
     *
     * @return a list of validation errors, or an empty list if no errors were found
     */
    @Override
    public List<String> validateNarrative() {
        // Delegate to the NarrativeValidator
        return narrativeValidator.validateNarrative();
    }

    /**
     * Update player progress based on a duel result.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @param playerWon whether the player won the duel
     * @return true if the progress was updated successfully, false otherwise
     */
    @Override
    @Transactional
    public boolean updateProgressFromDuel(Long playerId, Long npcId, Boolean playerWon) {
        try {
            Player player = playerRepository.findById(playerId).orElse(null);
            if (player == null) {
                log.warn("Player not found: {}", playerId);
                return false;
            }

            Progress progress = progressRepository.findByPlayer(player).orElse(null);
            if (progress == null) {
                log.info("No progress found for player {}, creating new progress", playerId);
                progress = new Progress();
                progress.setPlayer(player);
                progress.setCompletedChapters(new ArrayList<>());
                progressRepository.save(progress);
            }

            // Update relationships based on duel outcome
            Map<String, Integer> relationships = progress.getRelationships();
            if (relationships == null) {
                relationships = new HashMap<>();
                progress.setRelationships(relationships);
            }

            // Update relationship with the NPC
            String npcKey = npcId.toString();
            int currentRelationship = relationships.getOrDefault(npcKey, 0);
            int relationshipChange = playerWon ? 10 : -5; // Increase if won, decrease if lost
            relationships.put(npcKey, Math.max(-100, Math.min(100, currentRelationship + relationshipChange)));

            // Update player stats based on duel outcome
            if (playerWon) {
                // Increase experience
                int expGain = 20; // Base experience gain for winning a duel
                player.setExperience(player.getExperience() + expGain);

                // Possibly increase a random stat
                int statIncrease = 1;
                int randomStat = (int) (Math.random() * 4); // 0-3 for the four main stats
                switch (randomStat) {
                    case 0:
                        player.setStrength(player.getStrength() + statIncrease);
                        break;
                    case 1:
                        player.setAgility(player.getAgility() + statIncrease);
                        break;
                    case 2:
                        player.setIntelligence(player.getIntelligence() + statIncrease);
                        break;
                    case 3:
                        player.setCharisma(player.getCharisma() + statIncrease);
                        break;
                }
            } else {
                // Small experience gain even for losing
                int expGain = 5;
                player.setExperience(player.getExperience() + expGain);
            }

            // Save changes
            playerRepository.save(player);
            progressRepository.save(progress);

            log.info("Updated progress for player {} after duel with NPC {}, player won: {}", 
                    playerId, npcId, playerWon);
            return true;
        } catch (Exception e) {
            log.error("Error updating progress from duel: {}", e.getMessage(), e);
            return false;
        }
    }
}
