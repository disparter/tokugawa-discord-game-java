# Tokugawa Discord Game - Development Guide

## 🚀 **Quick Start for AI Development**

This guide provides AI assistants with everything needed to effectively work on the Tokugawa Discord Game project.

## 📋 **Current Project State**

### **✅ COMPLETED FEATURES (Production Ready)**

#### **Core Services - 100% Complete**
- **PlayerServiceImpl**: Full database integration, reputation system, skill management
- **ClubServiceImpl**: Complete club management, competitions, member relationships
- **TradeServiceImpl**: Advanced NPC trading with preference calculations
- **InventoryServiceImpl**: Comprehensive item management with validation
- **LocationServiceImpl**: Sophisticated exploration with temporal/item requirements
- **EventServiceImpl**: Database-driven romance routes with hot-reload capability
- **ProgressServiceImpl**: Comprehensive progress tracking with analytics
- **ConsequenceServiceImpl**: Advanced decision tracking with community analytics
- **ReputationServiceImpl**: Multi-faceted reputation with faction support
- **RelationshipServiceImpl**: Complex NPC relationship management

#### **Database Layer - 100% Complete**
- All JPA entities with proper relationships
- Comprehensive repository layer with custom queries
- Database migrations and schema management
- Hot-reloadable configuration system

#### **Discord Integration - 100% Complete**
- Reactive Discord4J integration
- Comprehensive slash command system
- Error handling and user feedback
- Rate limiting and performance optimization

## 🛠️ **Implementation Patterns & Examples**

### **1. Adding a New Service**

```java
// 1. Create Entity
@Entity
@Table(name = "example_entities")
public class ExampleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    // Constructors, getters, setters
}

// 2. Create Repository
@Repository
public interface ExampleRepository extends CrudRepository<ExampleEntity, Long> {
    List<ExampleEntity> findByPlayerId(Long playerId);
    
    @Query("SELECT e FROM ExampleEntity e WHERE e.name LIKE %?1%")
    List<ExampleEntity> findByNameContaining(String name);
}

// 3. Create Service Interface
public interface ExampleService {
    Optional<ExampleEntity> findById(Long id);
    List<ExampleEntity> findByPlayer(Long playerId);
    ExampleEntity save(ExampleEntity entity);
    void deleteById(Long id);
}

// 4. Create Service Implementation
@Service
@Transactional
@Slf4j
public class ExampleServiceImpl implements ExampleService {
    
    private final ExampleRepository exampleRepository;
    private final PlayerService playerService;
    
    public ExampleServiceImpl(ExampleRepository exampleRepository, 
                             PlayerService playerService) {
        this.exampleRepository = exampleRepository;
        this.playerService = playerService;
    }
    
    @Override
    @Cacheable("example-cache")
    public Optional<ExampleEntity> findById(Long id) {
        log.debug("Finding example entity by id: {}", id);
        try {
            return exampleRepository.findById(id);
        } catch (Exception e) {
            log.error("Error finding example entity: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<ExampleEntity> findByPlayer(Long playerId) {
        log.debug("Finding example entities for player: {}", playerId);
        
        // Validate player exists
        if (!playerService.existsById(playerId)) {
            log.warn("Player not found: {}", playerId);
            return Collections.emptyList();
        }
        
        return exampleRepository.findByPlayerId(playerId);
    }
    
    @Override
    @CacheEvict(value = "example-cache", key = "#entity.id")
    public ExampleEntity save(ExampleEntity entity) {
        log.info("Saving example entity: {}", entity.getName());
        
        try {
            ExampleEntity saved = exampleRepository.save(entity);
            log.debug("Successfully saved example entity: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error saving example entity: {}", e.getMessage(), e);
            throw new ServiceException("Failed to save example entity", e);
        }
    }
}
```

### **2. Adding a Discord Command**

```java
@Component
@Slf4j
public class ExampleCommand implements SlashCommand {
    
    private final ExampleService exampleService;
    private final PlayerService playerService;
    
    public ExampleCommand(ExampleService exampleService, PlayerService playerService) {
        this.exampleService = exampleService;
        this.playerService = playerService;
    }
    
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        log.info("Executing example command for user: {}", event.getInteraction().getUser().getId().asString());
        
        try {
            // 1. Extract parameters
            String discordId = event.getInteraction().getUser().getId().asString();
            String exampleParam = event.getOption("example_param")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
            
            // 2. Validate input
            if (exampleParam.trim().isEmpty()) {
                return event.reply("❌ Example parameter is required!")
                    .withEphemeral(true);
            }
            
            // 3. Get or create player
            Optional<Player> playerOpt = playerService.findByUserId(discordId);
            if (playerOpt.isEmpty()) {
                return event.reply("❌ You need to register first! Use `/register` command.")
                    .withEphemeral(true);
            }
            
            Player player = playerOpt.get();
            
            // 4. Execute business logic
            List<ExampleEntity> entities = exampleService.findByPlayer(player.getId());
            
            // 5. Format response
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("🎯 Example Command Results")
                .description(String.format("Found %d entities for %s", entities.size(), player.getUsername()))
                .color(Color.BLUE)
                .timestamp(Instant.now())
                .footer("Tokugawa Game", null)
                .build();
            
            // Add fields for each entity
            if (!entities.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                entities.forEach(entity -> 
                    sb.append(String.format("• **%s** (ID: %d)\n", entity.getName(), entity.getId()))
                );
                embed = embed.withFields(EmbedCreateFields.of("📋 Your Entities", sb.toString(), false));
            }
            
            // 6. Return response
            return event.reply()
                .withEmbeds(embed)
                .withEphemeral(false);
                
        } catch (Exception e) {
            log.error("Error executing example command: {}", e.getMessage(), e);
            return event.reply("❌ An error occurred while processing the command.")
                .withEphemeral(true);
        }
    }
}
```

### **3. Database Configuration Pattern**

```java
// Configuration Entity
@Entity
@Table(name = "example_configs")
public class ExampleConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String configKey;
    
    @Column(nullable = false)
    private String configValue;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

// Configuration Service
@Service
@Slf4j
public class ExampleConfigService {
    
    private final ExampleConfigRepository configRepository;
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initializeCache() {
        log.info("Initializing example configuration cache");
        reloadConfigurations();
    }
    
    public void reloadConfigurations() {
        log.info("Reloading example configurations from database");
        
        try {
            List<ExampleConfig> configs = configRepository.findByActiveTrue();
            configCache.clear();
            
            configs.forEach(config -> 
                configCache.put(config.getConfigKey(), config.getConfigValue()));
            
            log.info("Loaded {} example configurations", configCache.size());
            
        } catch (Exception e) {
            log.error("Error reloading configurations: {}", e.getMessage(), e);
        }
    }
    
    public String getConfigValue(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }
    
    public int getConfigValueAsInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getConfigValue(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.warn("Invalid integer config value for key {}: {}", key, getConfigValue(key, ""));
            return defaultValue;
        }
    }
}
```

## 🔧 **Common Development Tasks**

### **Adding New Game Mechanics**

1. **Define Entity**: Create JPA entity with proper relationships
2. **Create Repository**: Add custom queries for the new mechanics
3. **Implement Service**: Business logic with caching and error handling
4. **Add Discord Command**: User interface for the new feature
5. **Update Configuration**: Add any needed configuration entries
6. **Add Documentation**: Update relevant documentation files

### **Modifying Existing Features**

1. **Check Dependencies**: Review which services depend on the changes
2. **Update Tests**: Modify unit and integration tests
3. **Consider Migrations**: Add Flyway migrations for schema changes
4. **Cache Invalidation**: Update cache keys if data structure changes
5. **Discord Commands**: Update command responses if needed

### **Performance Optimization**

1. **Identify Bottlenecks**: Use profiling and logging
2. **Add Caching**: Use @Cacheable annotations appropriately
3. **Optimize Queries**: Add indexes and optimize JPA queries
4. **Review Transactions**: Minimize transaction scope
5. **Monitor Metrics**: Use Spring Actuator for monitoring

## 📊 **Error Handling Patterns**

### **Service Layer Exceptions**
```java
// Custom exception for business logic errors
public class GameServiceException extends RuntimeException {
    public GameServiceException(String message) {
        super(message);
    }
    
    public GameServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Exception handling in services
try {
    // Business logic
    return result;
} catch (EntityNotFoundException e) {
    log.warn("Entity not found: {}", e.getMessage());
    return Optional.empty();
} catch (DataIntegrityViolationException e) {
    log.error("Data integrity violation: {}", e.getMessage());
    throw new GameServiceException("Data validation failed", e);
} catch (Exception e) {
    log.error("Unexpected error in service: {}", e.getMessage(), e);
    throw new GameServiceException("Service operation failed", e);
}
```

### **Discord Command Error Handling**
```java
return event.reply()
    .withEmbeds(embed)
    .onErrorResume(throwable -> {
        log.error("Error in Discord command: {}", throwable.getMessage(), throwable);
        
        // Determine appropriate error message
        String errorMessage = throwable instanceof GameServiceException 
            ? throwable.getMessage()
            : "An unexpected error occurred. Please try again later.";
            
        return event.reply("❌ " + errorMessage)
            .withEphemeral(true);
    });
```

## 🎯 **Testing Patterns**

### **Service Layer Testing**
```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {
    
    @Mock
    private ExampleRepository exampleRepository;
    
    @Mock
    private PlayerService playerService;
    
    @InjectMocks
    private ExampleServiceImpl exampleService;
    
    @Test
    void testFindById_Success() {
        // Given
        Long id = 1L;
        ExampleEntity entity = new ExampleEntity();
        entity.setId(id);
        entity.setName("Test Entity");
        
        when(exampleRepository.findById(id)).thenReturn(Optional.of(entity));
        
        // When
        Optional<ExampleEntity> result = exampleService.findById(id);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Entity", result.get().getName());
        verify(exampleRepository).findById(id);
    }
    
    @Test
    void testFindById_NotFound() {
        // Given
        Long id = 999L;
        when(exampleRepository.findById(id)).thenReturn(Optional.empty());
        
        // When
        Optional<ExampleEntity> result = exampleService.findById(id);
        
        // Then
        assertFalse(result.isPresent());
    }
}
```

## 📝 **Code Quality Checklist**

### **Before Committing Code**
- [ ] All methods have appropriate JavaDoc comments
- [ ] Error handling is comprehensive and logged
- [ ] Unit tests cover all business logic paths
- [ ] Integration tests verify database operations
- [ ] Performance implications have been considered
- [ ] Cache invalidation is handled correctly
- [ ] Discord command responses are user-friendly
- [ ] Documentation has been updated

### **Code Review Guidelines**
- [ ] Follows established naming conventions
- [ ] Uses appropriate Spring annotations
- [ ] Handles exceptions appropriately
- [ ] Has comprehensive logging
- [ ] Follows reactive patterns for Discord integration
- [ ] Considers security implications
- [ ] Maintains database integrity

## 🚀 **Deployment Considerations**

### **Environment Configuration**
- Database connection settings
- Discord bot token and application ID
- Cache configuration
- Logging levels and appenders
- Performance monitoring settings

### **Health Checks**
- Database connectivity
- Discord API connectivity
- Cache system health
- Application memory usage
- Response time metrics

This guide provides everything needed for effective AI-assisted development on the Tokugawa Discord Game project. The codebase is production-ready with comprehensive features and enterprise-grade architecture.