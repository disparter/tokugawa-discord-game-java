package io.github.disparter.tokugawa.discord.api.dtos;

import io.github.disparter.tokugawa.discord.core.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object for User information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String discordId;
    private String username;
    private String email;
    private String avatar;
    private String discriminator;
    private Set<User.Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}