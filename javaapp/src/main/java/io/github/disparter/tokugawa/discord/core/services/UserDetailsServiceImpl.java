package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.User;
import io.github.disparter.tokugawa.discord.core.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDetailsService implementation for Spring Security.
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String discordId) throws UsernameNotFoundException {
        User user = userRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with Discord ID: " + discordId));

        return UserPrincipal.create(user);
    }

    /**
     * Inner class representing the UserDetails implementation.
     */
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String discordId;
        private String username;
        private String email;
        private List<GrantedAuthority> authorities;

        public UserPrincipal(Long id, String discordId, String username, String email, List<GrantedAuthority> authorities) {
            this.id = id;
            this.discordId = discordId;
            this.username = username;
            this.email = email;
            this.authorities = authorities;
        }

        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());

            return new UserPrincipal(
                    user.getId(),
                    user.getDiscordId(),
                    user.getUsername(),
                    user.getEmail(),
                    authorities
            );
        }

        public Long getId() {
            return id;
        }

        public String getDiscordId() {
            return discordId;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String getUsername() {
            return discordId; // Use Discord ID as username for authentication
        }

        @Override
        public String getPassword() {
            return null; // No password for OAuth2
        }

        @Override
        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}