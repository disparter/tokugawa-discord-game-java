package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
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
public class NarrativeServiceImplTest {

    @Mock
    private ChapterRepository chapterRepository;

    @InjectMocks
    private NarrativeServiceImpl narrativeService;

    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        testChapter = new Chapter();
        testChapter.setId(1L);
        testChapter.setTitle("Test Chapter");
        testChapter.setDescription("This is a test chapter");
    }

    @Test
    void getAllChapters_ShouldReturnAllChapters() {
        // Arrange
        Chapter chapter2 = new Chapter();
        chapter2.setId(2L);
        chapter2.setTitle("Another Chapter");
        
        when(chapterRepository.findAll()).thenReturn(Arrays.asList(testChapter, chapter2));

        // Act
        List<Chapter> result = narrativeService.getAllChapters();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Test Chapter")));
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Another Chapter")));
        verify(chapterRepository, times(1)).findAll();
    }

    @Test
    void findChapterById_ShouldReturnChapter_WhenChapterExists() {
        // Arrange
        when(chapterRepository.findById(1L)).thenReturn(Optional.of(testChapter));

        // Act
        Chapter result = narrativeService.findChapterById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Chapter", result.getTitle());
        verify(chapterRepository, times(1)).findById(1L);
    }

    @Test
    void findChapterById_ShouldReturnNull_WhenChapterDoesNotExist() {
        // Arrange
        when(chapterRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Chapter result = narrativeService.findChapterById(2L);

        // Assert
        assertNull(result);
        verify(chapterRepository, times(1)).findById(2L);
    }
}