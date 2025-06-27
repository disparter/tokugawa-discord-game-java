package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for loading chapter data from JSON files.
 * This is a migration of the Python FileChapterLoader class.
 */
@Service
@Slf4j
public class ChapterLoader {


    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;
    private final String dataDirectory;
    private final Map<String, Chapter> chapters = new HashMap<>();

    @Autowired
    public ChapterLoader(
            ChapterRepository chapterRepository,
            ObjectMapper objectMapper,
            @Value("${tokugawa.data.directory:data}") String dataDirectory) {
        this.chapterRepository = chapterRepository;
        this.objectMapper = objectMapper;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Load all chapters from JSON files in the data directory.
     * This method scans the narrative/chapters directory and club directories for chapter files.
     */
    public void loadChapters() {
        // Load main story chapters
        Path mainChapterDir = Paths.get(dataDirectory, "narrative", "chapters");
        if (!Files.exists(mainChapterDir)) {
            log.warn("Main chapter directory not found: {}", mainChapterDir);
        } else {
            loadChaptersFromDirectory(mainChapterDir, "");
        }

        // Load club chapters
        Path clubChapterDir = Paths.get(dataDirectory, "clubs");
        if (!Files.exists(clubChapterDir)) {
            log.warn("Club chapter directory not found: {}", clubChapterDir);
        } else {
            try (Stream<Path> clubDirs = Files.list(clubChapterDir)) {
                clubDirs.filter(Files::isDirectory).forEach(clubDir -> {
                    String clubId = clubDir.getFileName().toString();
                    loadChaptersFromDirectory(clubDir, "club_" + clubId + "_");
                });
            } catch (IOException e) {
                log.error("Error listing club directories: {}", e.getMessage(), e);
            }
        }

        // Save all loaded chapters to the database
        saveChaptersToDatabase();
    }

    /**
     * Load chapters from a specific directory.
     *
     * @param directory the directory to load chapters from
     * @param prefix the prefix to add to chapter IDs
     */
    private void loadChaptersFromDirectory(Path directory, String prefix) {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.toString().endsWith(".json"))
                 .forEach(path -> {
                     try {
                         String filename = path.getFileName().toString();
                         String chapterId = prefix + filename.replace(".json", "");

                         // Read the JSON file
                         Map<String, Object> chapterData = objectMapper.readValue(path.toFile(), Map.class);

                         // Add chapter ID to the data
                         chapterData.put("chapter_id", chapterId);

                         // Create chapter from data
                         Chapter chapter = createChapter(chapterData);

                         if (chapter != null) {
                             chapters.put(chapterId, chapter);
                             log.info("Loaded chapter: {}", chapterId);
                         } else {
                             log.error("Failed to create chapter {}", chapterId);
                         }
                     } catch (IOException e) {
                         log.error("Error loading chapter {}: {}", path.getFileName(), e.getMessage(), e);
                     }
                 });
        } catch (IOException e) {
            log.error("Error listing files in directory {}: {}", directory, e.getMessage(), e);
        }
    }

    /**
     * Create a Chapter entity from chapter data.
     *
     * @param chapterData the chapter data from JSON
     * @return the created Chapter entity, or null if creation failed
     */
    private Chapter createChapter(Map<String, Object> chapterData) {
        try {
            // Validate required fields
            List<String> requiredFields = List.of("title", "description");
            List<String> missingFields = requiredFields.stream()
                    .filter(field -> !chapterData.containsKey(field))
                    .collect(Collectors.toList());

            if (!missingFields.isEmpty()) {
                log.error("Chapter {} is missing required fields: {}", 
                        chapterData.get("chapter_id"), String.join(", ", missingFields));
                return null;
            }

            // Create chapter entity
            Chapter chapter = new Chapter();
            chapter.setChapterId((String) chapterData.get("chapter_id"));
            chapter.setTitle((String) chapterData.get("title"));
            chapter.setDescription((String) chapterData.get("description"));

            // Set chapter type
            String type = (String) chapterData.getOrDefault("type", "story");
            switch (type.toLowerCase()) {
                case "story":
                    chapter.setType(Chapter.ChapterType.STORY);
                    break;
                case "challenge":
                    chapter.setType(Chapter.ChapterType.CHALLENGE);
                    break;
                case "branching":
                    chapter.setType(Chapter.ChapterType.BRANCHING);
                    break;
                default:
                    log.warn("Unknown chapter type: {}, defaulting to STORY", type);
                    chapter.setType(Chapter.ChapterType.STORY);
            }

            // Set other properties
            if (chapterData.containsKey("phase")) {
                chapter.setPhase(((Number) chapterData.get("phase")).intValue());
            }

            if (chapterData.containsKey("completion_exp")) {
                chapter.setCompletionExp(((Number) chapterData.get("completion_exp")).intValue());
            }

            if (chapterData.containsKey("completion_tusd")) {
                chapter.setCompletionReward(((Number) chapterData.get("completion_tusd")).intValue());
            }

            if (chapterData.containsKey("next_chapter")) {
                chapter.setNextChapterId((String) chapterData.get("next_chapter"));
            }

            // Store choices
            if (chapterData.containsKey("choices")) {
                List<String> choices = new ArrayList<>();
                Object choicesObj = chapterData.get("choices");
                if (choicesObj instanceof List) {
                    for (Object choice : (List) choicesObj) {
                        if (choice instanceof String) {
                            choices.add((String) choice);
                        } else if (choice instanceof Map) {
                            // For complex choice objects, store as JSON string
                            choices.add(objectMapper.writeValueAsString(choice));
                        }
                    }
                }
                chapter.setChoices(choices);
            }

            // Store scenes
            if (chapterData.containsKey("scenes")) {
                List<String> scenes = new ArrayList<>();
                Object scenesObj = chapterData.get("scenes");
                if (scenesObj instanceof List) {
                    for (Object scene : (List) scenesObj) {
                        if (scene instanceof Map) {
                            // Store scene as JSON string
                            scenes.add(objectMapper.writeValueAsString(scene));
                        }
                    }
                }
                chapter.setScenes(scenes);
            }

            // Store dialogues
            if (chapterData.containsKey("dialogues")) {
                List<String> dialogues = new ArrayList<>();
                Object dialoguesObj = chapterData.get("dialogues");
                if (dialoguesObj instanceof List) {
                    for (Object dialogue : (List) dialoguesObj) {
                        if (dialogue instanceof String) {
                            dialogues.add((String) dialogue);
                        } else if (dialogue instanceof Map) {
                            // For complex dialogue objects, store as JSON string
                            dialogues.add(objectMapper.writeValueAsString(dialogue));
                        }
                    }
                }
                chapter.setDialogues(dialogues);
            }

            return chapter;
        } catch (Exception e) {
            log.error("Error creating chapter {}: {}", 
                    chapterData.get("chapter_id"), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save all loaded chapters to the database.
     */
    private void saveChaptersToDatabase() {
        for (Chapter chapter : chapters.values()) {
            try {
                chapterRepository.save(chapter);
                log.info("Saved chapter to database: {}", chapter.getChapterId());
            } catch (Exception e) {
                log.error("Error saving chapter {} to database: {}", 
                        chapter.getChapterId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Get a chapter by its ID.
     *
     * @param chapterId the chapter ID
     * @return the chapter, or null if not found
     */
    public Chapter getChapter(String chapterId) {
        return chapters.get(chapterId);
    }

    /**
     * Get all loaded chapters.
     *
     * @return a map of chapter IDs to chapters
     */
    public Map<String, Chapter> getAllChapters() {
        return new HashMap<>(chapters);
    }

    /**
     * Get available chapters for a player based on their progress.
     *
     * @param playerData the player's data
     * @return a list of available chapter IDs
     */
    public List<String> getAvailableChapters(Map<String, Object> playerData) {
        List<String> availableChapters = new ArrayList<>();

        for (Map.Entry<String, Chapter> entry : chapters.entrySet()) {
            if (isChapterAvailable(entry.getValue(), playerData)) {
                availableChapters.add(entry.getKey());
            }
        }

        return availableChapters;
    }

    /**
     * Check if a chapter is available to a player.
     *
     * @param chapter the chapter to check
     * @param playerData the player's data
     * @return true if the chapter is available, false otherwise
     */
    private boolean isChapterAvailable(Chapter chapter, Map<String, Object> playerData) {
        // Get completed chapters
        Map<String, Object> storyProgress = (Map<String, Object>) playerData.getOrDefault("story_progress", new HashMap<>());
        List<String> completedChapters = (List<String>) storyProgress.getOrDefault("completed_chapters", new ArrayList<>());

        // Check if the chapter is already completed
        if (completedChapters.contains(chapter.getChapterId())) {
            return false;
        }

        // Check chapter requirements
        List<String> requirementsList = chapter.getRequirements();
        if (requirementsList == null || requirementsList.isEmpty()) {
            return true;
        }

        try {
            // Parse requirements - they could be in different formats
            for (String requirementStr : requirementsList) {
                // If it's a JSON string, parse it
                if (requirementStr.startsWith("{")) {
                    Map<String, Object> requirementMap = objectMapper.readValue(requirementStr, Map.class);

                    // Check if this is a new player requirement
                    if (Boolean.TRUE.equals(requirementMap.get("is_new_player"))) {
                        if (!completedChapters.isEmpty()) {
                            return false;
                        }
                    }

                    // Check player stats requirements
                    if (requirementMap.containsKey("stats")) {
                        Map<String, Object> statsRequirements = (Map<String, Object>) requirementMap.get("stats");
                        Map<String, Object> playerStats = (Map<String, Object>) playerData.getOrDefault("attributes", new HashMap<>());

                        for (Map.Entry<String, Object> entry : statsRequirements.entrySet()) {
                            String stat = entry.getKey();
                            int requiredValue = ((Number) entry.getValue()).intValue();
                            int playerValue = playerStats.containsKey(stat) ? ((Number) playerStats.get(stat)).intValue() : 0;

                            if (playerValue < requiredValue) {
                                return false;
                            }
                        }
                    }

                    // Check completed chapters requirements
                    if (requirementMap.containsKey("chapters")) {
                        List<String> requiredChapters = (List<String>) requirementMap.get("chapters");
                        for (String requiredChapter : requiredChapters) {
                            if (!completedChapters.contains(requiredChapter)) {
                                return false;
                            }
                        }
                    }
                } else if (requirementStr.startsWith("chapter:")) {
                    // Legacy format: "chapter:chapter_id"
                    String requiredChapter = requirementStr.substring("chapter:".length());
                    if (!completedChapters.contains(requiredChapter)) {
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking chapter requirements: {}", e.getMessage(), e);
            return false;
        }
    }
}
