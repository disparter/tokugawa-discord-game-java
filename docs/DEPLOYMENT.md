# Deployment Guide

This document outlines the steps to deploy the Discord Game application to production.

## Prerequisites

- Java 21 JDK
- Docker and Docker Compose
- Access to a PostgreSQL database (or use the provided Docker Compose setup)
- Discord Bot Token

## Deployment Steps

### 1. Build the Application and Docker Image

```bash
# Navigate to the project directory
cd javaapp

# Build the application with Gradle
./gradlew clean build

# Build the Docker image
docker build -t discord-game:latest .
```

### 2. Deploy to Staging Environment

Before deploying to production, it's recommended to test the application in a staging environment.

```bash
# Set environment variables for staging
export DISCORD_TOKEN=your_discord_token
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password

# Deploy to staging using Docker Compose
SPRING_PROFILES_ACTIVE=prod docker-compose up -d
```

### 3. Validate in Staging

- Verify that the application is running correctly
- Check the logs for any errors: `docker-compose logs -f app`
- Test the API endpoints
- Verify Discord bot functionality
- Check monitoring endpoints:
  - Health: http://staging-server:8080/actuator/health
  - Info: http://staging-server:8080/actuator/info
  - Metrics: http://staging-server:8080/actuator/metrics

### 4. Deploy to Production

Once validated in staging, deploy to production:

```bash
# Set environment variables for production
export DISCORD_TOKEN=your_production_discord_token
export DB_USERNAME=your_production_db_username
export DB_PASSWORD=your_production_db_password

# Deploy to production using Docker Compose
SPRING_PROFILES_ACTIVE=prod docker-compose up -d
```

### 5. Post-Deployment Verification

- Verify that the application is running correctly in production
- Monitor the logs: `docker-compose logs -f app`
- Check the monitoring endpoints
- Set up alerts for critical metrics

## Environment Variables

The following environment variables are required:

- `DISCORD_TOKEN`: Your Discord bot token
- `DB_USERNAME`: PostgreSQL database username
- `DB_PASSWORD`: PostgreSQL database password

Optional environment variables:

- `DB_HOST`: PostgreSQL host (default: postgres)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: PostgreSQL database name (default: gamedb)
- `SPRING_PROFILES_ACTIVE`: Spring profile to use (default: dev, use prod for production)

## Monitoring

The application exposes the following monitoring endpoints:

- `/actuator/health`: Health status of the application
- `/actuator/info`: Information about the application
- `/actuator/metrics`: Metrics about the application
- `/actuator/prometheus`: Prometheus-compatible metrics

## Logging

Logs are stored in the `logs` directory and are also available via Docker:

```bash
docker-compose logs -f app
```

## Backup and Restore

### Database Backup

```bash
docker exec discord-game-postgres pg_dump -U ${DB_USERNAME} gamedb > backup.sql
```

### Database Restore

```bash
cat backup.sql | docker exec -i discord-game-postgres psql -U ${DB_USERNAME} gamedb
```

## Troubleshooting

- If the application fails to start, check the logs: `docker-compose logs -f app`
- If the database connection fails, verify the database credentials and connection details
- If the Discord bot doesn't respond, check the Discord token and bot permissions