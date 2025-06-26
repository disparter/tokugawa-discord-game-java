# Discord Game

A Spring Boot application for a Discord game bot using Discord4J.

## Project Structure

- **io.github.disparter.tokugawa.discord**
  - **bot**: Discord bot implementation using Discord4J
    - **commands**: Discord command implementations
    - **listeners**: Discord event listeners
  - **api**: REST API for the game
    - **controllers**: API endpoints
    - **dtos**: Data transfer objects
  - **core**: Core game logic
    - **models**: Domain models
    - **services**: Business logic services
    - **repositories**: Data access
  - **config**: Configuration classes
  - **utils**: Utility classes

## Technologies

- Java 21
- Spring Boot 3.2.0
- Gradle
- Discord4J
- Spring Data JPA
- H2 Database (development)
- PostgreSQL (production)
- Lombok

## Getting Started

### Prerequisites

- Java 21 JDK
- Gradle
- Discord Bot Token (for Discord integration)

### Running the Application

1. Clone the repository
2. Set up your Discord bot token as an environment variable:
   ```
   export DISCORD_TOKEN=your_token_here
   ```
3. Build the application:
   ```
   ./gradlew build
   ```
4. Run the application:
   ```
   ./gradlew bootRun
   ```

## Development

### Database

The application uses H2 in-memory database for development. The H2 console is available at:
```
http://localhost:8080/h2-console
```

### API Documentation

API documentation will be available at:
```
http://localhost:8080/swagger-ui.html
```