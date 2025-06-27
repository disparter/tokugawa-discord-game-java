package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Event.EventType;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the EventService interface.
 */
@Service
@Slf4j
public class EventServiceImpl implements EventService {


    private final EventRepository eventRepository;
    private final PlayerRepository playerRepository;
    private final ProgressRepository progressRepository;
    private final NPCRepository npcRepository;
    private final RelationshipService relationshipService;
    private final GameCalendarService gameCalendarService;
    private final Random random = new Random();

    // Romance route requirements - NPC ID to minimum affinity level
    private final Map<Long, Integer> romanceRequirements = new HashMap<>();

    // Romance route chapters - maps NPC ID to list of event IDs in order
    private final Map<Long, List<String>> romanceChapters = new HashMap<>();

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, 
                           PlayerRepository playerRepository,
                           ProgressRepository progressRepository,
                           NPCRepository npcRepository,
                           RelationshipService relationshipService,
                           GameCalendarService gameCalendarService) {
        this.eventRepository = eventRepository;
        this.playerRepository = playerRepository;
        this.progressRepository = progressRepository;
        this.npcRepository = npcRepository;
        this.relationshipService = relationshipService;
        this.gameCalendarService = gameCalendarService;

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
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    public List<Event> getAllEvents() {
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

    @Override
    public List<Event> getAvailableEventsForPlayerByType(Long playerId, EventType type) {
        List<Event> allEvents = eventRepository.findByType(type);
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

    @Override
    public List<Event> getAvailableSeasonalEventsForPlayer(Long playerId) {
        Season currentSeason = gameCalendarService.getCurrentSeason();
        return getAvailableSeasonalEventsForPlayer(playerId, currentSeason);
    }

    @Override
    public List<Event> getAvailableSeasonalEventsForPlayer(Long playerId, Season season) {
        List<Event> seasonalEvents = eventRepository.findByType(EventType.SEASONAL);
        List<Event> availableEvents = new ArrayList<>();

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        for (Event event : seasonalEvents) {
            if (isEventAvailableForPlayer(event, player)) {
                // Additional check for the specific season
                if (isEventForSeason(event, season)) {
                    availableEvents.add(event);
                }
            }
        }

        return availableEvents;
    }

    /**
     * Checks if an event is for a specific season.
     *
     * @param event the event to check
     * @param season the season
     * @return true if the event is for the specified season, false otherwise
     */
    private boolean isEventForSeason(Event event, Season season) {
        // Check if the event has seasonal data
        if (event.getStartMonth() == null || event.getStartDay() == null || 
            event.getEndMonth() == null || event.getEndDay() == null) {
            return false;
        }

        // Determine the season based on the start month
        int startMonth = event.getStartMonth();

        if (startMonth >= 3 && startMonth <= 5) {
            return season == Season.SPRING;
        } else if (startMonth >= 6 && startMonth <= 8) {
            return season == Season.SUMMER;
        } else if (startMonth >= 9 && startMonth <= 11) {
            return season == Season.AUTUMN;
        } else {
            return season == Season.WINTER;
        }
    }

    @Override
    public List<Event> checkForRandomEvents(Long playerId) {
        List<Event> randomEvents = eventRepository.findByType(EventType.RANDOM);
        List<Event> triggeredEvents = new ArrayList<>();

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        for (Event event : randomEvents) {
            if (isRandomEventAvailableForPlayer(event, player)) {
                triggeredEvents.add(event);
            }
        }

        return triggeredEvents;
    }

    @Override
    public List<Event> checkForActionTriggeredEvents(Long playerId, String actionType, Map<String, Object> actionData) {
        List<Event> allEvents = getAllEvents();
        List<Event> triggeredEvents = new ArrayList<>();

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        for (Event event : allEvents) {
            if (isEventTriggeredByAction(event, player, actionType, actionData)) {
                triggeredEvents.add(event);
            }
        }

        return triggeredEvents;
    }

    /**
     * Checks if an event is triggered by a specific player action.
     *
     * @param event the event to check
     * @param player the player
     * @param actionType the type of action performed
     * @param actionData additional data about the action
     * @return true if the event is triggered by the action, false otherwise
     */
    private boolean isEventTriggeredByAction(Event event, Player player, String actionType, Map<String, Object> actionData) {
        // Check if the event has trigger conditions
        if (event.getTriggerConditions() == null || event.getTriggerConditions().isEmpty()) {
            return false;
        }

        // Check if any of the trigger conditions match the action
        for (String condition : event.getTriggerConditions()) {
            if (condition.startsWith(actionType + ":")) {
                // Extract the specific condition after the action type
                String specificCondition = condition.substring(actionType.length() + 1);

                // Check if the specific condition is met based on the action data
                if (isSpecificConditionMet(specificCondition, actionData)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a specific condition is met based on action data.
     *
     * @param specificCondition the specific condition to check
     * @param actionData the action data
     * @return true if the condition is met, false otherwise
     */
    private boolean isSpecificConditionMet(String specificCondition, Map<String, Object> actionData) {
        // Parse the condition (format: key=value)
        String[] parts = specificCondition.split("=");
        if (parts.length != 2) {
            return false;
        }

        String key = parts[0];
        String expectedValue = parts[1];

        // Check if the action data contains the key
        if (!actionData.containsKey(key)) {
            return false;
        }

        // Check if the value matches the expected value
        Object actualValue = actionData.get(key);
        return expectedValue.equals(actualValue.toString());
    }

    @Override
    public List<Event> checkForStoryTriggeredEvents(Long playerId) {
        List<Event> allEvents = getAllEvents();
        List<Event> triggeredEvents = new ArrayList<>();

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Get player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);

        for (Event event : allEvents) {
            if (isEventTriggeredByStoryProgress(event, player, progressOptional)) {
                triggeredEvents.add(event);
            }
        }

        return triggeredEvents;
    }

    /**
     * Checks if an event is triggered by story progress.
     *
     * @param event the event to check
     * @param player the player
     * @param progressOptional the player's progress
     * @return true if the event is triggered by story progress, false otherwise
     */
    private boolean isEventTriggeredByStoryProgress(Event event, Player player, Optional<Progress> progressOptional) {
        // Check if the event has trigger conditions
        if (event.getTriggerConditions() == null || event.getTriggerConditions().isEmpty()) {
            return false;
        }

        // Check if any of the trigger conditions match the story progress
        for (String condition : event.getTriggerConditions()) {
            if (condition.startsWith("story:")) {
                // Extract the specific condition after "story:"
                String specificCondition = condition.substring(6);

                // Check if the specific condition is met based on the player's progress
                if (isStoryConditionMet(specificCondition, progressOptional)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a story condition is met based on player's progress.
     *
     * @param specificCondition the specific condition to check
     * @param progressOptional the player's progress
     * @return true if the condition is met, false otherwise
     */
    private boolean isStoryConditionMet(String specificCondition, Optional<Progress> progressOptional) {
        // Parse the condition (format: chapter=value)
        String[] parts = specificCondition.split("=");
        if (parts.length != 2) {
            return false;
        }

        String key = parts[0];
        String expectedValue = parts[1];

        // Check if the key is "chapter"
        if (!"chapter".equals(key)) {
            return false;
        }

        // Check if the player has completed the specified chapter
        if (progressOptional.isPresent()) {
            Progress progress = progressOptional.get();
            if (progress.getCompletedChapters().contains(expectedValue)) {
                return true;
            }
        }

        return false;
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
        switch (event.getType()) {
            case ROMANCE:
                return isRomanceEventAvailableForPlayer(event, player);
            case SEASONAL:
                return isSeasonalEventAvailableForPlayer(event, player);
            case RANDOM:
                return isRandomEventAvailableForPlayer(event, player);
            case CHOICE_TRIGGERED:
                return isChoiceTriggeredEventAvailableForPlayer(event, player);
            case CLIMACTIC:
                return isClimacticEventAvailableForPlayer(event, player);
            default:
                // For other event types, check general conditions
                return true;
        }
    }

    /**
     * Checks if a climactic event is available for a player based on story progress.
     *
     * @param event the climactic event to check
     * @param player the player
     * @return true if the climactic event is available, false otherwise
     */
    private boolean isClimacticEventAvailableForPlayer(Event event, Player player) {
        // Get player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);

        // Check if the event has trigger conditions
        if (event.getTriggerConditions() == null || event.getTriggerConditions().isEmpty()) {
            return false;
        }

        // Check if all trigger conditions are met
        for (String condition : event.getTriggerConditions()) {
            if (condition.startsWith("story:")) {
                // Extract the specific condition after "story:"
                String specificCondition = condition.substring(6);

                // Check if the specific condition is met based on the player's progress
                if (!isStoryConditionMet(specificCondition, progressOptional)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if a seasonal event is available for a player based on the current date.
     *
     * @param event the seasonal event to check
     * @param player the player
     * @return true if the seasonal event is available, false otherwise
     */
    private boolean isSeasonalEventAvailableForPlayer(Event event, Player player) {
        // Check if the event has seasonal data
        if (event.getStartMonth() == null || event.getStartDay() == null || 
            event.getEndMonth() == null || event.getEndDay() == null) {
            return false;
        }

        // Check if the current date is within the event period
        return gameCalendarService.isDateInRange(
            event.getStartMonth(), event.getStartDay(),
            event.getEndMonth(), event.getEndDay()
        );
    }

    /**
     * Checks if a random event is available for a player based on chance.
     *
     * @param event the random event to check
     * @param player the player
     * @return true if the random event is available, false otherwise
     */
    private boolean isRandomEventAvailableForPlayer(Event event, Player player) {
        // Check if the event has a trigger chance
        if (event.getTriggerChance() == null) {
            return false;
        }

        // Check random chance
        return random.nextDouble() < event.getTriggerChance();
    }

    /**
     * Checks if a choice-triggered event is available for a player based on their previous choices.
     *
     * @param event the choice-triggered event to check
     * @param player the player
     * @return true if the choice-triggered event is available, false otherwise
     */
    private boolean isChoiceTriggeredEventAvailableForPlayer(Event event, Player player) {
        // Check if the event has required choices
        if (event.getRequiredChoices() == null || event.getRequiredChoices().isEmpty()) {
            return false;
        }

        // Get player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);

        if (!progressOptional.isPresent()) {
            return false;
        }

        // Get player's choices from progress
        Map<String, String> playerChoices = progressOptional.get().getChoices();

        // Check if player has made all required choices
        return playerChoices.keySet().containsAll(event.getRequiredChoices());
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

        // Handle specific event types
        switch (event.getType()) {
            case ROMANCE:
                recordRomanceEvent(event, player);
                break;
            case SEASONAL:
                recordSeasonalEvent(event, player);
                break;
            case RANDOM:
                recordRandomEvent(event, player);
                break;
            case CHOICE_TRIGGERED:
                recordChoiceTriggeredEvent(event, player);
                break;
            case CLIMACTIC:
                recordClimacticEvent(event, player);
                break;
            default:
                // No special handling needed for other event types
                break;
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

            log.info("Romance event {} triggered for player {} with NPC {}", eventId, player.getId(), npcId);

        } catch (NumberFormatException e) {
            log.error("Error parsing NPC ID from romance event ID: {}", eventId, e);
        }
    }

    /**
     * Records a seasonal event for a player.
     *
     * @param event the seasonal event
     * @param player the player
     */
    private void recordSeasonalEvent(Event event, Player player) {
        // Get or create player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);
        Progress progress;

        if (progressOptional.isPresent()) {
            progress = progressOptional.get();
        } else {
            progress = new Progress();
            progress.setPlayer(player);
        }

        // Record the event in the player's triggered events
        progress.getTriggeredEvents().put(event.getEventId(), LocalDateTime.now().toString());

        progressRepository.save(progress);

        log.info("Seasonal event {} triggered for player {}", event.getEventId(), player.getId());
    }

    /**
     * Records a random event for a player.
     *
     * @param event the random event
     * @param player the player
     */
    private void recordRandomEvent(Event event, Player player) {
        // Get or create player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);
        Progress progress;

        if (progressOptional.isPresent()) {
            progress = progressOptional.get();
        } else {
            progress = new Progress();
            progress.setPlayer(player);
        }

        // Record the event in the player's triggered events
        progress.getTriggeredEvents().put(event.getEventId(), LocalDateTime.now().toString());

        progressRepository.save(progress);

        log.info("Random event {} triggered for player {}", event.getEventId(), player.getId());
    }

    /**
     * Records a choice-triggered event for a player.
     *
     * @param event the choice-triggered event
     * @param player the player
     */
    private void recordChoiceTriggeredEvent(Event event, Player player) {
        // Get or create player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);
        Progress progress;

        if (progressOptional.isPresent()) {
            progress = progressOptional.get();
        } else {
            progress = new Progress();
            progress.setPlayer(player);
        }

        // Record the event in the player's triggered events
        progress.getTriggeredEvents().put(event.getEventId(), LocalDateTime.now().toString());

        progressRepository.save(progress);

        log.info("Choice-triggered event {} triggered for player {}", event.getEventId(), player.getId());
    }

    /**
     * Records a climactic event for a player.
     *
     * @param event the climactic event
     * @param player the player
     */
    private void recordClimacticEvent(Event event, Player player) {
        // Get or create player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);
        Progress progress;

        if (progressOptional.isPresent()) {
            progress = progressOptional.get();
        } else {
            progress = new Progress();
            progress.setPlayer(player);
        }

        // Record the event in the player's triggered events
        progress.getTriggeredEvents().put(event.getEventId(), LocalDateTime.now().toString());

        progressRepository.save(progress);

        log.info("Climactic event {} triggered for player {}", event.getEventId(), player.getId());
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

        // Apply event consequences
        applyEventConsequences(eventId, playerId);

        // Mark the event as completed in the player's progress
        Optional<Progress> progressOptional = progressRepository.findByPlayer(player);
        if (progressOptional.isPresent()) {
            Progress progress = progressOptional.get();
            // Add the event to completed chapters
            progress.getCompletedChapters().add(event.getEventId());
            // Update the triggered events with completion timestamp
            progress.getTriggeredEvents().put(event.getEventId() + "_completed", LocalDateTime.now().toString());
            progressRepository.save(progress);
        }

        log.info("Event {} completed for player {}", event.getEventId(), player.getId());

        return event;
    }

    @Override
    @Transactional
    public Player applyEventConsequences(Long eventId, Long playerId) {
        Event event = findById(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found with ID: " + eventId);
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Process rewards based on event type
        if (event.getRewards() != null && !event.getRewards().isEmpty()) {
            for (String reward : event.getRewards()) {
                applyReward(player, reward);
            }
        }

        // Save the updated player
        return playerRepository.save(player);
    }

    /**
     * Applies a reward to a player.
     *
     * @param player the player
     * @param reward the reward string (format: type:value)
     */
    private void applyReward(Player player, String reward) {
        String[] parts = reward.split(":");
        if (parts.length != 2) {
            log.warn("Invalid reward format: {}", reward);
            return;
        }

        String type = parts[0];
        String valueStr = parts[1];

        try {
            switch (type) {
                case "exp":
                    int expValue = Integer.parseInt(valueStr);
                    player.setExp(player.getExp() + expValue);
                    log.info("Applied EXP reward of {} to player {}", expValue, player.getId());
                    break;
                case "tusd":
                    int tusdValue = Integer.parseInt(valueStr);
                    player.setPoints(player.getPoints() + tusdValue);
                    log.info("Applied TUSD reward of {} to player {}", tusdValue, player.getId());
                    break;
                case "item":
                    // In a real implementation, this would add the item to the player's inventory
                    log.info("Applied item reward of {} to player {}", valueStr, player.getId());
                    break;
                case "level":
                    int levelValue = Integer.parseInt(valueStr);
                    player.setLevel(player.getLevel() + levelValue);
                    log.info("Applied level reward of {} to player {}", levelValue, player.getId());
                    break;
                default:
                    log.warn("Unknown reward type: {}", type);
                    break;
            }
        } catch (NumberFormatException e) {
            log.error("Error parsing reward value: {}", valueStr, e);
        }
    }

    @Override
    @Transactional
    public Event createEvent(Event event) {
        // Validate the event
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }

        if (event.getName() == null || event.getName().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty");
        }

        if (event.getType() == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }

        // Check if an event with the same ID already exists
        if (eventRepository.findByEventId(event.getEventId()).isPresent()) {
            throw new IllegalArgumentException("Event with ID " + event.getEventId() + " already exists");
        }

        // Save the event
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event updateEvent(Event event) {
        // Validate the event
        if (event.getId() == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // Check if the event exists
        if (!eventRepository.existsById(event.getId())) {
            throw new IllegalArgumentException("Event with ID " + event.getId() + " does not exist");
        }

        // Save the event
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        // Check if the event exists
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event with ID " + eventId + " does not exist");
        }

        // Delete the event
        eventRepository.deleteById(eventId);
    }

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }
}
