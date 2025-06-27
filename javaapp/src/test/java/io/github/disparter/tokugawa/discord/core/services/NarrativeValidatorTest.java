package io.github.disparter.tokugawa.discord.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NarrativeValidatorTest {

    @Mock
    private ChapterLoader chapterLoader;

    private ObjectMapper objectMapper;
    private NarrativeValidator narrativeValidator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        narrativeValidator = new NarrativeValidator(chapterLoader, objectMapper);
    }

    @Test
    void validateNarrative_ShouldDetectMissingNextChapter() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create a chapter with a non-existent next chapter
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setNextChapterId("1_2"); // This chapter doesn't exist
        
        chapters.put("1_1", chapter1);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Chapter 1_1 references non-existent next chapter 1_2")));
    }

    @Test
    void validateNarrative_ShouldDetectChoiceWithInvalidNextChapter() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create a chapter with a choice that references a non-existent chapter
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setChoices(List.of("{\"text\":\"Choice 1\",\"next_chapter\":\"1_2\"}"));
        
        chapters.put("1_1", chapter1);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Choice 0 in chapter 1_1 references non-existent next chapter 1_2")));
    }

    @Test
    void validateNarrative_ShouldDetectChoiceWithInvalidNextDialogue() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create a chapter with a choice that references a non-existent dialogue
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setDialogues(List.of("Dialogue 1"));
        chapter1.setChoices(List.of("{\"text\":\"Choice 1\",\"next_dialogue\":1}"));
        
        chapters.put("1_1", chapter1);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Choice 0 in chapter 1_1 references non-existent dialogue index 1")));
    }

    @Test
    void validateNarrative_ShouldDetectUnreachableChapter() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create two chapters, one of which is unreachable
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setNextChapterId("1_3");
        
        Chapter chapter2 = new Chapter();
        chapter2.setChapterId("1_2");
        chapter2.setTitle("Chapter 2");
        chapter2.setDescription("Description 2");
        chapter2.setType(Chapter.ChapterType.STORY);
        
        chapters.put("1_1", chapter1);
        chapters.put("1_2", chapter2);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Chapter 1_2 is unreachable from any other chapter")));
    }

    @Test
    void validateNarrative_ShouldDetectDialogueChoiceWithInvalidNextChapter() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create a chapter with a dialogue that has a choice referencing a non-existent chapter
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setDialogues(List.of(
                "{\"text\":\"Dialogue 1\",\"choices\":[{\"text\":\"Choice 1\",\"next_chapter\":\"1_2\"}]}"
        ));
        
        chapters.put("1_1", chapter1);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Dialogue 0 choice 0 in chapter 1_1 references non-existent next chapter 1_2")));
    }

    @Test
    void validateNarrative_ShouldDetectDialogueChoiceWithInvalidNextDialogue() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create a chapter with a dialogue that has a choice referencing a non-existent dialogue
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setDialogues(List.of(
                "{\"text\":\"Dialogue 1\",\"choices\":[{\"text\":\"Choice 1\",\"next_dialogue\":1}]}"
        ));
        
        chapters.put("1_1", chapter1);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Dialogue 0 choice 0 in chapter 1_1 references non-existent dialogue index 1")));
    }

    @Test
    void validateNarrative_ShouldDetectConditionalNextChapterWithInvalidChapter() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create a chapter with a choice that has a conditional next chapter referencing a non-existent chapter
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setChoices(List.of(
                "{\"text\":\"Choice 1\",\"conditional_next_chapter\":{\"variable\":\"1_2\"}}"
        ));
        
        chapters.put("1_1", chapter1);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(error -> 
                error.contains("Choice 0 in chapter 1_1 has conditional next chapter that references non-existent chapter 1_2")));
    }

    @Test
    void validateNarrative_ShouldPassWithValidChapters() {
        // Arrange
        Map<String, Chapter> chapters = new HashMap<>();
        
        // Create valid chapters with proper references
        Chapter chapter1 = new Chapter();
        chapter1.setChapterId("1_1");
        chapter1.setTitle("Chapter 1");
        chapter1.setDescription("Description 1");
        chapter1.setType(Chapter.ChapterType.STORY);
        chapter1.setNextChapterId("1_2");
        
        Chapter chapter2 = new Chapter();
        chapter2.setChapterId("1_2");
        chapter2.setTitle("Chapter 2");
        chapter2.setDescription("Description 2");
        chapter2.setType(Chapter.ChapterType.STORY);
        
        chapters.put("1_1", chapter1);
        chapters.put("1_2", chapter2);
        
        when(chapterLoader.getAllChapters()).thenReturn(chapters);
        
        // Act
        List<String> errors = narrativeValidator.validateNarrative();
        
        // Assert
        assertTrue(errors.isEmpty());
    }
}