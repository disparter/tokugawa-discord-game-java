# 🎮 Tokugawa Visual Novel Interface

A modern, beautiful standalone visual novel web interface built with React, TypeScript, and cutting-edge web technologies. This interface connects to the Tokugawa Discord Game Spring Boot API to provide an immersive visual novel experience.

## ✨ Features

- **🎨 Modern UI/UX**: Beautiful glass-morphism design with smooth animations
- **🔐 Discord OAuth2**: Seamless authentication with Discord integration
- **🎯 Visual Novel Engine**: Complete story progression with choices and consequences
- **🎵 Audio System**: Background music and sound effects support
- **💾 Save System**: Multiple save slots with screenshots
- **🖼️ Gallery**: Unlock and view artwork, characters, and memories
- **⚙️ Settings**: Comprehensive game configuration options
- **📱 Responsive**: Works perfectly on desktop, tablet, and mobile devices
- **🌐 PWA Ready**: Installable as a Progressive Web App

## 🛠️ Technology Stack

### Core Technologies
- **React 18** - Modern React with hooks and concurrent features
- **TypeScript** - Type-safe development
- **Vite** - Lightning-fast build tool and dev server
- **Material-UI (MUI)** - Beautiful React components
- **Tailwind CSS** - Utility-first CSS framework

### State Management & Data
- **Zustand** - Lightweight state management
- **React Query** - Server state management and caching
- **Axios** - HTTP client for API communication

### Animations & Effects
- **Framer Motion** - Smooth animations and transitions
- **PixiJS** - Advanced 2D graphics and effects

### Audio & Media
- **Howler.js** - Cross-browser audio library
- **React Router** - Client-side routing

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ 
- npm or yarn
- Discord Application (for OAuth2)

### Installation

1. **Clone and navigate to the project**
   ```bash
   cd src/js/tokugawa-visual-novel
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Set up environment variables**
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` with your configuration:
   ```env
   VITE_API_BASE_URL=http://localhost:8080
   VITE_DISCORD_CLIENT_ID=your_discord_client_id
   VITE_DISCORD_CLIENT_SECRET=your_discord_client_secret
   VITE_DISCORD_REDIRECT_URI=http://localhost:5173/auth/callback
   ```

4. **Start the development server**
   ```bash
   npm run dev
   ```

5. **Open your browser**
   Navigate to `http://localhost:5173`

### Building for Production

```bash
# Build the project
npm run build

# Preview the build
npm run preview
```

## 🎯 Project Structure

```
src/
├── components/           # Reusable UI components
│   ├── common/          # Common components (buttons, modals, etc.)
│   ├── game/            # Game-specific components
│   ├── ui/              # Interface components
│   └── layout/          # Layout components
├── pages/               # Main application pages
│   ├── Login.tsx        # Discord OAuth login
│   ├── Dashboard.tsx    # Player dashboard
│   ├── GameView.tsx     # Main visual novel interface
│   ├── Gallery.tsx      # CG and asset gallery
│   └── Settings.tsx     # Game settings
├── services/            # API services and utilities
│   ├── api/             # API client and endpoints
│   ├── auth/            # Authentication services
│   └── game/            # Game logic services
├── stores/              # Zustand state stores
├── hooks/               # Custom React hooks
├── utils/               # Utility functions
├── types/               # TypeScript type definitions
└── assets/              # Static assets
```

## 🔧 Configuration

### Discord OAuth2 Setup

1. **Create a Discord Application**
   - Go to [Discord Developer Portal](https://discord.com/developers/applications)
   - Create a new application
   - Note your Client ID and Client Secret

2. **Configure OAuth2**
   - Go to OAuth2 → General
   - Add redirect URI: `http://localhost:5173/auth/callback`
   - Select scopes: `identify`, `email`

3. **Update Environment Variables**
   ```env
   VITE_DISCORD_CLIENT_ID=your_client_id_here
   VITE_DISCORD_CLIENT_SECRET=your_client_secret_here
   ```

### API Integration

The interface connects to the Spring Boot backend API:

- **Base URL**: `http://localhost:8080` (configurable)
- **Authentication**: Bearer token from Discord OAuth2
- **Endpoints**: 
  - `/api/auth/*` - Authentication
  - `/api/story/*` - Story and chapters
  - `/api/decisions/*` - Decision tracking
  - `/api/player/*` - Player data

## 🎨 Design System

### Color Palette
- **Primary**: Deep purple (#8b5cf6) with gold accents (#f59e0b)
- **Background**: Dark gradient with subtle patterns
- **Text**: High contrast white/cream on dark backgrounds

### Typography
- **Headers**: Noto Serif JP (Japanese-inspired serif)
- **Body**: Noto Sans JP (clean sans-serif)
- **UI**: Roboto (Material Design standard)

### Animations
- **Page Transitions**: Smooth fade and slide effects
- **UI Elements**: Hover states and micro-interactions
- **Text**: Typewriter effect for dialog
- **Particles**: Ambient background effects

## 🎮 Game Features

### Visual Novel Engine
- **Story Progression**: Chapter-based narrative structure
- **Choice System**: Meaningful decisions with consequences
- **Character Interactions**: Relationship building mechanics
- **Save System**: Multiple save slots with progress tracking

### Audio System
- **Background Music**: Atmospheric soundtracks
- **Sound Effects**: Interactive audio feedback
- **Voice Acting**: Character voice support (planned)
- **Volume Controls**: Separate audio channels

### Gallery System
- **CG Gallery**: Unlockable artwork and scenes
- **Character Gallery**: NPC profiles and relationships
- **Achievement System**: Progress milestones and rewards
- **Music Room**: Soundtrack collection

## 📱 Responsive Design

The interface is fully responsive and works on:
- **Desktop**: Full-featured experience (1920x1080+)
- **Tablet**: Touch-optimized interface (768px+)
- **Mobile**: Streamlined mobile experience (320px+)

## 🔒 Security

- **OAuth2 Flow**: Secure Discord authentication
- **Token Management**: Automatic token refresh
- **API Security**: Request/response validation
- **Data Protection**: Local storage encryption

## 🚀 Performance

- **Code Splitting**: Lazy loading for optimal performance
- **Asset Optimization**: Image compression and WebP support
- **Caching**: Intelligent API response caching
- **Bundle Size**: Optimized production builds

## 🧪 Development

### Available Scripts

```bash
# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Type checking
npm run type-check

# Linting
npm run lint

# Format code
npm run format
```

### Development Guidelines

1. **TypeScript**: Use strict typing for all components
2. **Components**: Follow atomic design principles
3. **State**: Use Zustand for global state, useState for local
4. **Styling**: Combine Tailwind classes with MUI components
5. **Testing**: Write tests for critical functionality

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Boot Backend**: Integration with existing game API
- **Discord**: OAuth2 authentication platform
- **Material-UI**: Beautiful React components
- **Framer Motion**: Smooth animations
- **Tokugawa Academy**: Game world and narrative

---

**Ready to enter the world of Tokugawa Academy? Start your visual novel journey today!** 🎌✨
