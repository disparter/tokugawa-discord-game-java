# Tokugawa Discord Game Migration Guide

## Overview

This document provides a comprehensive guide for migrating the Tokugawa Discord Game from its Python implementation (untitled directory) to its Java implementation (javaapp directory). It includes step-by-step instructions, code examples, and specific prompts for Junie AI to implement each migration step.

## Table of Contents

1. [Migration Strategy](#migration-strategy)
2. [Architecture Comparison](#architecture-comparison)
3. [Service Migration Steps](#service-migration-steps)
4. [Database Migration](#database-migration)
5. [Bot Implementation Migration](#bot-implementation-migration)
6. [Command System Migration](#command-system-migration)
7. [Story Mode Migration](#story-mode-migration)
8. [Event System Migration](#event-system-migration)
9. [API Endpoints Migration](#api-endpoints-migration)
10. [Verification Steps](#verification-steps)
11. [Junie AI Configuration](#junie-ai-configuration)

## Migration Strategy

The migration will follow a service-by-service approach, focusing on maintaining functionality while adapting to the Java/Spring architecture. The strategy includes:

1. **Incremental Migration**: Migrate one service at a time, ensuring each works before moving to the next
2. **Feature Parity**: Ensure all Python features are implemented in Java
3. **Architecture Adaptation**: Adapt Python's cog-based architecture to Java's Spring-based architecture
4. **Testing**: Verify each migrated component with appropriate tests

## Architecture Comparison

### Python Implementation
- **Framework**: Discord.py
- **Architecture**: Cog-based with dependency injection
- **Database**: DynamoDB
- **Command System**: Discord.py commands and app_commands
- **Dependency Management**: Custom dependency injection container

### Java Implementation
- **Framework**: Spring Boot with Discord4J
- **Architecture**: Spring MVC with controllers, services, and repositories
- **Database**: Not explicitly defined (likely JPA/Hibernate)
- **Command System**: Interface-based command system with Spring dependency injection
- **Dependency Management**: Spring's dependency injection

## Service Migration Steps

For each service, follow these general steps:

1. Identify the Python service and its dependencies
2. Create equivalent Java interfaces and implementations
3. Adapt the service to use Spring's dependency injection
4. Implement the service logic
5. Create appropriate tests
6. Verify functionality

Detailed steps for each service are provided in the following sections.

## Database Migration

### Overview

The Python implementation uses DynamoDB, while the Java implementation likely uses a relational database with JPA/Hibernate. This section outlines the steps to migrate the database layer.

### Steps

1. **Analyze Python Data Models**:
   - Examine all Python repository classes in `core/persistence/repositories/`
   - Identify entity structures and relationships

2. **Create Java Entity Classes**:
   - Create JPA entity classes for each Python model
   - Define appropriate relationships (OneToMany, ManyToOne, etc.)
   - Add JPA annotations for persistence

3. **Create Java Repositories**:
   - Create Spring Data JPA repositories for each entity
   - Implement custom query methods as needed

4. **Data Migration Script**:
   - Create a script to migrate data from DynamoDB to the relational database
   - Ensure data integrity during migration

### Example: Player Entity Migration

Python Player Repository:
```python
class PlayerRepository:
    def __init__(self, db):
        self.db = db
        self.table_name = "players"

    def find_by_id(self, player_id):
        response = self.db.get_item(
            TableName=self.table_name,
            Key={"id": {"S": str(player_id)}}
        )
        return self._convert_to_player(response.get("Item"))
```

Java Player Entity:
```java
@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private int level;

    @Column(name = "exp")
    private int exp;

    @Column(name = "points")
    private int points;

    @Column(name = "reputation")
    private int reputation;

    // Getters and setters
}
```

Java Player Repository:
```java
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUserId(String userId);
}
```

### Junie AI Prompt for Database Migration

```
Create JPA entity classes for the Tokugawa Discord Game based on the following Python models:

1. Player model with fields: id, user_id, name, level, exp, points, reputation
2. Inventory model with fields: id, player_id, items (list of items)
3. Club model with fields: id, name, leader_id, members (list of member IDs)

For each entity:
1. Add appropriate JPA annotations (@Entity, @Id, @Column, etc.)
2. Define relationships between entities
3. Create corresponding Spring Data JPA repository interfaces
4. Implement any custom query methods needed

Use the Spring Data JPA best practices and ensure proper indexing for frequently queried fields.
```

## Bot Implementation Migration

### Overview

The Python implementation uses discord.py, while the Java implementation uses Discord4J. This section outlines the steps to migrate the bot implementation.

### Steps

1. **Analyze Python Bot Structure**:
   - Examine the TokugawaBot class in `bot.py`
   - Identify event handlers and command registration

2. **Create Java Bot Structure**:
   - Enhance the existing DiscordBot class
   - Implement equivalent event handlers
   - Set up command registration

3. **Migrate Bot Configuration**:
   - Migrate environment variables and configuration
   - Set up application.properties for Spring

4. **Implement Bot Lifecycle**:
   - Implement initialization and shutdown logic
   - Handle connection and disconnection events

### Example: Bot Initialization Migration

Python Bot Initialization:
```python
class TokugawaBot(commands.Bot):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        intents.members = True

        super().__init__(
            command_prefix='!',
            intents=intents,
            help_command=None
        )

        self.container = None
        self.player_service = None
        self.events_manager = None
        self.config = None
        self.start_time = None

    async def setup_hook(self):
        # Initialize container
        self.container = Container()

        # Wire the container to all modules that need injection
        self.container.wire(modules=[...])

        # Initialize player_service from container
        self.player_service = self.container.player_service()

        # Load extensions
        for filename in os.listdir('src/cogs'):
            if filename.endswith('.py') and not filename.startswith('__'):
                await self.load_extension(f'cogs.{filename[:-3]}')
```

Java Bot Initialization:
```java
@Component
public class DiscordBot {
    @Value("${discord.token}")
    private String token;

    private GatewayDiscordClient gatewayClient;
    private final SlashCommandListener slashCommandListener;
    private final EventsManager eventsManager;
    private final PlayerService playerService;

    public DiscordBot(SlashCommandListener slashCommandListener, 
                     EventsManager eventsManager,
                     PlayerService playerService) {
        this.slashCommandListener = slashCommandListener;
        this.eventsManager = eventsManager;
        this.playerService = playerService;
    }

    @PostConstruct
    public void init() {
        DiscordClient client = DiscordClient.create(token);

        gatewayClient = client.login().block();

        if (gatewayClient != null) {
            // Register the slash command listener
            gatewayClient.on(SlashCommandInteractionEvent.class, slashCommandListener::handle)
                    .subscribe();

            // Start events manager
            eventsManager.start();

            System.out.println("Discord bot connected successfully!");
        }
    }
}
```

### Junie AI Prompt for Bot Implementation Migration

```
Enhance the DiscordBot class in the Java implementation to include features from the Python TokugawaBot:

1. Add support for the following features:
   - Events manager integration
   - Command synchronization
   - Error handling for commands
   - Bot status updates

2. Implement the following methods:
   - Enhanced initialization with proper error handling
   - Proper shutdown procedure
   - Command registration and synchronization
   - Event handling for various Discord events

3. Ensure the bot has the same intents and permissions as the Python version:
   - Message content intent
   - Server members intent
   - Appropriate permissions for commands

Use Discord4J's reactive programming model and integrate with Spring's dependency injection system.
```

## Command System Migration

### Overview

The Python implementation uses discord.py's cog system for commands, while the Java implementation uses a custom SlashCommand interface. This section outlines the steps to migrate the command system.

### Steps

1. **Analyze Python Command Structure**:
   - Examine the cog classes in `src/cogs/`
   - Identify command methods and their parameters
   - Note command decorators and options

2. **Create Java Command Classes**:
   - Create Java classes implementing the SlashCommand interface
   - Implement the getName() and execute() methods
   - Add appropriate Spring annotations

3. **Migrate Command Logic**:
   - Translate Python command logic to Java
   - Adapt to Discord4J's reactive programming model
   - Ensure proper error handling

4. **Register Commands**:
   - Ensure commands are properly registered with Discord
   - Implement command option handling

### Example: Registration Command Migration

Python Registration Command:
```python
@commands.command(name="register")
async def register(self, ctx):
    # Get the user
    user = ctx.author
    user_id = str(user.id)

    # Check if player already exists
    player = self.registration_service.find_player_by_discord_id(user_id)

    if player:
        await ctx.send(f"Olá, {user.name}! Você já está registrado no jogo Tokugawa. Bem-vindo de volta!")
    else:
        # Create new player
        player = self.registration_service.create_player(user_id, user.name)
        await ctx.send(f"Olá, {user.name}! Você foi registrado no jogo Tokugawa. Bem-vindo!")
```

Java Registration Command:
```java
@Component
public class RegisterCommand implements SlashCommand {

    private final PlayerService playerService;

    @Autowired
    public RegisterCommand(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public Mono<Void> execute(SlashCommandInteractionEvent event) {
        return Mono.justOrEmpty(event.getInteraction().getUser())
                .flatMap(user -> {
                    String userId = user.getId().asString();
                    String username = user.getUsername();

                    // Check if player already exists
                    Player player = playerService.findByDiscordId(userId);

                    String message;
                    if (player != null) {
                        message = "Olá, " + username + "! Você já está registrado no jogo Tokugawa. Bem-vindo de volta!";
                    } else {
                        // Create new player
                        player = new Player();
                        player.setUserId(userId);
                        player.setName(username);
                        player.setLevel(1);
                        player.setExp(0);
                        player.setPoints(0);

                        playerService.save(player);
                        message = "Olá, " + username + "! Você foi registrado no jogo Tokugawa. Bem-vindo!";
                    }

                    return event.reply()
                            .withContent(message)
                            .withEphemeral(true);
                });
    }
}
```

### Junie AI Prompt for Command System Migration

```
Migrate the following Python commands from the Tokugawa Discord Game to Java using the SlashCommand interface:

1. The "status" command from player_status.py:
   - Shows player status including level, experience, and reputation
   - Uses embeds for rich display
   - Has options for public/private visibility

2. The "story" command from story_mode.py:
   - Starts or continues the story mode
   - Presents choices to the player
   - Tracks player progress

For each command:
1. Create a Java class implementing the SlashCommand interface
2. Implement the getName() and execute() methods
3. Use Discord4J's reactive programming model
4. Ensure proper error handling
5. Add appropriate Spring annotations for dependency injection

Make sure to adapt the Python logic to Java while maintaining the same functionality.
```

## Story Mode Migration

### Overview

The Python implementation has an extensive story mode system in the `story_mode` directory and `StoryModeCog` class. The Java implementation needs equivalent functionality. This section outlines the steps to migrate the story mode system.

### Steps

1. **Analyze Python Story Mode Structure**:
   - Examine the StoryModeCog class in `src/cogs/story_mode.py`
   - Examine the story mode modules in `src/story_mode/`
   - Identify key components: story progression, choices, consequences

2. **Create Java Story Mode Structure**:
   - Create equivalent Java classes for story mode components
   - Implement story loading and progression
   - Implement choice handling and consequences

3. **Migrate Story Content**:
   - Migrate story chapters and scenes
   - Migrate choice options and outcomes
   - Ensure proper localization

4. **Implement UI Components**:
   - Create Discord embeds for story presentation
   - Implement buttons for choices
   - Create progress tracking UI

### Example: Story Progression Migration

Python Story Progression:
```python
def _send_dialogue_or_choices(self, ctx_or_interaction, chapter_data, player_data):
    # Get current scene
    current_scene = player_data.get("current_scene", "intro")
    scene_data = chapter_data["scenes"].get(current_scene)

    if not scene_data:
        return False

    # Check if scene has choices
    if "choices" in scene_data:
        return self._show_choices(ctx_or_interaction, chapter_data, player_data)
    else:
        # It's a dialogue scene, show text and continue button
        embed = create_story_embed(
            title=scene_data.get("title", ""),
            description=scene_data.get("text", ""),
            image_url=scene_data.get("image")
        )

        # Create continue button
        continue_button = discord.ui.Button(
            style=discord.ButtonStyle.primary,
            label="Continuar",
            custom_id=f"continue_{ctx_or_interaction.author.id if hasattr(ctx_or_interaction, 'author') else ctx_or_interaction.user.id}"
        )

        continue_button.callback = self._create_continue_callback(
            ctx_or_interaction.author.id if hasattr(ctx_or_interaction, 'author') else ctx_or_interaction.user.id
        )

        view = discord.ui.View()
        view.add_item(continue_button)

        if hasattr(ctx_or_interaction, 'send'):
            await ctx_or_interaction.send(embed=embed, view=view)
        else:
            await ctx_or_interaction.response.send_message(embed=embed, view=view)

        return True
```

Java Story Progression:
```java
@Service
public class StoryService {
    private final StoryRepository storyRepository;
    private final PlayerService playerService;

    @Autowired
    public StoryService(StoryRepository storyRepository, PlayerService playerService) {
        this.storyRepository = storyRepository;
        this.playerService = playerService;
    }

    public Mono<Void> sendDialogueOrChoices(SlashCommandInteractionEvent event, String chapterId) {
        return Mono.justOrEmpty(event.getInteraction().getUser())
                .flatMap(user -> {
                    String userId = user.getId().asString();

                    // Get player data
                    Player player = playerService.findByDiscordId(userId);
                    if (player == null) {
                        return event.reply()
                                .withContent("Você precisa se registrar primeiro usando /register")
                                .withEphemeral(true);
                    }

                    // Get chapter data
                    Chapter chapter = storyRepository.findChapterById(chapterId);
                    if (chapter == null) {
                        return event.reply()
                                .withContent("Capítulo não encontrado")
                                .withEphemeral(true);
                    }

                    // Get current scene
                    String currentScene = player.getCurrentScene() != null ? 
                            player.getCurrentScene() : "intro";
                    Scene scene = chapter.getScenes().get(currentScene);

                    if (scene == null) {
                        return event.reply()
                                .withContent("Cena não encontrada")
                                .withEphemeral(true);
                    }

                    // Check if scene has choices
                    if (scene.getChoices() != null && !scene.getChoices().isEmpty()) {
                        return showChoices(event, chapter, player, scene);
                    } else {
                        // It's a dialogue scene, show text and continue button
                        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                                .title(scene.getTitle())
                                .description(scene.getText())
                                .image(scene.getImageUrl() != null ? 
                                        scene.getImageUrl() : null)
                                .build();

                        // Create continue button
                        Button continueButton = Button.primary(
                                "continue_" + userId,
                                "Continuar");

                        return event.reply()
                                .withEmbeds(embed)
                                .withComponents(ActionRow.of(continueButton));
                    }
                });
    }
}
```

### Junie AI Prompt for Story Mode Migration

```
Create a comprehensive story mode system for the Java implementation of Tokugawa Discord Game based on the Python implementation:

1. Create the following core components:
   - StoryService: Manages story progression and player choices
   - ChapterLoader: Loads story chapters from JSON or database
   - ChoiceHandler: Processes player choices and their consequences
   - StoryCommand: Slash command for interacting with the story mode

2. Implement the following features:
   - Story progression tracking per player
   - Choice presentation with buttons
   - Consequence system for choices
   - Image support for scenes
   - Relationship system with NPCs

3. Create appropriate data models:
   - Chapter: Contains scenes and metadata
   - Scene: Contains text, choices, and images
   - Choice: Contains text, next scene, and consequences
   - StoryProgress: Tracks player progress through the story

Use Discord4J's reactive programming model and integrate with Spring's dependency injection system. Ensure the system is extensible for adding new chapters and features.
```

## Event System Migration

### Overview

The Python implementation has an event system managed by the EventsManager class, which handles daily, weekly, and special events. The Java implementation needs equivalent functionality. This section outlines the steps to migrate the event system.

### Steps

1. **Analyze Python Event System**:
   - Examine the EventsManager class in `src/events/events_manager.py`
   - Examine the event types: DailyEvents, WeeklyEvents, SpecialEvents
   - Identify event scheduling and handling mechanisms

2. **Create Java Event System**:
   - Create an EventsManager class
   - Implement event types and handlers
   - Set up scheduling with Spring's TaskScheduler

3. **Migrate Event Logic**:
   - Migrate event announcement logic
   - Migrate event participation logic
   - Migrate event rewards logic

4. **Implement Event Commands**:
   - Create commands for viewing current events
   - Create commands for participating in events
   - Create admin commands for managing events

### Example: Events Manager Migration

Python Events Manager:
```python
class EventsManager:
    """Manages and coordinates all types of events."""

    def __init__(self, bot, player_service):
        self.bot = bot
        self.player_service = player_service
        self.daily_events = DailyEvents(bot, player_service)
        self.weekly_events = WeeklyEvents(bot, player_service)
        self.special_events = SpecialEvents(bot, player_service)
        self.is_running = False
        self.task = None

    async def start(self):
        """Start the events manager."""
        try:
            if self.is_running:
                return

            self.is_running = True
            self.task = asyncio.create_task(self._run_event_loop())
            logger.info("Started events manager")

        except Exception as e:
            logger.error(f"Error starting events manager: {e}")
            self.is_running = False

    async def _run_event_loop(self):
        """Main event loop that manages all events."""
        try:
            while self.is_running:
                current_time = datetime.now()

                # Check for special events
                await self.special_events.check_for_special_events()

                # Handle daily events
                if current_time.hour == 0 and current_time.minute == 0:
                    await self.daily_events.send_daily_announcements()
                    await self.daily_events.select_daily_subject()
                    await self.daily_events.announce_daily_subject()
                    await self.daily_events.reset_daily_progress()

                # Handle weekly events
                if current_time.weekday() == 0 and current_time.hour == 0 and current_time.minute == 0:
                    await self.weekly_events.start_weekly_tournament()

                # Wait for next minute
                await asyncio.sleep(60)

        except asyncio.CancelledError:
            logger.info("Event loop cancelled")
        except Exception as e:
            logger.error(f"Error in event loop: {e}")
            self.is_running = False
```

Java Events Manager:
```java
@Service
public class EventsManager {
    private final Logger logger = LoggerFactory.getLogger(EventsManager.class);

    private final DiscordBot discordBot;
    private final PlayerService playerService;
    private final DailyEvents dailyEvents;
    private final WeeklyEvents weeklyEvents;
    private final SpecialEvents specialEvents;
    private final TaskScheduler taskScheduler;

    private boolean isRunning = false;
    private ScheduledFuture<?> scheduledTask;

    @Autowired
    public EventsManager(DiscordBot discordBot, 
                         PlayerService playerService,
                         TaskScheduler taskScheduler) {
        this.discordBot = discordBot;
        this.playerService = playerService;
        this.taskScheduler = taskScheduler;

        this.dailyEvents = new DailyEvents(discordBot, playerService);
        this.weeklyEvents = new WeeklyEvents(discordBot, playerService);
        this.specialEvents = new SpecialEvents(discordBot, playerService);
    }

    public void start() {
        if (isRunning) {
            return;
        }

        try {
            isRunning = true;

            // Schedule daily events at midnight
            scheduledTask = taskScheduler.scheduleAtFixedRate(() -> {
                LocalDateTime now = LocalDateTime.now();

                try {
                    // Check for special events
                    specialEvents.checkForSpecialEvents();

                    // Handle daily events at midnight
                    if (now.getHour() == 0 && now.getMinute() == 0) {
                        dailyEvents.sendDailyAnnouncements();
                        dailyEvents.selectDailySubject();
                        dailyEvents.announceDailySubject();
                        dailyEvents.resetDailyProgress();
                    }

                    // Handle weekly events on Monday at midnight
                    if (now.getDayOfWeek() == DayOfWeek.MONDAY && 
                        now.getHour() == 0 && now.getMinute() == 0) {
                        weeklyEvents.startWeeklyTournament();
                    }

                    // Check for ending events
                    weeklyEvents.checkForEndingTournament();
                    specialEvents.checkForEndingSpecialEvent();

                } catch (Exception e) {
                    logger.error("Error in event scheduler: " + e.getMessage(), e);
                }
            }, Duration.ofMinutes(1));

            logger.info("Started events manager");

        } catch (Exception e) {
            logger.error("Error starting events manager: " + e.getMessage(), e);
            isRunning = false;
        }
    }

    public void stop() {
        if (!isRunning) {
            return;
        }

        try {
            isRunning = false;

            if (scheduledTask != null) {
                scheduledTask.cancel(false);
            }

            // Clean up all events
            dailyEvents.cleanup();
            weeklyEvents.cleanup();
            specialEvents.cleanup();

            logger.info("Stopped events manager");

        } catch (Exception e) {
            logger.error("Error stopping events manager: " + e.getMessage(), e);
        }
    }
}
```

### Junie AI Prompt for Event System Migration

```
Create an event system for the Java implementation of Tokugawa Discord Game based on the Python implementation:

1. Create the following core components:
   - EventsManager: Main class that coordinates all events
   - DailyEvents: Handles daily events and challenges
   - WeeklyEvents: Handles weekly tournaments and special activities
   - SpecialEvents: Handles seasonal and special events

2. Implement the following features:
   - Event scheduling using Spring's TaskScheduler
   - Event announcements in Discord channels
   - Event participation tracking
   - Event rewards distribution
   - Event completion notifications

3. Create appropriate data models:
   - Event: Base class for all events
   - EventParticipant: Tracks player participation in events
   - EventReward: Defines rewards for event completion
   - EventSchedule: Defines when events occur

4. Implement commands for:
   - Viewing current events
   - Participating in events
   - Checking event progress
   - (Admin) Creating and managing events

Use Discord4J's reactive programming model and integrate with Spring's dependency injection system. Ensure proper error handling and logging throughout the event system.
```

## API Endpoints Migration

### Overview

The Java implementation includes RESTful API endpoints through the DecisionDashboardController, while the Python implementation may not have equivalent functionality. This section outlines the steps to migrate or create API endpoints.

### Steps

1. **Analyze Java API Endpoints**:
   - Examine the DecisionDashboardController class
   - Identify API endpoints and their functionality
   - Note request/response formats

2. **Create Python API Endpoints (if needed)**:
   - Create FastAPI or Flask endpoints to match Java functionality
   - Implement equivalent business logic
   - Ensure proper authentication and authorization

3. **Enhance Java API Documentation**:
   - Improve OpenAPI documentation
   - Add detailed descriptions and examples
   - Ensure proper error responses

4. **Implement API Tests**:
   - Create tests for all API endpoints
   - Test error handling and edge cases
   - Ensure consistent behavior across implementations

### Example: Decision Dashboard Endpoint

Java API Endpoint:
```java
@RestController
@RequestMapping("/api/decisions")
public class DecisionDashboardController {

    private final ConsequenceService consequenceService;

    @Autowired
    public DecisionDashboardController(ConsequenceService consequenceService) {
        this.consequenceService = consequenceService;
    }

    @GetMapping("/dashboard/{playerId}")
    public ResponseEntity<Map<String, List<Consequence>>> getDecisionDashboard(@PathVariable Long playerId) {
        try {
            Map<String, List<Consequence>> dashboard = consequenceService.getDecisionDashboardForPlayer(playerId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/track/{playerId}")
    public ResponseEntity<Consequence> trackPlayerDecision(
            @PathVariable Long playerId,
            @RequestBody TrackDecisionRequest requestBody) {

        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

### Junie AI Prompt for API Endpoints Migration

```
Create or enhance the API endpoints for the Tokugawa Discord Game:

1. Implement the following RESTful API endpoints:
   - GET /api/players/{playerId}: Get player details
   - GET /api/players/{playerId}/inventory: Get player inventory
   - GET /api/players/{playerId}/story-progress: Get player story progress
   - POST /api/players/{playerId}/decisions: Track a player decision
   - GET /api/story/chapters: Get all story chapters
   - GET /api/story/chapters/{chapterId}: Get a specific chapter

2. For each endpoint:
   - Implement proper request validation
   - Add appropriate error handling
   - Document with OpenAPI annotations
   - Implement security (authentication and authorization)

3. Create a configuration class for Swagger/OpenAPI documentation:
   - Add detailed descriptions for all endpoints
   - Include example requests and responses
   - Configure security schemes

4. Implement cross-cutting concerns:
   - Request logging
   - Performance monitoring
   - Rate limiting for public endpoints

Use Spring MVC best practices and ensure all endpoints follow RESTful principles.
```

## Verification Steps

### Overview

After completing the migration, it's essential to verify that all functionality works as expected. This section outlines the steps to verify the migration.

### Steps

1. **Unit Testing**:
   - Run all unit tests for Java implementation
   - Ensure all tests pass
   - Check code coverage

2. **Integration Testing**:
   - Test interactions between components
   - Verify database operations
   - Test API endpoints

3. **Bot Functionality Testing**:
   - Test bot connection to Discord
   - Test all commands
   - Verify event system

4. **Story Mode Testing**:
   - Test story progression
   - Test choices and consequences
   - Verify image loading

5. **Performance Testing**:
   - Test under load
   - Measure response times
   - Identify bottlenecks

### Verification Checklist

- [ ] Bot successfully connects to Discord
- [ ] All slash commands are registered
- [ ] Registration command works
- [ ] Story mode commands work
- [ ] Event system schedules and runs events
- [ ] API endpoints return correct responses
- [ ] Database operations work correctly
- [ ] Error handling works as expected
- [ ] Performance is acceptable under load

## Junie AI Configuration

### Overview

To optimize Junie AI for the migration tasks, specific configuration settings are recommended. This section outlines the configuration for Junie AI.

### Configuration Settings

1. **Temperature**: 1.8
   - Higher temperature for creative solutions
   - Allows for diverse approaches to migration challenges

2. **Context Window**: Maximum available
   - Ensures Junie has access to all relevant code
   - Helps maintain consistency across implementations

3. **Knowledge Base**: Include both Java and Python documentation
   - Discord4J documentation
   - Spring Boot documentation
   - discord.py documentation
   - DynamoDB and JPA documentation

4. **Specialized Prompts**:
   - Use the prompts provided in each section
   - Adapt prompts as needed for specific tasks
   - Include code examples when possible

### Example Junie AI Prompt

```
You are tasked with migrating a specific component of the Tokugawa Discord Game from Python to Java. 

Context:
- The Python implementation uses discord.py with a cog-based architecture
- The Java implementation uses Spring Boot with Discord4J
- The component needs to maintain the same functionality while adapting to Java patterns

Your task:
1. Analyze the Python code provided
2. Create equivalent Java code following Spring and Discord4J patterns
3. Ensure all functionality is preserved
4. Add appropriate error handling and logging
5. Include unit tests for the new code

Python code to migrate:
[Insert Python code here]

Please provide:
1. Java class(es) implementing the same functionality
2. Explanation of your approach
3. Any considerations for integration with the rest of the system
```

### Recommended Workflow

1. Start with database migration
2. Move to core services
3. Implement bot and command system
4. Add story mode functionality
5. Implement event system
6. Create API endpoints
7. Verify all functionality

By following this workflow and using the provided prompts, Junie AI can efficiently migrate the Tokugawa Discord Game from Python to Java while maintaining all functionality and following best practices.
