package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for validating narrative integrity.
 * This service checks for broken links, missing chapters, and other issues in the narrative flow.
 */
@Service
@Slf4j
public class NarrativeValidator {


    private final ChapterLoader chapterLoader;
    private final ObjectMapper objectMapper;

    @Autowired
    public NarrativeValidator(ChapterLoader chapterLoader, ObjectMapper objectMapper) {
        this.chapterLoader = chapterLoader;
        this.objectMapper = objectMapper;
    }

    /**
     * Validate the narrative flow of all chapters.
     * This checks for broken links, missing chapters, and other issues.
     *
     * @return a list of validation errors, or an empty list if no errors were found
     */
    public List<String> validateNarrative() {
        List<String> errors = new ArrayList<>();
        Map<String, Chapter> allChapters = chapterLoader.getAllChapters();

        if (allChapters.isEmpty()) {
            errors.add("No chapters found. Make sure chapters are loaded before validation.");
            return errors;
        }

        // Check for missing next chapters
        for (Chapter chapter : allChapters.values()) {
            validateChapter(chapter, allChapters, errors);
        }

        // Check for unreachable chapters
        List<String> unreachableChapters = findUnreachableChapters(allChapters);
        for (String chapterId : unreachableChapters) {
            errors.add(String.format("Chapter %s is unreachable from any other chapter", chapterId));
        }

        return errors;
    }

    /**
     * Validate a single chapter.
     *
     * @param chapter the chapter to validate
     * @param allChapters all available chapters
     * @param errors list to add errors to
     */
    private void validateChapter(Chapter chapter, Map<String, Chapter> allChapters, List<String> errors) {
        String chapterId = chapter.getChapterId();

        // Check for missing next chapter
        String nextChapterId = chapter.getNextChapterId();
        if (nextChapterId != null && !nextChapterId.isEmpty() && !allChapters.containsKey(nextChapterId)) {
            errors.add(String.format("Chapter %s references non-existent next chapter %s", 
                    chapterId, nextChapterId));
        }

        // Check choices for next chapter references
        List<String> choices = chapter.getChoices();
        if (choices != null) {
            for (int i = 0; i < choices.size(); i++) {
                String choice = choices.get(i);
                try {
                    // If the choice is a JSON string, parse it
                    if (choice.startsWith("{")) {
                        Map<String, Object> choiceData = objectMapper.readValue(choice, Map.class);

                        // Check for next chapter reference
                        if (choiceData.containsKey("next_chapter")) {
                            String choiceNextChapterId = (String) choiceData.get("next_chapter");
                            if (!allChapters.containsKey(choiceNextChapterId)) {
                                errors.add(String.format("Choice %d in chapter %s references non-existent next chapter %s", 
                                        i, chapterId, choiceNextChapterId));
                            }
                        }

                        // Check for next dialogue reference
                        if (choiceData.containsKey("next_dialogue")) {
                            Object nextDialogue = choiceData.get("next_dialogue");
                            if (!(nextDialogue instanceof Number)) {
                                errors.add(String.format("Choice %d in chapter %s has invalid next_dialogue value: %s", 
                                        i, chapterId, nextDialogue));
                            } else {
                                // Check if the dialogue exists in the chapter
                                int dialogueIndex = ((Number) nextDialogue).intValue();
                                List<String> dialogues = chapter.getDialogues();
                                if (dialogues == null || dialogueIndex < 0 || dialogueIndex >= dialogues.size()) {
                                    errors.add(String.format("Choice %d in chapter %s references non-existent dialogue index %d", 
                                            i, chapterId, dialogueIndex));
                                }
                            }
                        }

                        // Check for conditional next chapter
                        if (choiceData.containsKey("conditional_next_chapter")) {
                            Object conditionalNextChapter = choiceData.get("conditional_next_chapter");
                            if (conditionalNextChapter instanceof Map) {
                                Map<String, Object> conditions = (Map<String, Object>) conditionalNextChapter;
                                for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                                    if (entry.getValue() instanceof String) {
                                        String conditionNextChapterId = (String) entry.getValue();
                                        if (!conditionNextChapterId.isEmpty() && !allChapters.containsKey(conditionNextChapterId)) {
                                            errors.add(String.format("Choice %d in chapter %s has conditional next chapter that references non-existent chapter %s", 
                                                    i, chapterId, conditionNextChapterId));
                                        }
                                    }
                                }
                            }
                        }

                        // Check for effects
                        if (choiceData.containsKey("effects")) {
                            Object effects = choiceData.get("effects");
                            if (!(effects instanceof Map)) {
                                errors.add(String.format("Choice %d in chapter %s has invalid effects value: %s", 
                                        i, chapterId, effects));
                            }
                        }
                    }
                } catch (JsonProcessingException e) {
                    errors.add(String.format("Error parsing choice %d JSON in chapter %s: %s", 
                            i, chapterId, e.getMessage()));
                }
            }
        }

        // Check dialogues for choices that lead to invalid chapters or dialogues
        List<String> dialogues = chapter.getDialogues();
        if (dialogues != null) {
            for (int i = 0; i < dialogues.size(); i++) {
                String dialogue = dialogues.get(i);
                try {
                    // If the dialogue is a JSON string, parse it
                    if (dialogue.startsWith("{")) {
                        Map<String, Object> dialogueData = objectMapper.readValue(dialogue, Map.class);

                        // Check dialogue choices
                        if (dialogueData.containsKey("choices")) {
                            Object choicesObj = dialogueData.get("choices");
                            if (choicesObj instanceof List) {
                                List<Object> dialogueChoices = (List<Object>) choicesObj;
                                for (int j = 0; j < dialogueChoices.size(); j++) {
                                    Object choiceObj = dialogueChoices.get(j);
                                    if (choiceObj instanceof Map) {
                                        Map<String, Object> dialogueChoice = (Map<String, Object>) choiceObj;

                                        // Check for next chapter reference
                                        if (dialogueChoice.containsKey("next_chapter")) {
                                            String dialogueNextChapterId = (String) dialogueChoice.get("next_chapter");
                                            if (!allChapters.containsKey(dialogueNextChapterId)) {
                                                errors.add(String.format("Dialogue %d choice %d in chapter %s references non-existent next chapter %s", 
                                                        i, j, chapterId, dialogueNextChapterId));
                                            }
                                        }

                                        // Check for next dialogue reference
                                        if (dialogueChoice.containsKey("next_dialogue")) {
                                            Object nextDialogue = dialogueChoice.get("next_dialogue");
                                            if (!(nextDialogue instanceof Number)) {
                                                errors.add(String.format("Dialogue %d choice %d in chapter %s has invalid next_dialogue value: %s", 
                                                        i, j, chapterId, nextDialogue));
                                            } else {
                                                // Check if the dialogue exists in the chapter
                                                int dialogueIndex = ((Number) nextDialogue).intValue();
                                                if (dialogueIndex < 0 || dialogueIndex >= dialogues.size()) {
                                                    errors.add(String.format("Dialogue %d choice %d in chapter %s references non-existent dialogue index %d", 
                                                            i, j, chapterId, dialogueIndex));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (JsonProcessingException e) {
                    errors.add(String.format("Error parsing dialogue %d JSON in chapter %s: %s", 
                            i, chapterId, e.getMessage()));
                }
            }
        }

        // Check for required fields
        if (chapter.getTitle() == null || chapter.getTitle().isEmpty()) {
            errors.add(String.format("Chapter %s is missing a title", chapterId));
        }

        if (chapter.getDescription() == null || chapter.getDescription().isEmpty()) {
            errors.add(String.format("Chapter %s is missing a description", chapterId));
        }

        if (chapter.getType() == null) {
            errors.add(String.format("Chapter %s is missing a type", chapterId));
        }
    }

    /**
     * Find chapters that are unreachable from any other chapter.
     *
     * @param allChapters all available chapters
     * @return list of unreachable chapter IDs
     */
    private List<String> findUnreachableChapters(Map<String, Chapter> allChapters) {
        // Create a map to track which chapters are referenced by others
        Map<String, Boolean> isReferenced = new HashMap<>();
        for (String chapterId : allChapters.keySet()) {
            isReferenced.put(chapterId, false);
        }

        // Mark chapters that are referenced by others
        for (Chapter chapter : allChapters.values()) {
            // Check direct next chapter reference
            String nextChapterId = chapter.getNextChapterId();
            if (nextChapterId != null && !nextChapterId.isEmpty()) {
                isReferenced.put(nextChapterId, true);
            }

            // Check choices for next chapter references
            List<String> choices = chapter.getChoices();
            if (choices != null) {
                for (String choice : choices) {
                    try {
                        // If the choice is a JSON string, parse it
                        if (choice.startsWith("{")) {
                            Map<String, Object> choiceData = objectMapper.readValue(choice, Map.class);
                            if (choiceData.containsKey("next_chapter")) {
                                String choiceNextChapterId = (String) choiceData.get("next_chapter");
                                isReferenced.put(choiceNextChapterId, true);
                            }
                        }
                    } catch (JsonProcessingException e) {
                        // Already logged in validateChapter
                    }
                }
            }
        }

        // Find chapters that are not referenced
        List<String> unreachableChapters = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : isReferenced.entrySet()) {
            if (!entry.getValue()) {
                // Skip the first chapter, which is the entry point
                if (!entry.getKey().equals("1_1") && !entry.getKey().startsWith("tutorial_")) {
                    unreachableChapters.add(entry.getKey());
                }
            }
        }

        return unreachableChapters;
    }

    /**
     * Validate a specific chapter.
     *
     * @param chapterId the ID of the chapter to validate
     * @return a list of validation errors, or an empty list if no errors were found
     */
    public List<String> validateChapter(String chapterId) {
        List<String> errors = new ArrayList<>();
        Map<String, Chapter> allChapters = chapterLoader.getAllChapters();

        Chapter chapter = allChapters.get(chapterId);
        if (chapter == null) {
            errors.add(String.format("Chapter %s not found", chapterId));
            return errors;
        }

        validateChapter(chapter, allChapters, errors);
        return errors;
    }
}
