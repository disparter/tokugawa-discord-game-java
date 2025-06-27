package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Technique;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing Technique entities.
 */
@Repository
public interface TechniqueRepository extends CrudRepository<Technique, Long> {

    /**
     * Find a technique by its unique technique ID.
     *
     * @param techniqueId the technique ID
     * @return the technique if found
     */
    Optional<Technique> findByTechniqueId(String techniqueId);

    /**
     * Find all techniques of a specific type.
     *
     * @param type the technique type
     * @return a list of techniques
     */
    List<Technique> findByType(Technique.TechniqueType type);

    /**
     * Find all techniques known by a player.
     *
     * @param player the player
     * @return a list of techniques
     */
    List<Technique> findByPlayersContaining(Player player);

    /**
     * Find all techniques known by an NPC.
     *
     * @param npc the NPC
     * @return a list of techniques
     */
    List<Technique> findByNpcsContaining(NPC npc);

    /**
     * Find all learnable techniques.
     *
     * @return a list of learnable techniques
     */
    List<Technique> findByLearnable(Boolean learnable);
}