package io.github.disparter.tokugawa.discord.api.controllers;

import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.services.EventService;
import io.github.disparter.tokugawa.discord.core.services.NarrativeService;
import io.github.disparter.tokugawa.discord.core.services.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class StoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NarrativeService narrativeService;

    @Mock
    private ProgressService progressService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private StoryController storyController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storyController).build();

        // Set up common lenient stubs
        List<Chapter> chapters = new ArrayList<>();
        lenient().when(narrativeService.getAllChapters()).thenReturn(chapters);
    }

    @Test
    void getAllChapters_ShouldReturnOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/story/chapters")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
