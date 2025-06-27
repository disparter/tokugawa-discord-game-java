package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerRepositoryTest {

    @Mock
    private PlayerRepository playerRepository;

    private Player testPlayer;
    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    void setUp() {
        // Set up test player
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setUserId("123456789");
        testPlayer.setName("TestPlayer");
        testPlayer.setLevel(1);
        testPlayer.setExp(0);
        testPlayer.setPoints(0);

        // Set up players for club tests
        player1 = new Player();
        player1.setId(2L);
        player1.setUserId("111");
        player1.setName("Player1");
        player1.setClubId("club1");

        player2 = new Player();
        player2.setId(3L);
        player2.setUserId("222");
        player2.setName("Player2");
        player2.setClubId("club1");

        player3 = new Player();
        player3.setId(4L);
        player3.setUserId("333");
        player3.setName("Player3");
        player3.setClubId("club2");

        // Set up common lenient stubs
        lenient().when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        lenient().when(playerRepository.findByUserId("123456789")).thenReturn(Optional.of(testPlayer));
        lenient().when(playerRepository.findByName("TestPlayer")).thenReturn(Optional.of(testPlayer));
        lenient().when(playerRepository.findByClubId("club1")).thenReturn(Arrays.asList(player1, player2));
        lenient().when(playerRepository.findByClubId("club2")).thenReturn(Arrays.asList(player3));
    }

    @Test
    void saveAndFindById_ShouldReturnPlayer() {
        // Act
        Optional<Player> foundPlayer = playerRepository.findById(1L);

        // Assert
        assertTrue(foundPlayer.isPresent());
        assertEquals("123456789", foundPlayer.get().getUserId());
        assertEquals("TestPlayer", foundPlayer.get().getName());
    }

    @Test
    void findByUserId_ShouldReturnPlayer() {
        // Act
        Optional<Player> foundPlayer = playerRepository.findByUserId("123456789");

        // Assert
        assertTrue(foundPlayer.isPresent());
        assertEquals("TestPlayer", foundPlayer.get().getName());
        assertEquals(1, foundPlayer.get().getLevel());
    }

    @Test
    void findByName_ShouldReturnPlayer() {
        // Act
        Optional<Player> foundPlayer = playerRepository.findByName("TestPlayer");

        // Assert
        assertTrue(foundPlayer.isPresent());
        assertEquals("123456789", foundPlayer.get().getUserId());
        assertEquals(1, foundPlayer.get().getLevel());
    }

    @Test
    void findByClubId_ShouldReturnPlayers() {
        // Act
        List<Player> club1Players = playerRepository.findByClubId("club1");
        List<Player> club2Players = playerRepository.findByClubId("club2");

        // Assert
        assertEquals(2, club1Players.size());
        assertEquals(1, club2Players.size());
        assertTrue(club1Players.stream().anyMatch(p -> p.getName().equals("Player1")));
        assertTrue(club1Players.stream().anyMatch(p -> p.getName().equals("Player2")));
        assertTrue(club2Players.stream().anyMatch(p -> p.getName().equals("Player3")));
    }
}
