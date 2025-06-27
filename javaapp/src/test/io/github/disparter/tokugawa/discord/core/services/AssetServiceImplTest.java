package io.github.disparter.tokugawa.discord.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetServiceImplTest {

    private AssetServiceImpl assetService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        assetService = new AssetServiceImpl();
        
        // Create test directory structure
        Path assetsDir = tempDir.resolve("assets");
        Files.createDirectories(assetsDir);
        
        // Create test asset directories
        Path backgroundsDir = assetsDir.resolve("backgrounds");
        Path charactersDir = assetsDir.resolve("characters");
        Path locationsDir = assetsDir.resolve("locations");
        Files.createDirectories(backgroundsDir);
        Files.createDirectories(charactersDir);
        Files.createDirectories(locationsDir);
        
        // Create test assets
        createEmptyFile(assetsDir.resolve("default.png"));
        createEmptyFile(backgroundsDir.resolve("default_background.jpg"));
        createEmptyFile(charactersDir.resolve("default_character.png"));
        createEmptyFile(locationsDir.resolve("default_location.jpg"));
        createEmptyFile(backgroundsDir.resolve("school.jpg"));
        createEmptyFile(charactersDir.resolve("hero.png"));
        createEmptyFile(locationsDir.resolve("tokyo.jpg"));
        
        // Set the assets base path using reflection
        ReflectionTestUtils.setField(assetService, "assetsBasePath", assetsDir.toString());
    }
    
    private void createEmptyFile(Path path) throws IOException {
        Files.createFile(path);
    }

    @Test
    void getImageUrl_withExistingContext_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        String context = "school";
        createEmptyFile(tempDir.resolve("assets/backgrounds/school.jpg"));
        
        // Act
        URL result = assetService.getImageUrl(context);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("school.jpg"));
    }

    @Test
    void getImageUrl_withNonExistingContext_shouldReturnFallbackUrl() {
        // Arrange
        String context = "non_existing_context";
        
        // Act
        URL result = assetService.getImageUrl(context);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default.png"));
    }

    @Test
    void getImageFile_withExistingContext_shouldReturnCorrectFile() throws Exception {
        // Arrange
        String context = "hero";
        createEmptyFile(tempDir.resolve("assets/characters/hero.png"));
        
        // Act
        File result = assetService.getImageFile(context);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("hero.png"));
    }

    @Test
    void getImageFile_withNonExistingContext_shouldReturnFallbackFile() {
        // Arrange
        String context = "non_existing_context";
        
        // Act
        File result = assetService.getImageFile(context);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default.png"));
    }

    @Test
    void getBackgroundImageUrl_withExistingLocation_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        String location = "school";
        createEmptyFile(tempDir.resolve("assets/backgrounds/school.jpg"));
        
        // Act
        URL result = assetService.getBackgroundImageUrl(location);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("school.jpg"));
    }

    @Test
    void getBackgroundImageUrl_withNonExistingLocation_shouldReturnFallbackUrl() {
        // Arrange
        String location = "non_existing_location";
        
        // Act
        URL result = assetService.getBackgroundImageUrl(location);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default_background.jpg"));
    }

    @Test
    void getCharacterImageUrl_withExistingCharacter_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        String character = "hero";
        createEmptyFile(tempDir.resolve("assets/characters/hero.png"));
        
        // Act
        URL result = assetService.getCharacterImageUrl(character);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("hero.png"));
    }

    @Test
    void getCharacterImageUrl_withNonExistingCharacter_shouldReturnFallbackUrl() {
        // Arrange
        String character = "non_existing_character";
        
        // Act
        URL result = assetService.getCharacterImageUrl(character);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default_character.png"));
    }

    @Test
    void getLocationImageUrl_withExistingLocation_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        String location = "tokyo";
        createEmptyFile(tempDir.resolve("assets/locations/tokyo.jpg"));
        
        // Act
        URL result = assetService.getLocationImageUrl(location);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("tokyo.jpg"));
    }

    @Test
    void getLocationImageUrl_withNonExistingLocation_shouldReturnFallbackUrl() {
        // Arrange
        String location = "non_existing_location";
        
        // Act
        URL result = assetService.getLocationImageUrl(location);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default_location.jpg"));
    }

    @Test
    void assetExists_withExistingAsset_shouldReturnTrue() throws Exception {
        // Arrange
        String context = "hero";
        createEmptyFile(tempDir.resolve("assets/characters/hero.png"));
        
        // Act
        boolean result = assetService.assetExists(context);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void assetExists_withNonExistingAsset_shouldReturnFalse() {
        // Arrange
        String context = "non_existing_context";
        
        // Act
        boolean result = assetService.assetExists(context);
        
        // Assert
        assertFalse(result);
    }

    @Test
    void getFallbackImageUrl_withValidAssetType_shouldReturnCorrectUrl() {
        // Arrange
        String assetType = "background";
        
        // Act
        URL result = assetService.getFallbackImageUrl(assetType);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default_background.jpg"));
    }

    @Test
    void getFallbackImageUrl_withInvalidAssetType_shouldReturnDefaultUrl() {
        // Arrange
        String assetType = "invalid_type";
        
        // Act
        URL result = assetService.getFallbackImageUrl(assetType);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default.png"));
    }
}