package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.RomanceRouteConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing RomanceRouteConfig entities.
 */
@Repository
public interface RomanceRouteConfigRepository extends CrudRepository<RomanceRouteConfig, Long> {

    /**
     * Find a romance route configuration by NPC ID.
     * 
     * @param npcId the NPC ID
     * @return the romance route configuration if found
     */
    Optional<RomanceRouteConfig> findByNpcId(Long npcId);

    /**
     * Find all active romance route configurations.
     * 
     * @return list of active romance route configurations
     */
    List<RomanceRouteConfig> findByIsActiveTrue();

    /**
     * Find romance route configurations by required affinity level.
     * 
     * @param requiredAffinity the minimum affinity level
     * @return list of romance route configurations with the specified affinity requirement
     */
    List<RomanceRouteConfig> findByRequiredAffinity(Integer requiredAffinity);

    /**
     * Find romance route configurations with affinity requirement less than or equal to the specified value.
     * 
     * @param maxAffinity the maximum affinity level to filter by
     * @return list of romance route configurations
     */
    List<RomanceRouteConfig> findByRequiredAffinityLessThanEqual(Integer maxAffinity);

    /**
     * Check if a romance route configuration exists for the given NPC ID.
     * 
     * @param npcId the NPC ID
     * @return true if a configuration exists, false otherwise
     */
    boolean existsByNpcId(Long npcId);
}