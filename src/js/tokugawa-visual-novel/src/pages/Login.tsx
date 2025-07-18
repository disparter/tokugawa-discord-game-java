import React, { useEffect } from 'react';
import { Box, Typography, Button, Paper, Container, Alert } from '@mui/material';
import { motion } from 'framer-motion';
import { useNavigate, useLocation } from 'react-router-dom';
import { Login as LoginIcon } from '@mui/icons-material';
import { useAuthStore } from '../stores/authStore';
import { authService } from '../services/auth/authService';

// Discord Icon Component (since @mui/icons-material doesn't have Discord)
const DiscordIcon: React.FC<{ className?: string }> = ({ className }) => (
  <svg 
    className={className} 
    width="24" 
    height="24" 
    viewBox="0 0 24 24" 
    fill="currentColor"
  >
    <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
  </svg>
);

const Login: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isLoading, error, clearError } = useAuthStore();

  // Get the redirect path from location state
  const from = location.state?.from?.pathname || '/dashboard';

  useEffect(() => {
    // Clear any existing errors when component mounts
    clearError();

    // If user is already authenticated, redirect to dashboard
    if (isAuthenticated) {
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, from, clearError]);

  const handleDiscordLogin = async () => {
    try {
      if (!authService.isDiscordConfigured()) {
        throw new Error('Discord OAuth2 is not configured');
      }

      // Redirect to Discord OAuth2 authorization
      authService.redirectToDiscordAuth();
    } catch (error: any) {
      console.error('Discord login error:', error);
      useAuthStore.getState().setError(error.message || 'Failed to initiate Discord login');
    }
  };

  const containerVariants = {
    hidden: { opacity: 0, y: 50 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.6,
        staggerChildren: 0.1,
      },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.5 },
    },
  };

  return (
    <Container maxWidth="sm" className="min-h-screen flex items-center justify-center py-8">
      <motion.div
        variants={containerVariants}
        initial="hidden"
        animate="visible"
        className="w-full"
      >
        <Paper
          className="glass p-8 text-center"
          elevation={0}
          sx={{
            background: 'rgba(15, 23, 42, 0.9)',
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(139, 92, 246, 0.3)',
          }}
        >
          {/* Game Logo/Title */}
          <motion.div variants={itemVariants} className="mb-8">
            <Typography
              variant="h3"
              component="h1"
              className="text-glow font-serif text-primary-400 mb-2"
            >
              Tokugawa Academy
            </Typography>
            <Typography
              variant="h6"
              className="text-secondary-300 font-light"
            >
              Visual Novel Experience
            </Typography>
          </motion.div>

          {/* Welcome Message */}
          <motion.div variants={itemVariants} className="mb-8">
            <Typography
              variant="h5"
              className="text-white font-medium mb-4"
            >
              Welcome, Student
            </Typography>
            <Typography
              variant="body1"
              className="text-gray-300 leading-relaxed"
            >
              Enter the mystical world of Tokugawa Academy, where ancient magic meets modern education. 
              Your journey of discovery, friendship, and romance awaits.
            </Typography>
          </motion.div>

          {/* Error Alert */}
          {error && (
            <motion.div variants={itemVariants} className="mb-6">
              <Alert
                severity="error"
                onClose={clearError}
                sx={{
                  background: 'rgba(239, 68, 68, 0.1)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  color: '#fca5a5',
                  '& .MuiAlert-icon': {
                    color: '#ef4444',
                  },
                }}
              >
                {error}
              </Alert>
            </motion.div>
          )}

          {/* Login Button */}
          <motion.div variants={itemVariants} className="mb-8">
            <Button
              variant="contained"
              size="large"
              onClick={handleDiscordLogin}
              disabled={isLoading}
              startIcon={<DiscordIcon />}
              className="w-full py-4 text-lg font-semibold"
              sx={{
                background: 'linear-gradient(135deg, #5865f2 0%, #4752c4 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #4752c4 0%, #3c4299 100%)',
                  transform: 'translateY(-2px)',
                  boxShadow: '0 8px 25px rgba(88, 101, 242, 0.4)',
                },
                '&:disabled': {
                  background: 'rgba(88, 101, 242, 0.3)',
                },
                borderRadius: '12px',
                transition: 'all 0.3s ease',
              }}
            >
              {isLoading ? 'Connecting...' : 'Login with Discord'}
            </Button>
          </motion.div>

          {/* Alternative Login Options */}
          <motion.div variants={itemVariants} className="mb-6">
            <Typography
              variant="body2"
              className="text-gray-400 mb-4"
            >
              Or continue as guest (limited features)
            </Typography>
            <Button
              variant="outlined"
              size="large"
              startIcon={<LoginIcon />}
              className="w-full py-3"
              sx={{
                borderColor: 'rgba(139, 92, 246, 0.5)',
                color: 'rgba(139, 92, 246, 0.8)',
                '&:hover': {
                  borderColor: 'rgba(139, 92, 246, 0.8)',
                  background: 'rgba(139, 92, 246, 0.1)',
                },
              }}
              onClick={() => navigate('/dashboard')}
            >
              Continue as Guest
            </Button>
          </motion.div>

          {/* Features List */}
          <motion.div variants={itemVariants} className="text-left">
            <Typography
              variant="h6"
              className="text-primary-300 font-medium mb-3 text-center"
            >
              What awaits you:
            </Typography>
            <Box className="space-y-2">
              {[
                'Immersive storytelling with meaningful choices',
                'Build relationships with unique characters',
                'Explore the mysterious Tokugawa Academy',
                'Unlock achievements and collect memories',
                'Multiple story paths and endings',
              ].map((feature, index) => (
                <motion.div
                  key={index}
                  variants={itemVariants}
                  className="flex items-center space-x-3"
                >
                  <div className="w-2 h-2 bg-gold-400 rounded-full"></div>
                  <Typography variant="body2" className="text-gray-300">
                    {feature}
                  </Typography>
                </motion.div>
              ))}
            </Box>
          </motion.div>

          {/* Footer */}
          <motion.div variants={itemVariants} className="mt-8 pt-6 border-t border-gray-700">
            <Typography
              variant="body2"
              className="text-gray-500 text-xs"
            >
              By logging in, you agree to our Terms of Service and Privacy Policy.
              <br />
              Your Discord account will be used only for authentication.
            </Typography>
          </motion.div>
        </Paper>

        {/* Floating Particles */}
        <div className="fixed inset-0 pointer-events-none">
          {[...Array(15)].map((_, i) => (
            <motion.div
              key={i}
              className="absolute w-1 h-1 bg-primary-400 rounded-full opacity-20"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
              }}
              animate={{
                y: [0, -30, 0],
                opacity: [0.2, 0.6, 0.2],
                scale: [1, 1.5, 1],
              }}
              transition={{
                duration: 3 + Math.random() * 2,
                repeat: Infinity,
                delay: Math.random() * 2,
              }}
            />
          ))}
        </div>
      </motion.div>
    </Container>
  );
};

export default Login;