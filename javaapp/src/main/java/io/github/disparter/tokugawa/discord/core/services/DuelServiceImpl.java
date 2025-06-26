package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Duel;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Technique;
import io.github.disparter.tokugawa.discord.core.repositories.DuelRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.TechniqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Implementation of the DuelService interface.
 */
@Service
public class DuelServiceImpl implements DuelService {

    private final DuelRepository duelRepository;
    private final PlayerRepository playerRepository;
    private final NPCRepository npcRepository;
    private final TechniqueRepository techniqueRepository;
    private final PlayerService playerService;
    private final NarrativeService narrativeService;
    private final RelationshipService relationshipService;
    
    private final Random random = new Random();

    @Autowired
    public DuelServiceImpl(
            DuelRepository duelRepository,
            PlayerRepository playerRepository,
            NPCRepository npcRepository,
            TechniqueRepository techniqueRepository,
            PlayerService playerService,
            NarrativeService narrativeService,
            RelationshipService relationshipService) {
        this.duelRepository = duelRepository;
        this.playerRepository = playerRepository;
        this.npcRepository = npcRepository;
        this.techniqueRepository = techniqueRepository;
        this.playerService = playerService;
        this.narrativeService = narrativeService;
        this.relationshipService = relationshipService;
    }

    @Override
    @Transactional
    public Map<String, Object> initiateDuel(Long playerId, Long npcId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        NPC npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found with ID: " + npcId));
        
        // Create a new duel
        Duel duel = new Duel();
        duel.setDuelId(UUID.randomUUID().toString());
        duel.setPlayer(player);
        duel.setNpc(npc);
        duel.setStatus(Duel.DuelStatus.PLAYER_TURN);
        duel.setStartTime(new Date());
        
        // Save the duel
        duel = duelRepository.save(duel);
        
        // Add initial log
        duel.getLogs().put(1, "Duel initiated between " + player.getName() + " and " + npc.getName() + ".");
        duelRepository.save(duel);
        
        // Return the initial duel state
        Map<String, Object> result = new HashMap<>();
        result.put("duelId", duel.getDuelId());
        result.put("playerName", player.getName());
        result.put("npcName", npc.getName());
        result.put("playerHealth", duel.getPlayerHealth());
        result.put("npcHealth", duel.getNpcHealth());
        result.put("playerMana", duel.getPlayerMana());
        result.put("npcMana", duel.getNpcMana());
        result.put("round", duel.getRound());
        result.put("status", duel.getStatus().name());
        result.put("log", duel.getLogs().get(1));
        result.put("playerTechniques", getPlayerTechniques(playerId));
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> processTechniqueSelection(String duelId, Long techniqueId) {
        Duel duel = duelRepository.findByDuelId(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found with ID: " + duelId));
        
        if (duel.getStatus() == Duel.DuelStatus.COMPLETED || duel.getStatus() == Duel.DuelStatus.CANCELED) {
            throw new IllegalStateException("Duel is already completed or canceled");
        }
        
        Technique technique = techniqueRepository.findById(techniqueId)
                .orElseThrow(() -> new IllegalArgumentException("Technique not found with ID: " + techniqueId));
        
        // Process player's turn
        if (duel.getStatus() == Duel.DuelStatus.PLAYER_TURN) {
            // Check if player has enough mana
            if (duel.getPlayerMana() < technique.getManaCost()) {
                throw new IllegalStateException("Not enough mana to use this technique");
            }
            
            // Apply technique effects
            int damage = calculateDamage(technique, duel.getPlayer(), duel.getNpc());
            duel.setNpcHealth(Math.max(0, duel.getNpcHealth() - damage));
            duel.setPlayerMana(duel.getPlayerMana() - technique.getManaCost());
            duel.setPlayerLastTechnique(technique);
            
            // Add log
            String log = duel.getPlayer().getName() + " used " + technique.getName() + " and dealt " + damage + " damage.";
            duel.getLogs().put(duel.getRound(), log);
            
            // Check if NPC is defeated
            if (duel.getNpcHealth() <= 0) {
                return endDuel(duel, true);
            }
            
            // Switch to NPC's turn
            duel.setStatus(Duel.DuelStatus.NPC_TURN);
            duelRepository.save(duel);
            
            // Process NPC's turn automatically
            return processNPCTurn(duel);
        } else {
            throw new IllegalStateException("It's not the player's turn");
        }
    }
    
    private Map<String, Object> processNPCTurn(Duel duel) {
        // Get NPC's techniques
        List<Technique> npcTechniques = getNPCTechniques(duel.getNpc().getId());
        
        if (npcTechniques.isEmpty()) {
            // NPC has no techniques, use a basic attack
            int damage = 5 + random.nextInt(5); // 5-10 damage
            duel.setPlayerHealth(Math.max(0, duel.getPlayerHealth() - damage));
            
            // Add log
            String log = duel.getNpc().getName() + " used a basic attack and dealt " + damage + " damage.";
            duel.getLogs().put(duel.getRound(), duel.getLogs().get(duel.getRound()) + "\n" + log);
        } else {
            // Select a random technique
            Technique npcTechnique = npcTechniques.get(random.nextInt(npcTechniques.size()));
            
            // Check if NPC has enough mana
            if (duel.getNpcMana() < npcTechnique.getManaCost()) {
                // Not enough mana, use a basic attack
                int damage = 5 + random.nextInt(5); // 5-10 damage
                duel.setPlayerHealth(Math.max(0, duel.getPlayerHealth() - damage));
                
                // Add log
                String log = duel.getNpc().getName() + " used a basic attack and dealt " + damage + " damage.";
                duel.getLogs().put(duel.getRound(), duel.getLogs().get(duel.getRound()) + "\n" + log);
            } else {
                // Apply technique effects
                int damage = calculateDamage(npcTechnique, duel.getNpc(), duel.getPlayer());
                duel.setPlayerHealth(Math.max(0, duel.getPlayerHealth() - damage));
                duel.setNpcMana(duel.getNpcMana() - npcTechnique.getManaCost());
                duel.setNpcLastTechnique(npcTechnique);
                
                // Add log
                String log = duel.getNpc().getName() + " used " + npcTechnique.getName() + " and dealt " + damage + " damage.";
                duel.getLogs().put(duel.getRound(), duel.getLogs().get(duel.getRound()) + "\n" + log);
            }
        }
        
        // Check if player is defeated
        if (duel.getPlayerHealth() <= 0) {
            return endDuel(duel, false);
        }
        
        // Increment round and switch back to player's turn
        duel.setRound(duel.getRound() + 1);
        duel.setStatus(Duel.DuelStatus.PLAYER_TURN);
        
        // Regenerate some mana for both player and NPC
        duel.setPlayerMana(Math.min(100, duel.getPlayerMana() + 10));
        duel.setNpcMana(Math.min(100, duel.getNpcMana() + 10));
        
        duelRepository.save(duel);
        
        // Return the updated duel state
        return getDuelState(duel);
    }
    
    private int calculateDamage(Technique technique, Object attacker, Object defender) {
        // Base damage from technique
        int damage = technique.getBaseDamage();
        
        // Add some randomness
        damage += random.nextInt(5) - 2; // -2 to +2 damage
        
        // Apply technique effects
        for (Map.Entry<String, Integer> effect : technique.getEffects().entrySet()) {
            switch (effect.getKey()) {
                case "damage_boost":
                    damage += effect.getValue();
                    break;
                case "critical_chance":
                    if (random.nextInt(100) < effect.getValue()) {
                        damage *= 2; // Critical hit
                    }
                    break;
                // Add more effects as needed
            }
        }
        
        return Math.max(1, damage); // Minimum 1 damage
    }
    
    private Map<String, Object> endDuel(Duel duel, boolean playerWon) {
        duel.setStatus(Duel.DuelStatus.COMPLETED);
        duel.setEndTime(new Date());
        duel.setPlayerWon(playerWon);
        
        // Add final log
        String log = playerWon ? 
                duel.getPlayer().getName() + " has defeated " + duel.getNpc().getName() + "!" :
                duel.getPlayer().getName() + " has been defeated by " + duel.getNpc().getName() + "!";
        duel.getLogs().put(duel.getRound(), duel.getLogs().get(duel.getRound()) + "\n" + log);
        
        duelRepository.save(duel);
        
        // Process duel result
        processDuelResult(duel.getDuelId());
        
        // Return the final duel state
        return getDuelState(duel);
    }

    @Override
    @Transactional
    public Map<String, Object> processDuelResult(String duelId) {
        Duel duel = duelRepository.findByDuelId(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found with ID: " + duelId));
        
        if (duel.getStatus() != Duel.DuelStatus.COMPLETED) {
            throw new IllegalStateException("Duel is not completed yet");
        }
        
        // Update player's reputation based on duel result
        playerService.updateReputationFromDuel(duel.getPlayer().getId(), duel.getPlayerWon());
        
        // Update relationship with the NPC
        if (duel.getPlayerWon()) {
            relationshipService.increaseRelationship(duel.getPlayer().getId(), duel.getNpc().getId(), 5);
        } else {
            relationshipService.decreaseRelationship(duel.getPlayer().getId(), duel.getNpc().getId(), 2);
        }
        
        // Update narrative progress if applicable
        narrativeService.updateProgressFromDuel(duel.getPlayer().getId(), duel.getNpc().getId(), duel.getPlayerWon());
        
        // Return the final duel state with results
        Map<String, Object> result = getDuelState(duel);
        result.put("reputationChange", duel.getPlayerWon() ? 10 : -5);
        result.put("relationshipChange", duel.getPlayerWon() ? 5 : -2);
        
        return result;
    }
    
    private Map<String, Object> getDuelState(Duel duel) {
        Map<String, Object> result = new HashMap<>();
        result.put("duelId", duel.getDuelId());
        result.put("playerName", duel.getPlayer().getName());
        result.put("npcName", duel.getNpc().getName());
        result.put("playerHealth", duel.getPlayerHealth());
        result.put("npcHealth", duel.getNpcHealth());
        result.put("playerMana", duel.getPlayerMana());
        result.put("npcMana", duel.getNpcMana());
        result.put("round", duel.getRound());
        result.put("status", duel.getStatus().name());
        result.put("log", duel.getLogs().get(duel.getRound()));
        
        if (duel.getStatus() == Duel.DuelStatus.COMPLETED) {
            result.put("playerWon", duel.getPlayerWon());
            result.put("duration", (duel.getEndTime().getTime() - duel.getStartTime().getTime()) / 1000); // in seconds
        }
        
        return result;
    }

    @Override
    public List<Technique> getPlayerTechniques(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        return techniqueRepository.findByPlayersContaining(player);
    }

    @Override
    public List<Technique> getNPCTechniques(Long npcId) {
        NPC npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found with ID: " + npcId));
        
        return techniqueRepository.findByNpcsContaining(npc);
    }

    @Override
    @Transactional
    public Player teachTechniqueToPlayer(Long playerId, Long techniqueId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        Technique technique = techniqueRepository.findById(techniqueId)
                .orElseThrow(() -> new IllegalArgumentException("Technique not found with ID: " + techniqueId));
        
        if (!technique.getLearnable()) {
            throw new IllegalStateException("This technique cannot be learned");
        }
        
        if (player.getKnownTechniques().contains(technique)) {
            throw new IllegalStateException("Player already knows this technique");
        }
        
        player.getKnownTechniques().add(technique);
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public NPC teachTechniqueToNPC(Long npcId, Long techniqueId) {
        NPC npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found with ID: " + npcId));
        
        Technique technique = techniqueRepository.findById(techniqueId)
                .orElseThrow(() -> new IllegalArgumentException("Technique not found with ID: " + techniqueId));
        
        if (npc.getKnownTechniques().contains(technique)) {
            throw new IllegalStateException("NPC already knows this technique");
        }
        
        npc.getKnownTechniques().add(technique);
        return npcRepository.save(npc);
    }

    @Override
    public List<String> getActivePlayerDuels(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        return duelRepository.findByPlayerAndStatusNot(player, Duel.DuelStatus.COMPLETED)
                .stream()
                .map(Duel::getDuelId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean cancelDuel(String duelId) {
        Duel duel = duelRepository.findByDuelId(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duel not found with ID: " + duelId));
        
        if (duel.getStatus() == Duel.DuelStatus.COMPLETED) {
            return false; // Cannot cancel a completed duel
        }
        
        duel.setStatus(Duel.DuelStatus.CANCELED);
        duel.setEndTime(new Date());
        duelRepository.save(duel);
        
        return true;
    }
}