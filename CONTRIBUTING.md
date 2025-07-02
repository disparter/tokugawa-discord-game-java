# Contributing to Tokugawa Discord Game

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## 🚀 Quick Start for Contributors

### Prerequisites

- Java 21+ JDK
- PostgreSQL 13+
- Git
- Discord Developer Account

### Development Setup

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/yourusername/tokugawa-discord-game.git
   cd tokugawa-discord-game
   ```
3. **Set up the development environment**:
   ```bash
   # Set up PostgreSQL database
   createdb tokugawa_game_dev
   
   # Configure environment variables
   export DISCORD_TOKEN=your_test_bot_token
   export DB_URL=jdbc:postgresql://localhost:5432/tokugawa_game_dev
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```
4. **Build and test**:
   ```bash
   cd javaapp
   ./gradlew build test
   ```

## 📋 Development Process

We use [GitHub Flow](https://guides.github.com/introduction/flow/index.html), so all code changes happen through pull requests.

### Pull Request Process

1. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/amazing-feature
   ```
2. **Make your changes** following our coding standards
3. **Add tests** for your changes
4. **Ensure all tests pass**:
   ```bash
   ./gradlew test
   ```
5. **Update documentation** if needed
6. **Submit a pull request** with a clear title and description

### Pull Request Guidelines

- **Title**: Use a clear, descriptive title (e.g., "Add romance route configuration system")
- **Description**: Explain what changes you made and why
- **Testing**: Include information about how you tested your changes
- **Documentation**: Update relevant documentation
- **Breaking Changes**: Clearly mark any breaking changes

## 🎯 Coding Standards

### Java Code Style

- **Package Structure**: Follow `io.github.disparter.tokugawa.discord.{area}`
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Annotations**: Use Spring annotations consistently
  - `@Service` for business logic
  - `@Repository` for data access
  - `@Entity` for JPA entities
  - `@Component` for Discord commands

### Database Conventions

- **Table Names**: `snake_case` (e.g., `player_progress`, `club_members`)
- **Column Names**: `snake_case`
- **Primary Keys**: `id` column with `BIGINT` type
- **Foreign Keys**: Clear naming (e.g., `player_id`, `club_id`)

### Discord Command Structure

```java
@Component
public class ExampleCommand implements SlashCommand {
    private final ExampleService exampleService;
    
    public ExampleCommand(ExampleService exampleService) {
        this.exampleService = exampleService;
    }
    
    @Override
    public String getName() {
        return "example";
    }
    
    @Override
    public String getDescription() {
        return "Example command description";
    }
    
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.deferReply()
            .then(processCommand(event))
            .then(event.createFollowup("Command completed"))
            .then();
    }
}
```

### Service Layer Structure

```java
@Service
@Transactional
@Slf4j
public class ExampleServiceImpl implements ExampleService {
    private final ExampleRepository repository;
    
    public ExampleServiceImpl(ExampleRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public Optional<Example> findById(Long id) {
        log.debug("Finding example by id: {}", id);
        return repository.findById(id);
    }
}
```

## 🧪 Testing Guidelines

### Test Structure

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Repository Tests**: Use `@DataJpaTest`
- **Service Tests**: Use `@SpringBootTest` with test slices

### Test Naming

```java
@Test
void shouldReturnPlayerWhenValidIdProvided() {
    // Given
    Long playerId = 1L;
    
    // When
    Optional<Player> result = playerService.findById(playerId);
    
    // Then
    assertThat(result).isPresent();
}
```

### Test Coverage

- Aim for 80%+ code coverage
- Focus on critical business logic
- Test error conditions and edge cases

## 🐛 Bug Reports

Great bug reports tend to have:

- **Clear title** that summarizes the issue
- **Steps to reproduce** the bug
- **Expected behavior** vs actual behavior
- **Environment information** (Java version, database, etc.)
- **Logs or error messages**
- **Screenshots** for UI-related issues

**Template:**

```markdown
## Bug Description
A clear description of what the bug is.

## Steps to Reproduce
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

## Expected Behavior
What you expected to happen.

## Actual Behavior
What actually happened.

## Environment
- Java Version: [e.g. 21]
- PostgreSQL Version: [e.g. 13.3]
- OS: [e.g. Ubuntu 20.04]

## Additional Context
Add any other context or screenshots.
```

## 💡 Feature Requests

We love feature requests! Please provide:

- **Clear description** of the feature
- **Use case**: Why is this feature needed?
- **Proposed solution**: How should it work?
- **Alternatives**: Any alternative solutions considered?

## 📚 Documentation Contributions

Documentation improvements are always welcome:

- **API Documentation**: Update JavaDoc comments
- **User Guides**: Improve setup and usage instructions
- **Architecture Docs**: Keep architecture documentation current
- **Examples**: Add code examples and tutorials

## 🔒 Security Issues

**Do NOT report security vulnerabilities through public GitHub issues.**

Instead, email us at: security@your-domain.com

We'll respond within 48 hours and work with you to resolve the issue.

## 📝 Commit Message Guidelines

Use conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix  
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(player): add achievement tracking system
fix(trading): resolve NPC preference calculation bug
docs(readme): update installation instructions
```

## 🏷️ Issue and PR Labels

### Issue Labels

- `bug`: Something isn't working
- `enhancement`: New feature or request
- `documentation`: Improvements to documentation
- `good first issue`: Good for newcomers
- `help wanted`: Extra attention is needed
- `question`: Further information is requested

### PR Labels

- `work in progress`: PR is not ready for review
- `ready for review`: PR is ready for review
- `needs changes`: PR needs changes before merge
- `approved`: PR has been approved

## 🎖️ Recognition

Contributors will be recognized in:

- **README.md**: Listed in acknowledgments
- **Release Notes**: Mentioned in changelog
- **Hall of Fame**: Special recognition for major contributions

## 📞 Getting Help

- **Documentation**: Check our [docs](docs/) first
- **Discussions**: Use GitHub Discussions for questions
- **Discord**: Join our community server
- **Issues**: Create an issue for bugs or feature requests

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

## 🙏 Thank You

Thank you for considering contributing to Tokugawa Discord Game! Your contributions make this project better for everyone.

**Happy Coding!** 🎮