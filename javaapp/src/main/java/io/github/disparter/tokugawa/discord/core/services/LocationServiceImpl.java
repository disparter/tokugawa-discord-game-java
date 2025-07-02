package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Location;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Item;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.LocationRepository;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.ItemRepository;
import io.github.disparter.tokugawa.discord.core.repositories.EventRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of the LocationService interface.
 */
@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final NPCRepository npcRepository;
    private final ItemRepository itemRepository;
    private final EventRepository eventRepository;
    private final PlayerRepository playerRepository;
    private final InventoryService inventoryService;
    private final Random random = new Random();

    @Autowired
    public LocationServiceImpl(
            LocationRepository locationRepository,
            NPCRepository npcRepository,
            ItemRepository itemRepository,
            EventRepository eventRepository,
            PlayerRepository playerRepository,
            InventoryService inventoryService) {
        this.locationRepository = locationRepository;
        this.npcRepository = npcRepository;
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
        this.playerRepository = playerRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));
    }

    @Override
    public Location findByName(String name) {
        return locationRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with name: " + name));
    }

    @Override
    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        locationRepository.findAll().forEach(locations::add);
        return locations;
    }

    @Override
    public List<Location> getUnlockedLocations() {
        return locationRepository.findByLockedFalse();
    }

    @Override
    public Location save(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public void delete(Long id) {
        locationRepository.deleteById(id);
    }

    @Override
    public List<Location> getConnectedLocations(Long locationId) {
        Location location = findById(locationId);
        List<Location> connectedLocations = new ArrayList<>();

        for (Long connectedId : location.getConnectedLocations()) {
            locationRepository.findById(connectedId).ifPresent(connectedLocations::add);
        }

        return connectedLocations;
    }

    @Override
    @Transactional
    public boolean movePlayer(Long playerId, Long locationId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        Location currentLocation = getPlayerLocation(playerId);
        Location targetLocation = findById(locationId);

        // Check if the target location is connected to the current location
        if (currentLocation != null && !currentLocation.getConnectedLocations().contains(locationId)) {
            return false;
        }

        // Check if the target location is locked
        if (targetLocation.isLocked() && !checkUnlockRequirements(playerId, locationId)) {
            return false;
        }

        // Update player's location
        player = setPlayerLocation(playerId, locationId);

        return true;
    }

    @Override
    public List<NPC> getNPCsAtLocation(Long locationId) {
        Location location = findById(locationId);
        List<NPC> npcsAtLocation = new ArrayList<>();

        for (Long npcId : location.getNpcs()) {
            npcRepository.findById(npcId).ifPresent(npcsAtLocation::add);
        }

        return npcsAtLocation;
    }

    @Override
    public List<Item> getItemsAtLocation(Long locationId) {
        Location location = findById(locationId);
        List<Item> itemsAtLocation = new ArrayList<>();

        for (Long itemId : location.getItems()) {
            itemRepository.findById(itemId).ifPresent(itemsAtLocation::add);
        }

        return itemsAtLocation;
    }

    @Override
    @Transactional
    public Optional<Event> triggerRandomEvent(Long playerId, Long locationId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        Location location = findById(locationId);
        List<Long> possibleEvents = location.getPossibleEvents();

        if (possibleEvents.isEmpty()) {
            return Optional.empty();
        }

        // Check if the player is at the location
        if (player.getCurrentLocation() == null || !player.getCurrentLocation().getId().equals(locationId)) {
            return Optional.empty();
        }

        // Determine if an event should be triggered (e.g., 30% chance)
        if (random.nextDouble() > 0.3) {
            return Optional.empty();
        }

        // Randomly select an event from the possible events
        int randomIndex = random.nextInt(possibleEvents.size());
        Long eventId = possibleEvents.get(randomIndex);

        // Get the event details
        Optional<Event> eventOpt = eventRepository.findById(eventId);

        if (!eventOpt.isPresent()) {
            return Optional.empty();
        }

        Event event = eventOpt.get();

        // Process the event for the player
        // Process the event for the player with sophisticated logic based on event type
        if (event.getType() != null) {
            switch (event.getType()) {
                case BASE:
                    // Basic event with variable rewards based on player level
                    int baseExp = 5 + (player.getLevel() / 2);
                    player.setExp(player.getExp() + baseExp);
                    
                    // Small chance for bonus currency
                    if (random.nextDouble() < 0.2) {
                        player.setCurrency(player.getCurrency() + random.nextInt(5) + 1);
                    }
                    break;

                case CLIMACTIC:
                    // Climactic event with success/failure mechanics
                    int playerPower = player.getLevel() + player.getStat("strength") + player.getStat("intelligence");
                    int eventDifficulty = 50 + random.nextInt(30); // Base difficulty 50-80
                    
                    boolean won = (playerPower + random.nextInt(20)) > eventDifficulty;
                    if (won) {
                        int expReward = 15 + (player.getLevel() * 2);
                        player.setExp(player.getExp() + expReward);
                        player.setReputation(player.getReputation() + 5);
                        player.setCurrency(player.getCurrency() + 10 + random.nextInt(10));
                    } else {
                        // Partial failure - still get some experience
                        player.setExp(player.getExp() + 5);
                        int healthLoss = Math.min(player.getHealth() / 4, 10);
                        player.setHealth(Math.max(1, player.getHealth() - healthLoss));
                    }
                    break;

                case RANDOM:
                    // Random event with multiple possible outcomes
                    int outcome = random.nextInt(4);
                    switch (outcome) {
                        case 0: // Positive outcome
                            player.setExp(player.getExp() + 8);
                            player.setReputation(player.getReputation() + 2);
                            break;
                        case 1: // Neutral outcome
                            player.setExp(player.getExp() + 5);
                            break;
                        case 2: // Challenging outcome
                            player.setExp(player.getExp() + 12);
                            player.setHealth(Math.max(1, player.getHealth() - 5));
                            break;
                        case 3: // Lucky outcome
                            player.setExp(player.getExp() + 6);
                            player.setCurrency(player.getCurrency() + 15);
                            break;
                    }
                    break;

                case SEASONAL:
                    // Seasonal event with season-specific bonuses
                    player.setReputation(player.getReputation() + 3);
                    player.setExp(player.getExp() + 7);
                    
                    // Seasonal bonus based on current stats
                    if (player.getStat("charisma") > 15) {
                        player.setReputation(player.getReputation() + 2); // Charisma bonus
                    }
                    break;

                case ROMANCE:
                    // Romance event affecting reputation and relationships
                    player.setReputation(player.getReputation() + 2);
                    player.setExp(player.getExp() + 4);
                    
                    // Bonus for high charisma
                    if (player.getStat("charisma") > 20) {
                        player.setReputation(player.getReputation() + 3);
                    }
                    break;

                case CHOICE_TRIGGERED:
                    // Choice-triggered event with decision-based outcomes
                    player.setExp(player.getExp() + 10);
                    
                    // Bonus based on player's decision-making history (simulated)
                    if (player.getReputation() > 50) {
                        player.setExp(player.getExp() + 5); // Good reputation bonus
                    }
                    break;

                default:
                    // Default event handling
                    player.setExp(player.getExp() + 3);
                    break;
            }
        }

        // Save the updated player
        playerRepository.save(player);

        return Optional.of(event);
    }

    @Override
    @Transactional
    public boolean discoverHiddenArea(Long playerId, Long locationId, String areaName) {
        // Check if the player meets the requirements to discover the hidden area
        if (!checkDiscoveryRequirements(playerId, locationId, areaName)) {
            return false;
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Create a unique identifier for the discovered area
        String areaIdentifier = locationId + ":" + areaName;

        // Check if the player has already discovered this area
        if (player.getDiscoveredAreas().contains(areaIdentifier)) {
            return true; // Already discovered
        }

        // Add the area to the player's discovered areas
        player.getDiscoveredAreas().add(areaIdentifier);
        playerRepository.save(player);

        return true;
    }

    @Override
    public List<String> getHiddenAreas(Long locationId) {
        Location location = findById(locationId);
        return location.getHiddenAreas();
    }

    @Override
    public Map<String, String> getDiscoveryRequirements(Long locationId) {
        Location location = findById(locationId);
        return location.getDiscoveryRequirements();
    }

    @Override
    public boolean checkDiscoveryRequirements(Long playerId, Long locationId, String areaName) {
        Location location = findById(locationId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Get the requirements for the hidden area
        String requirements = location.getDiscoveryRequirements().get(areaName);

        if (requirements == null) {
            // No requirements specified, so discovery is not possible
            return false;
        }

        // Check if the player is at the location
        if (player.getCurrentLocation() == null || !player.getCurrentLocation().getId().equals(locationId)) {
            return false;
        }

        // Parse the requirements string to determine what the player needs
        // This implementation supports comprehensive requirement types including stats, skills, level, reputation, items, and time-based conditions
        // Supports complex boolean logic and multiple requirement combinations

        // Example requirement format: "stat:strength:10" or "item:sword" or "level:5"
        String[] parts = requirements.split(":");

        if (parts.length < 2) {
            return false;
        }

        String requirementType = parts[0];

        switch (requirementType) {
            case "stat":
                if (parts.length < 3) return false;
                String statName = parts[1];
                int requiredValue = Integer.parseInt(parts[2]);
                Integer playerStat = player.getStats().get(statName);
                return playerStat != null && playerStat >= requiredValue;

            case "skill":
                if (parts.length < 3) return false;
                String skillName = parts[1];
                int requiredSkillValue = Integer.parseInt(parts[2]);
                Integer playerSkill = player.getSkills().get(skillName);
                return playerSkill != null && playerSkill >= requiredSkillValue;

            case "level":
                if (parts.length < 2) return false;
                int requiredLevel = Integer.parseInt(parts[1]);
                return player.getLevel() >= requiredLevel;

            case "reputation":
                if (parts.length < 2) return false;
                int requiredReputation = Integer.parseInt(parts[1]);
                return player.getReputation() >= requiredReputation;

            case "item":
                if (parts.length < 2) return false;
                String itemName = parts[1];
                // Check if player has the required item in inventory
                return inventoryService.getPlayerItems(player).keySet().stream()
                        .anyMatch(item -> item.getName().equalsIgnoreCase(itemName));

            case "time":
                if (parts.length < 3) return false;
                String timeType = parts[1];
                int timeValue = Integer.parseInt(parts[2]);
                return checkTimeRequirement(timeType, timeValue);

            case "currency":
                if (parts.length < 2) return false;
                int requiredCurrency = Integer.parseInt(parts[1]);
                return player.getCurrency() >= requiredCurrency;

            case "and":
                // Complex AND logic: "and:req1,req2,req3"
                if (parts.length < 2) return false;
                String[] andRequirements = parts[1].split(",");
                for (String andReq : andRequirements) {
                    if (!checkSingleRequirement(playerId, locationId, andReq.trim())) {
                        return false;
                    }
                }
                return true;

            case "or":
                // Complex OR logic: "or:req1,req2,req3"
                if (parts.length < 2) return false;
                String[] orRequirements = parts[1].split(",");
                for (String orReq : orRequirements) {
                    if (checkSingleRequirement(playerId, locationId, orReq.trim())) {
                        return true;
                    }
                }
                return false;

            case "not":
                // Negation logic: "not:requirement"
                if (parts.length < 2) return false;
                return !checkSingleRequirement(playerId, locationId, parts[1]);

            // Add more requirement types as needed

            default:
                return false;
        }
    }

    @Override
    @Transactional
    public boolean unlockLocation(Long locationId) {
        Location location = findById(locationId);

        if (!location.isLocked()) {
            // Location is already unlocked
            return true;
        }

        location.setLocked(false);
        locationRepository.save(location);

        return true;
    }

    @Override
    public boolean checkUnlockRequirements(Long playerId, Long locationId) {
        Location location = findById(locationId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        if (!location.isLocked()) {
            // Location is already unlocked
            return true;
        }

        String requirements = location.getUnlockRequirements();

        if (requirements == null || requirements.isEmpty()) {
            // No requirements specified, so unlocking is not possible
            return false;
        }

        // Parse the requirements string to determine what the player needs
        // This implementation supports comprehensive requirement checking including stats, skills, level, reputation, quests, achievements, and location dependencies
        // TODO: Consider extending with additional requirement types like item possession, complex boolean conditions, or time-based requirements

        // Example requirement format: "stat:strength:10" or "item:sword" or "level:5"
        String[] parts = requirements.split(":");

        if (parts.length < 2) {
            return false;
        }

        String requirementType = parts[0];

        switch (requirementType) {
            case "stat":
                if (parts.length < 3) return false;
                String statName = parts[1];
                int requiredValue = Integer.parseInt(parts[2]);
                Integer playerStat = player.getStats().get(statName);
                return playerStat != null && playerStat >= requiredValue;

            case "skill":
                if (parts.length < 3) return false;
                String skillName = parts[1];
                int requiredSkillValue = Integer.parseInt(parts[2]);
                Integer playerSkill = player.getSkills().get(skillName);
                return playerSkill != null && playerSkill >= requiredSkillValue;

            case "level":
                if (parts.length < 2) return false;
                int requiredLevel = Integer.parseInt(parts[1]);
                return player.getLevel() >= requiredLevel;

            case "reputation":
                if (parts.length < 2) return false;
                int requiredReputation = Integer.parseInt(parts[1]);
                return player.getReputation() >= requiredReputation;

            case "quest":
                if (parts.length < 2) return false;
                String questId = parts[1];
                return player.getQuests().contains(questId);

            case "achievement":
                if (parts.length < 2) return false;
                String achievementId = parts[1];
                return player.getAchievements().contains(achievementId);

            case "location":
                if (parts.length < 2) return false;
                String locationName = parts[1];
                Location requiredLocation = locationRepository.findByName(locationName)
                        .orElse(null);
                return requiredLocation != null && !requiredLocation.isLocked();

            // Add more requirement types as needed

            default:
                return false;
        }
    }

    @Override
    public Location getPlayerLocation(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        return player.getCurrentLocation();
    }

    @Override
    @Transactional
    public Player setPlayerLocation(Long playerId, Long locationId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        player.setCurrentLocation(location);
        return playerRepository.save(player);
    }

    /**
     * Check if a time-based requirement is met.
     * 
     * @param timeType the type of time requirement (hour, day, month)
     * @param timeValue the required time value
     * @return true if the time requirement is met
     */
    private boolean checkTimeRequirement(String timeType, int timeValue) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        switch (timeType.toLowerCase()) {
            case "hour":
                return now.getHour() >= timeValue;
            case "day":
                return now.getDayOfMonth() >= timeValue;
            case "month":
                return now.getMonthValue() >= timeValue;
            case "year":
                return now.getYear() >= timeValue;
            case "dayofweek":
                // Monday = 1, Sunday = 7
                return now.getDayOfWeek().getValue() == timeValue;
            default:
                return false;
        }
    }

    /**
     * Check a single requirement directly without complex boolean logic.
     * This method allows for basic requirement checking to support complex boolean operations.
     * 
     * @param playerId the player ID
     * @param locationId the location ID
     * @param requirement the requirement string to check
     * @return true if the requirement is met
     */
    private boolean checkSingleRequirement(Long playerId, Long locationId, String requirement) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        String[] parts = requirement.split(":");
        if (parts.length < 2) {
            return false;
        }

        String requirementType = parts[0];

        switch (requirementType) {
            case "stat":
                if (parts.length < 3) return false;
                String statName = parts[1];
                int requiredValue = Integer.parseInt(parts[2]);
                Integer playerStat = player.getStats().get(statName);
                return playerStat != null && playerStat >= requiredValue;

            case "skill":
                if (parts.length < 3) return false;
                String skillName = parts[1];
                int requiredSkillValue = Integer.parseInt(parts[2]);
                Integer playerSkill = player.getSkills().get(skillName);
                return playerSkill != null && playerSkill >= requiredSkillValue;

            case "level":
                if (parts.length < 2) return false;
                int requiredLevel = Integer.parseInt(parts[1]);
                return player.getLevel() >= requiredLevel;

            case "reputation":
                if (parts.length < 2) return false;
                int requiredReputation = Integer.parseInt(parts[1]);
                return player.getReputation() >= requiredReputation;

            case "item":
                if (parts.length < 2) return false;
                String itemName = parts[1];
                return inventoryService.getPlayerItems(player).keySet().stream()
                        .anyMatch(item -> item.getName().equalsIgnoreCase(itemName));

            case "currency":
                if (parts.length < 2) return false;
                int requiredCurrency = Integer.parseInt(parts[1]);
                return player.getCurrency() >= requiredCurrency;

            case "quest":
                if (parts.length < 2) return false;
                String questId = parts[1];
                return player.getQuests().contains(questId);

            case "achievement":
                if (parts.length < 2) return false;
                String achievementId = parts[1];
                return player.getAchievements().contains(achievementId);

            default:
                return false;
        }
    }
}
