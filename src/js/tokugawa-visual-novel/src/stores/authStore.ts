import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthState, User } from '../types';

interface AuthStore extends AuthState {
  setUser: (user: User) => void;
  setToken: (token: string) => void;
  login: (user: User, token: string) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      setUser: (user: User) =>
        set(() => ({
          user,
          isAuthenticated: !!user,
        })),

      setToken: (token: string) =>
        set(() => ({
          token,
        })),

      login: (user: User, token: string) =>
        set(() => ({
          user,
          token,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        })),

      logout: () =>
        set(() => ({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
        })),

      setLoading: (loading: boolean) =>
        set(() => ({
          isLoading: loading,
        })),

      setError: (error: string | null) =>
        set(() => ({
          error,
          isLoading: false,
        })),

      clearError: () =>
        set(() => ({
          error: null,
        })),
    }),
    {
      name: 'tokugawa-auth',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);