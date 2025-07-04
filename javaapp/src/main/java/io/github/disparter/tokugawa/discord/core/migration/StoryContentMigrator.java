package io.github.disparter.tokugawa.discord.core.migration;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import io.github.disparter.tokugawa.discord.core.services.ChapterLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
@Slf4j
public class StoryContentMigrator implements CommandLineRunner {


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
        log.info("Starting story content migration from Python to Java");
        
        // Create Java data directories if they don't exist
        createDirectories();
        
        // Migrate main story chapters
        migrateMainStoryChapters();
        
        // Migrate club chapters
        migrateClubChapters();
        
        // Load all chapters into the database
        chapterLoader.loadChapters();
        
        log.info("Story content migration completed");
    }
    
    /**
     * Create the necessary directories for the Java version.
     */
    private void createDirectories() throws Exception {
        // Create main data directory
        Path dataDir = Paths.get(javaDataDirectory);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
            log.info("Created data directory: {}", dataDir);
        }
        
        // Create narrative directory
        Path narrativeDir = Paths.get(javaDataDirectory, "narrative");
        if (!Files.exists(narrativeDir)) {
            Files.createDirectories(narrativeDir);
            log.info("Created narrative directory: {}", narrativeDir);
        }
        
        // Create chapters directory
        Path chaptersDir = Paths.get(javaDataDirectory, "narrative", "chapters");
        if (!Files.exists(chaptersDir)) {
            Files.createDirectories(chaptersDir);
            log.info("Created chapters directory: {}", chaptersDir);
        }
        
        // Create clubs directory
        Path clubsDir = Paths.get(javaDataDirectory, "clubs");
        if (!Files.exists(clubsDir)) {
            Files.createDirectories(clubsDir);
            log.info("Created clubs directory: {}", clubsDir);
        }
    }
    
    /**
     * Migrate main story chapters from Python to Java.
     */
    private void migrateMainStoryChapters() throws Exception {
        Path pythonChaptersDir = Paths.get(pythonDataDirectory, "story_mode", "chapters");
        Path javaChaptersDir = Paths.get(javaDataDirectory, "narrative", "chapters");
        
        if (!Files.exists(pythonChaptersDir)) {
            log.warn("Python chapters directory not found: {}", pythonChaptersDir);
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
                    
                    log.info("Migrated chapter: {} -> {}", path, targetPath);
                } catch (Exception e) {
                    log.error("Error migrating chapter {}: {}", path, e.getMessage(), e);
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
            log.warn("Python clubs directory not found: {}", pythonClubsDir);
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
                        log.info("Created club directory: {}", javaClubDir);
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
                                
                                log.info("Migrated club chapter: {} -> {}", path, targetPath);
                            } catch (Exception e) {
                                log.error("Error migrating club chapter {}: {}", path, e.getMessage(), e);
                            }
                        });
                } catch (Exception e) {
                    log.error("Error migrating club {}: {}", clubDir, e.getMessage(), e);
                }
            });
    }
}