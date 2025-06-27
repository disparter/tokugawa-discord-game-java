# Tokugawa Discord Game

A Discord game based on the Tokugawa universe, migrated from Python to Java.

## Project Structure

- `javaapp/`: Java implementation of the game
- `untitled/`: Original Python implementation
- `git-hooks/`: Git hooks for semantic versioning

## Semantic Versioning

This project uses semantic versioning for git commits, pushes, and tags.

### Installation

To install the semantic versioning git hooks:

```bash
./install-hooks.sh
```

### Commit Message Format

Commit messages must follow this format:

```
<type>(<scope>): <subject>
```

Example:
```
feat(player): add reputation system
```

Types:
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **test**: Adding missing tests
- **chore**: Changes to the build process or auxiliary tools

For more details, see the [git-hooks README](git-hooks/README.md).

# Tokugawa Discord Game Migration Project

## Overview

This directory contains documentation and resources for migrating the Tokugawa Discord Game from its Python implementation (untitled directory) to its Java implementation (javaapp directory).

## Key Documents

- [Migration Guide](migration_guide.md): Comprehensive step-by-step guide for the migration process
- [Comparison Summary](../../comparison_summary.md): Detailed comparison of the Python and Java implementations

## Migration Strategy

The migration follows a service-by-service approach, focusing on maintaining functionality while adapting to the Java/Spring architecture. The strategy includes:

1. **Incremental Migration**: Migrate one service at a time, ensuring each works before moving to the next
2. **Feature Parity**: Ensure all Python features are implemented in Java
3. **Architecture Adaptation**: Adapt Python's cog-based architecture to Java's Spring-based architecture
4. **Testing**: Verify each migrated component with appropriate tests

## Components to Migrate

1. **Database Layer**: From DynamoDB to JPA/Hibernate
2. **Bot Implementation**: From discord.py to Discord4J
3. **Command System**: From cog-based to interface-based
4. **Story Mode**: Complex narrative system with choices and consequences
5. **Event System**: Daily, weekly, and special events
6. **API Endpoints**: RESTful API for external access

## Junie AI Configuration

For optimal results when using Junie AI to assist with the migration:

- **Temperature**: 1.8 (for creative solutions)
- **Context Window**: Maximum available
- **Knowledge Base**: Include both Java and Python documentation
- **Specialized Prompts**: Use the prompts provided in the migration guide

## Recommended Workflow

1. Start with database migration
2. Move to core services
3. Implement bot and command system
4. Add story mode functionality
5. Implement event system
6. Create API endpoints
7. Verify all functionality

## Verification

After migration, verify functionality using the checklist in the [Verification Steps](migration_guide.md#verification-steps) section of the migration guide.