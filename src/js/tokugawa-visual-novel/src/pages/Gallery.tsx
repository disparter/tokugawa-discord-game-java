import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Paper,
  Grid,
  Card,
  CardMedia,
  CardContent,
  Box,
  Tabs,
  Tab,
  Dialog,
  DialogContent,
  IconButton,
  Chip,
  LinearProgress,
  Button,
  Fade,
} from '@mui/material';
import {
  Close,
  Favorite,
  FavoriteBorder,
  PlayArrow,
  Pause,
  VolumeUp,
  VolumeOff,
  Download,
  Share,
  Fullscreen,
} from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import { useGameStore } from '../store/gameStore';
import { apiClient } from '../services/apiClient';
import type { GalleryItem, Character, Scene, Music } from '../types';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <div hidden={value !== index} className="py-6">
    {value === index && children}
  </div>
);

const Gallery: React.FC = () => {
  const { progress } = useGameStore();
  const [tabValue, setTabValue] = useState(0);
  const [characters, setCharacters] = useState<Character[]>([]);
  const [scenes, setScenes] = useState<Scene[]>([]);
  const [music, setMusic] = useState<Music[]>([]);
  const [selectedItem, setSelectedItem] = useState<GalleryItem | null>(null);
  const [favorites, setFavorites] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [currentMusic, setCurrentMusic] = useState<Music | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);

  useEffect(() => {
    const fetchGalleryData = async () => {
      try {
        setLoading(true);
        
        // Fetch unlocked characters
        const charactersResponse = await apiClient.get('/api/gallery/characters');
        if (charactersResponse.data.success) {
          setCharacters(charactersResponse.data.data);
        }

        // Fetch unlocked scenes
        const scenesResponse = await apiClient.get('/api/gallery/scenes');
        if (scenesResponse.data.success) {
          setScenes(scenesResponse.data.data);
        }

        // Fetch unlocked music
        const musicResponse = await apiClient.get('/api/gallery/music');
        if (musicResponse.data.success) {
          setMusic(musicResponse.data.data);
        }

        // Load favorites from localStorage
        const savedFavorites = localStorage.getItem('gallery-favorites');
        if (savedFavorites) {
          setFavorites(new Set(JSON.parse(savedFavorites)));
        }
      } catch (error) {
        console.error('Error fetching gallery data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchGalleryData();
  }, []);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleItemClick = (item: GalleryItem) => {
    setSelectedItem(item);
  };

  const handleCloseDialog = () => {
    setSelectedItem(null);
  };

  const toggleFavorite = (itemId: string) => {
    const newFavorites = new Set(favorites);
    if (newFavorites.has(itemId)) {
      newFavorites.delete(itemId);
    } else {
      newFavorites.add(itemId);
    }
    setFavorites(newFavorites);
    localStorage.setItem('gallery-favorites', JSON.stringify([...newFavorites]));
  };

  const playMusic = (musicItem: Music) => {
    if (currentMusic?.id === musicItem.id) {
      setIsPlaying(!isPlaying);
    } else {
      setCurrentMusic(musicItem);
      setIsPlaying(true);
    }
  };

  const toggleMute = () => {
    setIsMuted(!isMuted);
  };

  const getCompletionPercentage = () => {
    const totalItems = characters.length + scenes.length + music.length;
    const unlockedItems = characters.filter(c => c.unlocked).length +
                         scenes.filter(s => s.unlocked).length +
                         music.filter(m => m.unlocked).length;
    return totalItems > 0 ? (unlockedItems / totalItems) * 100 : 0;
  };

  if (loading) {
    return (
      <Container maxWidth="lg" className="min-h-screen py-8">
        <Box className="flex justify-center items-center h-64">
          <LinearProgress className="w-64" />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" className="min-h-screen py-8">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        {/* Header */}
        <Paper className="glass p-6 mb-6">
          <Box className="flex justify-between items-center mb-4">
            <Typography variant="h3" className="text-primary-400 font-serif">
              Gallery
            </Typography>
            <Box className="text-right">
              <Typography variant="h6" className="text-primary-400">
                {Math.round(getCompletionPercentage())}% Complete
              </Typography>
              <LinearProgress
                variant="determinate"
                value={getCompletionPercentage()}
                className="w-32 mt-2"
              />
            </Box>
          </Box>
          <Typography variant="body1" className="text-gray-300">
            Explore your collection of characters, scenes, and music unlocked throughout your journey.
          </Typography>
        </Paper>

        {/* Tabs */}
        <Paper className="glass mb-6">
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            variant="fullWidth"
            className="border-b border-primary-400/20"
          >
            <Tab
              label={`Characters (${characters.filter(c => c.unlocked).length}/${characters.length})`}
              className="text-primary-400"
            />
            <Tab
              label={`Scenes (${scenes.filter(s => s.unlocked).length}/${scenes.length})`}
              className="text-primary-400"
            />
            <Tab
              label={`Music (${music.filter(m => m.unlocked).length}/${music.length})`}
              className="text-primary-400"
            />
          </Tabs>

          {/* Characters Tab */}
          <TabPanel value={tabValue} index={0}>
            <Grid container spacing={3}>
              {characters.map((character) => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={character.id}>
                  <motion.div
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <Card
                      className={`glass-card cursor-pointer ${!character.unlocked ? 'opacity-50' : ''}`}
                      onClick={() => character.unlocked && handleItemClick(character)}
                    >
                      <CardMedia
                        component="img"
                        height="300"
                        image={character.unlocked ? character.portrait : '/api/placeholder/300/300'}
                        alt={character.name}
                        className="object-cover"
                      />
                      <CardContent>
                        <Box className="flex justify-between items-start mb-2">
                          <Typography variant="h6" className="text-primary-400 font-serif">
                            {character.unlocked ? character.name : '???'}
                          </Typography>
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              if (character.unlocked) toggleFavorite(character.id);
                            }}
                            className="text-pink-400"
                          >
                            {favorites.has(character.id) ? <Favorite /> : <FavoriteBorder />}
                          </IconButton>
                        </Box>
                        <Typography variant="body2" className="text-gray-300 mb-2">
                          {character.unlocked ? character.description : 'Character not yet unlocked'}
                        </Typography>
                        {character.unlocked && (
                          <Box className="flex flex-wrap gap-1">
                            {character.traits?.map((trait) => (
                              <Chip
                                key={trait}
                                label={trait}
                                size="small"
                                className="bg-primary-600 text-white"
                              />
                            ))}
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  </motion.div>
                </Grid>
              ))}
            </Grid>
          </TabPanel>

          {/* Scenes Tab */}
          <TabPanel value={tabValue} index={1}>
            <Grid container spacing={3}>
              {scenes.map((scene) => (
                <Grid item xs={12} sm={6} md={4} key={scene.id}>
                  <motion.div
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <Card
                      className={`glass-card cursor-pointer ${!scene.unlocked ? 'opacity-50' : ''}`}
                      onClick={() => scene.unlocked && handleItemClick(scene)}
                    >
                      <CardMedia
                        component="img"
                        height="200"
                        image={scene.unlocked ? scene.image : '/api/placeholder/400/200'}
                        alt={scene.title}
                        className="object-cover"
                      />
                      <CardContent>
                        <Box className="flex justify-between items-start mb-2">
                          <Typography variant="h6" className="text-primary-400 font-serif">
                            {scene.unlocked ? scene.title : '???'}
                          </Typography>
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              if (scene.unlocked) toggleFavorite(scene.id);
                            }}
                            className="text-pink-400"
                          >
                            {favorites.has(scene.id) ? <Favorite /> : <FavoriteBorder />}
                          </IconButton>
                        </Box>
                        <Typography variant="body2" className="text-gray-300">
                          {scene.unlocked ? scene.description : 'Scene not yet unlocked'}
                        </Typography>
                        {scene.unlocked && (
                          <Box className="flex justify-between items-center mt-2">
                            <Chip
                              label={scene.chapter}
                              size="small"
                              className="bg-blue-600 text-white"
                            />
                            <Typography variant="caption" className="text-gray-400">
                              {scene.unlockDate}
                            </Typography>
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  </motion.div>
                </Grid>
              ))}
            </Grid>
          </TabPanel>

          {/* Music Tab */}
          <TabPanel value={tabValue} index={2}>
            <Grid container spacing={3}>
              {music.map((musicItem) => (
                <Grid item xs={12} sm={6} md={4} key={musicItem.id}>
                  <motion.div
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <Card
                      className={`glass-card ${!musicItem.unlocked ? 'opacity-50' : ''}`}
                    >
                      <CardMedia
                        component="img"
                        height="200"
                        image={musicItem.unlocked ? musicItem.cover : '/api/placeholder/400/200'}
                        alt={musicItem.title}
                        className="object-cover"
                      />
                      <CardContent>
                        <Box className="flex justify-between items-start mb-2">
                          <Typography variant="h6" className="text-primary-400 font-serif">
                            {musicItem.unlocked ? musicItem.title : '???'}
                          </Typography>
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              if (musicItem.unlocked) toggleFavorite(musicItem.id);
                            }}
                            className="text-pink-400"
                          >
                            {favorites.has(musicItem.id) ? <Favorite /> : <FavoriteBorder />}
                          </IconButton>
                        </Box>
                        <Typography variant="body2" className="text-gray-300 mb-2">
                          {musicItem.unlocked ? musicItem.composer : 'Music not yet unlocked'}
                        </Typography>
                        {musicItem.unlocked && (
                          <Box className="flex justify-between items-center">
                            <Button
                              startIcon={
                                currentMusic?.id === musicItem.id && isPlaying ? <Pause /> : <PlayArrow />
                              }
                              onClick={() => playMusic(musicItem)}
                              className="text-primary-400 hover:bg-primary-400/10"
                            >
                              {currentMusic?.id === musicItem.id && isPlaying ? 'Pause' : 'Play'}
                            </Button>
                            <Typography variant="caption" className="text-gray-400">
                              {musicItem.duration}
                            </Typography>
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  </motion.div>
                </Grid>
              ))}
            </Grid>
          </TabPanel>
        </Paper>

        {/* Item Detail Dialog */}
        <Dialog
          open={!!selectedItem}
          onClose={handleCloseDialog}
          maxWidth="md"
          fullWidth
          PaperProps={{
            className: 'glass',
          }}
        >
          <DialogContent className="p-0">
            {selectedItem && (
              <Box className="relative">
                <IconButton
                  onClick={handleCloseDialog}
                  className="absolute top-4 right-4 z-10 bg-black/50 text-white"
                >
                  <Close />
                </IconButton>
                <img
                  src={selectedItem.image || selectedItem.portrait}
                  alt={selectedItem.title || selectedItem.name}
                  className="w-full h-auto max-h-[70vh] object-contain"
                />
                <Box className="p-6">
                  <Typography variant="h4" className="text-primary-400 font-serif mb-2">
                    {selectedItem.title || selectedItem.name}
                  </Typography>
                  <Typography variant="body1" className="text-gray-300 mb-4">
                    {selectedItem.description}
                  </Typography>
                  <Box className="flex justify-between items-center">
                    <Box className="flex gap-2">
                      <Button
                        startIcon={<Share />}
                        className="text-primary-400 hover:bg-primary-400/10"
                      >
                        Share
                      </Button>
                      <Button
                        startIcon={<Download />}
                        className="text-primary-400 hover:bg-primary-400/10"
                      >
                        Download
                      </Button>
                    </Box>
                    <IconButton
                      onClick={() => toggleFavorite(selectedItem.id)}
                      className="text-pink-400"
                    >
                      {favorites.has(selectedItem.id) ? <Favorite /> : <FavoriteBorder />}
                    </IconButton>
                  </Box>
                </Box>
              </Box>
            )}
          </DialogContent>
        </Dialog>

        {/* Music Player */}
        <AnimatePresence>
          {currentMusic && (
            <motion.div
              initial={{ y: 100 }}
              animate={{ y: 0 }}
              exit={{ y: 100 }}
              className="fixed bottom-4 right-4 z-50"
            >
              <Paper className="glass p-4 min-w-[300px]">
                <Box className="flex items-center gap-3">
                  <img
                    src={currentMusic.cover}
                    alt={currentMusic.title}
                    className="w-12 h-12 rounded object-cover"
                  />
                  <Box className="flex-1">
                    <Typography variant="subtitle2" className="text-primary-400">
                      {currentMusic.title}
                    </Typography>
                    <Typography variant="caption" className="text-gray-400">
                      {currentMusic.composer}
                    </Typography>
                  </Box>
                  <Box className="flex items-center gap-1">
                    <IconButton
                      onClick={() => playMusic(currentMusic)}
                      className="text-primary-400"
                    >
                      {isPlaying ? <Pause /> : <PlayArrow />}
                    </IconButton>
                    <IconButton
                      onClick={toggleMute}
                      className="text-primary-400"
                    >
                      {isMuted ? <VolumeOff /> : <VolumeUp />}
                    </IconButton>
                  </Box>
                </Box>
              </Paper>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </Container>
  );
};

export default Gallery;