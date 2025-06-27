package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Relationship;
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

    @InjectMocks
    private RelationshipServiceImpl relationshipService;

    private Relationship testRelationship;

    @BeforeEach
    void setUp() {
        testRelationship = new Relationship();
        testRelationship.setId(1L);
        testRelationship.setPlayerId(1L);
        testRelationship.setNpcId(1L);
        testRelationship.setLevel(1);
        testRelationship.setPoints(0);
    }

    @Test
    void getPlayerRelationships_ShouldReturnRelationshipList() {
        // Arrange
        Relationship relationship2 = new Relationship();
        relationship2.setId(2L);
        relationship2.setPlayerId(1L);
        relationship2.setNpcId(2L);
        
        when(relationshipRepository.findByPlayerId(1L)).thenReturn(Arrays.asList(testRelationship, relationship2));

        // Act
        List<Relationship> result = relationshipService.getPlayerRelationships(1L);

        // Assert
        assertEquals(2, result.size());
        verify(relationshipRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void getSpecificRelationship_ShouldReturnRelationship_WhenRelationshipExists() {
        // Arrange
        when(relationshipRepository.findByPlayerIdAndNpcId(1L, 1L)).thenReturn(Optional.of(testRelationship));

        // Act
        Relationship result = relationshipService.getSpecificRelationship(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPlayerId());
        assertEquals(1L, result.getNpcId());
        verify(relationshipRepository, times(1)).findByPlayerIdAndNpcId(1L, 1L);
    }

    @Test
    void getSpecificRelationship_ShouldReturnNull_WhenRelationshipDoesNotExist() {
        // Arrange
        when(relationshipRepository.findByPlayerIdAndNpcId(1L, 2L)).thenReturn(Optional.empty());

        // Act
        Relationship result = relationshipService.getSpecificRelationship(1L, 2L);

        // Assert
        assertNull(result);
        verify(relationshipRepository, times(1)).findByPlayerIdAndNpcId(1L, 2L);
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
        assertEquals(testRelationship.getPlayerId(), result.getPlayerId());
        verify(relationshipRepository, times(1)).save(testRelationship);
    }
}