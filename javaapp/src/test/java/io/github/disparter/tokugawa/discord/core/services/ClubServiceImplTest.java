package io.github.disparter.tokugawa.discord.core.services;

import io.github.disparter.tokugawa.discord.core.models.Club;
import io.github.disparter.tokugawa.discord.core.repositories.ClubRepository;
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
public class ClubServiceImplTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubServiceImpl clubService;

    private Club testClub;

    @BeforeEach
    void setUp() {
        testClub = new Club();
        testClub.setId(1L);
        testClub.setName("Test Club");
        testClub.setDescription("This is a test club");
    }

    @Test
    void getAllClubs_ShouldReturnAllClubs() {
        // Arrange
        Club club2 = new Club();
        club2.setId(2L);
        club2.setName("Another Club");
        
        when(clubRepository.findAll()).thenReturn(Arrays.asList(testClub, club2));

        // Act
        List<Club> result = clubService.getAllClubs();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Test Club")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Another Club")));
        verify(clubRepository, times(1)).findAll();
    }

    @Test
    void findClubById_ShouldReturnClub_WhenClubExists() {
        // Arrange
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));

        // Act
        Club result = clubService.findClubById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Club", result.getName());
        verify(clubRepository, times(1)).findById(1L);
    }

    @Test
    void findClubById_ShouldReturnNull_WhenClubDoesNotExist() {
        // Arrange
        when(clubRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Club result = clubService.findClubById(2L);

        // Assert
        assertNull(result);
        verify(clubRepository, times(1)).findById(2L);
    }

    @Test
    void save_ShouldReturnSavedClub() {
        // Arrange
        when(clubRepository.save(testClub)).thenReturn(testClub);

        // Act
        Club result = clubService.save(testClub);

        // Assert
        assertNotNull(result);
        assertEquals(testClub.getId(), result.getId());
        assertEquals(testClub.getName(), result.getName());
        verify(clubRepository, times(1)).save(testClub);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(clubRepository).deleteById(1L);

        // Act
        clubService.delete(1L);

        // Assert
        verify(clubRepository, times(1)).deleteById(1L);
    }
}