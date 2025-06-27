package io.github.disparter.tokugawa.discord.core.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import io.github.disparter.tokugawa.discord.core.services.ChapterLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for StoryContentMigrator.
 */
public class StoryContentMigratorTest {

    @Mock
    private ChapterLoader chapterLoader;

    @Mock
    private ChapterRepository chapterRepository;

    private ObjectMapper objectMapper;
    private StoryContentMigrator migrator;

    @TempDir
    Path tempDir;

    private Path pythonDataDir;
    private Path javaDataDir;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        // Create test directories
        pythonDataDir = tempDir.resolve("python_data");
        javaDataDir = tempDir.resolve("java_data");

        Files.createDirectories(pythonDataDir);
        Files.createDirectories(pythonDataDir.resolve("story_mode").resolve("chapters"));
        Files.createDirectories(pythonDataDir.resolve("clubs").resolve("club1"));

        // Create test chapter files
        createTestChapterFile(pythonDataDir.resolve("story_mode").resolve("chapters").resolve("chapter1.json"));
        createTestChapterFile(pythonDataDir.resolve("clubs").resolve("club1").resolve("club_chapter1.json"));

        // Create migrator
        migrator = new StoryContentMigrator(
                chapterLoader,
                chapterRepository,
                objectMapper,
                pythonDataDir.toString(),
                javaDataDir.toString()
        );
    }

    @Test
    public void testMigration() throws Exception {
        // Run migration
        migrator.run();

        // Verify directories were created
        assert Files.exists(javaDataDir);
        assert Files.exists(javaDataDir.resolve("narrative").resolve("chapters"));
        assert Files.exists(javaDataDir.resolve("clubs").resolve("club1"));

        // Verify files were migrated
        assert Files.exists(javaDataDir.resolve("narrative").resolve("chapters").resolve("chapter1.json"));
        assert Files.exists(javaDataDir.resolve("clubs").resolve("club1").resolve("club_chapter1.json"));

        // Verify chapterLoader.loadChapters was called
        verify(chapterLoader, times(1)).loadChapters();
    }

    /**
     * Create a test chapter file with sample data.
     */
    private void createTestChapterFile(Path filePath) throws Exception {
        // Create parent directories if they don't exist
        Files.createDirectories(filePath.getParent());

        // Create sample chapter data
        Map<String, Object> chapterData = new HashMap<>();
        chapterData.put("title", "Test Chapter");
        chapterData.put("description", "This is a test chapter");
        chapterData.put("type", "story");

        // Add scenes
        Map<String, Object> scene1 = new HashMap<>();
        scene1.put("scene_id", "scene1");
        scene1.put("title", "Scene 1");
        scene1.put("description", "This is scene 1");

        Map<String, Object> scene2 = new HashMap<>();
        scene2.put("scene_id", "scene2");
        scene2.put("title", "Scene 2");
        scene2.put("description", "This is scene 2");

        chapterData.put("scenes", new Object[]{scene1, scene2});

        // Add choices
        Map<String, Object> choice1 = new HashMap<>();
        choice1.put("text", "Choice 1");
        choice1.put("next_scene", "scene2");

        Map<String, Object> choice2 = new HashMap<>();
        choice2.put("text", "Choice 2");
        choice2.put("next_chapter", "chapter2");

        chapterData.put("choices", new Object[]{choice1, choice2});

        // Write to file
        objectMapper.writeValue(filePath.toFile(), chapterData);
    }
}