# Tokugawa Discord Game (Java Implementation)

## Overview

The Java implementation of the Tokugawa Discord Game is a Spring Boot application that uses Discord4J for Discord integration. This project is part of a migration effort from the original Python implementation to a more scalable and maintainable Java architecture.

## Project Structure

```
tokugawa-discord-game/javaapp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/github/disparter/tokugawa/discord/
│   │   │       ├── DiscordGameApplication.java    # Main application class
│   │   │       ├── api/                           # REST API
│   │   │       │   ├── controllers/               # API endpoints
│   │   │       │   └── dtos/                      # Data transfer objects
│   │   │       ├── bot/                           # Discord bot
│   │   │       │   ├── commands/                  # Discord commands
│   │   │       │   └── listeners/                 # Discord event listeners
│   │   │       ├── core/                          # Core game logic
│   │   │       │   ├── models/                    # Domain models
│   │   │       │   ├── repositories/              # Data access
│   │   │       │   └── services/                  # Business logic
│   │   │       ├── config/                        # Configuration
│   │   │       └── utils/                         # Utilities
│   │   └── resources/                             # Application resources
│   └── test/                                      # Test files
├── build.gradle                                   # Gradle build configuration
├── Dockerfile                                     # Docker configuration
└── docker-compose.yml                             # Docker Compose configuration
```

## Current Implementation Status

The Java implementation is in the early stages of development, with the basic structure in place but many features still to be implemented. The current status includes:

### 1. Discord Bot Integration

- **Basic Bot Setup**: The `DiscordBot` class initializes the Discord4J client.
- **Command Framework**: A `SlashCommand` interface and basic implementation structure.
- **Command Listener**: A `SlashCommandListener` to handle Discord slash commands.
- **Basic Commands**: Simple commands like `PingCommand` and `RegisterCommand` are implemented.

### 2. REST API

- **API Structure**: Basic controller and DTO structure for the REST API.
- **Story Controller**: Initial implementation of the `StoryController` for story mode interactions.
- **API Documentation**: OpenAPI configuration for API documentation.

### 3. Core Domain Models

The following domain models have been defined:

- **Player**: Represents a player in the game.
- **Chapter**: Represents a chapter in the story.
- **Event**: Represents an event in the game.
- **Club**: Represents a club that players can join.
- **NPC**: Represents a non-player character.
- **Relationship**: Represents a relationship between a player and an NPC.
- **Item**: Represents an item in the game.
- **Inventory**: Represents a player's inventory.
- **Progress**: Represents a player's progress in the story.

### 4. Data Access

- **Repository Interfaces**: JPA repository interfaces for all domain models.
- **Database Configuration**: Configuration for H2 (development) and PostgreSQL (production).

### 5. Service Layer

The following service interfaces and implementations have been defined:

- **PlayerService**: Manages player data and operations.
- **NarrativeService**: Manages the narrative flow and story progression.
- **EventService**: Manages game events.
- **ClubService**: Manages club operations and progression.
- **ProgressService**: Manages player progress tracking.
- **RelationshipService**: Manages player-NPC relationships.

## Technologies Used

- **Java 21**: The latest LTS version of Java.
- **Spring Boot 3.2.0**: Framework for building Java applications.
- **Discord4J**: Reactive Java library for Discord API.
- **Spring Data JPA**: For data access and persistence.
- **H2 Database**: In-memory database for development.
- **PostgreSQL**: Relational database for production.
- **Lombok**: For reducing boilerplate code.
- **Gradle**: Build automation tool.
- **Docker**: For containerization and deployment.

## Development Environment

The project is set up with:

- **Gradle Build System**: For dependency management and build automation.
- **Docker Support**: For containerized deployment.
- **H2 Console**: Available at `http://localhost:8080/h2-console` for development.
- **Swagger UI**: API documentation available at `http://localhost:8080/swagger-ui.html`.

## Current Limitations

The Java implementation is still in progress and has several limitations:

1. **Incomplete Feature Set**: Many features from the Python implementation are not yet implemented.
2. **Limited Discord Integration**: Only basic Discord commands are implemented.
3. **No Story Content**: The story content and chapter structure need to be migrated.
4. **No Game Mechanics**: Core game mechanics like the club system, relationship system, and event system are not fully implemented.
5. **No Asset Integration**: The image system and asset management are not implemented.