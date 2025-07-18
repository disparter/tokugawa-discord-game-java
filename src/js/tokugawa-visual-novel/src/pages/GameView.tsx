import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  LinearProgress,
  IconButton,
  Fade,
  Slide,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import {
  VolumeUp,
  VolumeOff,
  Settings,
  Save,
  Menu,
  SkipNext,
  Pause,
  PlayArrow,
  Fullscreen,
  FullscreenExit,
} from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { useGameStore } from '../store/gameStore';
import { apiClient } from '../services/apiClient';
import type { Chapter, Event, Choice, Scene, Dialog } from '../types';

interface GameState {
  currentChapter: Chapter | null;
  currentEvent: Event | null;
  currentDialog: Dialog | null;
  dialogIndex: number;
  choices: Choice[];
  scene: Scene | null;
  isAutoPlay: boolean;
  isMenuOpen: boolean;
  isFullscreen: boolean;
  isMuted: boolean;
}

const GameView: React.FC = () => {
  const navigate = useNavigate();
  const { progress, settings, saveGame, loadGame } = useGameStore();
  const [gameState, setGameState] = useState<GameState>({
    currentChapter: null,
    currentEvent: null,
    currentDialog: null,
    dialogIndex: 0,
    choices: [],
    scene: null,
    isAutoPlay: false,
    isMenuOpen: false,
    isFullscreen: false,
    isMuted: false,
  });
  const [loading, setLoading] = useState(true);
  const [typewriterText, setTypewriterText] = useState('');
  const [showChoices, setShowChoices] = useState(false);
  const audioRef = useRef<HTMLAudioElement>(null);

  useEffect(() => {
    const initializeGame = async () => {
      try {
        setLoading(true);
        
        // Load current chapter or start from beginning
        const chapterResponse = await apiClient.get('/api/story/current-chapter');
        if (chapterResponse.data.success) {
          setGameState(prev => ({
            ...prev,
            currentChapter: chapterResponse.data.data
          }));
        }

        // Load current event
        const eventResponse = await apiClient.get('/api/story/current-event');
        if (eventResponse.data.success) {
          setGameState(prev => ({
            ...prev,
            currentEvent: eventResponse.data.data
          }));
        }
      } catch (error) {
        console.error('Error initializing game:', error);
      } finally {
        setLoading(false);
      }
    };

    initializeGame();
  }, []);

  // Typewriter effect for dialog text
  useEffect(() => {
    if (gameState.currentDialog?.text) {
      setTypewriterText('');
      let index = 0;
      const timer = setInterval(() => {
        if (index < gameState.currentDialog.text.length) {
          setTypewriterText(prev => prev + gameState.currentDialog.text[index]);
          index++;
        } else {
          clearInterval(timer);
          if (gameState.currentDialog.choices?.length > 0) {
            setTimeout(() => setShowChoices(true), 500);
          }
        }
      }, 50);

      return () => clearInterval(timer);
    }
  }, [gameState.currentDialog]);

  const handleNextDialog = async () => {
    if (gameState.currentDialog?.choices?.length > 0 && !showChoices) {
      setShowChoices(true);
      return;
    }

    try {
      const response = await apiClient.post('/api/story/next-dialog');
      if (response.data.success) {
        const nextDialog = response.data.data;
        setGameState(prev => ({
          ...prev,
          currentDialog: nextDialog,
          dialogIndex: prev.dialogIndex + 1
        }));
        setShowChoices(false);
      }
    } catch (error) {
      console.error('Error advancing dialog:', error);
    }
  };

  const handleChoice = async (choice: Choice) => {
    try {
      const response = await apiClient.post('/api/story/make-choice', {
        choiceId: choice.id,
        eventId: gameState.currentEvent?.id
      });
      
      if (response.data.success) {
        setShowChoices(false);
        // Process choice consequences
        setTimeout(() => handleNextDialog(), 1000);
      }
    } catch (error) {
      console.error('Error making choice:', error);
    }
  };

  const toggleAutoPlay = () => {
    setGameState(prev => ({
      ...prev,
      isAutoPlay: !prev.isAutoPlay
    }));
  };

  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
    setGameState(prev => ({
      ...prev,
      isFullscreen: !prev.isFullscreen
    }));
  };

  const toggleMute = () => {
    setGameState(prev => ({
      ...prev,
      isMuted: !prev.isMuted
    }));
    if (audioRef.current) {
      audioRef.current.muted = !gameState.isMuted;
    }
  };

  const handleSaveGame = async () => {
    try {
      await saveGame();
      // Show save confirmation
    } catch (error) {
      console.error('Error saving game:', error);
    }
  };

  const handleLoadGame = async () => {
    try {
      await loadGame();
      // Reload game state
    } catch (error) {
      console.error('Error loading game:', error);
    }
  };

  const handleMenuToggle = () => {
    setGameState(prev => ({
      ...prev,
      isMenuOpen: !prev.isMenuOpen
    }));
  };

  const handleReturnToDashboard = () => {
    navigate('/dashboard');
  };

  if (loading) {
    return (
      <Box className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 flex items-center justify-center">
        <Box className="text-center">
          <LinearProgress className="w-64 mb-4" />
          <Typography variant="h6" className="text-white">
            Loading Chapter...
          </Typography>
        </Box>
      </Box>
    );
  }

  return (
    <Box className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 relative overflow-hidden">
      {/* Background Scene */}
      <Box
        className="absolute inset-0 bg-cover bg-center transition-all duration-1000"
        style={{
          backgroundImage: gameState.scene?.background || 'url(/api/placeholder/1920/1080)',
          filter: 'brightness(0.7)',
        }}
      />

      {/* Character Sprites */}
      <AnimatePresence>
        {gameState.scene?.characters?.map((character, index) => (
          <motion.div
            key={character.id}
            initial={{ opacity: 0, x: index === 0 ? -100 : 100 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: index === 0 ? -100 : 100 }}
            transition={{ duration: 0.5 }}
            className="absolute bottom-0"
            style={{
              left: index === 0 ? '10%' : '60%',
              height: '80%',
            }}
          >
            <img
              src={character.sprite || '/api/placeholder/400/800'}
              alt={character.name}
              className="h-full object-contain"
            />
          </motion.div>
        ))}
      </AnimatePresence>

      {/* Top UI Bar */}
      <Box className="absolute top-0 left-0 right-0 p-4 bg-gradient-to-b from-black/50 to-transparent">
        <Box className="flex justify-between items-center">
          <Box className="flex items-center space-x-2">
            <IconButton onClick={handleMenuToggle} className="text-white">
              <Menu />
            </IconButton>
            <Typography variant="h6" className="text-white font-serif">
              {gameState.currentChapter?.title || 'Chapter Loading...'}
            </Typography>
          </Box>
          
          <Box className="flex items-center space-x-2">
            <IconButton onClick={handleSaveGame} className="text-white">
              <Save />
            </IconButton>
            <IconButton onClick={toggleMute} className="text-white">
              {gameState.isMuted ? <VolumeOff /> : <VolumeUp />}
            </IconButton>
            <IconButton onClick={toggleFullscreen} className="text-white">
              {gameState.isFullscreen ? <FullscreenExit /> : <Fullscreen />}
            </IconButton>
          </Box>
        </Box>

        {/* Progress Bar */}
        <Box className="mt-2">
          <LinearProgress
            variant="determinate"
            value={(gameState.dialogIndex / (gameState.currentEvent?.totalDialogs || 1)) * 100}
            className="bg-white/20"
          />
        </Box>
      </Box>

      {/* Dialog Box */}
      <Fade in={!!gameState.currentDialog}>
        <Box className="absolute bottom-0 left-0 right-0 p-6">
          <motion.div
            initial={{ y: 100, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ duration: 0.3 }}
          >
            <Paper className="glass p-6 rounded-lg">
              {/* Speaker Name */}
              {gameState.currentDialog?.speaker && (
                <Typography variant="h6" className="text-primary-400 font-serif mb-2">
                  {gameState.currentDialog.speaker}
                </Typography>
              )}

              {/* Dialog Text */}
              <Typography
                variant="body1"
                className="text-white text-lg leading-relaxed mb-4 min-h-[3rem]"
                style={{ fontFamily: 'Noto Serif JP, serif' }}
              >
                {typewriterText}
              </Typography>

              {/* Choices */}
              <AnimatePresence>
                {showChoices && gameState.currentDialog?.choices && (
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -20 }}
                    transition={{ duration: 0.3 }}
                    className="space-y-2"
                  >
                    {gameState.currentDialog.choices.map((choice, index) => (
                      <motion.div
                        key={choice.id}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: index * 0.1 }}
                      >
                        <Button
                          fullWidth
                          variant="outlined"
                          onClick={() => handleChoice(choice)}
                          className="text-left justify-start border-primary-400 text-primary-400 hover:bg-primary-400/10 py-3"
                        >
                          {choice.text}
                        </Button>
                      </motion.div>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>

              {/* Continue Button */}
              {!showChoices && (
                <Box className="flex justify-between items-center mt-4">
                  <Box className="flex items-center space-x-2">
                    <IconButton
                      onClick={toggleAutoPlay}
                      className={`text-white ${gameState.isAutoPlay ? 'bg-primary-600' : ''}`}
                    >
                      {gameState.isAutoPlay ? <Pause /> : <PlayArrow />}
                    </IconButton>
                    <Typography variant="caption" className="text-gray-400">
                      {gameState.isAutoPlay ? 'Auto' : 'Manual'}
                    </Typography>
                  </Box>

                  <Button
                    onClick={handleNextDialog}
                    endIcon={<SkipNext />}
                    className="text-primary-400 hover:bg-primary-400/10"
                  >
                    Continue
                  </Button>
                </Box>
              )}
            </Paper>
          </motion.div>
        </Box>
      </Fade>

      {/* Game Menu */}
      <Dialog
        open={gameState.isMenuOpen}
        onClose={handleMenuToggle}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          className: 'glass',
        }}
      >
        <DialogTitle className="text-primary-400 font-serif">
          Game Menu
        </DialogTitle>
        <DialogContent>
          <List>
            <ListItem button onClick={handleSaveGame}>
              <ListItemText
                primary="Save Game"
                secondary="Save your current progress"
                className="text-white"
              />
            </ListItem>
            <ListItem button onClick={handleLoadGame}>
              <ListItemText
                primary="Load Game"
                secondary="Load a previous save"
                className="text-white"
              />
            </ListItem>
            <ListItem button onClick={() => navigate('/settings')}>
              <ListItemText
                primary="Settings"
                secondary="Configure game options"
                className="text-white"
              />
            </ListItem>
            <ListItem button onClick={handleReturnToDashboard}>
              <ListItemText
                primary="Return to Dashboard"
                secondary="Exit to main menu"
                className="text-white"
              />
            </ListItem>
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleMenuToggle} className="text-primary-400">
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Background Audio */}
      <audio
        ref={audioRef}
        loop
        autoPlay
        muted={gameState.isMuted}
        src={gameState.scene?.backgroundMusic || '/audio/default-bg.mp3'}
      />
    </Box>
  );
};

export default GameView;