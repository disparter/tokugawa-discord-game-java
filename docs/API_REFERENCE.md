# 📚 API Reference

This document provides comprehensive reference for the Tokugawa Discord Game APIs.

## 🌐 REST API Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
All API endpoints require Discord OAuth2 authentication or bot token authentication.

### Common Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Success message",
  "timestamp": "2024-12-01T12:00:00Z"
}
```

## 👤 Player API

### Get Player Information
```http
GET /api/v1/players/{playerId}
```

**Parameters:**
- `playerId` (path) - The Discord user ID

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 123456789,
    "discordId": "123456789012345678",
    "username": "player123",
    "level": 15,
    "experience": 2400,
    "stats": {
      "strength": 12,
      "intelligence": 15,
      "charisma": 10,
      "luck": 8
    },
    "reputation": {
      "tokugawa": 75,
      "merchants": 60,
      "samurai": 45
    },
    "currentLocation": "edo_castle",
    "joinedAt": "2024-01-15T10:30:00Z"
  }
}
```

### Update Player Stats
```http
PUT /api/v1/players/{playerId}/stats
```

**Request Body:**
```json
{
  "strength": 13,
  "intelligence": 16,
  "charisma": 11,
  "luck": 9
}
```

### Get Player Achievements
```http
GET /api/v1/players/{playerId}/achievements
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "first_trade",
      "name": "First Trade",
      "description": "Complete your first trade",
      "unlockedAt": "2024-01-16T14:20:00Z",
      "rarity": "common"
    }
  ]
}
```

## 🎒 Inventory API

### Get Player Inventory
```http
GET /api/v1/players/{playerId}/inventory
```

**Query Parameters:**
- `category` (optional) - Filter by item category
- `limit` (optional) - Number of items to return (default: 50)
- `offset` (optional) - Pagination offset

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "silk_cloth",
        "name": "Fine Silk Cloth",
        "description": "High-quality silk from the best merchants",
        "category": "trade_goods",
        "quantity": 3,
        "value": 150,
        "rarity": "uncommon"
      }
    ],
    "totalValue": 450,
    "totalItems": 1
  }
}
```

### Add Item to Inventory
```http
POST /api/v1/players/{playerId}/inventory
```

**Request Body:**
```json
{
  "itemId": "rice_bundle",
  "quantity": 5
}
```

## 🏯 Club API

### List All Clubs
```http
GET /api/v1/clubs
```

**Query Parameters:**
- `status` (optional) - Filter by club status (active, recruiting, closed)
- `limit` (optional) - Number of clubs to return
- `search` (optional) - Search by club name

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Merchant Alliance",
      "description": "A guild focused on trading and economic growth",
      "memberCount": 25,
      "maxMembers": 50,
      "status": "recruiting",
      "createdAt": "2024-01-01T00:00:00Z",
      "leader": {
        "discordId": "123456789012345678",
        "username": "guild_master"
      }
    }
  ]
}
```

### Get Club Details
```http
GET /api/v1/clubs/{clubId}
```

### Join Club
```http
POST /api/v1/clubs/{clubId}/members
```

**Request Body:**
```json
{
  "playerId": "123456789012345678"
}
```

## 🗾 Location API

### Get All Locations
```http
GET /api/v1/locations
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "edo_castle",
      "name": "Edo Castle",
      "description": "The seat of power in Edo",
      "type": "castle",
      "isUnlocked": true,
      "requirements": {
        "level": 1,
        "items": [],
        "reputation": {}
      },
      "npcs": [
        {
          "id": "castle_guard",
          "name": "Castle Guard",
          "type": "guard"
        }
      ]
    }
  ]
}
```

### Check Location Requirements
```http
GET /api/v1/locations/{locationId}/requirements/{playerId}
```

## 💕 Romance API

### Get Romance Routes
```http
GET /api/v1/romance/routes
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "hanako_route",
      "npcId": "hanako",
      "npcName": "Hanako",
      "currentStage": "introduction",
      "maxStage": "marriage",
      "requirements": {
        "level": 5,
        "charisma": 10,
        "items": ["flower_bouquet"]
      },
      "isAvailable": true
    }
  ]
}
```

### Progress Romance Route
```http
POST /api/v1/romance/routes/{routeId}/progress
```

**Request Body:**
```json
{
  "playerId": "123456789012345678",
  "choice": "give_gift"
}
```

## 🏪 Trading API

### Get NPC Trading Preferences
```http
GET /api/v1/trading/npcs/{npcId}/preferences
```

**Response:**
```json
{
  "success": true,
  "data": {
    "npcId": "merchant_tanaka",
    "name": "Merchant Tanaka",
    "preferences": {
      "loved": ["silk_cloth", "tea_leaves"],
      "liked": ["rice_bundle", "cotton_cloth"],
      "neutral": ["wooden_tools"],
      "disliked": ["weapons", "sake"]
    },
    "currentMood": "happy",
    "relationshipLevel": 3
  }
}
```

### Execute Trade
```http
POST /api/v1/trading/execute
```

**Request Body:**
```json
{
  "playerId": "123456789012345678",
  "npcId": "merchant_tanaka",
  "offeredItems": [
    {
      "itemId": "silk_cloth",
      "quantity": 2
    }
  ],
  "requestedItems": [
    {
      "itemId": "gold_coins",
      "quantity": 300
    }
  ]
}
```

## 🤖 Discord Bot Commands

### Player Commands

#### `/player register`
Register a new player account.

**Parameters:** None

**Response:** Confirmation message with initial stats

#### `/player stats`
Display current player statistics.

**Parameters:** None

**Response:** Embed with player stats, level, and reputation

#### `/player profile [user]`
Display player profile (own or another user's).

**Parameters:**
- `user` (optional) - Discord user to view

**Response:** Detailed player profile embed

### Inventory Commands

#### `/inventory list [category]`
List items in your inventory.

**Parameters:**
- `category` (optional) - Filter by item category

**Response:** Paginated list of inventory items

#### `/inventory use <item> [quantity]`
Use an item from your inventory.

**Parameters:**
- `item` (required) - Item name or ID
- `quantity` (optional) - Number of items to use (default: 1)

### Club Commands

#### `/club list [status]`
List available clubs.

**Parameters:**
- `status` (optional) - Filter by club status

**Response:** List of clubs with member counts and status

#### `/club join <club_name>`
Join a club.

**Parameters:**
- `club_name` (required) - Name of the club to join

#### `/club leave`
Leave your current club.

**Parameters:** None

#### `/club create <name> <description>`
Create a new club.

**Parameters:**
- `name` (required) - Club name
- `description` (required) - Club description

### Location Commands

#### `/location list [type]`
List available locations.

**Parameters:**
- `type` (optional) - Filter by location type

**Response:** List of locations with unlock status

#### `/location travel <location>`
Travel to a location.

**Parameters:**
- `location` (required) - Location name or ID

#### `/location explore`
Explore your current location.

**Parameters:** None

**Response:** Random events, NPC encounters, or item discoveries

### Trading Commands

#### `/trade npc <npc_name>`
Start trading with an NPC.

**Parameters:**
- `npc_name` (required) - NPC name or ID

**Response:** Interactive trading interface

#### `/trade offer <item> <quantity> <npc>`
Make a trade offer to an NPC.

**Parameters:**
- `item` (required) - Item to offer
- `quantity` (required) - Quantity to offer
- `npc` (required) - Target NPC

### Romance Commands

#### `/romance list`
List available romance routes.

**Parameters:** None

**Response:** List of NPCs and romance progress

#### `/romance progress <npc>`
Check romance progress with an NPC.

**Parameters:**
- `npc` (required) - NPC name

**Response:** Current romance stage and requirements

## 🔧 Configuration API

### Get Game Configuration
```http
GET /api/v1/config
```

**Response:**
```json
{
  "success": true,
  "data": {
    "gameVersion": "1.0.0",
    "features": {
      "trading": true,
      "romance": true,
      "clubs": true,
      "pvp": false
    },
    "limits": {
      "maxInventorySize": 100,
      "maxClubMembers": 50,
      "dailyTrades": 10
    },
    "seasonalEvents": {
      "current": "spring_festival",
      "endDate": "2024-04-30T23:59:59Z"
    }
  }
}
```

### Update Configuration (Admin)
```http
PUT /api/v1/config
```

**Request Body:**
```json
{
  "features": {
    "trading": true,
    "romance": true,
    "clubs": true,
    "pvp": true
  },
  "limits": {
    "maxInventorySize": 150,
    "maxClubMembers": 75,
    "dailyTrades": 15
  }
}
```

## 📊 Analytics API

### Get Player Statistics
```http
GET /api/v1/analytics/players
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalPlayers": 1250,
    "activePlayers": 890,
    "averageLevel": 12.5,
    "topPlayersByLevel": [
      {
        "discordId": "123456789012345678",
        "username": "top_player",
        "level": 45
      }
    ]
  }
}
```

### Get Trade Statistics
```http
GET /api/v1/analytics/trades
```

## 🔐 Admin API

### Get All Players (Admin)
```http
GET /api/v1/admin/players
```

### Ban Player (Admin)
```http
POST /api/v1/admin/players/{playerId}/ban
```

**Request Body:**
```json
{
  "reason": "Violation of terms of service",
  "duration": "7d"
}
```

### System Health Check
```http
GET /api/v1/admin/health
```

## 📋 Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid parameters |
| 401 | Unauthorized - Invalid authentication |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error - Server error |

## 🔗 WebSocket Events

### Connection
```
wss://localhost:8080/ws/game
```

### Event Types

#### Player Updates
```json
{
  "type": "player_update",
  "data": {
    "playerId": "123456789012345678",
    "field": "level",
    "oldValue": 14,
    "newValue": 15
  }
}
```

#### Club Events
```json
{
  "type": "club_event",
  "data": {
    "clubId": 1,
    "event": "member_joined",
    "playerId": "123456789012345678"
  }
}
```

#### Trading Events
```json
{
  "type": "trade_completed",
  "data": {
    "playerId": "123456789012345678",
    "npcId": "merchant_tanaka",
    "items": ["silk_cloth"],
    "value": 300
  }
}
```

## 📚 SDK Examples

### Java SDK
```java
// Initialize the client
TokugawaGameClient client = new TokugawaGameClient("your-api-key");

// Get player information
Player player = client.getPlayer("123456789012345678");

// Execute a trade
TradeResult result = client.executeTrade(
    "123456789012345678", 
    "merchant_tanaka", 
    Arrays.asList(new TradeItem("silk_cloth", 2)),
    Arrays.asList(new TradeItem("gold_coins", 300))
);
```

### JavaScript SDK
```javascript
// Initialize the client
const client = new TokugawaGameClient('your-api-key');

// Get player information
const player = await client.getPlayer('123456789012345678');

// Execute a trade
const result = await client.executeTrade({
  playerId: '123456789012345678',
  npcId: 'merchant_tanaka',
  offeredItems: [{ itemId: 'silk_cloth', quantity: 2 }],
  requestedItems: [{ itemId: 'gold_coins', quantity: 300 }]
});
```

---

## 📞 Support

For API support:
- **Documentation**: [docs/](../docs/)
- **Issues**: [GitHub Issues](https://github.com/yourusername/tokugawa-discord-game/issues)
- **Discord**: [Community Server](https://discord.gg/your-invite)

**Last Updated**: December 2024