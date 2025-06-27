package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import io.github.disparter.tokugawa.discord.core.models.Villain;
import io.github.disparter.tokugawa.discord.core.models.Villain.VillainType;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.repositories.VillainRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Implementation of the VillainService interface.
 */
@Service
@Slf4j
public class VillainServiceImpl implements VillainService {

    private final VillainRepository villainRepository;
    private final PlayerRepository playerRepository;
    private final Random random = new Random();

    @Autowired
    public VillainServiceImpl(VillainRepository villainRepository, PlayerRepository playerRepository) {
        this.villainRepository = villainRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Optional<Villain> findById(Long id) {
        return villainRepository.findById(id);
    }

    @Override
    public Optional<Villain> findByName(String name) {
        return villainRepository.findByName(name);
    }

    @Override
    public List<Villain> getActiveVillains() {
        return villainRepository.findByActiveTrue();
    }

    @Override
    public List<Villain> getActiveVillainsByType(VillainType type) {
        return villainRepository.findByActiveTrueAndType(type);
    }

    @Override
    public List<Villain> getActiveVillainsByLocation(String location) {
        return villainRepository.findByActiveTrueAndSpawnLocation(location);
    }

    @Override
    @Transactional
    public Villain createVillain(Villain villain) {
        log.info("Creating villain: {}", villain.getName());
        return villainRepository.save(villain);
    }

    @Override
    @Transactional
    public Villain updateVillain(Villain villain) {
        log.info("Updating villain: {}", villain.getName());
        return villainRepository.save(villain);
    }

    @Override
    @Transactional
    public void deleteVillain(Long id) {
        log.info("Deleting villain with ID: {}", id);
        villainRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Villain spawnVillain(Long villainId, String location) {
        Villain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found with ID: " + villainId));
        
        log.info("Spawning villain {} at location: {}", villain.getName(), location);
        
        // Set the spawn location and activate the villain
        villain.setSpawnLocation(location);
        villain.setActive(true);
        
        return villainRepository.save(villain);
    }

    @Override
    @Transactional
    public Optional<Villain> spawnRandomVillain(VillainType type, String location) {
        // Get all villains of the specified type that are not active
        List<Villain> availableVillains = villainRepository.findAll().stream()
                .filter(v -> v.getType() == type && !v.getActive())
                .collect(Collectors.toList());
        
        if (availableVillains.isEmpty()) {
            log.info("No available villains of type {} to spawn", type);
            return Optional.empty();
        }
        
        // Select a random villain
        Villain selectedVillain = availableVillains.get(random.nextInt(availableVillains.size()));
        
        log.info("Spawning random villain {} of type {} at location: {}", 
                selectedVillain.getName(), type, location);
        
        // Set the spawn location and activate the villain
        selectedVillain.setSpawnLocation(location);
        selectedVillain.setActive(true);
        
        return Optional.of(villainRepository.save(selectedVillain));
    }

    @Override
    @Transactional
    public List<Villain> spawnMinions(Long villainId, int count) {
        Villain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found with ID: " + villainId));
        
        log.info("Spawning {} minions for villain: {}", count, villain.getName());
        
        List<Villain> minions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Villain minion = new Villain();
            minion.setName(villain.getName() + "'s Minion " + (i + 1));
            minion.setDescription("A minion serving " + villain.getName());
            minion.setType(VillainType.MINION);
            minion.setLevel(Math.max(1, villain.getLevel() - 2)); // Minions are weaker than their master
            minion.setHealth(50);
            minion.setMaxHealth(50);
            minion.setMana(50);
            minion.setMaxMana(50);
            minion.setPower(Math.max(5, villain.getPower() - 5));
            minion.setDefense(Math.max(2, villain.getDefense() - 3));
            minion.setSpawnLocation(villain.getSpawnLocation());
            minion.setActive(true);
            
            // Save the minion
            Villain savedMinion = villainRepository.save(minion);
            minions.add(savedMinion);
            
            // Add the minion to the villain's minion list
            villain.getMinionIds().add(savedMinion.getId());
        }
        
        // Update the villain with the new minions
        villainRepository.save(villain);
        
        return minions;
    }

    @Override
    @Transactional
    public Map<String, Integer> defeatVillain(Long villainId, Long playerId) {
        Villain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found with ID: " + villainId));
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        log.info("Player {} defeated villain: {}", player.getName(), villain.getName());
        
        // Deactivate the villain
        villain.setActive(false);
        villainRepository.save(villain);
        
        // Deactivate all minions
        for (Long minionId : villain.getMinionIds()) {
            villainRepository.findById(minionId).ifPresent(minion -> {
                minion.setActive(false);
                villainRepository.save(minion);
            });
        }
        
        // Award rewards to the player
        Map<String, Integer> rewards = villain.getRewards();
        
        // Apply rewards to player
        for (Map.Entry<String, Integer> reward : rewards.entrySet()) {
            switch (reward.getKey()) {
                case "exp":
                    player.setExp(player.getExp() + reward.getValue());
                    break;
                case "currency":
                    player.setCurrency(player.getCurrency() + reward.getValue());
                    break;
                case "reputation":
                    player.setReputation(player.getReputation() + reward.getValue());
                    break;
                // Add more reward types as needed
            }
        }
        
        playerRepository.save(player);
        
        return rewards;
    }

    @Override
    public boolean canChallengeVillain(Long villainId, Long playerId) {
        Villain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found with ID: " + villainId));
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        // Check if the villain is active
        if (!villain.getActive()) {
            return false;
        }
        
        // Check if the player's level is high enough
        if (player.getLevel() < villain.getLevel() - 3) {
            return false;
        }
        
        // Check if the player has defeated all minions
        for (Long minionId : villain.getMinionIds()) {
            Optional<Villain> minionOpt = villainRepository.findById(minionId);
            if (minionOpt.isPresent() && minionOpt.get().getActive()) {
                return false; // Can't challenge the villain if minions are still active
            }
        }
        
        return true;
    }

    @Override
    public List<Villain> getChallengableVillains(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));
        
        List<Villain> activeVillains = villainRepository.findByActiveTrue();
        
        return activeVillains.stream()
                .filter(villain -> canChallengeVillain(villain.getId(), playerId))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getVillainRewards(Long villainId) {
        Villain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found with ID: " + villainId));
        
        return new HashMap<>(villain.getRewards());
    }
}