package io.github.disparter.tokugawa.discord.config;

import io.github.disparter.tokugawa.discord.core.models.User;
import io.github.disparter.tokugawa.discord.core.repositories.UserRepository;
import io.github.disparter.tokugawa.discord.utils.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * OAuth2 Authentication Success Handler for Discord login.
 */
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract user info from Discord OAuth2 response
        String discordId = oAuth2User.getAttribute("id");
        String username = oAuth2User.getAttribute("username");
        String email = oAuth2User.getAttribute("email");
        String avatar = oAuth2User.getAttribute("avatar");
        String discriminator = oAuth2User.getAttribute("discriminator");

        // Create or update user
        User user = userRepository.findByDiscordId(discordId)
                .orElse(User.builder()
                        .discordId(discordId)
                        .username(username)
                        .email(email)
                        .avatar(avatar)
                        .discriminator(discriminator)
                        .roles(Set.of(User.Role.USER))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        // Update user info if it exists
        if (user.getId() != null) {
            user.setUsername(username);
            user.setEmail(email);
            user.setAvatar(avatar);
            user.setDiscriminator(discriminator);
            user.setUpdatedAt(LocalDateTime.now());
        }

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtils.generateTokenFromUsername(discordId);

        // Get the first allowed origin for redirect
        String frontendUrl = allowedOrigins.split(",")[0];
        
        // Redirect to frontend with token
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("token", token)
                .build().toUriString();

        log.info("OAuth2 authentication successful for user: {}, redirecting to: {}", username, targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}