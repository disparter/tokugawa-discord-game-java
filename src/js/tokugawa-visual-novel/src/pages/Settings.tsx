import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Paper,

  Box,
  Switch,
  Slider,
  FormControlLabel,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Divider,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Chip,
  } from '@mui/material';
import { Grid } from '@mui/material';
import {
  VolumeUp,
  Brightness4,
  Language,
  Save,
  Restore,
  Delete,
  Download,
  Upload,
  AccountCircle,
  Notifications,
  Gamepad,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useAuthStore } from '../stores/authStore';
import { useGameStore } from '../stores/gameStore';
import { apiClient } from '../services/api/client';

interface GameSettings {
  // Audio Settings
  masterVolume: number;
  musicVolume: number;
  sfxVolume: number;
  voiceVolume: number;
  isMuted: boolean;
  
  // Visual Settings
  textSpeed: number;
  autoPlaySpeed: number;
  skipUnreadText: boolean;
  fullscreen: boolean;
  
  // Language & Accessibility
  language: string;
  fontSize: number;
  highContrast: boolean;
  
  // Gameplay Settings
  showChoiceHelp: boolean;
  autoSave: boolean;
  confirmChoices: boolean;
  
  // Notification Settings
  emailNotifications: boolean;
  pushNotifications: boolean;
  gameUpdates: boolean;
}

const Settings: React.FC = () => {
  const { user, logout } = useAuthStore();
  const { updateSettings } = useGameStore();
  const [settings, setSettings] = useState<GameSettings>({
    masterVolume: 80,
    musicVolume: 70,
    sfxVolume: 60,
    voiceVolume: 80,
    isMuted: false,
    textSpeed: 50,
    autoPlaySpeed: 30,
    skipUnreadText: false,
    fullscreen: false,
    language: 'en',
    fontSize: 16,
    highContrast: false,
    showChoiceHelp: true,
    autoSave: true,
    confirmChoices: false,
    emailNotifications: true,
    pushNotifications: true,
    gameUpdates: true,
  });
  const [showResetDialog, setShowResetDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved' | 'error'>('idle');
  const [activeSection, setActiveSection] = useState('audio');

  useEffect(() => {
    // Load settings from game store or API
    const loadSettings = async () => {
      try {
        const response = await apiClient.get('/api/settings');
        if (response.data.success) {
          setSettings({ ...settings, ...response.data.data });
        }
      } catch (error) {
        console.error('Error loading settings:', error);
      }
    };

    loadSettings();
  }, []);

  const handleSettingChange = (key: keyof GameSettings, value: any) => {
    setSettings(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const handleSaveSettings = async () => {
    try {
      setSaveStatus('saving');
      
      // Save to API
      const response = await apiClient.post('/api/settings', settings);
      if (response.data.success) {
        // Update game store
        updateSettings(settings);
        setSaveStatus('saved');
        setTimeout(() => setSaveStatus('idle'), 2000);
      } else {
        setSaveStatus('error');
      }
    } catch (error) {
      console.error('Error saving settings:', error);
      setSaveStatus('error');
    }
  };

  const handleResetSettings = () => {
    setSettings({
      masterVolume: 80,
      musicVolume: 70,
      sfxVolume: 60,
      voiceVolume: 80,
      isMuted: false,
      textSpeed: 50,
      autoPlaySpeed: 30,
      skipUnreadText: false,
      fullscreen: false,
      language: 'en',
      fontSize: 16,
      highContrast: false,
      showChoiceHelp: true,
      autoSave: true,
      confirmChoices: false,
      emailNotifications: true,
      pushNotifications: true,
      gameUpdates: true,
    });
    setShowResetDialog(false);
  };

  const handleDeleteAccount = async () => {
    try {
      const response = await apiClient.delete('/api/auth/account');
      if (response.data.success) {
        logout();
      }
    } catch (error) {
      console.error('Error deleting account:', error);
    }
    setShowDeleteDialog(false);
  };

  const handleExportData = async () => {
    try {
      const response = await apiClient.get('/api/user/export');
      if (response.data.success) {
        const blob = new Blob([JSON.stringify(response.data.data, null, 2)], {
          type: 'application/json'
        });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `tokugawa-data-${new Date().toISOString().split('T')[0]}.json`;
        a.click();
        URL.revokeObjectURL(url);
      }
    } catch (error) {
      console.error('Error exporting data:', error);
    }
  };

  const sections = [
    { id: 'audio', label: 'Audio', icon: <VolumeUp /> },
    { id: 'visual', label: 'Visual', icon: <Brightness4 /> },
    { id: 'gameplay', label: 'Gameplay', icon: <Gamepad /> },
    { id: 'language', label: 'Language', icon: <Language /> },
    { id: 'notifications', label: 'Notifications', icon: <Notifications /> },
    { id: 'account', label: 'Account', icon: <AccountCircle /> },
  ];

  return (
    <Container maxWidth="lg" className="min-h-screen py-8">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <Paper className="glass p-6 mb-6">
          <Typography variant="h3" className="text-primary-400 font-serif mb-4">
            Settings
          </Typography>
          <Typography variant="body1" className="text-gray-300">
            Customize your game experience and manage your account preferences.
          </Typography>
        </Paper>

        <Grid container spacing={4}>
          {/* Settings Navigation */}
          <Grid item xs={12} md={3}>
            <Paper className="glass p-4">
              <Typography variant="h6" className="text-primary-400 font-serif mb-4">
                Categories
              </Typography>
              <List>
                {sections.map((section) => (
                  <ListItem key={section.id} className="mb-2 rounded">
                    <ListItemButton
                      selected={activeSection === section.id}
                      onClick={() => setActiveSection(section.id)}
                      className={
                        activeSection === section.id
                          ? 'bg-primary-600/20 text-primary-400'
                          : 'text-gray-300 hover:bg-primary-600/10'
                      }
                    >
                                          <Box className="flex items-center gap-3">
                        {section.icon}
                        <Typography variant="body1">{section.label}</Typography>
                      </Box>
                    </ListItemButton>
                  </ListItem>
                ))}
              </List>
            </Paper>
          </Grid>

          {/* Settings Content */}
          <Grid item xs={12} md={9}>
            <Paper className="glass p-6">
              {/* Audio Settings */}
              {activeSection === 'audio' && (
                <Box>
                  <Typography variant="h5" className="text-primary-400 font-serif mb-4">
                    Audio Settings
                  </Typography>
                  
                  <Grid container spacing={4}>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Master Volume
                      </Typography>
                      <Slider
                        value={settings.masterVolume}
                        onChange={(_, value) => handleSettingChange('masterVolume', value)}
                        min={0}
                        max={100}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Music Volume
                      </Typography>
                      <Slider
                        value={settings.musicVolume}
                        onChange={(_, value) => handleSettingChange('musicVolume', value)}
                        min={0}
                        max={100}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Sound Effects
                      </Typography>
                      <Slider
                        value={settings.sfxVolume}
                        onChange={(_, value) => handleSettingChange('sfxVolume', value)}
                        min={0}
                        max={100}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Voice Volume
                      </Typography>
                      <Slider
                        value={settings.voiceVolume}
                        onChange={(_, value) => handleSettingChange('voiceVolume', value)}
                        min={0}
                        max={100}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.isMuted}
                            onChange={(e) => handleSettingChange('isMuted', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Mute All Audio"
                        className="text-gray-300"
                      />
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Visual Settings */}
              {activeSection === 'visual' && (
                <Box>
                  <Typography variant="h5" className="text-primary-400 font-serif mb-4">
                    Visual Settings
                  </Typography>
                  
                  <Grid container spacing={4}>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Text Speed
                      </Typography>
                      <Slider
                        value={settings.textSpeed}
                        onChange={(_, value) => handleSettingChange('textSpeed', value)}
                        min={1}
                        max={100}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Auto-Play Speed
                      </Typography>
                      <Slider
                        value={settings.autoPlaySpeed}
                        onChange={(_, value) => handleSettingChange('autoPlaySpeed', value)}
                        min={1}
                        max={100}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body1" className="text-gray-300 mb-2">
                        Font Size
                      </Typography>
                      <Slider
                        value={settings.fontSize}
                        onChange={(_, value) => handleSettingChange('fontSize', value)}
                        min={12}
                        max={24}
                        valueLabelDisplay="auto"
                        className="text-primary-400"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.skipUnreadText}
                            onChange={(e) => handleSettingChange('skipUnreadText', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Allow Skipping Unread Text"
                        className="text-gray-300"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.fullscreen}
                            onChange={(e) => handleSettingChange('fullscreen', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Fullscreen Mode"
                        className="text-gray-300"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.highContrast}
                            onChange={(e) => handleSettingChange('highContrast', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="High Contrast Mode"
                        className="text-gray-300"
                      />
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Gameplay Settings */}
              {activeSection === 'gameplay' && (
                <Box>
                  <Typography variant="h5" className="text-primary-400 font-serif mb-4">
                    Gameplay Settings
                  </Typography>
                  
                  <Grid container spacing={4}>
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.showChoiceHelp}
                            onChange={(e) => handleSettingChange('showChoiceHelp', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Show Choice Consequences"
                        className="text-gray-300"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.autoSave}
                            onChange={(e) => handleSettingChange('autoSave', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Auto-Save Progress"
                        className="text-gray-300"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.confirmChoices}
                            onChange={(e) => handleSettingChange('confirmChoices', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Confirm Important Choices"
                        className="text-gray-300"
                      />
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Language Settings */}
              {activeSection === 'language' && (
                <Box>
                  <Typography variant="h5" className="text-primary-400 font-serif mb-4">
                    Language & Accessibility
                  </Typography>
                  
                  <Grid container spacing={4}>
                    <Grid item xs={12} sm={6}>
                      <FormControl fullWidth>
                        <InputLabel className="text-gray-300">Language</InputLabel>
                        <Select
                          value={settings.language}
                          onChange={(e) => handleSettingChange('language', e.target.value)}
                          className="text-gray-300"
                        >
                          <MenuItem value="en">English</MenuItem>
                          <MenuItem value="ja">日本語</MenuItem>
                          <MenuItem value="zh">中文</MenuItem>
                          <MenuItem value="ko">한국어</MenuItem>
                        </Select>
                      </FormControl>
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Notification Settings */}
              {activeSection === 'notifications' && (
                <Box>
                  <Typography variant="h5" className="text-primary-400 font-serif mb-4">
                    Notification Settings
                  </Typography>
                  
                  <Grid container spacing={4}>
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.emailNotifications}
                            onChange={(e) => handleSettingChange('emailNotifications', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Email Notifications"
                        className="text-gray-300"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.pushNotifications}
                            onChange={(e) => handleSettingChange('pushNotifications', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Push Notifications"
                        className="text-gray-300"
                      />
                    </Grid>
                    
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={settings.gameUpdates}
                            onChange={(e) => handleSettingChange('gameUpdates', e.target.checked)}
                            className="text-primary-400"
                          />
                        }
                        label="Game Updates"
                        className="text-gray-300"
                      />
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Account Settings */}
              {activeSection === 'account' && (
                <Box>
                  <Typography variant="h5" className="text-primary-400 font-serif mb-4">
                    Account Management
                  </Typography>
                  
                  <Grid container spacing={4}>
                    <Grid item xs={12}>
                      <Paper className="glass-card p-4 mb-4">
                        <Box className="flex items-center gap-4">
                          <img
                            src={user?.avatar ? `https://cdn.discordapp.com/avatars/${user.discordId}/${user.avatar}.png` : '/api/placeholder/64/64'}
                            alt="Profile"
                            className="w-16 h-16 rounded-full"
                          />
                          <Box>
                            <Typography variant="h6" className="text-primary-400">
                              {user?.username}
                            </Typography>
                            <Typography variant="body2" className="text-gray-400">
                              {user?.email}
                            </Typography>
                            <Box className="flex gap-2 mt-2">
                              {user?.roles?.map((role: string) => (
                                <Chip
                                  key={role}
                                  label={role}
                                  size="small"
                                  className="bg-primary-600 text-white"
                                />
                              ))}
                            </Box>
                          </Box>
                        </Box>
                      </Paper>
                    </Grid>
                    
                    <Grid item xs={12}>
                      <Typography variant="h6" className="text-primary-400 mb-3">
                        Data Management
                      </Typography>
                      <Box className="space-y-2">
                        <Button
                          startIcon={<Download />}
                          onClick={handleExportData}
                          className="text-primary-400 hover:bg-primary-400/10 mr-2"
                        >
                          Export Game Data
                        </Button>
                        <Button
                          startIcon={<Upload />}
                          className="text-primary-400 hover:bg-primary-400/10"
                        >
                          Import Game Data
                        </Button>
                      </Box>
                    </Grid>
                    
                    <Grid item xs={12}>
                      <Divider className="my-4" />
                      <Typography variant="h6" className="text-red-400 mb-3">
                        Danger Zone
                      </Typography>
                      <Button
                        startIcon={<Delete />}
                        onClick={() => setShowDeleteDialog(true)}
                        className="text-red-400 hover:bg-red-400/10"
                      >
                        Delete Account
                      </Button>
                    </Grid>
                  </Grid>
                </Box>
              )}

              {/* Save/Reset Actions */}
              <Box className="flex justify-between items-center mt-6 pt-4 border-t border-gray-700">
                <Button
                  startIcon={<Restore />}
                  onClick={() => setShowResetDialog(true)}
                  className="text-gray-400 hover:bg-gray-400/10"
                >
                  Reset to Defaults
                </Button>
                
                <Box className="flex items-center gap-2">
                  {saveStatus === 'saved' && (
                    <Alert severity="success" className="mr-2">
                      Settings saved successfully!
                    </Alert>
                  )}
                  {saveStatus === 'error' && (
                    <Alert severity="error" className="mr-2">
                      Error saving settings
                    </Alert>
                  )}
                  <Button
                    startIcon={<Save />}
                    onClick={handleSaveSettings}
                    disabled={saveStatus === 'saving'}
                    className="bg-primary-600 hover:bg-primary-700 text-white"
                  >
                    {saveStatus === 'saving' ? 'Saving...' : 'Save Settings'}
                  </Button>
                </Box>
              </Box>
            </Paper>
          </Grid>
        </Grid>

        {/* Reset Confirmation Dialog */}
        <Dialog open={showResetDialog} onClose={() => setShowResetDialog(false)}>
          <DialogTitle className="text-primary-400">Reset Settings</DialogTitle>
          <DialogContent>
            <Typography variant="body1" className="text-gray-300">
              Are you sure you want to reset all settings to their default values? This action cannot be undone.
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowResetDialog(false)} className="text-gray-400">
              Cancel
            </Button>
            <Button onClick={handleResetSettings} className="text-red-400">
              Reset
            </Button>
          </DialogActions>
        </Dialog>

        {/* Delete Account Dialog */}
        <Dialog open={showDeleteDialog} onClose={() => setShowDeleteDialog(false)}>
          <DialogTitle className="text-red-400">Delete Account</DialogTitle>
          <DialogContent>
            <Typography variant="body1" className="text-gray-300 mb-4">
              Are you sure you want to delete your account? This will permanently remove all your game data, progress, and settings.
            </Typography>
            <Alert severity="warning" className="mb-4">
              This action cannot be undone!
            </Alert>
            <TextField
              fullWidth
              label="Type 'DELETE' to confirm"
              variant="outlined"
              className="text-gray-300"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowDeleteDialog(false)} className="text-gray-400">
              Cancel
            </Button>
            <Button onClick={handleDeleteAccount} className="text-red-400">
              Delete Account
            </Button>
          </DialogActions>
        </Dialog>
      </motion.div>
    </Container>
  );
};

export default Settings;