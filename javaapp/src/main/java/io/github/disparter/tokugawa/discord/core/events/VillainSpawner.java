package io.github.disparter.tokugawa.discord.core.events;

import lombok.extern.slf4j.Slf4j;
import io.github.disparter.tokugawa.discord.core.models.Villain;
import io.github.disparter.tokugawa.discord.core.models.Villain.VillainType;
import io.github.disparter.tokugawa.discord.core.models.GameCalendar.Season;
import io.github.disparter.tokugawa.discord.core.services.VillainService;
import io.github.disparter.tokugawa.discord.core.services.GameCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.Arrays;

/**
 * Scheduled task for spawning villains and minions in the game world.
 */
@Component
@Slf4j
public class VillainSpawner {

    private final VillainService villainService;
    private final GameCalendarService gameCalendarService;
    private final Random random = new Random();

    // List of possible spawn locations
    private final List<String> spawnLocations = Arrays.asList(
        "Forest", "Mountain", "Cave", "Dungeon", "Castle", "Village", "City", "Beach", "Desert", "Swamp"
    );

    @Autowired
    public VillainSpawner(VillainService villainService, GameCalendarService gameCalendarService) {
        this.villainService = villainService;
        this.gameCalendarService = gameCalendarService;
    }

    /**
     * Scheduled task that runs daily to spawn villains.
     * This method will spawn a random villain with a 30% chance.
     */
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void spawnDailyVillain() {
        try {
            log.info("Running daily villain spawn check");

            // 30% chance to spawn a villain
            if (random.nextInt(100) < 30) {
                // Select a random location
                String location = spawnLocations.get(random.nextInt(spawnLocations.size()));

                // Determine villain type based on current season
                VillainType villainType = determineVillainType();

                // Spawn a random villain
                villainService.spawnRandomVillain(villainType, location).ifPresent(villain -> {
                    log.info("Spawned daily villain: {} at {}", villain.getName(), location);

                    // Spawn minions for the villain (20% chance)
                    if (random.nextInt(100) < 20) {
                        int minionCount = random.nextInt(3) + 1; // 1-3 minions
                        List<Villain> minions = villainService.spawnMinions(villain.getId(), minionCount);
                        log.info("Spawned {} minions for villain: {}", minions.size(), villain.getName());
                    }
                });
            } else {
                log.info("No villain spawned today");
            }
        } catch (Exception e) {
            log.error("Error in spawnDailyVillain: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled task that runs weekly to spawn a boss villain.
     * This method will spawn a boss villain with a 50% chance.
     */
    @Scheduled(cron = "0 0 0 * * 1") // Run at midnight every Monday
    public void spawnWeeklyBoss() {
        try {
            log.info("Running weekly boss spawn check");

            // 50% chance to spawn a boss
            if (random.nextInt(100) < 50) {
                // Select a random location
                String location = spawnLocations.get(random.nextInt(spawnLocations.size()));

                // Spawn a random boss villain
                villainService.spawnRandomVillain(VillainType.BOSS, location).ifPresent(villain -> {
                    log.info("Spawned weekly boss: {} at {}", villain.getName(), location);

                    // Always spawn minions for a boss
                    int minionCount = random.nextInt(5) + 3; // 3-7 minions
                    List<Villain> minions = villainService.spawnMinions(villain.getId(), minionCount);
                    log.info("Spawned {} minions for boss: {}", minions.size(), villain.getName());
                });
            } else {
                log.info("No boss spawned this week");
            }
        } catch (Exception e) {
            log.error("Error in spawnWeeklyBoss: {}", e.getMessage(), e);
        }
    }

    /**
     * Determines the type of villain to spawn based on the current season.
     * 
     * @return the villain type to spawn
     */
    private VillainType determineVillainType() {
        // Get the current season
        Season currentSeason = gameCalendarService.getCurrentSeason();

        // Determine villain type based on season
        switch (currentSeason) {
            case WINTER:
                // Higher chance for elite villains in winter
                return random.nextInt(100) < 40 ? VillainType.ELITE : VillainType.REGULAR;
            case SPRING:
                // Mostly regular villains in spring
                return random.nextInt(100) < 20 ? VillainType.ELITE : VillainType.REGULAR;
            case SUMMER:
                // Mix of regular and elite villains in summer
                return random.nextInt(100) < 30 ? VillainType.ELITE : VillainType.REGULAR;
            case AUTUMN:
                // Higher chance for elite villains in autumn
                return random.nextInt(100) < 40 ? VillainType.ELITE : VillainType.REGULAR;
            default:
                return VillainType.REGULAR;
        }
    }

    /**
     * Cleans up inactive villains and minions.
     * This method will deactivate villains that have been active for too long.
     */
    @Scheduled(cron = "0 0 3 * * *") // Run at 3 AM every day
    public void cleanupVillains() {
        try {
            log.info("Running villain cleanup");

            // Get all active villains
            List<Villain> activeVillains = villainService.getActiveVillains();

            for (Villain villain : activeVillains) {
                // 10% chance to deactivate a villain each day
                if (random.nextInt(100) < 10) {
                    villain.setActive(false);
                    villainService.updateVillain(villain);
                    log.info("Deactivated villain: {}", villain.getName());

                    // Deactivate all minions
                    for (Long minionId : villain.getMinionIds()) {
                        villainService.findById(minionId).ifPresent(minion -> {
                            minion.setActive(false);
                            villainService.updateVillain(minion);
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in cleanupVillains: {}", e.getMessage(), e);
        }
    }
}
