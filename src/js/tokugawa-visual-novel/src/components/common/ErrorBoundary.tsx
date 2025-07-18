import React, { Component } from 'react';
import type { ReactNode } from 'react';
import { Box, Typography, Button, Paper } from '@mui/material';
import { motion } from 'framer-motion';
import { ErrorOutline, Refresh } from '@mui/icons-material';

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: React.ErrorInfo | null;
}

interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      error,
      errorInfo: null,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    this.setState({
      error,
      errorInfo,
    });

    // Log error to console in development
    if (import.meta.env.DEV) {
      console.error('Error caught by boundary:', error, errorInfo);
    }

    // Call optional error handler
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // TODO: Send error to logging service in production
    // logErrorToService(error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  render() {
    if (this.state.hasError) {
      // Custom fallback UI
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default error UI
      return (
        <Box
          className="min-h-screen flex items-center justify-center bg-gradient-to-br from-dark-900 via-dark-800 to-dark-700 p-4"
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
          >
            <Paper
              className="glass max-w-md w-full p-8 text-center"
              elevation={0}
            >
              <motion.div
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ delay: 0.2, duration: 0.5 }}
                className="mb-6"
              >
                <ErrorOutline
                  sx={{
                    fontSize: 80,
                    color: 'error.main',
                    filter: 'drop-shadow(0 0 10px rgba(239, 68, 68, 0.5))',
                  }}
                />
              </motion.div>

              <motion.div
                initial={{ y: 20, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ delay: 0.4, duration: 0.5 }}
                className="mb-6"
              >
                <Typography
                  variant="h4"
                  component="h1"
                  className="text-red-400 font-serif mb-2"
                >
                  Oops! Something went wrong
                </Typography>
                <Typography
                  variant="body1"
                  className="text-gray-300 mb-4"
                >
                  We encountered an unexpected error. This shouldn't happen in a well-crafted visual novel!
                </Typography>

                {import.meta.env.DEV && this.state.error && (
                  <Box className="text-left bg-dark-800 p-4 rounded-lg mb-4">
                    <Typography
                      variant="body2"
                      className="text-red-300 font-mono text-xs"
                    >
                      <strong>Error:</strong> {this.state.error.message}
                    </Typography>
                    {this.state.errorInfo && (
                      <Typography
                        variant="body2"
                        className="text-gray-400 font-mono text-xs mt-2"
                      >
                        <strong>Stack:</strong>
                        <pre className="whitespace-pre-wrap text-xs">
                          {this.state.errorInfo.componentStack}
                        </pre>
                      </Typography>
                    )}
                  </Box>
                )}
              </motion.div>

              <motion.div
                initial={{ y: 20, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ delay: 0.6, duration: 0.5 }}
                className="space-y-3"
              >
                <Button
                  variant="contained"
                  color="primary"
                  onClick={this.handleRetry}
                  startIcon={<Refresh />}
                  className="w-full"
                  sx={{
                    background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%)',
                    },
                  }}
                >
                  Try Again
                </Button>
                <Button
                  variant="outlined"
                  color="secondary"
                  onClick={this.handleReload}
                  className="w-full"
                >
                  Reload Page
                </Button>
              </motion.div>

              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.8, duration: 0.5 }}
                className="mt-6"
              >
                <Typography
                  variant="body2"
                  className="text-gray-500 text-xs"
                >
                  If this problem persists, please contact support or check the console for more details.
                </Typography>
              </motion.div>
            </Paper>
          </motion.div>
        </Box>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;