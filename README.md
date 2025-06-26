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

## Documentation

- [Java Project Documentation](java-project-documentation.md)
- [Python Project Documentation](python-project-documentation.md)
- [Migration Requirements](migration-requirements.md)