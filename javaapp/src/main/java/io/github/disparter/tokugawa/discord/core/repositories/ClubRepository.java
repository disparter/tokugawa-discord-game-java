package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Club;
import io.github.disparter.tokugawa.discord.core.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Club entity operations.
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    /**
     * Find a club by its club ID.
     *
     * @param clubId the club ID
     * @return the club, if found
     */
    Optional<Club> findByClubId(String clubId);

    /**
     * Find a club by name.
     *
     * @param name the club name
     * @return the club, if found
     */
    Optional<Club> findByName(String name);

    /**
     * Find clubs by leader ID.
     *
     * @param leaderId the leader ID
     * @return the list of clubs led by the specified leader
     */
    List<Club> findByLeaderId(String leaderId);

    /**
     * Find clubs by member.
     *
     * @param member the member
     * @return the list of clubs the player is a member of
     */
    List<Club> findByMembersContains(Player member);

    /**
     * Find clubs by name containing the given text.
     *
     * @param name the name to search for
     * @return the list of clubs with matching names
     */
    List<Club> findByNameContainingIgnoreCase(String name);

    /**
     * Find clubs that have an alliance with the specified club.
     *
     * @param club the club
     * @return the list of clubs that have an alliance with the specified club
     */
    List<Club> findByAlliancesContains(Club club);

    /**
     * Find clubs that have a rivalry with the specified club.
     *
     * @param club the club
     * @return the list of clubs that have a rivalry with the specified club
     */
    List<Club> findByRivalriesContains(Club club);
}