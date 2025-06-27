package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceImplTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private Progress testProgress;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId(1L);

        testProgress = new Progress();
        testProgress.setId(1L);
        testProgress.setPlayer(testPlayer);
        testProgress.setCurrentChapterId("1");
        testProgress.setCompletedChapters(new ArrayList<>());
        testProgress.setCompletedArcs(new ArrayList<>());
        testProgress.setChoices(new HashMap<>());
        testProgress.setTriggeredEvents(new HashMap<>());
    }

    @Test
    void getPlayerProgress_ShouldReturnProgressList() {
        // Arrange
        Progress progress2 = new Progress();
        progress2.setId(2L);
        progress2.setPlayer(testPlayer);
        progress2.setCurrentChapterId("2");

        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.of(testProgress));

        // Act
        List<Progress> result = progressService.getPlayerProgress(1L);

        // Assert
        assertEquals(1, result.size());
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void getSpecificProgress_ShouldReturnProgress_WhenProgressExists() {
        // Arrange
        testProgress.setCurrentChapterId("1");
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.of(testProgress));

        // Act
        Progress result = progressService.getSpecificProgress(1L, 1L, "CHAPTER");

        // Assert
        assertNotNull(result);
        assertEquals(testPlayer, result.getPlayer());
        assertEquals("1", result.getCurrentChapterId());
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void getSpecificProgress_ShouldReturnNull_WhenProgressDoesNotExist() {
        // Arrange
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.empty());

        // Act
        Progress result = progressService.getSpecificProgress(1L, 2L, "CHAPTER");

        // Assert
        assertNull(result);
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void save_ShouldReturnSavedProgress() {
        // Arrange
        when(progressRepository.save(testProgress)).thenReturn(testProgress);

        // Act
        Progress result = progressService.save(testProgress);

        // Assert
        assertNotNull(result);
        assertEquals(testProgress.getId(), result.getId());
        assertEquals(testProgress.getPlayer(), result.getPlayer());
        verify(progressRepository, times(1)).save(testProgress);
    }

    @Test
    void getProgressByPlayerId_ShouldReturnProgress_WhenProgressExists() {
        // Arrange
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.of(testProgress));

        // Act
        Progress result = progressService.getProgressByPlayerId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPlayer, result.getPlayer());
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void getProgressByPlayerId_ShouldReturnNull_WhenProgressDoesNotExist() {
        // Arrange
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.empty());

        // Act
        Progress result = progressService.getProgressByPlayerId(1L);

        // Assert
        assertNull(result);
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }
}
