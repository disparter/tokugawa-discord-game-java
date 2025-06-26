package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
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
public class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Test Event");
        testEvent.setDescription("This is a test event");
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
    void findEventById_ShouldReturnEvent_WhenEventExists() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        Event result = eventService.findEventById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void findEventById_ShouldReturnNull_WhenEventDoesNotExist() {
        // Arrange
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Event result = eventService.findEventById(2L);

        // Assert
        assertNull(result);
        verify(eventRepository, times(1)).findById(2L);
    }
}