// User and Authentication Types
export interface User {
  id: string;
  discordId: string;
  username: string;
  discriminator: string;
  avatar?: string;
  email?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// Game State Types
export interface Player {
  id: number;
  userId: string;
  name: string;
  level: number;
  exp: number;
  points: number;
  reputation: number;
  health: number;
  energy: number;
  knowledge: number;
  charm: number;
  strength: number;
  wisdom: number;
  luck: number;
  currentLocationId?: number;
  currentChapterId?: string;
  techniques: string[];
  achievements: string[];
  unlockedLocations: number[];
  createdAt: Date;
  updatedAt: Date;
}

export interface Chapter {
  id: number;
  chapterId: string;
  title: string;
  description: string;
  type: ChapterType;
  arcId: string;
  phase: number;
  completionExp: number;
  completionReward: number;
  choices: string[];
  isUnlocked: boolean;
  isCompleted: boolean;
  backgroundImage?: string;
  backgroundMusic?: string;
}

export const ChapterType = {
  MAIN: 'MAIN',
  SIDE: 'SIDE',
  ROMANCE: 'ROMANCE',
  CLUB: 'CLUB',
  EVENT: 'EVENT'
} as const;

export type ChapterType = typeof ChapterType[keyof typeof ChapterType];

export interface Progress {
  id: number;
  playerId: number;
  currentChapterId: string;
  completedChapters: string[];
  currentSceneId?: string;
  lastSaveDate: Date;
  totalPlayTime: number;
  choicesMade: Record<string, string>;
  flags: Record<string, boolean>;
  variables: Record<string, any>;
}

export interface Event {
  id: number;
  eventId: string;
  title: string;
  description: string;
  type: EventType;
  triggerConditions: string[];
  consequences: string[];
  isActive: boolean;
  startDate?: Date;
  endDate?: Date;
}

export const EventType = {
  STORY: 'STORY',
  ROMANCE: 'ROMANCE',
  CLUB: 'CLUB',
  SEASONAL: 'SEASONAL',
  SPECIAL: 'SPECIAL'
} as const;

export type EventType = typeof EventType[keyof typeof EventType];

export interface Consequence {
  id: number;
  playerId: number;
  chapterId: string;
  sceneId: string;
  choiceId: string;
  choiceMade: string;
  type: ConsequenceType;
  impact: string;
  description: string;
  timestamp: Date;
}

export const ConsequenceType = {
  RELATIONSHIP: 'RELATIONSHIP',
  REPUTATION: 'REPUTATION',
  STAT: 'STAT',
  ITEM: 'ITEM',
  STORY: 'STORY'
} as const;

export type ConsequenceType = typeof ConsequenceType[keyof typeof ConsequenceType];

export interface NPC {
  id: number;
  npcId: string;
  name: string;
  description: string;
  type: NPCType;
  locationId: number;
  personality: string[];
  interests: string[];
  dialogues: Record<string, string>;
  sprites: Record<string, string>;
  isAvailable: boolean;
}

export const NPCType = {
  STUDENT: 'STUDENT',
  TEACHER: 'TEACHER',
  MENTOR: 'MENTOR',
  RIVAL: 'RIVAL',
  ROMANCE: 'ROMANCE',
  VENDOR: 'VENDOR'
} as const;

export type NPCType = typeof NPCType[keyof typeof NPCType];

export interface Relationship {
  id: number;
  playerId: number;
  npcId: number;
  affinity: number;
  trust: number;
  romance: number;
  friendship: number;
  rivalry: number;
  status: RelationshipStatus;
  lastInteraction: Date;
  interactions: number;
  specialEvents: string[];
}

export const RelationshipStatus = {
  STRANGER: 'STRANGER',
  ACQUAINTANCE: 'ACQUAINTANCE',
  FRIEND: 'FRIEND',
  CLOSE_FRIEND: 'CLOSE_FRIEND',
  ROMANTIC_INTEREST: 'ROMANTIC_INTEREST',
  DATING: 'DATING',
  RIVAL: 'RIVAL',
  ENEMY: 'ENEMY'
} as const;

export type RelationshipStatus = typeof RelationshipStatus[keyof typeof RelationshipStatus];

export interface Item {
  id: number;
  itemId: string;
  name: string;
  description: string;
  type: ItemType;
  rarity: ItemRarity;
  value: number;
  effects: Record<string, number>;
  requirements: string[];
  isConsumable: boolean;
  maxStack: number;
  iconUrl?: string;
}

export const ItemType = {
  CONSUMABLE: 'CONSUMABLE',
  EQUIPMENT: 'EQUIPMENT',
  MATERIAL: 'MATERIAL',
  GIFT: 'GIFT',
  QUEST: 'QUEST',
  COLLECTIBLE: 'COLLECTIBLE'
} as const;

export type ItemType = typeof ItemType[keyof typeof ItemType];

export const ItemRarity = {
  COMMON: 'COMMON',
  UNCOMMON: 'UNCOMMON',
  RARE: 'RARE',
  EPIC: 'EPIC',
  LEGENDARY: 'LEGENDARY'
} as const;

export type ItemRarity = typeof ItemRarity[keyof typeof ItemRarity];

export interface Inventory {
  id: number;
  playerId: number;
  items: InventoryItem[];
  capacity: number;
  updatedAt: Date;
}

export interface InventoryItem {
  itemId: string;
  quantity: number;
  acquiredAt: Date;
}

export interface Location {
  id: number;
  locationId: string;
  name: string;
  description: string;
  type: LocationType;
  parentLocationId?: number;
  backgroundImage: string;
  backgroundMusic?: string;
  npcs: number[];
  items: string[];
  isUnlocked: boolean;
  discoveryRequirements: string[];
  timeOfDay: TimeOfDay[];
  season: Season[];
}

export const LocationType = {
  ACADEMY: 'ACADEMY',
  CLASSROOM: 'CLASSROOM',
  DORMITORY: 'DORMITORY',
  LIBRARY: 'LIBRARY',
  GARDEN: 'GARDEN',
  TRAINING: 'TRAINING',
  SPECIAL: 'SPECIAL'
} as const;

export type LocationType = typeof LocationType[keyof typeof LocationType];

export const TimeOfDay = {
  MORNING: 'MORNING',
  AFTERNOON: 'AFTERNOON',
  EVENING: 'EVENING',
  NIGHT: 'NIGHT'
} as const;

export type TimeOfDay = typeof TimeOfDay[keyof typeof TimeOfDay];

export const Season = {
  SPRING: 'SPRING',
  SUMMER: 'SUMMER',
  AUTUMN: 'AUTUMN',
  WINTER: 'WINTER'
} as const;

export type Season = typeof Season[keyof typeof Season];

// UI State Types
export interface GameUIState {
  isMenuOpen: boolean;
  currentDialog: Dialog | null;
  showChoices: boolean;
  isLoading: boolean;
  currentScene: Scene | null;
  showInventory: boolean;
  showSettings: boolean;
  showSaveLoad: boolean;
  showGallery: boolean;
  isFullscreen: boolean;
  autoPlay: boolean;
  textSpeed: number;
  musicVolume: number;
  sfxVolume: number;
}

export interface Dialog {
  id: string;
  speaker: string;
  text: string;
  emotion?: string;
  voiceFile?: string;
  choices?: Choice[];
  effects?: DialogEffect[];
}

export interface Choice {
  id: string;
  text: string;
  condition?: string;
  consequences?: string[];
  nextDialog?: string;
  nextScene?: string;
}

export interface DialogEffect {
  type: 'fade' | 'shake' | 'glow' | 'typewriter';
  duration: number;
  intensity?: number;
}

export interface Scene {
  id: string;
  chapterId: string;
  title: string;
  description: string;
  backgroundImage: string;
  backgroundMusic?: string;
  characters: SceneCharacter[];
  dialogs: Dialog[];
  effects: SceneEffect[];
  choices?: Choice[];
  nextScene?: string;
}

export interface SceneCharacter {
  npcId: string;
  position: 'left' | 'center' | 'right';
  sprite: string;
  emotion: string;
  scale: number;
  opacity: number;
  effects: CharacterEffect[];
}

export interface CharacterEffect {
  type: 'fade' | 'slide' | 'bounce' | 'glow';
  duration: number;
  delay?: number;
}

export interface SceneEffect {
  type: 'particle' | 'weather' | 'lighting' | 'transition';
  config: Record<string, any>;
}

// Save System Types
export interface SaveSlot {
  id: string;
  name: string;
  thumbnail?: string;
  progress: Progress;
  timestamp: Date;
  chapterTitle: string;
  sceneTitle: string;
  playTime: number;
  isEmpty: boolean;
}

export interface GameSettings {
  textSpeed: number;
  autoPlay: boolean;
  autoPlaySpeed: number;
  musicVolume: number;
  sfxVolume: number;
  voiceVolume: number;
  fullscreen: boolean;
  skipUnread: boolean;
  skipChoices: boolean;
  language: string;
  theme: string;
  animations: boolean;
  particles: boolean;
  showStats: boolean;
  showTutorial: boolean;
}

// Gallery Types
export interface GalleryItem {
  id: string;
  type: 'cg' | 'character' | 'background' | 'music';
  title: string;
  description: string;
  url: string;
  thumbnail?: string;
  isUnlocked: boolean;
  unlockCondition: string;
  unlockDate?: Date;
  tags: string[];
  category: string;
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  type: AchievementType;
  condition: string;
  reward: string;
  isUnlocked: boolean;
  unlockDate?: Date;
  progress?: number;
  maxProgress?: number;
}

export const AchievementType = {
  STORY: 'STORY',
  ROMANCE: 'ROMANCE',
  COLLECTION: 'COLLECTION',
  SOCIAL: 'SOCIAL',
  SKILL: 'SKILL',
  SPECIAL: 'SPECIAL'
} as const;

export type AchievementType = typeof AchievementType[keyof typeof AchievementType];

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  hasNext: boolean;
  hasPrev: boolean;
}

// Discord OAuth Types
export interface DiscordOAuthConfig {
  clientId: string;
  redirectUri: string;
  scopes: string[];
}

export interface DiscordTokenResponse {
  access_token: string;
  token_type: string;
  expires_in: number;
  refresh_token: string;
  scope: string;
}

export interface DiscordUser {
  id: string;
  username: string;
  discriminator: string;
  avatar?: string;
  bot?: boolean;
  system?: boolean;
  mfa_enabled?: boolean;
  verified?: boolean;
  email?: string;
  flags?: number;
  premium_type?: number;
  public_flags?: number;
}

// Audio Types
export interface AudioTrack {
  id: string;
  name: string;
  url: string;
  type: 'bgm' | 'sfx' | 'voice';
  volume: number;
  loop: boolean;
  fadeIn?: number;
  fadeOut?: number;
}

export interface AudioState {
  currentBGM: AudioTrack | null;
  isPlaying: boolean;
  volume: {
    master: number;
    bgm: number;
    sfx: number;
    voice: number;
  };
  muted: boolean;
}

// Animation Types
export interface AnimationConfig {
  type: 'fade' | 'slide' | 'scale' | 'rotate' | 'bounce';
  duration: number;
  delay?: number;
  easing?: string;
  repeat?: number;
  yoyo?: boolean;
}

export interface TransitionConfig {
  type: 'fade' | 'slide' | 'wipe' | 'dissolve';
  duration: number;
  direction?: 'left' | 'right' | 'up' | 'down';
  easing?: string;
}

// Error Types
export interface GameError {
  code: string;
  message: string;
  details?: any;
  timestamp: Date;
}

export interface ValidationError {
  field: string;
  message: string;
  value?: any;
}

// Utility Types
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

export type Optional<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

export type RequiredFields<T, K extends keyof T> = T & Required<Pick<T, K>>;

export type EventHandler<T = any> = (event: T) => void;

export type AsyncEventHandler<T = any> = (event: T) => Promise<void>;

export type Nullable<T> = T | null;