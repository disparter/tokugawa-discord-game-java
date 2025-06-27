package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Relationship entity operations.
 */
@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    /**
     * Find a relationship by player and NPC.
     *
     * @param player the player
     * @param npc the NPC
     * @return the relationship, if found
     */
    Optional<Relationship> findByPlayerAndNpc(Player player, NPC npc);

    /**
     * Find relationships by player.
     *
     * @param player the player
     * @return the list of relationships for the player
     */
    List<Relationship> findByPlayer(Player player);

    /**
     * Find relationships by NPC.
     *
     * @param npc the NPC
     * @return the list of relationships for the NPC
     */
    List<Relationship> findByNpc(NPC npc);

    /**
     * Find relationships by status.
     *
     * @param status the relationship status
     * @return the list of relationships with the specified status
     */
    List<Relationship> findByStatus(Relationship.RelationshipStatus status);

    /**
     * Find relationships by player and status.
     *
     * @param player the player
     * @param status the relationship status
     * @return the list of relationships for the player with the specified status
     */
    List<Relationship> findByPlayerAndStatus(Player player, Relationship.RelationshipStatus status);

    /**
     * Find relationships by affinity greater than or equal to the specified value.
     *
     * @param affinity the minimum affinity value
     * @return the list of relationships with affinity >= the specified value
     */
    List<Relationship> findByAffinityGreaterThanEqual(Integer affinity);
}