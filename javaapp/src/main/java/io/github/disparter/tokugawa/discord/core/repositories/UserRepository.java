package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their Discord ID.
     *
     * @param discordId The Discord ID to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByDiscordId(String discordId);

    /**
     * Find a user by their email address.
     *
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists by Discord ID.
     *
     * @param discordId The Discord ID to check
     * @return true if user exists, false otherwise
     */
    boolean existsByDiscordId(String discordId);

    /**
     * Check if a user exists by email.
     *
     * @param email The email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
}