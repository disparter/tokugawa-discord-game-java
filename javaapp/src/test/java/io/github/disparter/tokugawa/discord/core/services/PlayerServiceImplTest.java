package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setUserId("123456789");
        testPlayer.setName("TestPlayer");
        testPlayer.setLevel(1);
        testPlayer.setExp(0);
        testPlayer.setPoints(0);
    }

    @Test
    void findById_ShouldReturnPlayer_WhenPlayerExists() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

        // Act
        Player result = playerService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestPlayer", result.getName());
        verify(playerRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldReturnNull_WhenPlayerDoesNotExist() {
        // Arrange
        when(playerRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Player result = playerService.findById(2L);

        // Assert
        assertNull(result);
        verify(playerRepository, times(1)).findById(2L);
    }

    @Test
    void findByDiscordId_ShouldReturnPlayer_WhenPlayerExists() {
        // Arrange
        // Note: There's a mismatch between method names. PlayerServiceImpl.findByDiscordId calls
        // playerRepository.findByDiscordId, but the repository has findByUserId instead.
        // This test assumes the repository method is correctly named findByUserId.
        when(playerRepository.findByUserId("123456789")).thenReturn(Optional.of(testPlayer));

        // Act
        Player result = playerService.findByDiscordId("123456789");

        // Assert
        assertNotNull(result);
        assertEquals("123456789", result.getUserId());
        assertEquals("TestPlayer", result.getName());
        verify(playerRepository, times(1)).findByUserId("123456789");
    }

    @Test
    void save_ShouldReturnSavedPlayer() {
        // Arrange
        when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

        // Act
        Player result = playerService.save(testPlayer);

        // Assert
        assertNotNull(result);
        assertEquals(testPlayer.getId(), result.getId());
        assertEquals(testPlayer.getName(), result.getName());
        verify(playerRepository, times(1)).save(testPlayer);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(playerRepository).deleteById(1L);

        // Act
        playerService.delete(1L);

        // Assert
        verify(playerRepository, times(1)).deleteById(1L);
    }
}