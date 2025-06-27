package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Location;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.LocationRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Location testLocation;
    private Location connectedLocation;
    private Location lockedLocation;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        // Set up test locations
        testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setName("Test Location");
        testLocation.setDescription("This is a test location");
        testLocation.setLocked(false);
        testLocation.setConnectedLocations(Arrays.asList(2L, 3L));

        connectedLocation = new Location();
        connectedLocation.setId(2L);
        connectedLocation.setName("Connected Location");
        connectedLocation.setDescription("This is a connected location");
        connectedLocation.setLocked(false);

        lockedLocation = new Location();
        lockedLocation.setId(3L);
        lockedLocation.setName("Locked Location");
        lockedLocation.setDescription("This is a locked location");
        lockedLocation.setLocked(true);
        lockedLocation.setUnlockRequirements("level:5");

        // Set up test player
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setName("Test Player");
        testPlayer.setLevel(10); // Higher than required level for locked location
        testPlayer.setCurrentLocation(testLocation);

        // Set up common lenient stubs
        lenient().when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        lenient().when(locationRepository.findById(2L)).thenReturn(Optional.of(connectedLocation));
        lenient().when(locationRepository.findById(3L)).thenReturn(Optional.of(lockedLocation));
        lenient().when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        lenient().when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
    }

    @Test
    void getConnectedLocations_ShouldReturnConnectedLocations() {
        // Act
        List<Location> result = locationService.getConnectedLocations(1L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(l -> l.getName().equals("Connected Location")));
        assertTrue(result.stream().anyMatch(l -> l.getName().equals("Locked Location")));
    }

    @Test
    void movePlayer_ShouldReturnTrue_WhenMovingToConnectedUnlockedLocation() {
        // Act
        boolean result = locationService.movePlayer(1L, 2L);

        // Assert
        assertTrue(result);
        // Verify only that the player was saved
        verify(playerRepository, atLeastOnce()).save(any(Player.class));
    }

    @Test
    void movePlayer_ShouldReturnFalse_WhenMovingToUnconnectedLocation() {
        // Arrange
        Location unconnectedLocation = new Location();
        unconnectedLocation.setId(4L);
        unconnectedLocation.setName("Unconnected Location");

        lenient().when(locationRepository.findById(4L)).thenReturn(Optional.of(unconnectedLocation));

        // Act
        boolean result = locationService.movePlayer(1L, 4L);

        // Assert
        assertFalse(result);
        // Verify that the player was never saved
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void movePlayer_ShouldReturnTrue_WhenMovingToConnectedLockedLocationWithRequirementsMet() {
        // Act
        boolean result = locationService.movePlayer(1L, 3L);

        // Assert
        assertTrue(result);
        // Verify only that the player was saved
        verify(playerRepository, atLeastOnce()).save(any(Player.class));
    }

    @Test
    void movePlayer_ShouldReturnFalse_WhenMovingToConnectedLockedLocationWithRequirementsNotMet() {
        // Arrange
        Player lowLevelPlayer = new Player();
        lowLevelPlayer.setId(2L);
        lowLevelPlayer.setName("Low Level Player");
        lowLevelPlayer.setLevel(3); // Lower than required level for locked location
        lowLevelPlayer.setCurrentLocation(testLocation);

        lenient().when(playerRepository.findById(2L)).thenReturn(Optional.of(lowLevelPlayer));

        // Act
        boolean result = locationService.movePlayer(2L, 3L);

        // Assert
        assertFalse(result);
        // Verify that the player was never saved
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void checkUnlockRequirements_ShouldReturnTrue_WhenPlayerMeetsRequirements() {
        // Act
        boolean result = locationService.checkUnlockRequirements(1L, 3L);

        // Assert
        assertTrue(result);
    }

    @Test
    void checkUnlockRequirements_ShouldReturnFalse_WhenPlayerDoesNotMeetRequirements() {
        // Arrange
        Player lowLevelPlayer = new Player();
        lowLevelPlayer.setId(2L);
        lowLevelPlayer.setName("Low Level Player");
        lowLevelPlayer.setLevel(3); // Lower than required level for locked location

        lenient().when(playerRepository.findById(2L)).thenReturn(Optional.of(lowLevelPlayer));

        // Act
        boolean result = locationService.checkUnlockRequirements(2L, 3L);

        // Assert
        assertFalse(result);
    }

    @Test
    void checkUnlockRequirements_ShouldReturnTrue_WhenLocationIsAlreadyUnlocked() {
        // Act
        boolean result = locationService.checkUnlockRequirements(1L, 2L);

        // Assert
        assertTrue(result);
    }
}
