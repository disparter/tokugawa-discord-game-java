import { apiClient } from '../api/client';
import { useAuthStore } from '../../stores/authStore';
import type { 
  User, 
  DiscordOAuthConfig, 
  DiscordTokenResponse, 
  DiscordUser,
  ApiResponse 
} from '../../types';

// Discord OAuth2 Configuration
const DISCORD_CLIENT_ID = import.meta.env.VITE_DISCORD_CLIENT_ID || '';
const DISCORD_REDIRECT_URI = import.meta.env.VITE_DISCORD_REDIRECT_URI || `${window.location.origin}/auth/callback`;
const DISCORD_SCOPES = ['identify', 'email'];

const discordConfig: DiscordOAuthConfig = {
  clientId: DISCORD_CLIENT_ID,
  redirectUri: DISCORD_REDIRECT_URI,
  scopes: DISCORD_SCOPES,
};

export class AuthService {
  private static instance: AuthService;
  private authStore = useAuthStore;

  private constructor() {}

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  /**
   * Generate Discord OAuth2 authorization URL
   */
  generateDiscordAuthUrl(): string {
    const params = new URLSearchParams({
      client_id: discordConfig.clientId,
      redirect_uri: discordConfig.redirectUri,
      response_type: 'code',
      scope: discordConfig.scopes.join(' '),
      state: this.generateState(),
    });

    return `https://discord.com/api/oauth2/authorize?${params.toString()}`;
  }

  /**
   * Handle Discord OAuth2 callback
   */
  async handleDiscordCallback(code: string, state: string): Promise<ApiResponse<User>> {
    try {
      // Verify state parameter
      if (!this.verifyState(state)) {
        return {
          success: false,
          error: 'Invalid state parameter',
        };
      }

      this.authStore.getState().setLoading(true);

      // Exchange code for access token
      const tokenResponse = await this.exchangeCodeForToken(code);
      if (!tokenResponse.success || !tokenResponse.data) {
        return {
          success: false,
          error: tokenResponse.error || 'Failed to exchange code for token',
        };
      }

      // Get Discord user info
      const discordUser = await this.getDiscordUserInfo(tokenResponse.data.access_token);
      if (!discordUser.success || !discordUser.data) {
        return {
          success: false,
          error: discordUser.error || 'Failed to get Discord user info',
        };
      }

      // Authenticate with backend
      const authResponse = await this.authenticateWithBackend(discordUser.data, tokenResponse.data);
      if (!authResponse.success || !authResponse.data) {
        return {
          success: false,
          error: authResponse.error || 'Failed to authenticate with backend',
        };
      }

      // Store user and token
      this.authStore.getState().login(authResponse.data.user, authResponse.data.token);

      return {
        success: true,
        data: authResponse.data.user,
      };
    } catch (error: any) {
      this.authStore.getState().setError(error.message || 'Authentication failed');
      return {
        success: false,
        error: error.message || 'Authentication failed',
      };
    } finally {
      this.authStore.getState().setLoading(false);
    }
  }

  /**
   * Exchange authorization code for access token
   */
  private async exchangeCodeForToken(code: string): Promise<ApiResponse<DiscordTokenResponse>> {
    const params = new URLSearchParams({
      client_id: discordConfig.clientId,
      client_secret: import.meta.env.VITE_DISCORD_CLIENT_SECRET || '',
      grant_type: 'authorization_code',
      code,
      redirect_uri: discordConfig.redirectUri,
    });

    try {
      const response = await fetch('https://discord.com/api/oauth2/token', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params.toString(),
      });

      if (!response.ok) {
        throw new Error(`Discord API error: ${response.status}`);
      }

      const data = await response.json();
      return {
        success: true,
        data,
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.message || 'Failed to exchange code for token',
      };
    }
  }

  /**
   * Get Discord user information
   */
  private async getDiscordUserInfo(accessToken: string): Promise<ApiResponse<DiscordUser>> {
    try {
      const response = await fetch('https://discord.com/api/users/@me', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Discord API error: ${response.status}`);
      }

      const data = await response.json();
      return {
        success: true,
        data,
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.message || 'Failed to get Discord user info',
      };
    }
  }

  /**
   * Authenticate with backend using Discord user info
   */
  private async authenticateWithBackend(
    discordUser: DiscordUser,
    tokenData: DiscordTokenResponse
  ): Promise<ApiResponse<{ user: User; token: string }>> {
    return apiClient.post('/api/auth/discord', {
      discordId: discordUser.id,
      username: discordUser.username,
      discriminator: discordUser.discriminator,
      avatar: discordUser.avatar,
      email: discordUser.email,
      accessToken: tokenData.access_token,
      refreshToken: tokenData.refresh_token,
    });
  }

  /**
   * Refresh authentication token
   */
  async refreshToken(): Promise<ApiResponse<string>> {
    const currentToken = this.authStore.getState().token;
    if (!currentToken) {
      return {
        success: false,
        error: 'No token to refresh',
      };
    }

    try {
      const response = await apiClient.post<{ token: string }>('/api/auth/refresh', {
        token: currentToken,
      });

      if (response.success && response.data) {
        this.authStore.getState().setToken(response.data.token);
        return {
          success: true,
          data: response.data.token,
        };
      }

      return {
        success: false,
        error: response.error || 'Failed to refresh token',
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.message || 'Failed to refresh token',
      };
    }
  }

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    try {
      // Call backend logout endpoint
      await apiClient.post('/api/auth/logout');
    } catch (error) {
      // Continue with logout even if backend call fails
      console.error('Logout error:', error);
    } finally {
      // Clear local state
      this.authStore.getState().logout();
      // Remove token from API client
      apiClient.removeAuthToken();
    }
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.authStore.getState().user;
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.authStore.getState().isAuthenticated;
  }

  /**
   * Get current token
   */
  getToken(): string | null {
    return this.authStore.getState().token;
  }

  /**
   * Generate state parameter for OAuth2
   */
  private generateState(): string {
    const state = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    localStorage.setItem('discord_oauth_state', state);
    return state;
  }

  /**
   * Verify state parameter
   */
  private verifyState(state: string): boolean {
    const storedState = localStorage.getItem('discord_oauth_state');
    localStorage.removeItem('discord_oauth_state');
    return storedState === state;
  }

  /**
   * Initialize authentication from stored data
   */
  async initializeAuth(): Promise<void> {
    const token = this.authStore.getState().token;
    const user = this.authStore.getState().user;

    if (token && user) {
      // Set token in API client
      apiClient.setAuthToken(token);
      
      // Verify token is still valid
      try {
        const response = await apiClient.get<User>('/api/auth/me');
        if (response.success && response.data) {
          this.authStore.getState().setUser(response.data);
        } else {
          // Token is invalid, logout
          this.logout();
        }
      } catch (error) {
        // Token is invalid, logout
        this.logout();
      }
    }
  }

  /**
   * Redirect to Discord OAuth2 authorization
   */
  redirectToDiscordAuth(): void {
    const authUrl = this.generateDiscordAuthUrl();
    window.location.href = authUrl;
  }

  /**
   * Check if Discord OAuth2 is configured
   */
  isDiscordConfigured(): boolean {
    return !!(DISCORD_CLIENT_ID && discordConfig.redirectUri);
  }
}

// Export singleton instance
export const authService = AuthService.getInstance();

// Export default
export default authService;