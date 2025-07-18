package io.github.disparter.tokugawa.discord.api.controllers;

import io.github.disparter.tokugawa.discord.api.dtos.ApiResponseDto;
import io.github.disparter.tokugawa.discord.api.dtos.UserDto;
import io.github.disparter.tokugawa.discord.core.models.User;
import io.github.disparter.tokugawa.discord.core.repositories.UserRepository;
import io.github.disparter.tokugawa.discord.core.services.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication API endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API endpoints")
@Slf4j
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get current user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserDto>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(
                    new ApiResponseDto<>(false, "Not authenticated", null)
                );
            }

            UserDetailsServiceImpl.UserPrincipal userPrincipal = 
                (UserDetailsServiceImpl.UserPrincipal) authentication.getPrincipal();

            User user = userRepository.findByDiscordId(userPrincipal.getDiscordId())
                .orElseThrow(() -> new RuntimeException("User not found"));

            UserDto userDto = convertToDto(user);

            return ResponseEntity.ok(new ApiResponseDto<>(true, "User retrieved successfully", userDto));
        } catch (Exception e) {
            log.error("Error retrieving current user: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Login with Discord OAuth2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Redirect to Discord OAuth2"),
        @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping("/login/discord")
    public ResponseEntity<Void> loginWithDiscord() {
        // This endpoint redirects to Discord OAuth2
        // The actual redirect is handled by Spring Security
        return ResponseEntity.status(302).build();
    }

    @Operation(summary = "Logout current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logout() {
        try {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Logout successful", null));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    /**
     * Convert User entity to UserDto.
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setDiscordId(user.getDiscordId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setDiscriminator(user.getDiscriminator());
        dto.setRoles(user.getRoles());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}