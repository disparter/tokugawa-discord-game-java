import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../services/auth/authService';
import { useAuthStore } from '../stores/authStore';
import LoadingScreen from '../components/common/LoadingScreen';

const AuthCallback: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setError } = useAuthStore();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const code = searchParams.get('code');
        const state = searchParams.get('state');
        const error = searchParams.get('error');

        // Handle Discord OAuth2 errors
        if (error) {
          throw new Error(`Discord OAuth2 error: ${error}`);
        }

        // Validate required parameters
        if (!code || !state) {
          throw new Error('Missing required OAuth2 parameters');
        }

        // Handle Discord OAuth2 callback
        const result = await authService.handleDiscordCallback(code, state);
        
        if (result.success) {
          // Redirect to dashboard on success
          navigate('/dashboard', { replace: true });
        } else {
          throw new Error(result.error || 'Authentication failed');
        }
      } catch (error: any) {
        console.error('OAuth callback error:', error);
        setError(error.message || 'Authentication failed');
        
        // Redirect to login page with error
        setTimeout(() => {
          navigate('/login', { replace: true });
        }, 3000);
      }
    };

    handleCallback();
  }, [searchParams, navigate, setError]);

  return (
    <LoadingScreen 
      message="Completing authentication..." 
    />
  );
};

export default AuthCallback;