package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Technique;
import io.github.disparter.tokugawa.discord.core.repositories.TechniqueRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of the TechniqueService interface.
 */
@Service
public class TechniqueServiceImpl implements TechniqueService {

    private static final Logger logger = LoggerFactory.getLogger(TechniqueServiceImpl.class);

    private final TechniqueRepository techniqueRepository;
    private final PlayerRepository playerRepository;
    private final NPCRepository npcRepository;

    @Autowired
    public TechniqueServiceImpl(
            TechniqueRepository techniqueRepository,
            PlayerRepository playerRepository,
            NPCRepository npcRepository) {
        this.techniqueRepository = techniqueRepository;
        this.playerRepository = playerRepository;
        this.npcRepository = npcRepository;
    }

    @Override
    public Optional<Technique> findById(Long id) {
        return techniqueRepository.findById(id);
    }

    @Override
    public Optional<Technique> findByTechniqueId(String techniqueId) {
        return techniqueRepository.findByTechniqueId(techniqueId);
    }

    @Override
    public Optional<Technique> findByName(String name) {
        // This method is not directly provided by the repository, so we need to implement it
        List<Technique> techniques = new ArrayList<>();
        techniqueRepository.findAll().forEach(techniques::add);
        return techniques.stream()
                .filter(technique -> technique.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Technique> findByType(Technique.TechniqueType type) {
        return techniqueRepository.findByType(type);
    }

    @Override
    public List<Technique> getPlayerTechniques(Player player) {
        return new ArrayList<>(player.getKnownTechniques());
    }

    @Override
    public List<Technique> getNPCTechniques(NPC npc) {
        return new ArrayList<>(npc.getKnownTechniques());
    }

    @Override
    @Transactional
    public Player teachTechniqueToPlayer(Player player, Technique technique) {
        // Check if the technique is learnable
        if (!technique.getLearnable()) {
            throw new IllegalArgumentException("Technique is not learnable: " + technique.getName());
        }

        // Check if the player already knows the technique
        if (playerKnowsTechnique(player, technique)) {
            throw new IllegalArgumentException("Player already knows technique: " + technique.getName());
        }

        // Add the technique to the player's known techniques
        Set<Technique> knownTechniques = player.getKnownTechniques();
        knownTechniques.add(technique);
        player.setKnownTechniques(knownTechniques);

        // Update the technique's players list
        Set<Player> players = technique.getPlayers();
        players.add(player);
        technique.setPlayers(players);

        // Save the technique and player
        techniqueRepository.save(technique);

        logger.info("Taught technique {} to player {}", technique.getName(), player.getUsername());

        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public NPC teachTechniqueToNPC(NPC npc, Technique technique) {
        // Check if the NPC already knows the technique
        if (npcKnowsTechnique(npc, technique)) {
            throw new IllegalArgumentException("NPC already knows technique: " + technique.getName());
        }

        // Add the technique to the NPC's known techniques
        Set<Technique> knownTechniques = npc.getKnownTechniques();
        knownTechniques.add(technique);
        npc.setKnownTechniques(knownTechniques);

        // Update the technique's NPCs list
        Set<NPC> npcs = technique.getNpcs();
        npcs.add(npc);
        technique.setNpcs(npcs);

        // Save the technique and NPC
        techniqueRepository.save(technique);

        logger.info("Taught technique {} to NPC {}", technique.getName(), npc.getName());

        return npcRepository.save(npc);
    }

    @Override
    public boolean playerKnowsTechnique(Player player, Technique technique) {
        return player.getKnownTechniques().contains(technique);
    }

    @Override
    public boolean npcKnowsTechnique(NPC npc, Technique technique) {
        return npc.getKnownTechniques().contains(technique);
    }

    @Override
    @Transactional
    public Technique evolveTechnique(Player player, Technique technique, int powerPoints) {
        // Check if the player knows the technique
        if (!playerKnowsTechnique(player, technique)) {
            throw new IllegalArgumentException("Player doesn't know technique: " + technique.getName());
        }

        // Check if the player has enough power points
        if (player.getPowerPoints() < powerPoints) {
            throw new IllegalArgumentException("Player doesn't have enough power points");
        }

        // Get the evolution requirements
        Map<String, Integer> requirements = getTechniqueEvolutionRequirements(technique);
        int requiredPowerPoints = requirements.getOrDefault("powerPoints", 0);

        // Check if the player is investing enough power points
        if (powerPoints < requiredPowerPoints) {
            throw new IllegalArgumentException(
                    String.format("Technique requires %d power points to evolve", requiredPowerPoints));
        }

        // Evolve the technique
        int currentPowerPoints = technique.getPowerPoints() != null ? technique.getPowerPoints() : 0;
        technique.setPowerPoints(currentPowerPoints + powerPoints);

        // Increase the technique's stats based on the power points invested
        Map<String, Integer> effects = technique.getEffects();
        for (Map.Entry<String, Integer> effect : effects.entrySet()) {
            int currentValue = effect.getValue();
            int increase = (int) (currentValue * 0.1 * (powerPoints / 10.0)); // 10% increase per 10 power points
            effects.put(effect.getKey(), currentValue + increase);
        }

        // If the base damage is set, increase it as well
        if (technique.getBaseDamage() != null && technique.getBaseDamage() > 0) {
            int currentDamage = technique.getBaseDamage();
            int increase = (int) (currentDamage * 0.1 * (powerPoints / 10.0)); // 10% increase per 10 power points
            technique.setBaseDamage(currentDamage + increase);
        }

        // Deduct the power points from the player
        player.setPowerPoints(player.getPowerPoints() - powerPoints);
        playerRepository.save(player);

        logger.info("Evolved technique {} for player {} with {} power points", 
                technique.getName(), player.getUsername(), powerPoints);

        // Save and return the evolved technique
        return techniqueRepository.save(technique);
    }

    @Override
    public Map<String, Integer> getTechniqueEvolutionRequirements(Technique technique) {
        Map<String, Integer> requirements = new HashMap<>();

        // Base requirements
        requirements.put("powerPoints", 10); // Base requirement of 10 power points

        // Additional requirements based on technique type
        switch (technique.getType()) {
            case ATTACK:
                requirements.put("playerLevel", 5);
                break;
            case DEFENSE:
                requirements.put("playerLevel", 5);
                break;
            case SUPPORT:
                requirements.put("playerLevel", 5);
                break;
            case SPECIAL:
                requirements.put("playerLevel", 10);
                requirements.put("powerPoints", 20); // Special techniques require more power points
                break;
            case ULTIMATE:
                requirements.put("playerLevel", 15);
                requirements.put("powerPoints", 30); // Ultimate techniques require even more power points
                break;
        }

        // If the technique already has power points, increase the requirement
        if (technique.getPowerPoints() != null && technique.getPowerPoints() > 0) {
            int currentPowerPoints = technique.getPowerPoints();
            int currentRequirement = requirements.get("powerPoints");
            // Each evolution becomes more expensive
            requirements.put("powerPoints", currentRequirement + (currentPowerPoints / 10) * 5);
        }

        return requirements;
    }

    @Override
    public Map<String, String> getTechniqueEffectDescriptions() {
        Map<String, String> effectDescriptions = new HashMap<>();

        effectDescriptions.put("damage", "Increases the damage dealt by the technique");
        effectDescriptions.put("healing", "Increases the healing provided by the technique");
        effectDescriptions.put("defense", "Increases the defense bonus provided by the technique");
        effectDescriptions.put("speed", "Increases the speed bonus provided by the technique");
        effectDescriptions.put("criticalChance", "Increases the critical hit chance of the technique");
        effectDescriptions.put("stunChance", "Increases the chance to stun the opponent");
        effectDescriptions.put("bleedChance", "Increases the chance to cause bleeding");
        effectDescriptions.put("burnChance", "Increases the chance to cause burning");
        effectDescriptions.put("poisonChance", "Increases the chance to poison the opponent");

        return effectDescriptions;
    }

    @Override
    public Technique save(Technique technique) {
        return techniqueRepository.save(technique);
    }

    @Override
    public void delete(Technique technique) {
        techniqueRepository.delete(technique);
    }
}
