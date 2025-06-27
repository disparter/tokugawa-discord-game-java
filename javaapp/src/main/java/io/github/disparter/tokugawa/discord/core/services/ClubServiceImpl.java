package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Club;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.ClubRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the ClubService interface.
 */
@Service
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;

    // Map to store ongoing competitions: competitionId -> map of clubId to score
    private final Map<String, Map<Long, Integer>> ongoingCompetitions = new HashMap<>();

    @Autowired
    public ClubServiceImpl(ClubRepository clubRepository, 
                          PlayerRepository playerRepository) {
        this.clubRepository = clubRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Club findById(Long id) {
        return clubRepository.findById(id).orElse(null);
    }

    @Override
    public List<Club> getAllClubs() {
        List<Club> clubs = new ArrayList<>();
        clubRepository.findAll().forEach(clubs::add);
        return clubs;
    }

    @Override
    public List<Club> getAvailableClubsForPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Get all clubs
        List<Club> allClubs = getAllClubs();

        // Filter out clubs that the player is already a member of
        return allClubs.stream()
                .filter(club -> !club.getMembers().contains(player))
                .collect(Collectors.toList());
    }

    @Override
    public List<Club> getPlayerClubs(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Get all clubs
        List<Club> allClubs = getAllClubs();

        // Filter clubs that the player is a member of
        return allClubs.stream()
                .filter(club -> club.getMembers().contains(player))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Club joinClub(Long clubId, Long playerId) {
        Club club = findById(clubId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Check if player is already a member
        if (club.getMembers().contains(player)) {
            return club;
        }

        // Add player to club
        player.setClubId(club.getId().toString());
        playerRepository.save(player);

        // Refresh club to get updated members
        return findById(clubId);
    }

    @Override
    @Transactional
    public Club leaveClub(Long clubId, Long playerId) {
        Club club = findById(clubId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

        // Check if player is a member
        if (!club.getMembers().contains(player)) {
            return club;
        }

        // Remove player from club
        player.setClubId(null);
        playerRepository.save(player);

        // Refresh club to get updated members
        return findById(clubId);
    }

    @Override
    public List<Player> getClubMembers(Long clubId) {
        Club club = findById(clubId);
        return club.getMembers();
    }

    @Override
    @Transactional
    public Club formAlliance(Long clubId1, Long clubId2) {
        Club club1 = findById(clubId1);
        Club club2 = findById(clubId2);

        // Check if already allied
        if (club1.getAlliances().contains(club2)) {
            return club1;
        }

        // Add to alliances
        club1.getAlliances().add(club2);
        club2.getAlliances().add(club1);

        // Remove from rivalries if they were rivals
        if (club1.getRivalries().contains(club2)) {
            club1.getRivalries().remove(club2);
            club2.getRivalries().remove(club1);
        }

        // Save both clubs
        clubRepository.save(club2);
        return clubRepository.save(club1);
    }

    @Override
    @Transactional
    public Club breakAlliance(Long clubId1, Long clubId2) {
        Club club1 = findById(clubId1);
        Club club2 = findById(clubId2);

        // Check if allied
        if (!club1.getAlliances().contains(club2)) {
            return club1;
        }

        // Remove from alliances
        club1.getAlliances().remove(club2);
        club2.getAlliances().remove(club1);

        // Save both clubs
        clubRepository.save(club2);
        return clubRepository.save(club1);
    }

    @Override
    @Transactional
    public Club declareRivalry(Long clubId1, Long clubId2) {
        Club club1 = findById(clubId1);
        Club club2 = findById(clubId2);

        // Check if already rivals
        if (club1.getRivalries().contains(club2)) {
            return club1;
        }

        // Add to rivalries
        club1.getRivalries().add(club2);
        club2.getRivalries().add(club1);

        // Remove from alliances if they were allies
        if (club1.getAlliances().contains(club2)) {
            club1.getAlliances().remove(club2);
            club2.getAlliances().remove(club1);
        }

        // Save both clubs
        clubRepository.save(club2);
        return clubRepository.save(club1);
    }

    @Override
    @Transactional
    public Club endRivalry(Long clubId1, Long clubId2) {
        Club club1 = findById(clubId1);
        Club club2 = findById(clubId2);

        // Check if rivals
        if (!club1.getRivalries().contains(club2)) {
            return club1;
        }

        // Remove from rivalries
        club1.getRivalries().remove(club2);
        club2.getRivalries().remove(club1);

        // Save both clubs
        clubRepository.save(club2);
        return clubRepository.save(club1);
    }

    @Override
    public List<Club> getAlliances(Long clubId) {
        Club club = findById(clubId);
        return club.getAlliances();
    }

    @Override
    public List<Club> getRivalries(Long clubId) {
        Club club = findById(clubId);
        return club.getRivalries();
    }

    @Override
    @Transactional
    public Map<Long, Integer> startCompetition(String competitionId, List<Long> participatingClubIds) {
        // Check if competition already exists
        if (ongoingCompetitions.containsKey(competitionId)) {
            throw new IllegalArgumentException("Competition already exists with ID: " + competitionId);
        }

        // Initialize scores for all participating clubs
        Map<Long, Integer> scores = new HashMap<>();
        for (Long clubId : participatingClubIds) {
            Club club = findById(clubId);
            scores.put(clubId, 0);
        }

        // Store in ongoing competitions
        ongoingCompetitions.put(competitionId, scores);

        return scores;
    }

    @Override
    @Transactional
    public Map<Long, Integer> updateCompetitionScores(String competitionId, Map<Long, Integer> scores) {
        // Check if competition exists
        if (!ongoingCompetitions.containsKey(competitionId)) {
            throw new IllegalArgumentException("Competition not found with ID: " + competitionId);
        }

        // Update scores
        Map<Long, Integer> currentScores = ongoingCompetitions.get(competitionId);
        currentScores.putAll(scores);

        return currentScores;
    }

    @Override
    @Transactional
    public Map<Long, Integer> endCompetition(String competitionId) {
        // Check if competition exists
        if (!ongoingCompetitions.containsKey(competitionId)) {
            throw new IllegalArgumentException("Competition not found with ID: " + competitionId);
        }

        // Get final scores
        Map<Long, Integer> finalScores = ongoingCompetitions.get(competitionId);

        // Update club competition results and reputation
        for (Map.Entry<Long, Integer> entry : finalScores.entrySet()) {
            Long clubId = entry.getKey();
            Integer score = entry.getValue();

            Club club = findById(clubId);

            // Add to competition results
            club.getCompetitionResults().put(competitionId, score);

            // Update reputation based on score
            int reputationChange = score / 10; // Simple formula: 10 points = 1 reputation
            club.setReputation(club.getReputation() + reputationChange);

            // Save club
            clubRepository.save(club);

            // Update ranking
            updateRanking(clubId);
        }

        // Remove from ongoing competitions
        ongoingCompetitions.remove(competitionId);

        return finalScores;
    }

    @Override
    public Map<String, Integer> getCompetitionResults(Long clubId) {
        Club club = findById(clubId);
        return club.getCompetitionResults();
    }

    @Override
    @Transactional
    public Club updateRanking(Long clubId) {
        // Get all clubs sorted by reputation
        List<Club> rankedClubs = getRankedClubs();

        // Find the club's position in the ranking
        Club club = findById(clubId);
        int ranking = rankedClubs.indexOf(club) + 1; // +1 because index is 0-based

        // Update ranking
        club.setRanking(ranking);

        // Save club
        return clubRepository.save(club);
    }

    @Override
    public List<Club> getRankedClubs() {
        // Get all clubs
        List<Club> allClubs = getAllClubs();

        // Sort by reputation (descending)
        return allClubs.stream()
                .sorted(Comparator.comparing(Club::getReputation).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Club save(Club club) {
        return clubRepository.save(club);
    }
}
