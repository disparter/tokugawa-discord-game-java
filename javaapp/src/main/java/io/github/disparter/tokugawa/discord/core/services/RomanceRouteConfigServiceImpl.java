package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.RomanceRouteConfig;
import io.github.disparter.tokugawa.discord.core.repositories.RomanceRouteConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of RomanceRouteConfigService that loads configurations from database
 * with fallback to hardcoded data for compatibility.
 */
@Service
@Slf4j
public class RomanceRouteConfigServiceImpl implements RomanceRouteConfigService {

    private final RomanceRouteConfigRepository romanceRouteConfigRepository;

    // Cache for loaded configurations
    private final Map<Long, Integer> romanceRequirementsCache = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> romanceChaptersCache = new ConcurrentHashMap<>();

    // Fallback hardcoded data for compatibility
    private final Map<Long, Integer> fallbackRomanceRequirements = new HashMap<>();
    private final Map<Long, List<String>> fallbackRomanceChapters = new HashMap<>();

    @Autowired
    public RomanceRouteConfigServiceImpl(RomanceRouteConfigRepository romanceRouteConfigRepository) {
        this.romanceRouteConfigRepository = romanceRouteConfigRepository;
        initializeFallbackData();
    }

    @PostConstruct
    public void loadConfigurationsOnStartup() {
        reloadConfigurations();
    }

    /**
     * Initialize fallback hardcoded data for existing NPCs.
     * This ensures backward compatibility while transitioning to database-driven configuration.
     */
    private void initializeFallbackData() {
        // Hardcoded romance route data (same as original implementation)
        fallbackRomanceRequirements.put(1L, 80);
        fallbackRomanceChapters.put(1L, List.of("romance_1_1", "romance_1_2", "romance_1_3"));

        fallbackRomanceRequirements.put(2L, 80);
        fallbackRomanceChapters.put(2L, List.of("romance_2_1", "romance_2_2", "romance_2_3"));

        fallbackRomanceRequirements.put(3L, 80);
        fallbackRomanceChapters.put(3L, List.of("romance_3_1", "romance_3_2", "romance_3_3"));

        fallbackRomanceRequirements.put(4L, 80);
        fallbackRomanceChapters.put(4L, List.of("romance_4_1", "romance_4_2", "romance_4_3"));

        fallbackRomanceRequirements.put(5L, 80);
        fallbackRomanceChapters.put(5L, List.of("romance_5_1", "romance_5_2", "romance_5_3"));

        fallbackRomanceRequirements.put(6L, 80);
        fallbackRomanceChapters.put(6L, List.of("romance_6_1", "romance_6_2", "romance_6_3"));

        fallbackRomanceRequirements.put(7L, 80);
        fallbackRomanceChapters.put(7L, List.of("romance_7_1", "romance_7_2", "romance_7_3"));

        log.info("Initialized fallback romance route data for {} NPCs", fallbackRomanceRequirements.size());
    }

    @Override
    public void reloadConfigurations() {
        try {
            // Clear existing cache
            romanceRequirementsCache.clear();
            romanceChaptersCache.clear();

            // Load active configurations from database
            List<RomanceRouteConfig> configs = romanceRouteConfigRepository.findByIsActiveTrue();

            for (RomanceRouteConfig config : configs) {
                romanceRequirementsCache.put(config.getNpcId(), config.getRequiredAffinity());
                romanceChaptersCache.put(config.getNpcId(), config.getChapterSequenceAsList());
            }

            log.info("Loaded {} romance route configurations from database", configs.size());

            // If no database configurations exist, create them from fallback data
            if (configs.isEmpty()) {
                createDefaultConfigurations();
            }

        } catch (Exception e) {
            log.warn("Failed to load romance configurations from database, using fallback data: {}", e.getMessage());
            // Use fallback data if database loading fails
            romanceRequirementsCache.putAll(fallbackRomanceRequirements);
            romanceChaptersCache.putAll(fallbackRomanceChapters);
        }
    }

    /**
     * Create default romance route configurations in the database from fallback data.
     * This is called when no configurations exist in the database.
     */
    private void createDefaultConfigurations() {
        log.info("No romance configurations found in database, creating default configurations");

        for (Map.Entry<Long, Integer> entry : fallbackRomanceRequirements.entrySet()) {
            Long npcId = entry.getKey();
            Integer requiredAffinity = entry.getValue();
            List<String> chapters = fallbackRomanceChapters.get(npcId);

            try {
                RomanceRouteConfig config = RomanceRouteConfig.builder()
                        .npcId(npcId)
                        .requiredAffinity(requiredAffinity)
                        .isActive(true)
                        .description("Default romance route for NPC " + npcId)
                        .build();
                config.setChapterSequenceFromList(chapters);

                romanceRouteConfigRepository.save(config);
                
                // Update cache
                romanceRequirementsCache.put(npcId, requiredAffinity);
                romanceChaptersCache.put(npcId, chapters);

                log.debug("Created default romance configuration for NPC {}", npcId);
            } catch (Exception e) {
                log.error("Failed to create default romance configuration for NPC {}: {}", npcId, e.getMessage());
            }
        }

        log.info("Created {} default romance configurations", fallbackRomanceRequirements.size());
    }

    @Override
    public Optional<Integer> getRequiredAffinity(Long npcId) {
        Integer affinity = romanceRequirementsCache.get(npcId);
        if (affinity != null) {
            return Optional.of(affinity);
        }

        // Fallback to hardcoded data
        return Optional.ofNullable(fallbackRomanceRequirements.get(npcId));
    }

    @Override
    public List<String> getChapterSequence(Long npcId) {
        List<String> chapters = romanceChaptersCache.get(npcId);
        if (chapters != null) {
            return chapters;
        }

        // Fallback to hardcoded data
        return fallbackRomanceChapters.getOrDefault(npcId, List.of());
    }

    @Override
    public Map<Long, Integer> getAllRomanceRequirements() {
        Map<Long, Integer> combined = new HashMap<>(fallbackRomanceRequirements);
        combined.putAll(romanceRequirementsCache);
        return combined;
    }

    @Override
    public Map<Long, List<String>> getAllRomanceChapters() {
        Map<Long, List<String>> combined = new HashMap<>(fallbackRomanceChapters);
        combined.putAll(romanceChaptersCache);
        return combined;
    }

    @Override
    public boolean hasRomanceRoute(Long npcId) {
        return romanceRequirementsCache.containsKey(npcId) || 
               fallbackRomanceRequirements.containsKey(npcId);
    }

    @Override
    public List<Long> getAllRomanceNpcIds() {
        return getAllRomanceRequirements().keySet()
                .stream()
                .collect(Collectors.toList());
    }
}