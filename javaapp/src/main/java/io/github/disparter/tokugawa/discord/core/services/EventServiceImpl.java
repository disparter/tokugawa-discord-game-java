package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Event.EventType;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import io.github.disparter.tokugawa.discord.core.models.Relationship.RelationshipStatus;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the EventService interface.
 */
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PlayerRepository playerRepository;
    private final ProgressRepository progressRepository;
    private final NPCRepository npcRepository;
    private final RelationshipService relationshipService;

    // Romance route requirements - NPC ID to minimum affinity level
    private final Map<Long, Integer> romanceRequirements = new HashMap<>();

    // Romance route chapters - maps NPC ID to list of event IDs in order
    private final Map<Long, List<String>> romanceChapters = new HashMap<>();

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, 
                           PlayerRepository playerRepository,
                           ProgressRepository progressRepository,
                           NPCRepository npcRepository,
                           RelationshipService relationshipService) {
        this.eventRepository = eventRepository;
        this.playerRepository = playerRepository;
        this.progressRepository = progressRepository;
        this.npcRepository = npcRepository;
        this.relationshipService = relationshipService;

        // Initialize romance routes
        initializeRomanceRoutes();
    }

    /**
     * Initializes the romance routes with their requirements and chapters.
     */
    private void initializeRomanceRoutes() {
        // For each romance-capable NPC, set the minimum affinity required
        // and the sequence of events that make up their romance route

        // Example for 7 romance routes (in a real implementation, this would be loaded from a database or config)
        // Route 1
        romanceRequirements.put(1L, 80); // NPC with ID 1 requires affinity 80
        romanceChapters.put(1L, List.of("romance_1_1", "romance_1_2", "romance_1_3"));

        // Route 2
        romanceRequirements.put(2L, 80);
        romanceChapters.put(2L, List.of("romance_2_1", "romance_2_2", "romance_2_3"));

        // Route 3
        romanceRequirements.put(3L, 80);
        romanceChapters.put(3L, List.of("romance_3_1", "romance_3_2", "romance_3_3"));

        // Route 4
        romanceRequirements.put(4L, 80);
        romanceChapters.put(4L, List.of("romance_4_1", "romance_4_2", "romance_4_3"));

        // Route 5
        romanceRequirements.put(5L, 80);
        romanceChapters.put(5L, List.of("romance_5_1", "romance_5_2", "romance_5_3"));

        // Route 6
        romanceRequirements.put(6L, 80);
        romanceChapters.put(6L, List.of("romance_6_1", "romance_6_2", "romance_6_3"));

        // Route 7
        romanceRequirements.put(7L, 80);
        romanceChapters.put(7L, List.of("romance_7_1", "romance_7_2", "romance_7_3"));
    }

    @Override
    public Event findById(Long id) {
        // Placeholder implementation
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    public List<Event> getAllEvents() {
        // Placeholder implementation
        List<Event> events = new ArrayList<>();
        eventRepository.findAll().forEach(events::add);
        return events;
    }

    @Override
    public List<Event> getAvailableEventsForPlayer(Long playerId) {
        List<Event> allEvents = getAllEvents();
        List<Event> availableEvents = new ArrayList<>();

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        for (Event event : allEvents) {
            if (isEventAvailableForPlayer(event, player)) {
                availableEvents.add(event);
            }
        }

        return availableEvents;
    }

    /**
     * Checks if an event is available for a player based on various conditions.
     *
     * @param event the event to check
     * @param player the player
     * @return true if the event is available, false otherwise
     */
    private boolean isEventAvailableForPlayer(Event event, Player player) {
        // Check event type
        if (event.getType() == EventType.ROMANCE) {
            return isRomanceEventAvailableForPlayer(event, player);
        }

        // For other event types, check general conditions
        // This is a simplified implementation
        return true;
    }

    /**
     * Checks if a romance event is available for a player based on relationship status.
     *
     * @param event the romance event to check
     * @param player the player
     * @return true if the romance event is available, false otherwise
     */
    private boolean isRomanceEventAvailableForPlayer(Event event, Player player) {
        // Extract NPC ID from event ID (assuming format like "romance_1_2" where 1 is the NPC ID)
        String eventId = event.getEventId();
        if (!eventId.startsWith("romance_")) {
            return false;
        }

        String[] parts = eventId.split("_");
        if (parts.length < 3) {
            return false;
        }

        try {
            Long npcId = Long.parseLong(parts[1]);

            // Check if player has required affinity with this NPC
            Relationship relationship = relationshipService.getRelationship(player.getId(), npcId);
            if (relationship == null) {
                return false;
            }

            Integer requiredAffinity = romanceRequirements.get(npcId);
            if (requiredAffinity == null) {
                return false;
            }

            // Check if player has completed previous chapters in this romance route
            List<String> chapters = romanceChapters.get(npcId);
            if (chapters == null) {
                return false;
            }

            int currentChapterIndex = chapters.indexOf(eventId);
            if (currentChapterIndex == -1) {
                return false;
            }

            // First chapter is available if affinity is high enough
            if (currentChapterIndex == 0) {
                return relationship.getAffinity() >= requiredAffinity;
            }

            // Later chapters require previous chapters to be completed
            String previousChapter = chapters.get(currentChapterIndex - 1);
            List<String> triggeredEvents = relationship.getTriggeredEvents();

            return relationship.getAffinity() >= requiredAffinity && triggeredEvents.contains(previousChapter);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public Event triggerEvent(Long eventId, Long playerId) {
        Event event = findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Check if event is available for player
        if (!isEventAvailableForPlayer(event, player)) {
            throw new IllegalArgumentException("Event is not available for player");
        }

        // Add player to participants if not already there
        if (!event.getParticipants().contains(player)) {
            event.getParticipants().add(player);
        }

        // For romance events, record in relationship
        if (event.getType() == EventType.ROMANCE) {
            recordRomanceEvent(event, player);
        }

        // Save the event
        return eventRepository.save(event);
    }

    /**
     * Records a romance event in the relationship between the player and the NPC.
     *
     * @param event the romance event
     * @param player the player
     */
    private void recordRomanceEvent(Event event, Player player) {
        String eventId = event.getEventId();
        if (!eventId.startsWith("romance_")) {
            return;
        }

        String[] parts = eventId.split("_");
        if (parts.length < 3) {
            return;
        }

        try {
            Long npcId = Long.parseLong(parts[1]);

            Relationship relationship = relationshipService.getRelationship(player.getId(), npcId);
            if (relationship == null) {
                return;
            }

            // Add event to triggered events if not already there
            List<String> triggeredEvents = relationship.getTriggeredEvents();
            if (!triggeredEvents.contains(eventId)) {
                triggeredEvents.add(eventId);
            }

            // Save the relationship
            relationshipService.save(relationship);

        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    @Override
    @Transactional
    public Event completeEvent(Long eventId, Long playerId) {
        Event event = findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Process rewards
        // This is a simplified implementation

        return event;
    }

    @Override
    public Event save(Event event) {
        // Placeholder implementation
        return eventRepository.save(event);
    }
}
