# Tokugawa Discord Game - Quick Reference

## 🚀 **Instant Implementation Patterns**

This reference provides copy-paste ready patterns for common development tasks.

## 📁 **File Locations Quick Map**

```
javaapp/src/main/java/com/tokugawa/
├── commands/           # Discord commands (*.java)
├── services/          # Business logic (*ServiceImpl.java)  
├── repositories/      # Data access (*Repository.java)
├── entities/          # JPA entities (*.java)
├── config/           # Configuration classes
└── TokugawaApplication.java

Key Files:
- application.yml (database/discord config)
- .cursorrules (AI development rules)
- docs/ARCHITECTURE.md (system overview)
- docs/DEVELOPMENT_GUIDE.md (detailed patterns)
```

## ⚡ **Copy-Paste Patterns**

### **1. Basic Discord Command Template**

```java
@Component
@Slf4j
public class [CommandName]Command implements SlashCommand {
    
    private final [Service]Service [service]Service;
    private final PlayerService playerService;
    
    public [CommandName]Command([Service]Service [service]Service, PlayerService playerService) {
        this.[service]Service = [service]Service;
        this.playerService = playerService;
    }
    
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        String discordId = event.getInteraction().getUser().getId().asString();
        log.info("Executing [command] command for user: {}", discordId);
        
        try {
            // Get player
            Optional<Player> playerOpt = playerService.findByUserId(discordId);
            if (playerOpt.isEmpty()) {
                return event.reply("❌ You need to register first! Use `/register` command.")
                    .withEphemeral(true);
            }
            
            Player player = playerOpt.get();
            
            // Extract parameters
            String param = event.getOption("param_name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
            
            // Validate input
            if (param.trim().isEmpty()) {
                return event.reply("❌ Parameter is required!")
                    .withEphemeral(true);
            }
            
            // Execute business logic
            // [Your business logic here]
            
            // Create response embed
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("🎯 [Command Name] Results")
                .description("Description here")
                .color(Color.BLUE)
                .timestamp(Instant.now())
                .footer("Tokugawa Game", null)
                .build();
            
            return event.reply().withEmbeds(embed);
            
        } catch (Exception e) {
            log.error("Error in [command] command: {}", e.getMessage(), e);
            return event.reply("❌ An error occurred while processing the command.")
                .withEphemeral(true);
        }
    }
}
```

### **2. Service Implementation Template**

```java
@Service
@Transactional
@Slf4j
public class [Entity]ServiceImpl implements [Entity]Service {
    
    private final [Entity]Repository [entity]Repository;
    private final PlayerService playerService;
    
    public [Entity]ServiceImpl([Entity]Repository [entity]Repository, PlayerService playerService) {
        this.[entity]Repository = [entity]Repository;
        this.playerService = playerService;
    }
    
    @Override
    @Cacheable("[entity]-cache")
    public Optional<[Entity]> findById(Long id) {
        log.debug("Finding [entity] by id: {}", id);
        try {
            return [entity]Repository.findById(id);
        } catch (Exception e) {
            log.error("Error finding [entity]: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<[Entity]> findByPlayer(Long playerId) {
        log.debug("Finding [entity] for player: {}", playerId);
        
        if (!playerService.existsById(playerId)) {
            log.warn("Player not found: {}", playerId);
            return Collections.emptyList();
        }
        
        return [entity]Repository.findByPlayerId(playerId);
    }
    
    @Override
    @CacheEvict(value = "[entity]-cache", key = "#entity.id")
    public [Entity] save([Entity] entity) {
        log.info("Saving [entity]: {}", entity.getName());
        
        try {
            [Entity] saved = [entity]Repository.save(entity);
            log.debug("Successfully saved [entity]: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error saving [entity]: {}", e.getMessage(), e);
            throw new ServiceException("Failed to save [entity]", e);
        }
    }
    
    @Override
    @CacheEvict(value = "[entity]-cache", key = "#id")
    public void deleteById(Long id) {
        log.info("Deleting [entity]: {}", id);
        
        try {
            [entity]Repository.deleteById(id);
            log.debug("Successfully deleted [entity]: {}", id);
        } catch (Exception e) {
            log.error("Error deleting [entity]: {}", e.getMessage(), e);
            throw new ServiceException("Failed to delete [entity]", e);
        }
    }
}
```

### **3. JPA Entity Template**

```java
@Entity
@Table(name = "[table_name]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class [Entity] {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Additional fields as needed
}
```

### **4. Repository Interface Template**

```java
@Repository
public interface [Entity]Repository extends CrudRepository<[Entity], Long> {
    
    List<[Entity]> findByPlayerId(Long playerId);
    List<[Entity]> findByActiveTrue();
    Optional<[Entity]> findByName(String name);
    
    @Query("SELECT e FROM [Entity] e WHERE e.name LIKE %?1% AND e.active = true")
    List<[Entity]> findByNameContaining(String name);
    
    @Query("SELECT e FROM [Entity] e WHERE e.player.id = ?1 AND e.active = true ORDER BY e.createdAt DESC")
    List<[Entity]> findActiveByPlayerIdOrderByCreatedDesc(Long playerId);
    
    @Modifying
    @Query("UPDATE [Entity] e SET e.active = false WHERE e.id = ?1")
    void softDeleteById(Long id);
}
```

## 🎯 **Discord Embed Templates**

### **Success Response**
```java
EmbedCreateSpec.builder()
    .title("✅ Success")
    .description("Action completed successfully!")
    .color(Color.GREEN)
    .timestamp(Instant.now())
    .footer("Tokugawa Game", null)
    .build();
```

### **Error Response**
```java
EmbedCreateSpec.builder()
    .title("❌ Error")
    .description("Something went wrong!")
    .color(Color.RED)
    .timestamp(Instant.now())
    .footer("Tokugawa Game", null)
    .build();
```

### **Information Display**
```java
EmbedCreateSpec.builder()
    .title("📋 Information")
    .description("Here's what you need to know:")
    .addField("Field 1", "Value 1", true)
    .addField("Field 2", "Value 2", true)
    .addField("Field 3", "Value 3", false)
    .color(Color.BLUE)
    .timestamp(Instant.now())
    .footer("Tokugawa Game", null)
    .build();
```

### **Player Status Display**
```java
EmbedCreateSpec.builder()
    .title("🎮 " + player.getUsername() + "'s Status")
    .description("Current player information")
    .addField("💰 Currency", String.valueOf(player.getCurrency()), true)
    .addField("⭐ Level", String.valueOf(player.getLevel()), true)
    .addField("🏆 Reputation", String.valueOf(player.getReputation()), true)
    .color(Color.GOLD)
    .timestamp(Instant.now())
    .footer("Tokugawa Game", null)
    .build();
```

## 🗃️ **Database Query Patterns**

### **Basic CRUD Operations**
```java
// Find by ID with Optional
Optional<Entity> entity = repository.findById(id);

// Find all active entities
List<Entity> entities = repository.findByActiveTrue();

// Find by player with validation
if (playerService.existsById(playerId)) {
    List<Entity> entities = repository.findByPlayerId(playerId);
}

// Save with error handling
try {
    Entity saved = repository.save(entity);
    return saved;
} catch (DataIntegrityViolationException e) {
    throw new ServiceException("Data validation failed", e);
}
```

### **Complex Queries**
```java
// Custom query with parameters
@Query("SELECT e FROM Entity e WHERE e.field1 = ?1 AND e.field2 > ?2 ORDER BY e.createdAt DESC")
List<Entity> findCustomQuery(String field1, Integer field2);

// Pagination support
@Query("SELECT e FROM Entity e WHERE e.playerId = ?1")
Page<Entity> findByPlayerIdPaged(Long playerId, Pageable pageable);

// Aggregate queries
@Query("SELECT COUNT(e) FROM Entity e WHERE e.playerId = ?1 AND e.active = true")
Long countActiveByPlayerId(Long playerId);
```

## ⚡ **Common Service Patterns**

### **Player Validation**
```java
private Player validatePlayer(String discordId) {
    return playerService.findByUserId(discordId)
        .orElseThrow(() -> new ServiceException("Player not found"));
}
```

### **Permission Checking**
```java
private void checkPermission(Player player, String action) {
    if (!hasPermission(player, action)) {
        throw new ServiceException("Insufficient permissions for " + action);
    }
}
```

### **Resource Validation**
```java
private void validateResources(Player player, int cost) {
    if (player.getCurrency() < cost) {
        throw new ServiceException("Insufficient currency. Required: " + cost);
    }
}
```

### **Cooldown Checking**
```java
private void checkCooldown(Player player, String action, Duration cooldownDuration) {
    LocalDateTime lastAction = getLastActionTime(player, action);
    if (lastAction != null && Duration.between(lastAction, LocalDateTime.now()).compareTo(cooldownDuration) < 0) {
        throw new ServiceException("Action on cooldown");
    }
}
```

## 🔧 **Configuration Patterns**

### **Database-driven Configuration**
```java
@Service
@Slf4j
public class ConfigService {
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    public String getConfig(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }
    
    public int getConfigInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getConfig(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
```

### **Cache Configuration**
```java
@Cacheable("config-cache")
public String getCachedConfig(String key) {
    return configRepository.findByKey(key)
        .map(Config::getValue)
        .orElse(null);
}

@CacheEvict(value = "config-cache", key = "#key")
public void invalidateConfig(String key) {
    // Cache will be refreshed on next access
}
```

## 🎮 **Game Mechanics Snippets**

### **Experience Calculation**
```java
public int calculateExperienceGain(int baseExp, Player player) {
    double multiplier = 1.0 + (player.getLevel() * 0.1);
    return (int) (baseExp * multiplier);
}
```

### **Level Up Check**
```java
public boolean checkLevelUp(Player player) {
    int requiredExp = calculateRequiredExp(player.getLevel() + 1);
    return player.getExperience() >= requiredExp;
}
```

### **Reputation Calculation**
```java
public void updateReputation(Player player, String action, int amount) {
    int newReputation = player.getReputation() + amount;
    player.setReputation(Math.max(0, Math.min(1000, newReputation)));
    playerService.save(player);
}
```

## 🔍 **Debugging Patterns**

### **Comprehensive Logging**
```java
@Slf4j
public class ServiceImpl {
    public void performAction(String action, Long playerId) {
        log.info("Starting {}: playerId={}", action, playerId);
        
        try {
            // Action logic
            log.debug("{} completed successfully: playerId={}", action, playerId);
        } catch (Exception e) {
            log.error("{} failed: playerId={}, error={}", action, playerId, e.getMessage(), e);
            throw e;
        }
    }
}
```

### **Performance Monitoring**
```java
public void monitorPerformance() {
    long startTime = System.currentTimeMillis();
    
    try {
        // Execute operation
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Operation completed in {}ms", duration);
        
        if (duration > 1000) {
            log.warn("Slow operation detected: {}ms", duration);
        }
    }
}
```

## 🚀 **Ready-to-Use Implementations**

All services in this project are **PRODUCTION READY**:
- ✅ PlayerServiceImpl - Complete database integration
- ✅ ClubServiceImpl - Full club management system  
- ✅ EventServiceImpl - Database-driven romance routes
- ✅ TradeServiceImpl - Advanced NPC trading
- ✅ LocationServiceImpl - Sophisticated exploration
- ✅ InventoryServiceImpl - Comprehensive item management

**Key Features Available:**
- Hot-reload configuration system
- Multi-layered caching (Caffeine + Database)
- Reactive Discord integration
- Comprehensive error handling
- Advanced game mechanics
- Enterprise-grade architecture

This reference provides everything needed for instant implementation following established patterns!