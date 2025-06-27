package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Relationship;
import io.github.disparter.tokugawa.discord.core.repositories.NPCRepository;
import io.github.disparter.tokugawa.discord.core.repositories.PlayerRepository;
import io.github.disparter.tokugawa.discord.core.repositories.RelationshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RelationshipServiceImplTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private NPCRepository npcRepository;

    @InjectMocks
    private RelationshipServiceImpl relationshipService;

    private Relationship testRelationship;
    private Player testPlayer;
    private NPC testNpc;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId(1L);

        testNpc = new NPC();
        testNpc.setId(1L);

        testRelationship = new Relationship();
        testRelationship.setId(1L);
        testRelationship.setPlayer(testPlayer);
        testRelationship.setNpc(testNpc);
        testRelationship.setAffinity(0);
        testRelationship.setStatus(Relationship.RelationshipStatus.STRANGER);
    }

    @Test
    void getRelationshipsForPlayer_ShouldReturnRelationshipList() {
        // Arrange
        Relationship relationship2 = new Relationship();
        relationship2.setId(2L);
        relationship2.setPlayer(testPlayer);

        NPC npc2 = new NPC();
        npc2.setId(2L);
        relationship2.setNpc(npc2);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(relationshipRepository.findByPlayer(testPlayer)).thenReturn(Arrays.asList(testRelationship, relationship2));

        // Act
        List<Relationship> result = relationshipService.getRelationshipsForPlayer(1L);

        // Assert
        assertEquals(2, result.size());
        verify(relationshipRepository, times(1)).findByPlayer(testPlayer);
    }

    @Test
    void getRelationship_ShouldReturnRelationship_WhenRelationshipExists() {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(npcRepository.findById(1L)).thenReturn(Optional.of(testNpc));
        when(relationshipRepository.findByPlayerAndNpc(testPlayer, testNpc)).thenReturn(Optional.of(testRelationship));

        // Act
        Relationship result = relationshipService.getRelationship(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPlayer, result.getPlayer());
        assertEquals(testNpc, result.getNpc());
        verify(relationshipRepository, times(1)).findByPlayerAndNpc(testPlayer, testNpc);
    }

    @Test
    void getRelationship_ShouldCreateNewRelationship_WhenRelationshipDoesNotExist() {
        // Arrange
        NPC npc2 = new NPC();
        npc2.setId(2L);

        Relationship newRelationship = new Relationship();
        newRelationship.setPlayer(testPlayer);
        newRelationship.setNpc(npc2);
        newRelationship.setAffinity(0);
        newRelationship.setStatus(Relationship.RelationshipStatus.STRANGER);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(npcRepository.findById(2L)).thenReturn(Optional.of(npc2));
        when(relationshipRepository.findByPlayerAndNpc(testPlayer, npc2)).thenReturn(Optional.empty());
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(newRelationship);

        // Act
        Relationship result = relationshipService.getRelationship(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(testPlayer, result.getPlayer());
        assertEquals(npc2, result.getNpc());
        verify(relationshipRepository, times(1)).findByPlayerAndNpc(testPlayer, npc2);
        verify(relationshipRepository, times(1)).save(any(Relationship.class));
    }

    @Test
    void save_ShouldReturnSavedRelationship() {
        // Arrange
        when(relationshipRepository.save(testRelationship)).thenReturn(testRelationship);

        // Act
        Relationship result = relationshipService.save(testRelationship);

        // Assert
        assertNotNull(result);
        assertEquals(testRelationship.getId(), result.getId());
        assertEquals(testRelationship.getPlayer(), result.getPlayer());
        assertEquals(testRelationship.getNpc(), result.getNpc());
        verify(relationshipRepository, times(1)).save(testRelationship);
    }
}
