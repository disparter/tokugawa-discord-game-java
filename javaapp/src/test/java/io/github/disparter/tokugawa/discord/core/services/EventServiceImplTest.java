package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Event.EventType;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private GameCalendarService gameCalendarService;

    @Mock
    private RelationshipService relationshipService;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;
    private Event seasonalEvent;
    private Event randomEvent;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        // Set up a regular test event
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setEventId("test_event_1");
        testEvent.setName("Test Event");
        testEvent.setDescription("This is a test event");
        testEvent.setType(EventType.BASE);

        // Set up a seasonal event
        seasonalEvent = new Event();
        seasonalEvent.setId(2L);
        seasonalEvent.setEventId("seasonal_event_1");
        seasonalEvent.setName("Spring Festival");
        seasonalEvent.setDescription("A seasonal spring event");
        seasonalEvent.setType(EventType.SEASONAL);
        seasonalEvent.setStartMonth(3); // March
        seasonalEvent.setStartDay(1);
        seasonalEvent.setEndMonth(5); // May
        seasonalEvent.setEndDay(31);

        // Set up a random event
        randomEvent = new Event();
        randomEvent.setId(3L);
        randomEvent.setEventId("random_event_1");
        randomEvent.setName("Random Encounter");
        randomEvent.setDescription("A random encounter");
        randomEvent.setType(EventType.RANDOM);
        randomEvent.setTriggerChance(0.5); // 50% chance

        // Set up a test player
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setName("Test Player");
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents() {
        // Arrange
        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("Another Event");

        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent, event2));

        // Act
        List<Event> result = eventService.getAllEvents();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Test Event")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Another Event")));
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnEvent_WhenEventExists() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        Event result = eventService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldReturnNull_WhenEventDoesNotExist() {
        // Arrange
        when(eventRepository.findById(4L)).thenReturn(Optional.empty());

        // Act
        Event result = eventService.findById(4L);

        // Assert
        assertNull(result);
        verify(eventRepository, times(1)).findById(4L);
    }

    @Test
    void getAvailableSeasonalEventsForPlayer_ShouldReturnSeasonalEvents_WhenInSeason() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameCalendarService.getCurrentSeason()).thenReturn(Season.SPRING);
        when(eventRepository.findByType(EventType.SEASONAL)).thenReturn(Arrays.asList(seasonalEvent));
        when(gameCalendarService.isDateInRange(
            seasonalEvent.getStartMonth(), seasonalEvent.getStartDay(),
            seasonalEvent.getEndMonth(), seasonalEvent.getEndDay()
        )).thenReturn(true);

        // Act
        List<Event> result = eventService.getAvailableSeasonalEventsForPlayer(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Spring Festival", result.get(0).getName());
        verify(gameCalendarService, times(1)).getCurrentSeason();
        verify(eventRepository, times(1)).findByType(EventType.SEASONAL);
        verify(gameCalendarService, times(1)).isDateInRange(
            seasonalEvent.getStartMonth(), seasonalEvent.getStartDay(),
            seasonalEvent.getEndMonth(), seasonalEvent.getEndDay()
        );
    }

    @Test
    void getAvailableSeasonalEventsForPlayer_ShouldReturnEmptyList_WhenOutOfSeason() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(gameCalendarService.getCurrentSeason()).thenReturn(Season.WINTER);
        when(eventRepository.findByType(EventType.SEASONAL)).thenReturn(Arrays.asList(seasonalEvent));
        when(gameCalendarService.isDateInRange(
            seasonalEvent.getStartMonth(), seasonalEvent.getStartDay(),
            seasonalEvent.getEndMonth(), seasonalEvent.getEndDay()
        )).thenReturn(false);

        // Act
        List<Event> result = eventService.getAvailableSeasonalEventsForPlayer(1L);

        // Assert
        assertTrue(result.isEmpty());
        verify(gameCalendarService, times(1)).getCurrentSeason();
        verify(eventRepository, times(1)).findByType(EventType.SEASONAL);
        verify(gameCalendarService, times(1)).isDateInRange(
            seasonalEvent.getStartMonth(), seasonalEvent.getStartDay(),
            seasonalEvent.getEndMonth(), seasonalEvent.getEndDay()
        );
    }

    @Test
    void checkForRandomEvents_ShouldCallRepositories() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(eventRepository.findByType(EventType.RANDOM)).thenReturn(Arrays.asList(randomEvent));

        // Act
        List<Event> result = eventService.checkForRandomEvents(1L);

        // Assert
        // Since the random factor is involved, we can't assert the exact result
        // But we can verify that the method calls the repository correctly
        verify(playerRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).findByType(EventType.RANDOM);
    }
}
