# 🏯 Tokugawa Discord Game

[![CI](https://github.com/disparter/tokugawa-discord-game/actions/workflows/ci.yml/badge.svg)](https://github.com/disparter/tokugawa-discord-game/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)](https://spring.io/projects/spring-boot)
[![Discord4J](https://img.shields.io/badge/Discord4J-3.2-blue.svg)](https://discord4j.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Production Ready](https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg)](#-deployment-status)

> An immersive **visual novel-style Discord game** set in feudal Japan, featuring advanced gameplay mechanics, social systems, and narrative progression.

![Tokugawa Discord Game Banner](https://img.shields.io/badge/🎮%20Game%20Type-Visual%20Novel%20%2B%20Social%20Simulation-purple)
![Database Driven](https://img.shields.io/badge/⚡%20Features-Database%20Driven%20Configuration-blue)
![Real Time](https://img.shields.io/badge/🔄%20Updates-Real%20Time%20Hot%20Reload-orange)

## 🌟 What is Tokugawa Discord Game?

Tokugawa Discord Game brings the rich storytelling tradition of visual novels directly into Discord servers. Players navigate complex social relationships, join clubs, engage in trading, and experience branching storylines—all through intuitive Discord slash commands.

### � Key Features

- **📖 Visual Novel Storytelling**: Rich narrative experiences with branching storylines and character development
- **�️ Club System**: Join clubs, build relationships, compete in tournaments, and form alliances
- **💰 Trading Economy**: Dynamic NPC-based trading system with preference calculations and market dynamics
- **🌸 Romance Routes**: Configurable romance progression with multiple characters and endings
- **🗺️ Location Exploration**: Discover new areas with sophisticated requirement and unlock systems
- **📊 Progress Tracking**: Comprehensive achievement and reputation systems with detailed analytics
- **⚡ Real-Time Configuration**: Hot-reload game settings without server restarts
- **🔄 Consequence System**: Community-driven decision tracking with statistical analysis

## 🚀 Quick Start

### For Players
1. **Join a Server**: Find a Discord server running the Tokugawa game
2. **Register**: Use `/register` to create your character
3. **Explore**: Start with `/locations` to see available areas
4. **Interact**: Use `/clubs` to join communities and `/trade` to interact with NPCs

### For Server Administrators
1. **Setup**: Follow our [Deployment Guide](docs/DEPLOYMENT.md)
2. **Configure**: Use the database-driven configuration system
3. **Customize**: Modify romance routes, NPCs, and game content through the admin interface

## 📚 Documentation

Our comprehensive documentation is organized for different audiences:

### 🎮 For Players & Game Masters
- **[Game Mechanics](docs/visual_novel.md)** - Understanding the game systems and narrative elements
- **[API Reference](docs/API_REFERENCE.md)** - Complete command reference and usage examples

### 🛠️ For Developers
- **[Architecture Overview](docs/ARCHITECTURE.md)** - System design, data flow, and technical architecture
- **[Development Guide](docs/DEVELOPMENT_GUIDE.md)** - Comprehensive patterns and implementation examples
- **[Quick Reference](docs/QUICK_REFERENCE.md)** - Copy-paste ready code templates and snippets
- **[Deployment Guide](docs/DEPLOYMENT.md)** - Production setup and configuration

### 📊 Project Status
- **[Implementation Status](docs/finalization_summary.md)** - Current feature completion and roadmap
- **[Migration Guide](docs/migration_guide.md)** - Upgrading from previous versions
- **[Security Policy](SECURITY.md)** - Security guidelines and vulnerability reporting

## 🏗️ Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Backend Framework** | Spring Boot 3.2+ | Application foundation and dependency injection |
| **Discord Integration** | Discord4J 3.2 | Reactive Discord bot functionality |
| **Database** | PostgreSQL + JPA/Hibernate | Data persistence and relationship management |
| **Build System** | Gradle | Dependency management and build automation |
| **Caching** | Caffeine | High-performance in-memory caching |
| **Configuration** | Spring Config + Database | Hot-reload configuration system |

## 🎨 Game Highlights

### 🏛️ Social Simulation
- **Club Management**: Create and manage clubs with unique activities and competitions
- **Relationship Dynamics**: Build complex relationships with NPCs and other players
- **Reputation System**: Multi-faceted reputation tracking across different factions

### � Narrative Experience
- **Branching Storylines**: Player choices influence story progression and available paths
- **Character Development**: Rich character backgrounds and development arcs
- **Seasonal Events**: Time-based content that changes with the game calendar

### 💼 Economic Systems
- **Dynamic Trading**: NPC preferences change based on relationships and story progress
- **Inventory Management**: Sophisticated item system with attributes and effects
- **Resource Management**: Balance time, energy, and social capital

## 🔧 Development Status

![Production Ready](https://img.shields.io/badge/Status-✅%20Production%20Ready-brightgreen)

**Current Version**: `0.0.1-SNAPSHOT`

✅ **Completed Systems**:
- Core gameplay mechanics with advanced features
- Complete database persistence layer with hot-reload configuration  
- Comprehensive Discord bot commands with error handling
- Advanced user progress tracking with analytics
- Full club and social systems with relationships
- Enterprise-grade error handling and logging
- Multi-layered caching and performance optimization
- Database-driven configuration with runtime reloading

📊 **Quality Metrics**:
- **Test Coverage**: Comprehensive unit and functional test suites
- **Documentation**: Complete developer and user documentation
- **Architecture**: Clean service-oriented design with proper separation of concerns
- **Security**: Vulnerability scanning and secure coding practices

## 🤝 Contributing

We welcome contributions from developers, game designers, and community members!

1. **Read**: Check our [Contributing Guidelines](CONTRIBUTING.md)
2. **Explore**: Review the [Development Guide](docs/DEVELOPMENT_GUIDE.md)
3. **Start**: Look for issues labeled `good first issue`
4. **Connect**: Join our community discussions

### 🧑‍💻 For Developers
- **Architecture**: Follow our established service-oriented patterns
- **Testing**: Maintain comprehensive test coverage
- **Documentation**: Update docs for any new features
- **Code Style**: Use provided `.cursorrules` for AI-assisted development

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 🛡️ Security

Security is a top priority. Please review our [Security Policy](SECURITY.md) and report vulnerabilities responsibly.

## 📞 Support & Community

- **� Bug Reports**: [Open an Issue](https://github.com/disparter/tokugawa-discord-game/issues)
- **💡 Feature Requests**: [Discussion Forum](https://github.com/disparter/tokugawa-discord-game/discussions)
- **📖 Documentation**: [Full Documentation](docs/)
- **🔧 Development**: [Contributing Guide](CONTRIBUTING.md)

---

<div align="center">

**🏯 Built with ❤️ for the Discord community**

*Experience feudal Japan like never before - right in your Discord server*

</div>
