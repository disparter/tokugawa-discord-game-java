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
}