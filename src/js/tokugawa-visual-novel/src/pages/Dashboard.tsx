import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Avatar,
  Box,
  LinearProgress,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
} from '@mui/material';
import {
  PlayArrow,
  BookOpen,
  Photo,
  Settings,
  TrendingUp,
  Favorite,
  Group,
  EmojiEvents,
  History,
  Notifications,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useGameStore } from '../store/gameStore';
import { apiClient } from '../services/apiClient';
import type { User, Player, Progress } from '../types';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { player, progress, setPlayer, setProgress } = useGameStore();
  const [loading, setLoading] = useState(true);
  const [recentActivity, setRecentActivity] = useState<string[]>([]);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        
        // Fetch user's player data
        const playerResponse = await apiClient.get('/api/players/me');
        if (playerResponse.data.success) {
          setPlayer(playerResponse.data.data);
        }

        // Fetch user's progress
        const progressResponse = await apiClient.get('/api/story/progress');
        if (progressResponse.data.success) {
          setProgress(progressResponse.data.data);
        }

        // Mock recent activity for now
        setRecentActivity([
          'Completed Chapter 1: The Arrival',
          'Made a choice in "The School Garden"',
          'Increased reputation with Sakura',
          'Unlocked new location: Library',
          'Achieved level 2',
        ]);
      } catch (error) {
        console.error('Error fetching user data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [setPlayer, setProgress]);

  const handleStartGame = () => {
    navigate('/game');
  };

  const handleContinueGame = () => {
    navigate('/game');
  };

  const handleViewGallery = () => {
    navigate('/gallery');
  };

  const handleSettings = () => {
    navigate('/settings');
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
        <Grid container spacing={4}>
          {/* User Profile Section */}
          <Grid item xs={12} md={4}>
            <Paper className="glass p-6">
              <Box className="flex flex-col items-center mb-4">
                <Avatar
                  src={user?.avatar ? `https://cdn.discordapp.com/avatars/${user.discordId}/${user.avatar}.png` : undefined}
                  sx={{ width: 80, height: 80, mb: 2 }}
                >
                  {user?.username?.charAt(0).toUpperCase()}
                </Avatar>
                <Typography variant="h5" className="text-primary-400 font-serif">
                  {user?.username || 'Player'}
                </Typography>
                <Typography variant="body2" className="text-gray-400">
                  {user?.email}
                </Typography>
              </Box>

              {player && (
                <Box className="space-y-3">
                  <Box>
                    <Typography variant="body2" className="text-gray-300 mb-1">
                      Level {player.level}
                    </Typography>
                    <LinearProgress
                      variant="determinate"
                      value={(player.exp % 100)}
                      className="h-2 rounded"
                    />
                    <Typography variant="caption" className="text-gray-400">
                      {player.exp % 100}/100 EXP
                    </Typography>
                  </Box>

                  <Divider className="my-3" />

                  <Box className="grid grid-cols-2 gap-2">
                    <Box className="text-center">
                      <Typography variant="h6" className="text-primary-400">
                        {player.reputation}
                      </Typography>
                      <Typography variant="caption" className="text-gray-400">
                        Reputation
                      </Typography>
                    </Box>
                    <Box className="text-center">
                      <Typography variant="h6" className="text-primary-400">
                        {player.points}
                      </Typography>
                      <Typography variant="caption" className="text-gray-400">
                        Points
                      </Typography>
                    </Box>
                  </Box>

                  <Box className="flex flex-wrap gap-1 mt-3">
                    <Chip
                      icon={<Favorite />}
                      label="Romantic"
                      size="small"
                      className="bg-pink-600"
                    />
                    <Chip
                      icon={<Group />}
                      label="Social"
                      size="small"
                      className="bg-blue-600"
                    />
                    <Chip
                      icon={<EmojiEvents />}
                      label="Achiever"
                      size="small"
                      className="bg-yellow-600"
                    />
                  </Box>
                </Box>
              )}
            </Paper>
          </Grid>

          {/* Main Content */}
          <Grid item xs={12} md={8}>
            <Grid container spacing={3}>
              {/* Quick Actions */}
              <Grid item xs={12}>
                <Paper className="glass p-6">
                  <Typography variant="h6" className="text-primary-400 font-serif mb-4">
                    Quick Actions
                  </Typography>
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6} md={3}>
                      <Card className="glass-card hover:scale-105 transition-transform cursor-pointer">
                        <CardContent className="text-center">
                          <PlayArrow className="text-primary-400 mb-2" sx={{ fontSize: 40 }} />
                          <Typography variant="h6" className="text-primary-400">
                            {progress ? 'Continue' : 'Start Game'}
                          </Typography>
                        </CardContent>
                        <CardActions>
                          <Button
                            fullWidth
                            variant="contained"
                            onClick={progress ? handleContinueGame : handleStartGame}
                            className="bg-primary-600 hover:bg-primary-700"
                          >
                            {progress ? 'Continue' : 'Start'}
                          </Button>
                        </CardActions>
                      </Card>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                      <Card className="glass-card hover:scale-105 transition-transform cursor-pointer">
                        <CardContent className="text-center">
                          <Photo className="text-primary-400 mb-2" sx={{ fontSize: 40 }} />
                          <Typography variant="h6" className="text-primary-400">
                            Gallery
                          </Typography>
                        </CardContent>
                        <CardActions>
                          <Button
                            fullWidth
                            variant="outlined"
                            onClick={handleViewGallery}
                            className="border-primary-600 text-primary-400 hover:bg-primary-600"
                          >
                            View
                          </Button>
                        </CardActions>
                      </Card>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                      <Card className="glass-card hover:scale-105 transition-transform cursor-pointer">
                        <CardContent className="text-center">
                          <BookOpen className="text-primary-400 mb-2" sx={{ fontSize: 40 }} />
                          <Typography variant="h6" className="text-primary-400">
                            Story
                          </Typography>
                        </CardContent>
                        <CardActions>
                          <Button
                            fullWidth
                            variant="outlined"
                            className="border-primary-600 text-primary-400 hover:bg-primary-600"
                          >
                            Browse
                          </Button>
                        </CardActions>
                      </Card>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                      <Card className="glass-card hover:scale-105 transition-transform cursor-pointer">
                        <CardContent className="text-center">
                          <Settings className="text-primary-400 mb-2" sx={{ fontSize: 40 }} />
                          <Typography variant="h6" className="text-primary-400">
                            Settings
                          </Typography>
                        </CardContent>
                        <CardActions>
                          <Button
                            fullWidth
                            variant="outlined"
                            onClick={handleSettings}
                            className="border-primary-600 text-primary-400 hover:bg-primary-600"
                          >
                            Configure
                          </Button>
                        </CardActions>
                      </Card>
                    </Grid>
                  </Grid>
                </Paper>
              </Grid>

              {/* Game Statistics */}
              <Grid item xs={12} md={6}>
                <Paper className="glass p-6">
                  <Typography variant="h6" className="text-primary-400 font-serif mb-4">
                    Game Statistics
                  </Typography>
                  <Box className="space-y-3">
                    <Box className="flex justify-between items-center">
                      <Typography variant="body2" className="text-gray-300">
                        Chapters Completed
                      </Typography>
                      <Typography variant="body2" className="text-primary-400">
                        {progress?.chaptersCompleted || 0}/12
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={((progress?.chaptersCompleted || 0) / 12) * 100}
                      className="h-2 rounded"
                    />

                    <Box className="flex justify-between items-center">
                      <Typography variant="body2" className="text-gray-300">
                        Choices Made
                      </Typography>
                      <Typography variant="body2" className="text-primary-400">
                        {progress?.choicesMade || 0}
                      </Typography>
                    </Box>

                    <Box className="flex justify-between items-center">
                      <Typography variant="body2" className="text-gray-300">
                        Relationships
                      </Typography>
                      <Typography variant="body2" className="text-primary-400">
                        {progress?.relationshipsFormed || 0}
                      </Typography>
                    </Box>

                    <Box className="flex justify-between items-center">
                      <Typography variant="body2" className="text-gray-300">
                        Playtime
                      </Typography>
                      <Typography variant="body2" className="text-primary-400">
                        {progress?.playtimeHours || 0}h {progress?.playtimeMinutes || 0}m
                      </Typography>
                    </Box>
                  </Box>
                </Paper>
              </Grid>

              {/* Recent Activity */}
              <Grid item xs={12} md={6}>
                <Paper className="glass p-6">
                  <Typography variant="h6" className="text-primary-400 font-serif mb-4">
                    Recent Activity
                  </Typography>
                  <List className="space-y-2">
                    {recentActivity.map((activity, index) => (
                      <ListItem key={index} className="px-0">
                        <ListItemIcon>
                          <History className="text-primary-400" />
                        </ListItemIcon>
                        <ListItemText
                          primary={activity}
                          className="text-gray-300"
                          primaryTypographyProps={{
                            variant: 'body2',
                            className: 'text-gray-300',
                          }}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Paper>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </motion.div>
    </Container>
  );
};

export default Dashboard;