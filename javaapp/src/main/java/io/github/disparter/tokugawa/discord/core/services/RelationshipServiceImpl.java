package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import io.github.disparter.tokugawa.discord.core.models.Relationship.RelationshipStatus;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.RelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the RelationshipService interface.
 */
@Service
public class RelationshipServiceImpl implements RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final PlayerRepository playerRepository;
    private final NPCRepository npcRepository;

    // Thresholds for relationship status changes
    private static final int ACQUAINTANCE_THRESHOLD = 10;
    private static final int FRIEND_THRESHOLD = 30;
    private static final int CLOSE_FRIEND_THRESHOLD = 50;
    private static final int BEST_FRIEND_THRESHOLD = 70;
    private static final int CRUSH_THRESHOLD = 80;
    private static final int DATING_THRESHOLD = 90;
    private static final int COMMITTED_THRESHOLD = 100;
    private static final int RIVAL_THRESHOLD = -30;
    private static final int ENEMY_THRESHOLD = -60;

    @Autowired
    public RelationshipServiceImpl(RelationshipRepository relationshipRepository,
                                  PlayerRepository playerRepository,
                                  NPCRepository npcRepository) {
        this.relationshipRepository = relationshipRepository;
        this.playerRepository = playerRepository;
        this.npcRepository = npcRepository;
    }

    @Override
    public Relationship findById(Long id) {
        return relationshipRepository.findById(id).orElse(null);
    }

    @Override
    public List<Relationship> getRelationshipsForPlayer(Long playerId) {
        return relationshipRepository.findByPlayerId(playerId);
    }

    @Override
    public Relationship getRelationship(Long playerId, Long npcId) {
        return relationshipRepository.findByPlayerIdAndNpcId(playerId, npcId)
                .orElseGet(() -> createNewRelationship(playerId, npcId));
    }

    /**
     * Creates a new relationship between a player and an NPC if one doesn't exist.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return the new relationship
     */
    private Relationship createNewRelationship(Long playerId, Long npcId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        NPC npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found with ID: " + npcId));

        Relationship relationship = new Relationship();
        relationship.setPlayer(player);
        relationship.setNpc(npc);
        relationship.setAffinity(0);
        relationship.setStatus(RelationshipStatus.STRANGER);

        return relationshipRepository.save(relationship);
    }

    @Override
    @Transactional
    public Relationship improveRelationship(Long playerId, Long npcId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for improving relationship");
        }

        Relationship relationship = getRelationship(playerId, npcId);
        int newAffinity = relationship.getAffinity() + amount;
        relationship.setAffinity(newAffinity);

        // Update relationship status based on new affinity
        updateRelationshipStatus(relationship);

        return relationshipRepository.save(relationship);
    }

    @Override
    @Transactional
    public Relationship worsenRelationship(Long playerId, Long npcId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive for worsening relationship");
        }

        Relationship relationship = getRelationship(playerId, npcId);
        int newAffinity = relationship.getAffinity() - amount;
        relationship.setAffinity(newAffinity);

        // Update relationship status based on new affinity
        updateRelationshipStatus(relationship);

        return relationshipRepository.save(relationship);
    }

    /**
     * Updates the relationship status based on the current affinity value.
     *
     * @param relationship the relationship to update
     */
    private void updateRelationshipStatus(Relationship relationship) {
        int affinity = relationship.getAffinity();

        if (affinity >= COMMITTED_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.COMMITTED);
        } else if (affinity >= DATING_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.DATING);
        } else if (affinity >= CRUSH_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.CRUSH);
        } else if (affinity >= BEST_FRIEND_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.BEST_FRIEND);
        } else if (affinity >= CLOSE_FRIEND_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.CLOSE_FRIEND);
        } else if (affinity >= FRIEND_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.FRIEND);
        } else if (affinity >= ACQUAINTANCE_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.ACQUAINTANCE);
        } else if (affinity <= ENEMY_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.ENEMY);
        } else if (affinity <= RIVAL_THRESHOLD) {
            relationship.setStatus(RelationshipStatus.RIVAL);
        } else {
            relationship.setStatus(RelationshipStatus.STRANGER);
        }
    }

    @Override
    public List<NPC> getFriendlyNPCs(Long playerId) {
        List<Relationship> relationships = getRelationshipsForPlayer(playerId);
        return relationships.stream()
                .filter(r -> r.getAffinity() >= FRIEND_THRESHOLD)
                .map(Relationship::getNpc)
                .collect(Collectors.toList());
    }

    @Override
    public List<NPC> getHostileNPCs(Long playerId) {
        List<Relationship> relationships = getRelationshipsForPlayer(playerId);
        return relationships.stream()
                .filter(r -> r.getAffinity() <= RIVAL_THRESHOLD)
                .map(Relationship::getNpc)
                .collect(Collectors.toList());
    }

    @Override
    public Relationship save(Relationship relationship) {
        return relationshipRepository.save(relationship);
    }
}
