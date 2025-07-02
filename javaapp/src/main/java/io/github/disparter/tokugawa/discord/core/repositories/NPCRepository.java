package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for NPC entity operations.
 */
@Repository
public interface NPCRepository extends JpaRepository<NPC, Long> {

    /**
     * Find an NPC by its NPC ID.
     *
     * @param npcId the NPC ID
     * @return the NPC, if found
     */
    Optional<NPC> findByNpcId(String npcId);

    /**
     * Find an NPC by name.
     *
     * @param name the NPC name
     * @return the NPC, if found
     */
    Optional<NPC> findByName(String name);

    /**
     * Find NPCs by type.
     *
     * @param type the NPC type
     * @return the list of NPCs of the specified type
     */
    List<NPC> findByType(NPC.NPCType type);

    /**
     * Find NPCs by name containing the given text.
     *
     * @param name the name to search for
     * @return the list of NPCs with matching names
     */
    List<NPC> findByNameContainingIgnoreCase(String name);

    /**
     * Find NPCs by location ID.
     *
     * @param locationId the location ID
     * @return the list of NPCs at the specified location
     */
    List<NPC> findByLocationId(Long locationId);

    /**
     * Find NPCs that are traders.
     *
     * @return the list of trader NPCs
     */
    List<NPC> findByIsTraderTrue();

    /**
     * Find NPCs by location ID and trader status.
     *
     * @param locationId the location ID
     * @param isTrader whether the NPC is a trader
     * @return the list of NPCs at the specified location with the specified trader status
     */
    List<NPC> findByLocationIdAndIsTrader(Long locationId, Boolean isTrader);

    /**
     * Find trader NPCs at a specific location.
     *
     * @param locationId the location ID
     * @return the list of trader NPCs at the specified location
     */
    List<NPC> findByLocationIdAndIsTraderTrue(Long locationId);
}