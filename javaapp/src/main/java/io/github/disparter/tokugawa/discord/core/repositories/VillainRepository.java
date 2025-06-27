package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Villain;
import io.github.disparter.tokugawa.discord.core.models.Villain.VillainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing villain data.
 */
@Repository
public interface VillainRepository extends JpaRepository<Villain, Long> {

    /**
     * Find a villain by its name.
     *
     * @param name the villain name
     * @return the villain if found
     */
    Optional<Villain> findByName(String name);

    /**
     * Find all active villains.
     *
     * @return a list of active villains
     */
    List<Villain> findByActiveTrue();

    /**
     * Find all active villains of a specific type.
     *
     * @param type the villain type
     * @return a list of active villains of the specified type
     */
    List<Villain> findByActiveTrueAndType(VillainType type);

    /**
     * Find all active villains at a specific location.
     *
     * @param location the spawn location
     * @return a list of active villains at the specified location
     */
    List<Villain> findByActiveTrueAndSpawnLocation(String location);

    /**
     * Find all active villains with a specific spawn condition.
     *
     * @param condition the spawn condition
     * @return a list of active villains with the specified spawn condition
     */
    List<Villain> findByActiveTrueAndSpawnCondition(String condition);

    /**
     * Find all active villains with minions.
     *
     * @return a list of active villains with minions
     */
    List<Villain> findByActiveTrueAndMinionIdsIsNotEmpty();
}