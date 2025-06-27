package io.github.disparter.tokugawa.discord.api;

import io.github.disparter.tokugawa.discord.core.models.Consequence;
import io.github.disparter.tokugawa.discord.core.models.Consequence.ConsequenceType;
import io.github.disparter.tokugawa.discord.core.services.ConsequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Gets the enhanced decision dashboard for a player.
     *
     * @param playerId the player ID
     * @return the enhanced decision dashboard
     */
    @GetMapping("/dashboard/{playerId}/enhanced")
    public ResponseEntity<Map<String, List<Consequence>>> getEnhancedDecisionDashboard(@PathVariable Long playerId) {
        Map<String, List<Consequence>> dashboard = consequenceService.getEnhancedDecisionDashboard(playerId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Gets consequences for a player by chapter and scene.
     *
     * @param playerId the player ID
     * @param chapterId the chapter ID
     * @param sceneId the scene ID
     * @return list of consequences
     */
    @GetMapping("/consequences/{playerId}/chapter/{chapterId}/scene/{sceneId}")
    public ResponseEntity<List<Consequence>> getConsequencesByChapterAndScene(
            @PathVariable Long playerId, @PathVariable String chapterId, @PathVariable String sceneId) {
        List<Consequence> consequences = consequenceService.getConsequencesByChapterAndScene(playerId, chapterId, sceneId);
        return ResponseEntity.ok(consequences);
    }

    /**
     * Gets ethical reflections for a player's decisions.
     *
     * @param playerId the player ID
     * @return map of consequence IDs to lists of ethical reflections
     */
    @GetMapping("/reflections/{playerId}")
    public ResponseEntity<Map<Long, List<String>>> getEthicalReflectionsForPlayer(@PathVariable Long playerId) {
        Map<Long, List<String>> reflections = consequenceService.getEthicalReflectionsForPlayer(playerId);
        return ResponseEntity.ok(reflections);
    }

    /**
     * Gets alternative paths for a player's decisions.
     *
     * @param playerId the player ID
     * @return map of consequence IDs to lists of alternative paths
     */
    @GetMapping("/alternatives/{playerId}")
    public ResponseEntity<Map<Long, List<String>>> getAlternativePathsForPlayer(@PathVariable Long playerId) {
        Map<Long, List<String>> alternatives = consequenceService.getAlternativePathsForPlayer(playerId);
        return ResponseEntity.ok(alternatives);
    }

    /**
     * Gets the percentage of players who made the same choice.
     *
     * @param chapterId the chapter ID
     * @param sceneId the scene ID
     * @param choiceMade the choice that was made
     * @return the percentage of players who made the same choice
     */
    @GetMapping("/community/percentage/chapter/{chapterId}/scene/{sceneId}/choice/{choiceMade}")
    public ResponseEntity<Double> getCommunityChoicePercentage(
            @PathVariable String chapterId, @PathVariable String sceneId, @PathVariable String choiceMade) {
        double percentage = consequenceService.getCommunityChoicePercentage(chapterId, sceneId, choiceMade);
        return ResponseEntity.ok(percentage);
    }

    /**
     * Tracks a player's decision.
     *
     * @param playerId the player ID
     * @param requestBody the decision details
     * @return the created consequence
     */
    @PostMapping("/track/{playerId}")
    public ResponseEntity<Consequence> trackPlayerDecision(
            @PathVariable Long playerId, @RequestBody TrackDecisionRequest requestBody) {
        Consequence consequence = consequenceService.trackPlayerDecision(
                playerId,
                requestBody.getChapterId(),
                requestBody.getSceneId(),
                requestBody.getChoiceMade(),
                requestBody.getDecisionContext(),
                requestBody.getName(),
                requestBody.getDescription(),
                requestBody.getType(),
                requestBody.getEffects(),
                requestBody.getRelatedChoices(),
                requestBody.getAffectedNpcs()
        );
        return ResponseEntity.ok(consequence);
    }

    /**
     * Adds ethical reflections to a consequence.
     *
     * @param consequenceId the consequence ID
     * @param reflections the ethical reflections to add
     * @return the updated consequence
     */
    @PostMapping("/reflections/{consequenceId}")
    public ResponseEntity<Consequence> addEthicalReflections(
            @PathVariable Long consequenceId, @RequestBody List<String> reflections) {
        Consequence consequence = consequenceService.addEthicalReflections(consequenceId, reflections);
        return ResponseEntity.ok(consequence);
    }

    /**
     * Adds alternative paths to a consequence.
     *
     * @param consequenceId the consequence ID
     * @param alternativePaths the alternative paths to add
     * @return the updated consequence
     */
    @PostMapping("/alternatives/{consequenceId}")
    public ResponseEntity<Consequence> addAlternativePaths(
            @PathVariable Long consequenceId, @RequestBody List<String> alternativePaths) {
        Consequence consequence = consequenceService.addAlternativePaths(consequenceId, alternativePaths);
        return ResponseEntity.ok(consequence);
    }

    /**
     * Request body for tracking a player's decision.
     */
    public static class TrackDecisionRequest {
        private String chapterId;
        private String sceneId;
        private String choiceMade;
        private String decisionContext;
        private String name;
        private String description;
        private ConsequenceType type;
        private List<String> effects;
        private List<String> relatedChoices;
        private List<Long> affectedNpcs;

        public String getChapterId() {
            return chapterId;
        }

        public void setChapterId(String chapterId) {
            this.chapterId = chapterId;
        }

        public String getSceneId() {
            return sceneId;
        }

        public void setSceneId(String sceneId) {
            this.sceneId = sceneId;
        }

        public String getChoiceMade() {
            return choiceMade;
        }

        public void setChoiceMade(String choiceMade) {
            this.choiceMade = choiceMade;
        }

        public String getDecisionContext() {
            return decisionContext;
        }

        public void setDecisionContext(String decisionContext) {
            this.decisionContext = decisionContext;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ConsequenceType getType() {
            return type;
        }

        public void setType(ConsequenceType type) {
            this.type = type;
        }

        public List<String> getEffects() {
            return effects;
        }

        public void setEffects(List<String> effects) {
            this.effects = effects;
        }

        public List<String> getRelatedChoices() {
            return relatedChoices;
        }

        public void setRelatedChoices(List<String> relatedChoices) {
            this.relatedChoices = relatedChoices;
        }

        public List<Long> getAffectedNpcs() {
            return affectedNpcs;
        }

        public void setAffectedNpcs(List<Long> affectedNpcs) {
            this.affectedNpcs = affectedNpcs;
        }
    }
}
