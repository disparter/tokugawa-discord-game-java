# Tokugawa Discord Game - Architecture Documentation

## 🏗️ **System Architecture Overview**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           TOKUGAWA DISCORD GAME ARCHITECTURE                        │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Discord User  │    │   Web Client    │    │  Discord Bot    │    │   Admin Panel   │
│                 │    │                 │    │                 │    │                 │
│ /command        │    │ HTTP Requests   │    │ Slash Commands  │    │ Configuration   │
│ interactions    │    │ API calls       │    │ Events          │    │ Management      │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │                      │
          └──────────────────────┼──────────────────────┼──────────────────────┘
                                 │                      │
                                 ▼                      ▼
         ┌─────────────────────────────────────────────────────────────────────────────┐
         │                      SPRING BOOT APPLICATION                              │
         │                                                                           │
         │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ │
         │  │   API Layer     │  │   Bot Layer     │  │      Config Layer          │ │
         │  │                 │  │                 │  │                            │ │
         │  │ @RestController │  │ @Component      │  │ @Configuration             │ │
         │  │ - PlayerAPI     │  │ - ClubCommand   │  │ - DiscordConfig            │ │
         │  │ - ClubAPI       │  │ - EventCommand  │  │ - DatabaseConfig           │ │
         │  │ - EventAPI      │  │ - TradeCommand  │  │ - SecurityConfig           │ │
         │  │ - TradeAPI      │  │ - RelationCmd   │  │ - CacheConfig              │ │
         │  └─────────┬───────┘  └─────────┬───────┘  └─────────────┬───────────────┘ │
         │            │                    │                        │                 │
         │            └────────────────────┼────────────────────────┘                 │
         │                                 │                                          │
         │  ┌─────────────────────────────────────────────────────────────────────────┐ │
         │  │                        SERVICE LAYER                                   │ │
         │  │                                                                       │ │
         │  │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────────┐ │ │
         │  │ │ Core Services   │ │ Game Services   │ │ Configuration Services     │ │ │
         │  │ │                 │ │                 │ │                            │ │ │
         │  │ │ PlayerService   │ │ ClubService     │ │ RomanceRouteConfigService  │ │ │
         │  │ │ InventoryService│ │ EventService    │ │ GameCalendarService        │ │ │
         │  │ │ LocationService │ │ TradeService    │ │ AssetService               │ │ │
         │  │ │ NPCService      │ │ DuelService     │ │                            │ │ │
         │  │ └─────────┬───────┘ └─────────┬───────┘ └─────────────┬───────────────┘ │ │
         │  │           │                   │                       │                 │ │
         │  │ ┌─────────┴───────┐ ┌─────────┴───────┐ ┌─────────────┴───────────────┐ │ │
         │  │ │ Business Logic  │ │ Relationship    │ │ Progress & Analytics        │ │ │
         │  │ │                 │ │ Services        │ │ Services                    │ │ │
         │  │ │ ProgressService │ │ RelationshipSvc │ │ ConsequenceService          │ │ │
         │  │ │ ReputationSvc   │ │ RomanceService  │ │ AchievementService          │ │ │
         │  │ │ SkillService    │ │ SocialService   │ │ AnalyticsService            │ │ │
         │  │ └─────────┬───────┘ └─────────┬───────┘ └─────────────┬───────────────┘ │ │
         │  └───────────┼───────────────────┼───────────────────────┼─────────────────┘ │
         │              │                   │                       │                   │
         │  ┌───────────┴───────────────────┴───────────────────────┴─────────────────┐ │
         │  │                        REPOSITORY LAYER                                │ │
         │  │                                                                       │ │
         │  │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────────┐ │ │
         │  │ │ Core Entities   │ │ Game Entities   │ │ Configuration Entities     │ │ │
         │  │ │                 │ │                 │ │                            │ │ │
         │  │ │ PlayerRepo      │ │ ClubRepo        │ │ RomanceRouteConfigRepo     │ │ │
         │  │ │ InventoryRepo   │ │ EventRepo       │ │ GameCalendarRepo           │ │ │
         │  │ │ ItemRepo        │ │ ConsequenceRepo │ │ AssetRepo                  │ │ │
         │  │ │ NPCRepo         │ │ ProgressRepo    │ │                            │ │ │
         │  │ │ LocationRepo    │ │ RelationshipRepo│ │                            │ │ │
         │  │ └─────────┬───────┘ └─────────┬───────┘ └─────────────┬───────────────┘ │ │
         │  └───────────┼───────────────────┼───────────────────────┼─────────────────┘ │
         └──────────────┼───────────────────┼───────────────────────┼───────────────────┘
                        │                   │                       │
                        ▼                   ▼                       ▼
         ┌─────────────────────────────────────────────────────────────────────────────┐
         │                           DATABASE LAYER                                   │
         │                                                                           │
         │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ │
         │  │   PostgreSQL    │  │    Caching      │  │     File Storage           │ │
         │  │                 │  │                 │  │                            │ │
         │  │ ┌─────────────┐ │  │ ┌─────────────┐ │  │ ┌─────────────────────────┐ │ │
         │  │ │Core Tables  │ │  │ │ Redis/      │ │  │ │ Asset Files            │ │ │
         │  │ │- players    │ │  │ │ Caffeine    │ │  │ │ - Images               │ │ │
         │  │ │- items      │ │  │ │ Cache       │ │  │ │ - Audio                │ │ │
         │  │ │- inventories│ │  │ │             │ │  │ │ - Configuration Files  │ │ │
         │  │ │- locations  │ │  │ └─────────────┘ │  │ └─────────────────────────┘ │ │
         │  │ │- npcs       │ │  └─────────────────┘  └─────────────────────────────┘ │
         │  │ └─────────────┘ │                                                       │
         │  │                 │                                                       │
         │  │ ┌─────────────┐ │                                                       │
         │  │ │Game Tables  │ │                                                       │
         │  │ │- clubs      │ │                                                       │
         │  │ │- events     │ │                                                       │
         │  │ │- progress   │ │                                                       │
         │  │ │- relationships│ │                                                       │
         │  │ │- consequences│ │                                                       │
         │  │ └─────────────┘ │                                                       │
         │  │                 │                                                       │
         │  │ ┌─────────────┐ │                                                       │
         │  │ │Config Tables│ │                                                       │
         │  │ │- romance_routes│ │                                                     │
         │  │ │- game_calendar│ │                                                       │
         │  │ │- asset_configs│ │                                                       │
         │  │ └─────────────┘ │                                                       │
         │  └─────────────────┘                                                       │
         └─────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────────┐
                                    │ External APIs   │
                                    │                 │
                                    │ - Discord API   │
                                    │ - AWS Services  │
                                    │ - Monitoring    │
                                    └─────────────────┘
```

## 🔄 **Data Flow Architecture**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              DATA FLOW DIAGRAM                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

Discord User Action
        │
        ▼
┌─────────────────┐
│ Discord4J       │ ──┐
│ Event Listener  │   │
└─────────────────┘   │
        │             │
        ▼             │
┌─────────────────┐   │
│ Command Router  │   │ ◄─── Reactive Streams
│ (SlashCommand)  │   │
└─────────────────┘   │
        │             │
        ▼             │
┌─────────────────┐   │
│ Command         │   │
│ Implementation  │   │
│ (e.g. ClubCmd)  │ ──┘
└─────────────────┘
        │
        ▼
┌─────────────────┐
│ Service Layer   │ ◄──── @Transactional Boundaries
│ (Business Logic)│
└─────────────────┘
        │
        ├─── Validation & Business Rules
        │
        ▼
┌─────────────────┐
│ Repository      │ ◄──── JPA/Hibernate
│ Layer           │
└─────────────────┘
        │
        ▼
┌─────────────────┐
│ Database        │ ◄──── Connection Pooling
│ (PostgreSQL)    │
└─────────────────┘
        │
        ▼
┌─────────────────┐
│ Response        │ ──┐
│ Processing      │   │
└─────────────────┘   │
        │             │
        ▼             │
┌─────────────────┐   │
│ Discord Response│   │ ◄─── Reactive Response
│ (Embed/Message) │   │
└─────────────────┘   │
        │             │
        ▼             │
┌─────────────────┐   │
│ User Receives   │ ──┘
│ Response        │
└─────────────────┘
```

## 🎯 **Core Design Patterns**

### **1. Service Layer Pattern**
```java
@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    // Business logic implementation
}
```

### **2. Repository Pattern**
```java
@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {
    Optional<Player> findByUserId(String discordId);
    List<Player> findByReputation(int reputation);
}
```

### **3. Command Pattern (Discord Commands)**
```java
@Component
public class ClubCommand implements SlashCommand {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event);
}
```

### **4. Configuration Pattern**
```java
@Service
public class RomanceRouteConfigService {
    // Database-driven configuration with fallback
    Map<Long, Integer> getAllRomanceRequirements();
}
```

## 🔧 **Technology Stack Details**

### **Backend Framework**
- **Spring Boot 3.2+**: Main application framework
- **Spring Data JPA**: Database abstraction layer
- **Spring Transaction**: Declarative transaction management
- **Spring Security**: Authentication and authorization
- **Spring Cache**: Caching abstraction

### **Discord Integration**
- **Discord4J**: Reactive Discord API client
- **Reactive Streams**: Non-blocking I/O operations
- **WebFlux**: Reactive web framework

### **Database Layer**
- **PostgreSQL**: Primary database
- **Hibernate**: ORM implementation
- **HikariCP**: Connection pooling
- **Flyway**: Database migration management

### **Configuration & Caching**
- **Database-driven config**: Hot-reloadable configurations
- **Caffeine Cache**: In-memory caching
- **Concurrent Collections**: Thread-safe data structures

## 📊 **Database Schema Overview**

### **Core Entities**
```sql
-- Player management
players (id, user_id, username, level, exp, reputation, currency, stats, skills)
inventories (id, player_id, items, capacity, last_updated)
items (id, name, type, price, tradable, usable, description)

-- Location and exploration
locations (id, name, description, connected_locations, npcs, items, locked)
npcs (id, name, type, location_id, dialogue, trade_inventory, preferences)

-- Game progression
progress (id, player_id, current_chapter, completed_chapters, triggered_events)
consequences (id, player_id, choice_made, effects, community_percentage)
achievements (id, player_id, achievement_type, completed_at, metadata)
```

### **Social & Club Systems**
```sql
-- Club management
clubs (id, name, type, description, reputation, ranking, leader_id)
club_members (club_id, player_id, joined_at, role)
club_relationships (club1_id, club2_id, relationship_type, created_at)

-- Player relationships
relationships (id, player_id, npc_id, affinity, status, triggered_events)
romance_route_configs (id, npc_id, required_affinity, chapter_sequence, is_active)
```

### **Event & Configuration Systems**
```sql
-- Event management
events (id, event_id, name, type, description, trigger_conditions, rewards)
event_participants (event_id, player_id, participated_at, completed)

-- Configuration management
game_calendar (id, current_date, current_season, special_events)
asset_configs (id, asset_type, asset_path, metadata, is_active)
```

## 🚀 **Deployment Architecture**

### **Production Environment**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │────│  Spring Boot    │────│   PostgreSQL    │
│   (nginx/ALB)   │    │   Application   │    │   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌─────────┐           ┌─────────────┐         ┌─────────────┐
    │ Discord │           │   Redis     │         │  File       │
    │   API   │           │   Cache     │         │  Storage    │
    └─────────┘           └─────────────┘         └─────────────┘
```

### **Key Features**
- **Horizontal Scaling**: Stateless Spring Boot instances
- **Database Clustering**: PostgreSQL with read replicas
- **Caching Strategy**: Multi-level caching (Redis + Caffeine)
- **Monitoring**: Comprehensive logging and metrics
- **Security**: OAuth2, rate limiting, input validation

## 🔍 **Performance Considerations**

### **Caching Strategy**
- **L1 Cache**: Caffeine (in-memory, application level)
- **L2 Cache**: Redis (distributed, shared across instances)
- **Database Cache**: PostgreSQL buffer cache optimization

### **Database Optimization**
- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: Proper indexing and query analysis
- **Transaction Management**: Minimal transaction scope
- **Read Replicas**: Separate read/write operations

### **Discord Integration**
- **Rate Limiting**: Built-in Discord4J rate limiting
- **Reactive Streams**: Non-blocking I/O for scalability
- **Connection Management**: Efficient WebSocket handling

## 🛡️ **Security Architecture**

### **Authentication & Authorization**
- **Discord OAuth2**: User authentication via Discord
- **Role-based Access**: Player, moderator, admin roles
- **Rate Limiting**: Command and API rate limiting
- **Input Validation**: Comprehensive input sanitization

### **Data Protection**
- **Encryption**: Database encryption at rest
- **Secure Communications**: TLS/SSL for all connections
- **Audit Logging**: Comprehensive security event logging
- **Privacy Compliance**: GDPR-compliant data handling

This architecture provides a robust, scalable foundation for the Tokugawa Discord Game with enterprise-grade reliability and performance.