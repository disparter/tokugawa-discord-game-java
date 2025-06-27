package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import java.util.List;
import java.util.Optional;

/**
 * Service for NPC operations.
 */
public interface NPCService {

    /**
     * Find an NPC by its ID.
     *
     * @param id the NPC ID
     * @return the NPC, if found
     */
    NPC findById(Long id);

    /**
     * Find an NPC by its name.
     *
     * @param name the NPC name
     * @return the NPC, if found
     */
    NPC findByName(String name);

    /**
     * Find all NPCs.
     *
     * @return the list of all NPCs
     */
    List<NPC> findAll();

    /**
     * Find NPCs by type.
     *
     * @param type the NPC type
     * @return the list of NPCs of the specified type
     */
    List<NPC> findByType(NPC.NPCType type);

    /**
     * Save an NPC.
     *
     * @param npc the NPC to save
     * @return the saved NPC
     */
    NPC save(NPC npc);

    /**
     * Delete an NPC.
     *
     * @param id the ID of the NPC to delete
     */
    void delete(Long id);
}