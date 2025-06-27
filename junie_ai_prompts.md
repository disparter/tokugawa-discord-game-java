# Junie AI Prompts for Tokugawa Discord Game Migration

This document contains specialized prompts for Junie AI to assist with the migration of the Tokugawa Discord Game from Python to Java. These prompts are designed to be used with a temperature setting of 1.8 for optimal results.

## Table of Contents

1. [Database Migration Prompts](#database-migration-prompts)
2. [Bot Implementation Prompts](#bot-implementation-prompts)
3. [Command System Prompts](#command-system-prompts)
4. [Story Mode Prompts](#story-mode-prompts)
5. [Event System Prompts](#event-system-prompts)
6. [API Endpoints Prompts](#api-endpoints-prompts)
7. [Testing Prompts](#testing-prompts)

## Database Migration Prompts

### Entity Migration Prompt

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

### Data Migration Script Prompt

```
Create a data migration script to transfer data from DynamoDB to a relational database for the Tokugawa Discord Game:

1. The script should:
   - Connect to both DynamoDB and the relational database
   - Read all data from DynamoDB tables
   - Transform the data to fit the relational model
   - Insert the data into the relational database
   - Verify data integrity after migration

2. Handle the following special cases:
   - List fields in DynamoDB that need to be mapped to join tables
   - JSON fields that need to be parsed and stored in separate tables
   - Maintaining relationships between entities

3. Include error handling and logging for:
   - Connection failures
   - Data transformation errors
   - Insertion failures
   - Validation errors

Provide the script in Java using appropriate libraries for both DynamoDB and JDBC/JPA operations.
```

## Bot Implementation Prompts

### Bot Class Enhancement Prompt

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

### Bot Configuration Prompt

```
Create a comprehensive configuration system for the Java implementation of the Tokugawa Discord Game:

1. Implement the following configuration components:
   - application.properties file with all necessary properties
   - Configuration class to load and validate properties
   - Environment-specific configurations (dev, test, prod)

2. Include configurations for:
   - Discord bot token and settings
   - Database connection details
   - API settings and security
   - Logging configuration
   - Feature toggles

3. Implement a mechanism to:
   - Reload configuration at runtime
   - Validate configuration values
   - Provide sensible defaults
   - Handle missing or invalid configuration

Use Spring Boot's configuration capabilities and follow best practices for secure configuration management.
```

## Command System Prompts

### Command Migration Prompt

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

### Command Registration Prompt

```
Create a command registration system for the Java implementation of the Tokugawa Discord Game:

1. Implement a CommandRegistrar class that:
   - Discovers all SlashCommand implementations using Spring's ApplicationContext
   - Registers commands with Discord when the bot starts
   - Handles global and guild-specific commands
   - Provides logging for command registration

2. Implement a CommandOptionBuilder utility that:
   - Creates command options (arguments) for slash commands
   - Supports various option types (string, integer, boolean, user, channel, etc.)
   - Handles required vs. optional options
   - Provides validation for option values

3. Create a CommandPermissionHandler that:
   - Manages command permissions
   - Restricts commands to specific roles or users
   - Provides admin override capabilities
   - Logs permission denials

Use Discord4J's command registration API and integrate with Spring's dependency injection system.
```

## Story Mode Prompts

### Story System Prompt

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

### Story Content Migration Prompt

```
Create a story content migration tool for the Tokugawa Discord Game:

1. The tool should:
   - Read story content from the Python implementation (JSON or database)
   - Transform it to the format needed for the Java implementation
   - Save the transformed content to the Java system
   - Validate content integrity after migration

2. Handle the following content types:
   - Chapters and scenes
   - Choices and consequences
   - Character dialogues
   - Images and media
   - Relationship values

3. Include features for:
   - Content validation (checking for broken links, missing scenes, etc.)
   - Content optimization (compressing images, etc.)
   - Content versioning
   - Content backup

Provide the tool as a Java application that can be run as part of the migration process or as a standalone utility.
```

## Event System Prompts

### Event System Prompt

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

### Event Content Migration Prompt

```
Create an event content migration tool for the Tokugawa Discord Game:

1. The tool should:
   - Read event definitions from the Python implementation
   - Transform them to the format needed for the Java implementation
   - Save the transformed events to the Java system
   - Validate event integrity after migration

2. Handle the following event types:
   - Daily events and challenges
   - Weekly tournaments
   - Special and seasonal events
   - Event rewards and participation records

3. Include features for:
   - Event validation (checking for consistency, etc.)
   - Event scheduling verification
   - Event reward balance checking
   - Event participation history migration

Provide the tool as a Java application that can be run as part of the migration process or as a standalone utility.
```

## API Endpoints Prompts

### API Endpoints Prompt

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

### API Security Prompt

```
Implement a comprehensive security system for the API endpoints of the Tokugawa Discord Game:

1. Create the following security components:
   - Authentication system using JWT tokens
   - Role-based authorization
   - API key validation for external clients
   - Rate limiting to prevent abuse

2. Implement security for the following scenarios:
   - Player accessing their own data
   - Admin accessing any player's data
   - External systems accessing public data
   - Discord bot accessing player data

3. Add the following security features:
   - HTTPS enforcement
   - CSRF protection
   - Input validation and sanitization
   - Secure logging (no sensitive data)
   - Audit trail for sensitive operations

4. Create documentation for:
   - How to obtain authentication tokens
   - How to use API keys
   - Permission levels and roles
   - Security best practices for clients

Use Spring Security and follow OWASP security best practices throughout the implementation.
```

## Testing Prompts

### Unit Testing Prompt

```
Create comprehensive unit tests for the Java implementation of the Tokugawa Discord Game:

1. Write tests for the following components:
   - Service classes (PlayerService, StoryService, etc.)
   - Repository classes
   - Command classes
   - Event system classes
   - API controllers

2. For each component, test:
   - Normal operation with valid inputs
   - Edge cases and boundary conditions
   - Error handling and exceptions
   - Performance under load (where appropriate)

3. Use the following testing techniques:
   - Mocking of dependencies
   - Parameterized tests for multiple scenarios
   - Test fixtures for common test data
   - Assertions for verifying results

4. Ensure tests follow best practices:
   - Arrange-Act-Assert pattern
   - Descriptive test names
   - Independent tests (no dependencies between tests)
   - Fast execution

Use JUnit 5, Mockito, and other appropriate testing libraries. Aim for high code coverage while focusing on testing business logic and edge cases.
```

### Integration Testing Prompt

```
Create integration tests for the Java implementation of the Tokugawa Discord Game:

1. Write tests for the following integration points:
   - Database interactions
   - Discord API interactions
   - API endpoints
   - Event scheduling and execution
   - Component interactions

2. For each integration point, test:
   - End-to-end functionality
   - Error handling and recovery
   - Performance and resource usage
   - Security constraints

3. Use the following testing approaches:
   - TestContainers for database testing
   - Mock Discord API for bot testing
   - RestAssured for API testing
   - Spring test context for component testing

4. Implement test utilities for:
   - Setting up test data
   - Cleaning up after tests
   - Simulating various conditions
   - Measuring performance

Use Spring Boot Test, TestContainers, WireMock, and other appropriate testing libraries. Focus on testing real interactions between components while minimizing external dependencies.
```

## General Migration Prompt

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