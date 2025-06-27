package io.github.disparter.tokugawa.discord.api.controllers;

import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.services.EventService;
import io.github.disparter.tokugawa.discord.core.services.NarrativeService;
import io.github.disparter.tokugawa.discord.core.services.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = io.github.disparter.tokugawa.discord.config.TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NarrativeService narrativeService;

    @MockBean
    private ProgressService progressService;

    @MockBean
    private EventService eventService;

    @Test
    void getAllChapters_ShouldReturnOk() throws Exception {
        // Arrange
        List<Chapter> chapters = new ArrayList<>();
        when(narrativeService.getAllChapters()).thenReturn(chapters);

        // Act & Assert
        mockMvc.perform(get("/api/story/chapters"))
                .andExpect(status().isOk());
    }
}
