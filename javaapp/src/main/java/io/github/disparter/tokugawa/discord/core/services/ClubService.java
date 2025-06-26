package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Club;
import io.github.disparter.tokugawa.discord.core.models.Player;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing club-related operations.
 */
public interface ClubService {

    /**
     * Find a club by its ID.
     *
     * @param id the club ID
     * @return the club if found, or null
     */
    Club findById(Long id);

    /**
     * Get all clubs.
     *
     * @return list of all clubs
     */
    List<Club> getAllClubs();

    /**
     * Get clubs available for a player to join.
     *
     * @param playerId the player ID
     * @return list of available clubs
     */
    List<Club> getAvailableClubsForPlayer(Long playerId);

    /**
     * Get clubs that a player is a member of.
     *
     * @param playerId the player ID
     * @return list of clubs the player is a member of
     */
    List<Club> getPlayerClubs(Long playerId);

    /**
     * Join a club.
     *
     * @param clubId the club ID
     * @param playerId the player ID
     * @return the club that was joined
     */
    Club joinClub(Long clubId, Long playerId);

    /**
     * Leave a club.
     *
     * @param clubId the club ID
     * @param playerId the player ID
     * @return the club that was left
     */
    Club leaveClub(Long clubId, Long playerId);

    /**
     * Get members of a club.
     *
     * @param clubId the club ID
     * @return list of players in the club
     */
    List<Player> getClubMembers(Long clubId);

    /**
     * Form an alliance between two clubs.
     *
     * @param clubId1 the first club ID
     * @param clubId2 the second club ID
     * @return the first club with the updated alliance
     */
    Club formAlliance(Long clubId1, Long clubId2);

    /**
     * Break an alliance between two clubs.
     *
     * @param clubId1 the first club ID
     * @param clubId2 the second club ID
     * @return the first club with the updated alliance
     */
    Club breakAlliance(Long clubId1, Long clubId2);

    /**
     * Declare a rivalry between two clubs.
     *
     * @param clubId1 the first club ID
     * @param clubId2 the second club ID
     * @return the first club with the updated rivalry
     */
    Club declareRivalry(Long clubId1, Long clubId2);

    /**
     * End a rivalry between two clubs.
     *
     * @param clubId1 the first club ID
     * @param clubId2 the second club ID
     * @return the first club with the updated rivalry
     */
    Club endRivalry(Long clubId1, Long clubId2);

    /**
     * Get all alliances of a club.
     *
     * @param clubId the club ID
     * @return list of allied clubs
     */
    List<Club> getAlliances(Long clubId);

    /**
     * Get all rivalries of a club.
     *
     * @param clubId the club ID
     * @return list of rival clubs
     */
    List<Club> getRivalries(Long clubId);

    /**
     * Start a competition between clubs.
     *
     * @param competitionId the competition ID
     * @param participatingClubIds list of participating club IDs
     * @return map of club IDs to their initial scores
     */
    Map<Long, Integer> startCompetition(String competitionId, List<Long> participatingClubIds);

    /**
     * Update competition scores.
     *
     * @param competitionId the competition ID
     * @param scores map of club IDs to their scores
     * @return map of club IDs to their updated scores
     */
    Map<Long, Integer> updateCompetitionScores(String competitionId, Map<Long, Integer> scores);

    /**
     * End a competition and update club rankings and reputations.
     *
     * @param competitionId the competition ID
     * @return map of club IDs to their final scores
     */
    Map<Long, Integer> endCompetition(String competitionId);

    /**
     * Get competition results for a club.
     *
     * @param clubId the club ID
     * @return map of competition IDs to scores
     */
    Map<String, Integer> getCompetitionResults(Long clubId);

    /**
     * Update club ranking based on reputation.
     *
     * @param clubId the club ID
     * @return the updated club
     */
    Club updateRanking(Long clubId);

    /**
     * Get clubs ranked by reputation.
     *
     * @return list of clubs sorted by ranking
     */
    List<Club> getRankedClubs();

    /**
     * Save a club.
     *
     * @param club the club to save
     * @return the saved club
     */
    Club save(Club club);
}
