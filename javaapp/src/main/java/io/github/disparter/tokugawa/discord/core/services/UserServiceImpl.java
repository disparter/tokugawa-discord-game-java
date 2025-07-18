package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.User;
import io.github.disparter.tokugawa.discord.core.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for User management operations.
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByDiscordId(String discordId) {
        log.debug("Finding user by Discord ID: {}", discordId);
        return userRepository.findByDiscordId(discordId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Override
    public boolean existsByDiscordId(String discordId) {
        log.debug("Checking if user exists by Discord ID: {}", discordId);
        return userRepository.existsByDiscordId(discordId);
    }

    @Override
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting user by ID: {}", id);
        userRepository.deleteById(id);
    }
}