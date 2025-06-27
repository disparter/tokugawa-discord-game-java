package io.github.disparter.tokugawa.discord.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChapterLoaderTest {

    @Mock
    private ChapterRepository chapterRepository;

    private ObjectMapper objectMapper;
    private ChapterLoader chapterLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chapterLoader = new ChapterLoader(chapterRepository, objectMapper, tempDir.toString());
    }

    @Test
    void loadChapters_ShouldLoadChaptersFromMainDirectory() throws IOException {
        // Arrange
        // Create the directory structure
        Path narrativeDir = Files.createDirectory(tempDir.resolve("narrative"));
        Path chaptersDir = Files.createDirectory(narrativeDir.resolve("chapters"));
        
        // Create a test chapter JSON file
        Map<String, Object> chapterData = new HashMap<>();
        chapterData.put("title", "Test Chapter");
        chapterData.put("description", "This is a test chapter");
        chapterData.put("type", "story");
        chapterData.put("dialogues", List.of("Dialogue 1", "Dialogue 2"));
        
        File chapterFile = chaptersDir.resolve("chapter1.json").toFile();
        objectMapper.writeValue(chapterFile, chapterData);
        
        // Act
        chapterLoader.loadChapters();
        
        // Assert
        Chapter chapter = chapterLoader.getChapter("chapter1");
        assertNotNull(chapter);
        assertEquals("Test Chapter", chapter.getTitle());
        assertEquals("This is a test chapter", chapter.getDescription());
        assertEquals(Chapter.ChapterType.STORY, chapter.getType());
        assertEquals(2, chapter.getDialogues().size());
        assertEquals("Dialogue 1", chapter.getDialogues().get(0));
        assertEquals("Dialogue 2", chapter.getDialogues().get(1));
        
        verify(chapterRepository, times(1)).saveAll(any());
    }

    @Test
    void loadChapters_ShouldLoadChaptersFromClubDirectories() throws IOException {
        // Arrange
        // Create the directory structure
        Path clubsDir = Files.createDirectory(tempDir.resolve("clubs"));
        Path club1Dir = Files.createDirectory(clubsDir.resolve("club1"));
        
        // Create a test chapter JSON file
        Map<String, Object> chapterData = new HashMap<>();
        chapterData.put("title", "Club Chapter");
        chapterData.put("description", "This is a club chapter");
        chapterData.put("type", "challenge");
        chapterData.put("dialogues", List.of("Club Dialogue 1", "Club Dialogue 2"));
        
        File chapterFile = club1Dir.resolve("event1.json").toFile();
        objectMapper.writeValue(chapterFile, chapterData);
        
        // Act
        chapterLoader.loadChapters();
        
        // Assert
        Chapter chapter = chapterLoader.getChapter("club_club1_event1");
        assertNotNull(chapter);
        assertEquals("Club Chapter", chapter.getTitle());
        assertEquals("This is a club chapter", chapter.getDescription());
        assertEquals(Chapter.ChapterType.CHALLENGE, chapter.getType());
        assertEquals(2, chapter.getDialogues().size());
        assertEquals("Club Dialogue 1", chapter.getDialogues().get(0));
        assertEquals("Club Dialogue 2", chapter.getDialogues().get(1));
        
        verify(chapterRepository, times(1)).saveAll(any());
    }

    @Test
    void loadChapters_ShouldHandleMissingDirectories() {
        // Act & Assert
        assertDoesNotThrow(() -> chapterLoader.loadChapters());
        verify(chapterRepository, times(1)).saveAll(any());
    }

    @Test
    void loadChapters_ShouldHandleInvalidJsonFiles() throws IOException {
        // Arrange
        // Create the directory structure
        Path narrativeDir = Files.createDirectory(tempDir.resolve("narrative"));
        Path chaptersDir = Files.createDirectory(narrativeDir.resolve("chapters"));
        
        // Create an invalid JSON file
        File invalidFile = chaptersDir.resolve("invalid.json").toFile();
        Files.writeString(invalidFile.toPath(), "This is not valid JSON");
        
        // Act & Assert
        assertDoesNotThrow(() -> chapterLoader.loadChapters());
        verify(chapterRepository, times(1)).saveAll(any());
    }
}