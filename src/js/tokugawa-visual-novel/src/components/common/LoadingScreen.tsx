import React from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { motion } from 'framer-motion';

interface LoadingScreenProps {
  message?: string;
  showProgress?: boolean;
  progress?: number;
}

const LoadingScreen: React.FC<LoadingScreenProps> = ({
  message = 'Loading...',
  showProgress = false,
  progress = 0,
}) => {
  return (
    <Box
      className="fixed inset-0 flex items-center justify-center bg-gradient-to-br from-dark-900 via-dark-800 to-dark-700"
      sx={{
        zIndex: 9999,
      }}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.5 }}
        className="text-center"
      >
        {/* Logo or Game Title */}
        <motion.div
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.5 }}
          className="mb-8"
        >
          <Typography
            variant="h2"
            component="h1"
            className="text-glow font-serif text-primary-400 mb-2"
          >
            Tokugawa Academy
          </Typography>
          <Typography
            variant="h6"
            className="text-secondary-300 font-light"
          >
            Visual Novel
          </Typography>
        </motion.div>

        {/* Loading Animation */}
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ delay: 0.4, duration: 0.5 }}
          className="mb-6"
        >
          <Box className="relative">
            <CircularProgress
              size={80}
              thickness={2}
              sx={{
                color: 'primary.main',
                '& .MuiCircularProgress-circle': {
                  filter: 'drop-shadow(0 0 10px rgba(139, 92, 246, 0.5))',
                },
              }}
            />
            {showProgress && (
              <Box
                className="absolute inset-0 flex items-center justify-center"
              >
                <Typography
                  variant="body2"
                  className="text-primary-300 font-medium"
                >
                  {Math.round(progress)}%
                </Typography>
              </Box>
            )}
          </Box>
        </motion.div>

        {/* Loading Message */}
        <motion.div
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.6, duration: 0.5 }}
        >
          <Typography
            variant="body1"
            className="text-gray-300 font-medium"
          >
            {message}
          </Typography>
        </motion.div>

        {/* Animated Dots */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8, duration: 0.5 }}
          className="mt-4 flex justify-center space-x-1"
        >
          {[0, 1, 2].map((i) => (
            <motion.div
              key={i}
              className="w-2 h-2 bg-primary-400 rounded-full"
              animate={{
                scale: [1, 1.5, 1],
                opacity: [0.5, 1, 0.5],
              }}
              transition={{
                duration: 1.5,
                repeat: Infinity,
                delay: i * 0.2,
              }}
            />
          ))}
        </motion.div>

        {/* Particle Effects */}
        <div className="absolute inset-0 pointer-events-none">
          {[...Array(20)].map((_, i) => (
            <motion.div
              key={i}
              className="absolute w-1 h-1 bg-primary-400 rounded-full opacity-30"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
              }}
              animate={{
                y: [0, -20, 0],
                opacity: [0.3, 0.8, 0.3],
              }}
              transition={{
                duration: 2 + Math.random() * 2,
                repeat: Infinity,
                delay: Math.random() * 2,
              }}
            />
          ))}
        </div>
      </motion.div>
    </Box>
  );
};

export default LoadingScreen;