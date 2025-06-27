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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Affinity changes for different interaction types
    private static final Map<String, Integer> INTERACTION_AFFINITY_CHANGES = new HashMap<>();

    // Romance event triggers based on relationship status
    private static final Map<RelationshipStatus, List<String>> ROMANCE_EVENTS = new HashMap<>();

    static {
        // Initialize interaction affinity changes
        INTERACTION_AFFINITY_CHANGES.put("GREET", 1);
        INTERACTION_AFFINITY_CHANGES.put("CHAT", 2);
        INTERACTION_AFFINITY_CHANGES.put("COMPLIMENT", 3);
        INTERACTION_AFFINITY_CHANGES.put("GIFT", 5);
        INTERACTION_AFFINITY_CHANGES.put("HELP", 7);
        INTERACTION_AFFINITY_CHANGES.put("SPECIAL_FAVOR", 10);
        INTERACTION_AFFINITY_CHANGES.put("INSULT", -3);
        INTERACTION_AFFINITY_CHANGES.put("ARGUE", -5);
        INTERACTION_AFFINITY_CHANGES.put("BETRAY", -10);

        // Initialize romance events
        ROMANCE_EVENTS.put(RelationshipStatus.ACQUAINTANCE, List.of("first_impression", "casual_meeting"));
        ROMANCE_EVENTS.put(RelationshipStatus.FRIEND, List.of("growing_friendship", "shared_interest"));
        ROMANCE_EVENTS.put(RelationshipStatus.CLOSE_FRIEND, List.of("deep_conversation", "personal_secret"));
        ROMANCE_EVENTS.put(RelationshipStatus.BEST_FRIEND, List.of("emotional_support", "life_crisis"));
        ROMANCE_EVENTS.put(RelationshipStatus.CRUSH, List.of("romantic_tension", "jealousy_moment"));
        ROMANCE_EVENTS.put(RelationshipStatus.DATING, List.of("first_date", "relationship_challenge"));
        ROMANCE_EVENTS.put(RelationshipStatus.COMMITTED, List.of("commitment_ceremony", "future_plans"));
    }

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
        RelationshipStatus oldStatus = relationship.getStatus();

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

    @Override
    @Transactional
    public Relationship updateAffinityByInteraction(Long playerId, Long npcId, String interactionType) {
        if (!INTERACTION_AFFINITY_CHANGES.containsKey(interactionType)) {
            throw new IllegalArgumentException("Unknown interaction type: " + interactionType);
        }

        int affinityChange = INTERACTION_AFFINITY_CHANGES.get(interactionType);

        if (affinityChange > 0) {
            return improveRelationship(playerId, npcId, affinityChange);
        } else if (affinityChange < 0) {
            return worsenRelationship(playerId, npcId, Math.abs(affinityChange));
        } else {
            return getRelationship(playerId, npcId);
        }
    }

    @Override
    public boolean hasReachedStatus(Long playerId, Long npcId, RelationshipStatus status) {
        Relationship relationship = getRelationship(playerId, npcId);

        // Check if the current status is equal to or higher than the target status
        // For positive statuses (ACQUAINTANCE to COMMITTED)
        if (status.ordinal() >= RelationshipStatus.ACQUAINTANCE.ordinal() && 
            status.ordinal() <= RelationshipStatus.COMMITTED.ordinal()) {
            return relationship.getStatus().ordinal() >= status.ordinal();
        }

        // For negative statuses (RIVAL, ENEMY)
        if (status == RelationshipStatus.RIVAL || status == RelationshipStatus.ENEMY) {
            return relationship.getStatus() == status || 
                  (status == RelationshipStatus.RIVAL && relationship.getStatus() == RelationshipStatus.ENEMY);
        }

        return relationship.getStatus() == status;
    }

    @Override
    public List<NPC> getNPCsByStatus(Long playerId, RelationshipStatus status) {
        List<Relationship> relationships = getRelationshipsForPlayer(playerId);
        return relationships.stream()
                .filter(r -> r.getStatus() == status)
                .map(Relationship::getNpc)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String triggerRomanceEvent(Long playerId, Long npcId) {
        Relationship relationship = getRelationship(playerId, npcId);
        RelationshipStatus status = relationship.getStatus();
        int affinity = relationship.getAffinity();

        // Check if there are events for this status
        if (!ROMANCE_EVENTS.containsKey(status)) {
            return null;
        }

        // Get potential events for this status
        List<String> potentialEvents = ROMANCE_EVENTS.get(status);

        // Filter out events that have already been triggered
        List<String> availableEvents = potentialEvents.stream()
                .filter(eventId -> !hasTriggeredEvent(playerId, npcId, eventId))
                .collect(Collectors.toList());

        if (availableEvents.isEmpty()) {
            return null;
        }

        // Check if the player meets the affinity requirements for the event
        // Different events may have different affinity requirements
        String eventId = null;

        // For CRUSH events, require at least CRUSH_THRESHOLD + 5
        if (status == RelationshipStatus.CRUSH) {
            if (affinity >= CRUSH_THRESHOLD + 5) {
                eventId = availableEvents.get(0);
            }
        }
        // For DATING events, require at least DATING_THRESHOLD + 5
        else if (status == RelationshipStatus.DATING) {
            if (affinity >= DATING_THRESHOLD + 5) {
                eventId = availableEvents.get(0);
            }
        }
        // For COMMITTED events, require at least COMMITTED_THRESHOLD + 5
        else if (status == RelationshipStatus.COMMITTED) {
            if (affinity >= COMMITTED_THRESHOLD + 5) {
                eventId = availableEvents.get(0);
            }
        }
        // For other statuses, just check if affinity is at least at the minimum threshold for that status
        else {
            int minThreshold = getMinThresholdForStatus(status);
            if (affinity >= minThreshold) {
                eventId = availableEvents.get(0);
            }
        }

        // If an event was selected, record it
        if (eventId != null) {
            recordTriggeredEvent(playerId, npcId, eventId);
        }

        return eventId;
    }

    /**
     * Gets the minimum affinity threshold for a relationship status.
     *
     * @param status the relationship status
     * @return the minimum affinity threshold
     */
    private int getMinThresholdForStatus(RelationshipStatus status) {
        switch (status) {
            case ACQUAINTANCE:
                return ACQUAINTANCE_THRESHOLD;
            case FRIEND:
                return FRIEND_THRESHOLD;
            case CLOSE_FRIEND:
                return CLOSE_FRIEND_THRESHOLD;
            case BEST_FRIEND:
                return BEST_FRIEND_THRESHOLD;
            case CRUSH:
                return CRUSH_THRESHOLD;
            case DATING:
                return DATING_THRESHOLD;
            case COMMITTED:
                return COMMITTED_THRESHOLD;
            case RIVAL:
                return RIVAL_THRESHOLD;
            case ENEMY:
                return ENEMY_THRESHOLD;
            default:
                return 0;
        }
    }

    @Override
    @Transactional
    public Relationship recordTriggeredEvent(Long playerId, Long npcId, String eventId) {
        Relationship relationship = getRelationship(playerId, npcId);

        if (!relationship.getTriggeredEvents().contains(eventId)) {
            relationship.getTriggeredEvents().add(eventId);
            return relationshipRepository.save(relationship);
        }

        return relationship;
    }

    @Override
    public boolean hasTriggeredEvent(Long playerId, Long npcId, String eventId) {
        Relationship relationship = getRelationship(playerId, npcId);
        return relationship.getTriggeredEvents().contains(eventId);
    }
}
