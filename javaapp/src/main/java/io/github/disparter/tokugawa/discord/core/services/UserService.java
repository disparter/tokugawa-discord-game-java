package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.User;

import java.util.Optional;

/**
 * Service interface for User management operations.
 */
public interface UserService {

    /**
     * Find a user by Discord ID.
     *
     * @param discordId The Discord ID to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByDiscordId(String discordId);

    /**
     * Find a user by email.
     *
     * @param email The email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Create or update a user.
     *
     * @param user The user to save
     * @return The saved user
     */
    User saveUser(User user);

    /**
     * Check if a user exists by Discord ID.
     *
     * @param discordId The Discord ID to check
     * @return true if user exists, false otherwise
     */
    boolean existsByDiscordId(String discordId);

    /**
     * Get user by ID.
     *
     * @param id The user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Delete a user by ID.
     *
     * @param id The user ID to delete
     */
    void deleteById(Long id);
}