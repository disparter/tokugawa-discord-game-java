package io.github.disparter.tokugawa.discord.api.controllers;

import io.github.disparter.tokugawa.discord.api.dtos.ApiResponseDto;
import io.github.disparter.tokugawa.discord.core.models.NPC;
import io.github.disparter.tokugawa.discord.core.services.NPCService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for gallery API endpoints.
 */
@RestController
@RequestMapping("/api/gallery")
@Tag(name = "Gallery", description = "Gallery API endpoints")
@Slf4j
public class GalleryController {

    @Autowired
    private NPCService npcService;

    @Operation(summary = "Get unlocked characters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Characters retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/characters")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> getCharacters() {
        try {
            List<NPC> npcs = npcService.getAllNPCs();
            List<Map<String, Object>> characters = npcs.stream()
                .map(npc -> Map.of(
                    "id", npc.getId().toString(),
                    "name", npc.getName(),
                    "description", npc.getDescription(),
                    "portrait", "/api/assets/characters/" + npc.getName().toLowerCase() + ".png",
                    "unlocked", true, // For now, all characters are unlocked
                    "traits", List.of("Friendly", "Mysterious") // Mock traits
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponseDto<>(true, "Characters retrieved successfully", characters));
        } catch (Exception e) {
            log.error("Error retrieving characters: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Get unlocked scenes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scenes retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/scenes")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> getScenes() {
        try {
            // Mock scene data - in real implementation, this would come from a Scene service
            List<Map<String, Object>> scenes = List.of(
                Map.of(
                    "id", "scene_1",
                    "title", "Cherry Blossom Garden",
                    "description", "A beautiful scene in the school garden during spring",
                    "image", "/api/assets/scenes/cherry_blossom_garden.jpg",
                    "chapter", "Chapter 1",
                    "unlocked", true,
                    "unlockDate", "2024-01-15"
                ),
                Map.of(
                    "id", "scene_2",
                    "title", "Classroom Encounter",
                    "description", "An important conversation in the classroom",
                    "image", "/api/assets/scenes/classroom_encounter.jpg",
                    "chapter", "Chapter 2",
                    "unlocked", true,
                    "unlockDate", "2024-01-20"
                ),
                Map.of(
                    "id", "scene_3",
                    "title", "Festival Night",
                    "description", "A magical evening at the school festival",
                    "image", "/api/assets/scenes/festival_night.jpg",
                    "chapter", "Chapter 3",
                    "unlocked", false,
                    "unlockDate", null
                )
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "Scenes retrieved successfully", scenes));
        } catch (Exception e) {
            log.error("Error retrieving scenes: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Get unlocked music")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Music retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/music")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> getMusic() {
        try {
            // Mock music data - in real implementation, this would come from a Music service
            List<Map<String, Object>> musicList = List.of(
                Map.of(
                    "id", "music_1",
                    "title", "Peaceful Moments",
                    "composer", "Tokugawa Soundtrack Team",
                    "duration", "3:45",
                    "cover", "/api/assets/music/peaceful_moments_cover.jpg",
                    "unlocked", true
                ),
                Map.of(
                    "id", "music_2",
                    "title", "Dramatic Encounter",
                    "composer", "Tokugawa Soundtrack Team",
                    "duration", "4:20",
                    "cover", "/api/assets/music/dramatic_encounter_cover.jpg",
                    "unlocked", true
                ),
                Map.of(
                    "id", "music_3",
                    "title", "Love Theme",
                    "composer", "Tokugawa Soundtrack Team",
                    "duration", "5:15",
                    "cover", "/api/assets/music/love_theme_cover.jpg",
                    "unlocked", false
                )
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "Music retrieved successfully", musicList));
        } catch (Exception e) {
            log.error("Error retrieving music: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }
}