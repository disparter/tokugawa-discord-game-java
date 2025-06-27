package io.github.disparter.tokugawa.discord.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.repositories.ChapterRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NarrativeServiceImplTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private ChapterLoader chapterLoader;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NarrativeValidator narrativeValidator;

    @Mock
    private ConsequenceService consequenceService;

    @Mock
    private PlayerService playerService;

    @Mock
    private ReputationService reputationService;

    @InjectMocks
    private NarrativeServiceImpl narrativeService;

    private Chapter testChapter;
    private Player testPlayer;
    private Progress testProgress;

    @BeforeEach
    void setUp() {
        // Set up test chapter
        testChapter = new Chapter();
        testChapter.setId(1L);
        testChapter.setChapterId("chapter_1");
        testChapter.setTitle("Test Chapter");
        testChapter.setDescription("This is a test chapter");

        List<String> dialogues = new ArrayList<>();
        dialogues.add("{\"text\":\"First dialogue\",\"choices\":[\"Option 1\",\"Option 2\"]}");
        dialogues.add("{\"text\":\"Second dialogue\",\"choices\":[\"Option A\",\"Option B\"]}");
        testChapter.setDialogues(dialogues);

        List<String> choices = new ArrayList<>();
        choices.add("Choice 1");
        choices.add("Choice 2");
        testChapter.setChoices(choices);

        // Set up test player
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setName("Test Player");
        testPlayer.setStrength(10);
        testPlayer.setIntelligence(10);
        testPlayer.setCharisma(10);
        testPlayer.setAgility(10);
        testPlayer.setReputation(50);
        testPlayer.setCurrency(100);
        testPlayer.setExperience(200);

        // Set up test progress
        testProgress = new Progress();
        testProgress.setId(1L);
        testProgress.setPlayer(testPlayer);
        testProgress.setCurrentChapterId("chapter_1");
        testProgress.setCurrentDialogueIndex(0);
        testProgress.setCompletedChapters(new ArrayList<>());
        testProgress.setChoices(new HashMap<>());
        testProgress.setRelationships(new HashMap<>());
        testProgress.setFactionReputations(new HashMap<>());
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

    @Test
    void processChoice_ShouldReturnErrorResponse_WhenPlayerNotFound() {
        // Arrange
        when(playerRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> result = narrativeService.processChoice(2L, 0);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("Player not found", result.get("error"));
        verify(playerRepository, times(1)).findById(2L);
        verify(progressRepository, never()).findByPlayerId(anyLong());
    }

    @Test
    void processChoice_ShouldReturnErrorResponse_WhenProgressNotFound() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> result = narrativeService.processChoice(1L, 0);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("No progress found for player", result.get("error"));
        verify(playerRepository, times(1)).findById(1L);
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void processChoice_ShouldReturnErrorResponse_WhenNoCurrentChapter() {
        // Arrange
        testProgress.setCurrentChapterId(null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.of(testProgress));

        // Act
        Map<String, Object> result = narrativeService.processChoice(1L, 0);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("No current chapter", result.get("error"));
        verify(playerRepository, times(1)).findById(1L);
        verify(progressRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void processChoice_ShouldReturnErrorResponse_WhenChapterNotFound() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.of(testProgress));
        when(chapterRepository.findByChapterId("chapter_1")).thenReturn(Optional.empty());

        // Act
        Map<String, Object> result = narrativeService.processChoice(1L, 0);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("Current chapter not found", result.get("error"));
        verify(playerRepository, times(1)).findById(1L);
        verify(progressRepository, times(1)).findByPlayerId(1L);
        verify(chapterRepository, times(1)).findByChapterId("chapter_1");
    }

    @Test
    void processChoice_ShouldProcessChoiceSuccessfully_WithDialogueChoices() throws Exception {
        // Arrange
        Map<String, Object> dialogueData = new HashMap<>();
        dialogueData.put("text", "First dialogue");
        List<String> dialogueChoices = Arrays.asList("Option 1", "Option 2");
        dialogueData.put("choices", dialogueChoices);

        Map<String, Object> choiceData = new HashMap<>();
        choiceData.put("text", "Option 1");
        choiceData.put("next_dialogue", 1);
        Map<String, Object> effects = new HashMap<>();
        effects.put("attributes", Map.of("strength", 5, "intelligence", -2));
        choiceData.put("effects", effects);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(progressRepository.findByPlayerId(1L)).thenReturn(Optional.of(testProgress));
        when(chapterRepository.findByChapterId("chapter_1")).thenReturn(Optional.of(testChapter));
        when(objectMapper.readValue(testChapter.getDialogues().get(0), Map.class)).thenReturn(dialogueData);
        when(objectMapper.readValue("Option 1", Map.class)).thenReturn(choiceData);

        // Act
        Map<String, Object> result = narrativeService.processChoice(1L, 0);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(testPlayer, result.get("player"));
        assertEquals(testProgress, result.get("progress"));
        assertEquals(1, result.get("next_dialogue_index"));
        assertEquals(1, testProgress.getCurrentDialogueIndex());

        // Verify that the choice was recorded
        Map<String, String> choices = testProgress.getChoices();
        assertEquals("0", choices.get("chapter_1_dialogue_0"));

        // Verify that the services were called
        verify(playerRepository, times(1)).findById(1L);
        verify(progressRepository, times(1)).findByPlayerId(1L);
        verify(chapterRepository, times(1)).findByChapterId("chapter_1");
        verify(objectMapper, times(1)).readValue(testChapter.getDialogues().get(0), Map.class);
        verify(playerRepository, times(1)).save(testPlayer);
        verify(progressRepository, times(1)).save(testProgress);
        verify(consequenceService, times(1)).trackPlayerDecision(
            eq(1L),
            eq("chapter_1"),
            eq("scene_0"),
            eq("Option 1"),
            anyString(),
            anyString(),
            anyString(),
            eq(Consequence.ConsequenceType.IMMEDIATE),
            anyList(),
            anyList(),
            anyList()
        );
        verify(playerService, times(1)).updatePlayerAttributes(testPlayer);
    }
}
