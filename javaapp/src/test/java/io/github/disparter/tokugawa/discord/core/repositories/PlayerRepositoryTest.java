package io.github.disparter.tokugawa.discord.core.repositories;

import io.github.disparter.tokugawa.discord.core.models.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void saveAndFindById_ShouldReturnPlayer() {
        // Arrange
        Player player = new Player();
        player.setUserId("123456789");
        player.setName("TestPlayer");
        player.setLevel(1);
        player.setExp(0);
        player.setPoints(0);

        // Act
        Player savedPlayer = entityManager.persistAndFlush(player);
        Optional<Player> foundPlayer = playerRepository.findById(savedPlayer.getId());

        // Assert
        assertTrue(foundPlayer.isPresent());
        assertEquals("123456789", foundPlayer.get().getUserId());
        assertEquals("TestPlayer", foundPlayer.get().getName());
    }

    @Test
    void findByUserId_ShouldReturnPlayer() {
        // Arrange
        Player player = new Player();
        player.setUserId("987654321");
        player.setName("AnotherPlayer");
        player.setLevel(2);
        player.setExp(100);
        player.setPoints(50);

        // Act
        entityManager.persistAndFlush(player);
        Optional<Player> foundPlayer = playerRepository.findByUserId("987654321");

        // Assert
        assertTrue(foundPlayer.isPresent());
        assertEquals("AnotherPlayer", foundPlayer.get().getName());
        assertEquals(2, foundPlayer.get().getLevel());
    }

    @Test
    void findByName_ShouldReturnPlayer() {
        // Arrange
        Player player = new Player();
        player.setUserId("111222333");
        player.setName("UniquePlayer");
        player.setLevel(3);
        player.setExp(200);
        player.setPoints(100);

        // Act
        entityManager.persistAndFlush(player);
        Optional<Player> foundPlayer = playerRepository.findByName("UniquePlayer");

        // Assert
        assertTrue(foundPlayer.isPresent());
        assertEquals("111222333", foundPlayer.get().getUserId());
        assertEquals(3, foundPlayer.get().getLevel());
    }

    @Test
    void findByClubId_ShouldReturnPlayers() {
        // Arrange
        Player player1 = new Player();
        player1.setUserId("111");
        player1.setName("Player1");
        player1.setClubId("club1");

        Player player2 = new Player();
        player2.setUserId("222");
        player2.setName("Player2");
        player2.setClubId("club1");

        Player player3 = new Player();
        player3.setUserId("333");
        player3.setName("Player3");
        player3.setClubId("club2");

        // Act
        entityManager.persistAndFlush(player1);
        entityManager.persistAndFlush(player2);
        entityManager.persistAndFlush(player3);

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
