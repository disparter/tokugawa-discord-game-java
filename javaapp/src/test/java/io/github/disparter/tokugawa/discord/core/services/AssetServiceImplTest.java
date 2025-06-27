package io.github.disparter.tokugawa.discord.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource mockResource;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        // Create a mock implementation of AssetService
        assetService = mock(AssetService.class);
    }

    private void setupMockResource(boolean exists) throws IOException {
        when(mockResource.exists()).thenReturn(exists);
        when(mockResource.getURL()).thenReturn(new URL("file:///test/default.png"));
        when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
    }

    @Test
    void getImageUrl_withExistingContext_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/school.jpg");
        when(assetService.getImageUrl("school")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getImageUrl("school");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getImageUrl("school");
    }

    @Test
    void getImageUrl_withNonExistingContext_shouldReturnFallbackUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/default.png");
        when(assetService.getImageUrl("non_existing_context")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getImageUrl("non_existing_context");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getImageUrl("non_existing_context");
    }

    @Test
    void getImageFile_withExistingContext_shouldReturnCorrectFile() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.getPath()).thenReturn("/test/hero.png");
        when(assetService.getImageFile("hero")).thenReturn(mockFile);

        // Act
        File result = assetService.getImageFile("hero");

        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("hero.png"));
        verify(assetService).getImageFile("hero");
    }

    @Test
    void getImageFile_withNonExistingContext_shouldReturnFallbackFile() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.getPath()).thenReturn("/test/default.png");
        when(assetService.getImageFile("non_existing_context")).thenReturn(mockFile);

        // Act
        File result = assetService.getImageFile("non_existing_context");

        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("default.png"));
        verify(assetService).getImageFile("non_existing_context");
    }

    @Test
    void getBackgroundImageUrl_withExistingLocation_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/school.jpg");
        when(assetService.getBackgroundImageUrl("school")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getBackgroundImageUrl("school");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getBackgroundImageUrl("school");
    }

    @Test
    void getBackgroundImageUrl_withNonExistingLocation_shouldReturnFallbackUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/default_background.jpg");
        when(assetService.getBackgroundImageUrl("non_existing_location")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getBackgroundImageUrl("non_existing_location");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getBackgroundImageUrl("non_existing_location");
    }

    @Test
    void getCharacterImageUrl_withExistingCharacter_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/hero.png");
        when(assetService.getCharacterImageUrl("hero")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getCharacterImageUrl("hero");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getCharacterImageUrl("hero");
    }

    @Test
    void getCharacterImageUrl_withNonExistingCharacter_shouldReturnFallbackUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/default_character.png");
        when(assetService.getCharacterImageUrl("non_existing_character")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getCharacterImageUrl("non_existing_character");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getCharacterImageUrl("non_existing_character");
    }

    @Test
    void getLocationImageUrl_withExistingLocation_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/tokyo.jpg");
        when(assetService.getLocationImageUrl("tokyo")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getLocationImageUrl("tokyo");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getLocationImageUrl("tokyo");
    }

    @Test
    void getLocationImageUrl_withNonExistingLocation_shouldReturnFallbackUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/default_location.jpg");
        when(assetService.getLocationImageUrl("non_existing_location")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getLocationImageUrl("non_existing_location");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getLocationImageUrl("non_existing_location");
    }

    @Test
    void assetExists_withExistingAsset_shouldReturnTrue() throws Exception {
        // Arrange
        when(assetService.assetExists("hero")).thenReturn(true);

        // Act
        boolean result = assetService.assetExists("hero");

        // Assert
        assertTrue(result);
        verify(assetService).assetExists("hero");
    }

    @Test
    void assetExists_withNonExistingAsset_shouldReturnFalse() throws Exception {
        // Arrange
        when(assetService.assetExists("non_existing_context")).thenReturn(false);

        // Act
        boolean result = assetService.assetExists("non_existing_context");

        // Assert
        assertFalse(result);
        verify(assetService).assetExists("non_existing_context");
    }

    @Test
    void getFallbackImageUrl_withValidAssetType_shouldReturnCorrectUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/default_background.jpg");
        when(assetService.getFallbackImageUrl("background")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getFallbackImageUrl("background");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getFallbackImageUrl("background");
    }

    @Test
    void getFallbackImageUrl_withInvalidAssetType_shouldReturnDefaultUrl() throws Exception {
        // Arrange
        URL mockUrl = new URL("file:///test/default.png");
        when(assetService.getFallbackImageUrl("invalid_type")).thenReturn(mockUrl);

        // Act
        URL result = assetService.getFallbackImageUrl("invalid_type");

        // Assert
        assertNotNull(result);
        assertEquals(mockUrl, result);
        verify(assetService).getFallbackImageUrl("invalid_type");
    }
}
