package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private ProgressServiceImpl progressService;

    private Progress testProgress;

    @BeforeEach
    void setUp() {
        testProgress = new Progress();
        testProgress.setId(1L);
        testProgress.setPlayerId(1L);
        testProgress.setContentId(1L);
        testProgress.setContentType("CHAPTER");
        testProgress.setCompleted(false);
        testProgress.setChoices(new HashMap<>());
    }

    @Test
    void getPlayerProgress_ShouldReturnProgressList() {
        // Arrange
        Progress progress2 = new Progress();
        progress2.setId(2L);
        progress2.setPlayerId(1L);
        progress2.setContentId(2L);
        
        when(progressRepository.findByPlayerId(1L)).thenReturn(Arrays.asList(testProgress, progress2));

        // Act
        List<Progress> result = progressService.getPlayerProgress(1L);

        // Assert
        assertEquals(2, result.size());
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void getSpecificProgress_ShouldReturnProgress_WhenProgressExists() {
        // Arrange
        when(progressRepository.findByPlayerIdAndContentIdAndContentType(1L, 1L, "CHAPTER"))
            .thenReturn(Optional.of(testProgress));

        // Act
        Progress result = progressService.getSpecificProgress(1L, 1L, "CHAPTER");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPlayerId());
        assertEquals(1L, result.getContentId());
        assertEquals("CHAPTER", result.getContentType());
        verify(progressRepository, times(1)).findByPlayerIdAndContentIdAndContentType(1L, 1L, "CHAPTER");
    }

    @Test
    void getSpecificProgress_ShouldReturnNull_WhenProgressDoesNotExist() {
        // Arrange
        when(progressRepository.findByPlayerIdAndContentIdAndContentType(1L, 2L, "CHAPTER"))
            .thenReturn(Optional.empty());

        // Act
        Progress result = progressService.getSpecificProgress(1L, 2L, "CHAPTER");

        // Assert
        assertNull(result);
        verify(progressRepository, times(1)).findByPlayerIdAndContentIdAndContentType(1L, 2L, "CHAPTER");
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
        assertEquals(testProgress.getPlayerId(), result.getPlayerId());
        verify(progressRepository, times(1)).save(testProgress);
    }
}