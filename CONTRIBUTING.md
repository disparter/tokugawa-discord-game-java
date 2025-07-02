# Contributing to Tokugawa Discord Game

Thank you for your interest in contributing to the Tokugawa Discord Game project! This document provides guidelines for contributing to our Spring Boot Discord game implementation.

## 🎯 Project Overview

This is a production-ready Spring Boot Discord game featuring:
- Advanced visual novel gameplay mechanics
- Database-driven configuration system
- Reactive Discord integration with Discord4J
- Comprehensive club/relationship/trading systems
- Multi-layered caching and performance optimization

## 📋 Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 13+
- Docker & Docker Compose (for testing)
- Git

### Quick Start
```bash
# Clone the repository
git clone <repository-url>
cd tokugawa-discord-game

# Set up the application
cd javaapp
./gradlew build

# Run tests
./gradlew test
./gradlew functionalTest
```

## 🔧 Development Guidelines

### Code Style
- Follow Java naming conventions (PascalCase for classes, camelCase for methods)
- Use Spring annotations consistently (@Service, @Repository, @Entity)
- Add comprehensive JavaDoc for all public methods
- Maintain 80% test coverage minimum

### Architecture Patterns
- **Service Layer**: All business logic in @Service classes with @Transactional
- **Repository Pattern**: JPA repositories extending CrudRepository
- **Command Pattern**: Discord commands implementing SlashCommand interface
- **Configuration Pattern**: Database-driven config with fallback mechanisms

### Database Conventions
- Table names: snake_case (players, romance_route_configs, club_members)
- Primary keys: Long id with @GeneratedValue(strategy = GenerationType.IDENTITY)
- Foreign keys: Explicit @JoinColumn annotations
- Add @Index annotations for frequently queried fields

## 🧪 Testing Requirements

### Unit Tests
- All service layer methods must have unit tests
- Use Mockito for mocking dependencies
- Aim for 80%+ code coverage

### Functional Tests
- BDD scenarios using Cucumber in Portuguese
- Black-box testing with Testcontainers
- WireMock for external API simulation

### Integration Tests
- Database operations testing
- Discord command interaction testing
- End-to-end workflow validation

## 📝 Pull Request Process

1. **Fork the repository** and create a feature branch
2. **Write tests** for your changes (unit + functional)
3. **Follow coding standards** and architecture patterns
4. **Update documentation** if needed
5. **Submit PR** with clear description of changes

### PR Requirements
- [ ] All tests pass (unit + functional + integration)
- [ ] Code coverage maintained above 80%
- [ ] No linting errors
- [ ] Documentation updated if applicable
- [ ] Follows established architecture patterns

## 🐛 Bug Reports

When reporting bugs, please include:
- Steps to reproduce
- Expected vs actual behavior
- Environment details (Java version, OS, etc.)
- Relevant logs/stack traces

## 💡 Feature Requests

For new features:
- Check existing issues/discussions first
- Provide clear use case and benefits
- Consider impact on existing systems
- Discuss architecture implications

## 📚 Documentation

All documentation should be placed in the `docs/` directory:
- Technical documentation in `docs/`
- API references in `docs/API_REFERENCE.md`
- Architecture details in `docs/ARCHITECTURE.md`
- Development guides in `docs/DEVELOPMENT_GUIDE.md`

## 🤝 Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help newcomers learn the codebase
- Maintain professional communication

## 📧 Contact

For questions or discussions:
- Open an issue for technical questions
- Use discussions for general questions
- Contact maintainers for security issues

---

Thank you for contributing to Tokugawa Discord Game! 🎮