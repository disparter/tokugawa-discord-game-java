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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

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
    private final Random random = new Random();

    @Autowired
    public LocationServiceImpl(
            LocationRepository locationRepository,
            NPCRepository npcRepository,
            ItemRepository itemRepository,
            EventRepository eventRepository,
            PlayerRepository playerRepository) {
        this.locationRepository = locationRepository;
        this.npcRepository = npcRepository;
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
        this.playerRepository = playerRepository;
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
        // This is a simplified implementation
        // In a real implementation, this would be more sophisticated

        // Example: Update player stats based on event type
        switch (event.getType()) {
            case "COMBAT":
                // Simulate a combat event
                boolean won = random.nextBoolean();
                if (won) {
                    player.setExp(player.getExp() + 10);
                    // Could also add items to inventory, etc.
                } else {
                    // Maybe lose some health or items
                }
                break;

            case "DISCOVERY":
                // Player discovers something
                player.setExp(player.getExp() + 5);
                // Could also add items to inventory, etc.
                break;

            case "SOCIAL":
                // Social interaction event
                player.setReputation(player.getReputation() + 2);
                break;

            case "QUEST":
                // Quest-related event
                // Could add a quest to the player's quest list
                break;

            // Add more event types as needed
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
        // This is a simplified implementation that supports basic requirement types
        // In a real implementation, this would be more sophisticated

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
        // This is a simplified implementation that supports basic requirement types
        // In a real implementation, this would be more sophisticated

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
}
