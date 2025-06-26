# Semantic Versioning Git Hooks

This directory contains git hooks for implementing semantic versioning in the Tokugawa Discord Game project.

## Installation

Run the installation script from the project root:

```bash
./install-hooks.sh
```

This will install the hooks in your local `.git/hooks` directory.

## Semantic Commit Messages

Commit messages must follow this format:

```
<type>(<scope>): <subject>
```

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools and libraries

### Scope

The scope is optional and should be a noun describing a section of the codebase:

```
feat(auth): add login functionality
```

### Breaking Changes

For breaking changes, include "BREAKING CHANGE" in the commit message body:

```
feat(api): change authentication endpoints

BREAKING CHANGE: The authentication endpoints now require a different payload structure.
```

## Automatic Versioning

The hooks will automatically:

1. Validate commit messages to ensure they follow the semantic format
2. Create version tags based on commit messages:
   - **feat**: Increments the MINOR version (1.2.0 → 1.3.0)
   - **fix**: Increments the PATCH version (1.2.3 → 1.2.4)
   - **BREAKING CHANGE**: Increments the MAJOR version (1.2.3 → 2.0.0)
3. Remind you to push tags when pushing commits

## Version Format

Versions follow the [Semantic Versioning](https://semver.org/) format:

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: Incompatible API changes
- **MINOR**: Added functionality in a backward-compatible manner
- **PATCH**: Backward-compatible bug fixes

## Examples

```bash
# Feature (increments MINOR version)
git commit -m "feat(player): add reputation system"

# Bug fix (increments PATCH version)
git commit -m "fix(combat): correct damage calculation"

# Breaking change (increments MAJOR version)
git commit -m "feat(api): redesign authentication system

BREAKING CHANGE: The authentication API has been completely redesigned."
```

## Pushing Tags

After committing, tags are created locally. To push them to the remote repository:

```bash
git push origin --tags
```

The pre-push hook will remind you to do this when you push commits.