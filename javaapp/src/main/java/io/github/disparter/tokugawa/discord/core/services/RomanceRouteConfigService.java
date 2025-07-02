package io.github.disparter.tokugawa.discord.core.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing romance route configurations.
 * This replaces hardcoded romance data with configurable database or file-based loading.
 */
public interface RomanceRouteConfigService {

    /**
     * Get the required affinity level for a specific NPC's romance route.
     * 
     * @param npcId the NPC ID
     * @return the required affinity level, or empty if no route exists
     */
    Optional<Integer> getRequiredAffinity(Long npcId);

    /**
     * Get the chapter sequence (event IDs) for a specific NPC's romance route.
     * 
     * @param npcId the NPC ID
     * @return the list of event IDs in order, or empty list if no route exists
     */
    List<String> getChapterSequence(Long npcId);

    /**
     * Get all romance requirements as a map for compatibility with existing code.
     * 
     * @return map of NPC ID to required affinity level
     */
    Map<Long, Integer> getAllRomanceRequirements();

    /**
     * Get all romance chapters as a map for compatibility with existing code.
     * 
     * @return map of NPC ID to list of event IDs
     */
    Map<Long, List<String>> getAllRomanceChapters();

    /**
     * Check if a romance route exists for the given NPC.
     * 
     * @param npcId the NPC ID
     * @return true if a romance route exists, false otherwise
     */
    boolean hasRomanceRoute(Long npcId);

    /**
     * Get all active NPCs with romance routes.
     * 
     * @return list of NPC IDs that have active romance routes
     */
    List<Long> getAllRomanceNpcIds();

    /**
     * Reload romance configurations from the data source.
     * This method can be called to refresh configurations without restarting the application.
     */
    void reloadConfigurations();
}