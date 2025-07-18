import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { 
  Player, 
  Progress, 
  Chapter, 
  Scene, 
  Dialog, 
  Choice, 
  GameSettings,
  SaveSlot,
  Inventory,
  Relationship 
} from '../types';

interface GameState {
  // Player Data
  player: Player | null;
  progress: Progress | null;
  inventory: Inventory | null;
  relationships: Relationship[];
  
  // Game State
  currentChapter: Chapter | null;
  currentScene: Scene | null;
  currentDialog: Dialog | null;
  availableChoices: Choice[];
  
  // UI State
  isLoading: boolean;
  isPlaying: boolean;
  isPaused: boolean;
  showMenu: boolean;
  showInventory: boolean;
  showSettings: boolean;
  showSaveLoad: boolean;
  showGallery: boolean;
  
  // Settings
  settings: GameSettings;
  
  // Save System
  saveSlots: SaveSlot[];
  currentSaveSlot: string | null;
  
  // Error State
  error: string | null;
}

interface GameStore extends GameState {
  // Player Actions
  setPlayer: (player: Player) => void;
  updatePlayer: (updates: Partial<Player>) => void;
  setProgress: (progress: Progress) => void;
  updateProgress: (updates: Partial<Progress>) => void;
  setInventory: (inventory: Inventory) => void;
  setRelationships: (relationships: Relationship[]) => void;
  updateRelationship: (npcId: number, updates: Partial<Relationship>) => void;
  
  // Game Flow
  setCurrentChapter: (chapter: Chapter) => void;
  setCurrentScene: (scene: Scene) => void;
  setCurrentDialog: (dialog: Dialog) => void;
  setAvailableChoices: (choices: Choice[]) => void;
  makeChoice: (choiceId: string) => void;
  nextDialog: () => void;
  previousDialog: () => void;
  
  // UI Actions
  setLoading: (loading: boolean) => void;
  setPlaying: (playing: boolean) => void;
  setPaused: (paused: boolean) => void;
  toggleMenu: () => void;
  toggleInventory: () => void;
  toggleSettings: () => void;
  toggleSaveLoad: () => void;
  toggleGallery: () => void;
  closeAllModals: () => void;
  
  // Settings
  updateSettings: (updates: Partial<GameSettings>) => void;
  resetSettings: () => void;
  
  // Save System
  setSaveSlots: (slots: SaveSlot[]) => void;
  setCurrentSaveSlot: (slotId: string | null) => void;
  createSaveSlot: (name: string) => void;
  deleteSaveSlot: (slotId: string) => void;
  
  // Error Handling
  setError: (error: string | null) => void;
  clearError: () => void;
  
  // Game Actions
  startGame: () => void;
  pauseGame: () => void;
  resumeGame: () => void;
  resetGame: () => void;
}

const defaultSettings: GameSettings = {
  textSpeed: 50,
  autoPlay: false,
  autoPlaySpeed: 2000,
  musicVolume: 0.7,
  sfxVolume: 0.8,
  voiceVolume: 0.9,
  fullscreen: false,
  skipUnread: false,
  skipChoices: false,
  language: 'en',
  theme: 'dark',
  animations: true,
  particles: true,
  showStats: true,
  showTutorial: true,
};

export const useGameStore = create<GameStore>()(
  persist(
    (set, get) => ({
      // Initial State
      player: null,
      progress: null,
      inventory: null,
      relationships: [],
      currentChapter: null,
      currentScene: null,
      currentDialog: null,
      availableChoices: [],
      isLoading: false,
      isPlaying: false,
      isPaused: false,
      showMenu: false,
      showInventory: false,
      showSettings: false,
      showSaveLoad: false,
      showGallery: false,
      settings: defaultSettings,
      saveSlots: [],
      currentSaveSlot: null,
      error: null,

      // Player Actions
      setPlayer: (player: Player) =>
        set({ player }),

      updatePlayer: (updates: Partial<Player>) =>
        set((state) => ({
          player: state.player ? { ...state.player, ...updates } : null,
        })),

      setProgress: (progress: Progress) =>
        set({ progress }),

      updateProgress: (updates: Partial<Progress>) =>
        set((state) => ({
          progress: state.progress ? { ...state.progress, ...updates } : null,
        })),

      setInventory: (inventory: Inventory) =>
        set({ inventory }),

      setRelationships: (relationships: Relationship[]) =>
        set({ relationships }),

      updateRelationship: (npcId: number, updates: Partial<Relationship>) =>
        set((state) => ({
          relationships: state.relationships.map((rel) =>
            rel.npcId === npcId ? { ...rel, ...updates } : rel
          ),
        })),

      // Game Flow
      setCurrentChapter: (chapter: Chapter) =>
        set({ currentChapter: chapter }),

      setCurrentScene: (scene: Scene) =>
        set({ currentScene: scene }),

      setCurrentDialog: (dialog: Dialog) =>
        set({ currentDialog: dialog }),

      setAvailableChoices: (choices: Choice[]) =>
        set({ availableChoices: choices }),

      makeChoice: (choiceId: string) => {
        const state = get();
        const choice = state.availableChoices.find(c => c.id === choiceId);
        if (choice && state.progress) {
          // Update progress with choice
          const updatedProgress = {
            ...state.progress,
            choicesMade: {
              ...state.progress.choicesMade,
              [state.currentScene?.id || 'unknown']: choiceId,
            },
          };
          set({ progress: updatedProgress });
        }
      },

      nextDialog: () => {
        // Implementation for advancing to next dialog
        // This would be handled by the game engine
      },

      previousDialog: () => {
        // Implementation for going back to previous dialog
        // This would be handled by the game engine
      },

      // UI Actions
      setLoading: (loading: boolean) =>
        set({ isLoading: loading }),

      setPlaying: (playing: boolean) =>
        set({ isPlaying: playing }),

      setPaused: (paused: boolean) =>
        set({ isPaused: paused }),

      toggleMenu: () =>
        set((state) => ({ showMenu: !state.showMenu })),

      toggleInventory: () =>
        set((state) => ({ showInventory: !state.showInventory })),

      toggleSettings: () =>
        set((state) => ({ showSettings: !state.showSettings })),

      toggleSaveLoad: () =>
        set((state) => ({ showSaveLoad: !state.showSaveLoad })),

      toggleGallery: () =>
        set((state) => ({ showGallery: !state.showGallery })),

      closeAllModals: () =>
        set({
          showMenu: false,
          showInventory: false,
          showSettings: false,
          showSaveLoad: false,
          showGallery: false,
        }),

      // Settings
      updateSettings: (updates: Partial<GameSettings>) =>
        set((state) => ({
          settings: { ...state.settings, ...updates },
        })),

      resetSettings: () =>
        set({ settings: defaultSettings }),

      // Save System
      setSaveSlots: (slots: SaveSlot[]) =>
        set({ saveSlots: slots }),

      setCurrentSaveSlot: (slotId: string | null) =>
        set({ currentSaveSlot: slotId }),

      createSaveSlot: (name: string) => {
        const state = get();
        if (state.progress && state.currentChapter) {
          const newSlot: SaveSlot = {
            id: Date.now().toString(),
            name,
            progress: state.progress,
            timestamp: new Date(),
            chapterTitle: state.currentChapter.title,
            sceneTitle: state.currentScene?.title || 'Unknown Scene',
            playTime: state.progress.totalPlayTime,
            isEmpty: false,
          };
          set((state) => ({
            saveSlots: [...state.saveSlots, newSlot],
          }));
        }
      },

      deleteSaveSlot: (slotId: string) =>
        set((state) => ({
          saveSlots: state.saveSlots.filter(slot => slot.id !== slotId),
        })),

      // Error Handling
      setError: (error: string | null) =>
        set({ error }),

      clearError: () =>
        set({ error: null }),

      // Game Actions
      startGame: () =>
        set({ isPlaying: true, isPaused: false, showMenu: false }),

      pauseGame: () =>
        set({ isPaused: true }),

      resumeGame: () =>
        set({ isPaused: false }),

      resetGame: () =>
        set({
          player: null,
          progress: null,
          inventory: null,
          relationships: [],
          currentChapter: null,
          currentScene: null,
          currentDialog: null,
          availableChoices: [],
          isPlaying: false,
          isPaused: false,
          showMenu: false,
          showInventory: false,
          showSettings: false,
          showSaveLoad: false,
          showGallery: false,
          currentSaveSlot: null,
          error: null,
        }),
    }),
    {
      name: 'tokugawa-game',
      partialize: (state) => ({
        settings: state.settings,
        saveSlots: state.saveSlots,
        currentSaveSlot: state.currentSaveSlot,
      }),
    }
  )
);