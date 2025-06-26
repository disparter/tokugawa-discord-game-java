package io.github.disparter.tokugawa.discord.api;

import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.services.ConsequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Decision Dashboard.
 */
@RestController
@RequestMapping("/api/decisions")
public class DecisionDashboardController {

    private final ConsequenceService consequenceService;

    @Autowired
    public DecisionDashboardController(ConsequenceService consequenceService) {
        this.consequenceService = consequenceService;
    }

    /**
     * Gets the decision dashboard for a player.
     *
     * @param playerId the player ID
     * @return the decision dashboard
     */
    @GetMapping("/dashboard/{playerId}")
    public ResponseEntity<Map<String, List<Consequence>>> getDecisionDashboard(@PathVariable Long playerId) {
        Map<String, List<Consequence>> dashboard = consequenceService.getDecisionDashboard(playerId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Gets all consequences for a player.
     *
     * @param playerId the player ID
     * @return list of consequences
     */
    @GetMapping("/consequences/{playerId}")
    public ResponseEntity<List<Consequence>> getConsequencesForPlayer(@PathVariable Long playerId) {
        List<Consequence> consequences = consequenceService.getConsequencesForPlayer(playerId);
        return ResponseEntity.ok(consequences);
    }

    /**
     * Gets active consequences for a player.
     *
     * @param playerId the player ID
     * @return list of active consequences
     */
    @GetMapping("/consequences/{playerId}/active")
    public ResponseEntity<List<Consequence>> getActiveConsequencesForPlayer(@PathVariable Long playerId) {
        List<Consequence> consequences = consequenceService.getActiveConsequencesForPlayer(playerId);
        return ResponseEntity.ok(consequences);
    }

    /**
     * Gets consequences for a player related to a choice.
     *
     * @param playerId the player ID
     * @param choiceId the choice ID
     * @return list of consequences
     */
    @GetMapping("/consequences/{playerId}/choice/{choiceId}")
    public ResponseEntity<List<Consequence>> getConsequencesForChoice(
            @PathVariable Long playerId, @PathVariable String choiceId) {
        List<Consequence> consequences = consequenceService.getConsequencesForChoice(playerId, choiceId);
        return ResponseEntity.ok(consequences);
    }

    /**
     * Gets consequences for a player affecting an NPC.
     *
     * @param playerId the player ID
     * @param npcId the NPC ID
     * @return list of consequences
     */
    @GetMapping("/consequences/{playerId}/npc/{npcId}")
    public ResponseEntity<List<Consequence>> getConsequencesAffectingNpc(
            @PathVariable Long playerId, @PathVariable Long npcId) {
        List<Consequence> consequences = consequenceService.getConsequencesAffectingNpc(playerId, npcId);
        return ResponseEntity.ok(consequences);
    }
}