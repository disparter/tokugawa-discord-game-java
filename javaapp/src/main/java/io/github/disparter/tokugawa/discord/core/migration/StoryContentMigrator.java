package io.github.disparter.tokugawa.discord.core.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import io.github.disparter.tokugawa.discord.core.services.ChapterLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Migration script for story content from Python version to Java version.
 * This class is responsible for migrating the story content from the Python version to the Java version.
 * It is run automatically when the application starts with the "migration" profile.
 */
@Component
@Profile("migration")
public class StoryContentMigrator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StoryContentMigrator.class);

    private final ChapterLoader chapterLoader;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;
    private final String pythonDataDirectory;
    private final String javaDataDirectory;

    @Autowired
    public StoryContentMigrator(
            ChapterLoader chapterLoader,
            ChapterRepository chapterRepository,
            ObjectMapper objectMapper,
            @Value("${tokugawa.python.data.directory:../untitled/data}") String pythonDataDirectory,
            @Value("${tokugawa.data.directory:data}") String javaDataDirectory) {
        this.chapterLoader = chapterLoader;
        this.chapterRepository = chapterRepository;
        this.objectMapper = objectMapper;
        this.pythonDataDirectory = pythonDataDirectory;
        this.javaDataDirectory = javaDataDirectory;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting story content migration from Python to Java");
        
        // Create Java data directories if they don't exist
        createDirectories();
        
        // Migrate main story chapters
        migrateMainStoryChapters();
        
        // Migrate club chapters
        migrateClubChapters();
        
        // Load all chapters into the database
        chapterLoader.loadChapters();
        
        logger.info("Story content migration completed");
    }
    
    /**
     * Create the necessary directories for the Java version.
     */
    private void createDirectories() throws Exception {
        // Create main data directory
        Path dataDir = Paths.get(javaDataDirectory);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
            logger.info("Created data directory: {}", dataDir);
        }
        
        // Create narrative directory
        Path narrativeDir = Paths.get(javaDataDirectory, "narrative");
        if (!Files.exists(narrativeDir)) {
            Files.createDirectories(narrativeDir);
            logger.info("Created narrative directory: {}", narrativeDir);
        }
        
        // Create chapters directory
        Path chaptersDir = Paths.get(javaDataDirectory, "narrative", "chapters");
        if (!Files.exists(chaptersDir)) {
            Files.createDirectories(chaptersDir);
            logger.info("Created chapters directory: {}", chaptersDir);
        }
        
        // Create clubs directory
        Path clubsDir = Paths.get(javaDataDirectory, "clubs");
        if (!Files.exists(clubsDir)) {
            Files.createDirectories(clubsDir);
            logger.info("Created clubs directory: {}", clubsDir);
        }
    }
    
    /**
     * Migrate main story chapters from Python to Java.
     */
    private void migrateMainStoryChapters() throws Exception {
        Path pythonChaptersDir = Paths.get(pythonDataDirectory, "story_mode", "chapters");
        Path javaChaptersDir = Paths.get(javaDataDirectory, "narrative", "chapters");
        
        if (!Files.exists(pythonChaptersDir)) {
            logger.warn("Python chapters directory not found: {}", pythonChaptersDir);
            return;
        }
        
        Files.list(pythonChaptersDir)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(path -> {
                try {
                    String filename = path.getFileName().toString();
                    Path targetPath = javaChaptersDir.resolve(filename);
                    
                    // Read the Python JSON file
                    Map<String, Object> chapterData = objectMapper.readValue(path.toFile(), Map.class);
                    
                    // Write to Java JSON file
                    objectMapper.writeValue(targetPath.toFile(), chapterData);
                    
                    logger.info("Migrated chapter: {} -> {}", path, targetPath);
                } catch (Exception e) {
                    logger.error("Error migrating chapter {}: {}", path, e.getMessage(), e);
                }
            });
    }
    
    /**
     * Migrate club chapters from Python to Java.
     */
    private void migrateClubChapters() throws Exception {
        Path pythonClubsDir = Paths.get(pythonDataDirectory, "clubs");
        Path javaClubsDir = Paths.get(javaDataDirectory, "clubs");
        
        if (!Files.exists(pythonClubsDir)) {
            logger.warn("Python clubs directory not found: {}", pythonClubsDir);
            return;
        }
        
        Files.list(pythonClubsDir)
            .filter(Files::isDirectory)
            .forEach(clubDir -> {
                try {
                    String clubName = clubDir.getFileName().toString();
                    Path javaClubDir = javaClubsDir.resolve(clubName);
                    
                    // Create club directory if it doesn't exist
                    if (!Files.exists(javaClubDir)) {
                        Files.createDirectories(javaClubDir);
                        logger.info("Created club directory: {}", javaClubDir);
                    }
                    
                    // Migrate club chapters
                    Files.list(clubDir)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String filename = path.getFileName().toString();
                                Path targetPath = javaClubDir.resolve(filename);
                                
                                // Read the Python JSON file
                                Map<String, Object> chapterData = objectMapper.readValue(path.toFile(), Map.class);
                                
                                // Write to Java JSON file
                                objectMapper.writeValue(targetPath.toFile(), chapterData);
                                
                                logger.info("Migrated club chapter: {} -> {}", path, targetPath);
                            } catch (Exception e) {
                                logger.error("Error migrating club chapter {}: {}", path, e.getMessage(), e);
                            }
                        });
                } catch (Exception e) {
                    logger.error("Error migrating club {}: {}", clubDir, e.getMessage(), e);
                }
            });
    }
}