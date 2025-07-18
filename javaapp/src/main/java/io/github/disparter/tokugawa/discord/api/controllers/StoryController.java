package io.github.disparter.tokugawa.discord.api.controllers;

import io.github.disparter.tokugawa.discord.api.dtos.ApiResponseDto;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Player;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.services.ChapterService;
import io.github.disparter.tokugawa.discord.core.services.EventService;
import io.github.disparter.tokugawa.discord.core.services.PlayerService;
import io.github.disparter.tokugawa.discord.core.services.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for story API endpoints.
 */
@RestController
@RequestMapping("/api/story")
@Tag(name = "Story", description = "Story API endpoints")
@Slf4j
public class StoryController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private EventService eventService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ProgressService progressService;

    @Operation(summary = "Get all chapters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chapters retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/chapters")
    public ResponseEntity<ApiResponseDto<List<Chapter>>> getChapters() {
        try {
            List<Chapter> chapters = chapterService.getAllChapters();
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error retrieving chapters: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Get current chapter for user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current chapter retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Chapter not found")
    })
    @GetMapping("/current-chapter")
    public ResponseEntity<ApiResponseDto<Chapter>> getCurrentChapter(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Player> playerOpt = playerService.getPlayerByUsername(userDetails.getUsername());
            if (playerOpt.isEmpty()) {
                return ResponseEntity.status(404).body(
                    new ApiResponseDto<>(false, "Player not found", null)
                );
            }

            Player player = playerOpt.get();
            Optional<Progress> progressOpt = progressService.getProgressByPlayerId(player.getId());
            
            if (progressOpt.isEmpty()) {
                // Return first chapter if no progress exists
                Optional<Chapter> firstChapter = chapterService.getChapterById("chapter_1");
                if (firstChapter.isPresent()) {
                    return ResponseEntity.ok(new ApiResponseDto<>(true, "Current chapter retrieved successfully", firstChapter.get()));
                } else {
                    return ResponseEntity.status(404).body(
                        new ApiResponseDto<>(false, "No chapters available", null)
                    );
                }
            }

            Progress progress = progressOpt.get();
            Optional<Chapter> currentChapter = chapterService.getChapterById(progress.getCurrentChapterId());
            
            if (currentChapter.isPresent()) {
                return ResponseEntity.ok(new ApiResponseDto<>(true, "Current chapter retrieved successfully", currentChapter.get()));
            } else {
                return ResponseEntity.status(404).body(
                    new ApiResponseDto<>(false, "Current chapter not found", null)
                );
            }
        } catch (Exception e) {
            log.error("Error retrieving current chapter: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Get current event for user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current event retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/current-event")
    public ResponseEntity<ApiResponseDto<Event>> getCurrentEvent(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Player> playerOpt = playerService.getPlayerByUsername(userDetails.getUsername());
            if (playerOpt.isEmpty()) {
                return ResponseEntity.status(404).body(
                    new ApiResponseDto<>(false, "Player not found", null)
                );
            }

            Player player = playerOpt.get();
            List<Event> activeEvents = eventService.getActiveEventsForPlayer(player.getId());
            
            if (!activeEvents.isEmpty()) {
                Event currentEvent = activeEvents.get(0); // Get the first active event
                return ResponseEntity.ok(new ApiResponseDto<>(true, "Current event retrieved successfully", currentEvent));
            } else {
                return ResponseEntity.status(404).body(
                    new ApiResponseDto<>(false, "No active events found", null)
                );
            }
        } catch (Exception e) {
            log.error("Error retrieving current event: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Get user progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Progress not found")
    })
    @GetMapping("/progress")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getProgress(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Player> playerOpt = playerService.getPlayerByUsername(userDetails.getUsername());
            if (playerOpt.isEmpty()) {
                return ResponseEntity.status(404).body(
                    new ApiResponseDto<>(false, "Player not found", null)
                );
            }

            Player player = playerOpt.get();
            Optional<Progress> progressOpt = progressService.getProgressByPlayerId(player.getId());
            
            if (progressOpt.isEmpty()) {
                // Create default progress
                Map<String, Object> defaultProgress = Map.of(
                    "id", 0,
                    "playerId", player.getId(),
                    "currentChapterId", "chapter_1",
                    "completedChapters", List.of(),
                    "currentSceneId", "scene_1",
                    "lastSaveDate", System.currentTimeMillis(),
                    "totalPlayTime", 0,
                    "choicesMade", Map.of(),
                    "flags", Map.of(),
                    "variables", Map.of(),
                    "chaptersCompleted", 0,
                    "relationshipsFormed", 0,
                    "playtimeHours", 0,
                    "playtimeMinutes", 0
                );
                
                return ResponseEntity.ok(new ApiResponseDto<>(true, "Default progress retrieved successfully", defaultProgress));
            }

            Progress progress = progressOpt.get();
            Map<String, Object> progressData = Map.of(
                "id", progress.getId(),
                "playerId", progress.getPlayerId(),
                "currentChapterId", progress.getCurrentChapterId(),
                "completedChapters", progress.getCompletedChapters(),
                "currentSceneId", progress.getCurrentSceneId(),
                "lastSaveDate", progress.getLastSaveDate(),
                "totalPlayTime", progress.getTotalPlayTime(),
                "choicesMade", progress.getChoicesMade(),
                "flags", progress.getFlags(),
                "variables", progress.getVariables(),
                "chaptersCompleted", progress.getCompletedChapters().size(),
                "relationshipsFormed", 3, // Mock data
                "playtimeHours", progress.getTotalPlayTime() / 3600,
                "playtimeMinutes", (progress.getTotalPlayTime() % 3600) / 60
            );
            
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Progress retrieved successfully", progressData));
        } catch (Exception e) {
            log.error("Error retrieving progress: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Make a choice in the story")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Choice made successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Choice not found")
    })
    @PostMapping("/choice")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> makeChoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        try {
            String choiceId = request.get("choiceId");
            if (choiceId == null || choiceId.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(false, "Choice ID is required", null)
                );
            }

            // Mock response for now - in real implementation, this would process the choice
            Map<String, Object> nextDialog = Map.of(
                "id", "dialog_" + System.currentTimeMillis(),
                "text", "You made an interesting choice! The story continues...",
                "speaker", "Narrator",
                "choices", List.of()
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "Choice made successfully", nextDialog));
        } catch (Exception e) {
            log.error("Error making choice: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }

    @Operation(summary = "Get next dialog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Next dialog retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/next-dialog")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getNextDialog(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Mock response for now - in real implementation, this would get the next dialog
            Map<String, Object> nextDialog = Map.of(
                "id", "dialog_" + System.currentTimeMillis(),
                "text", "The story continues with new adventures ahead...",
                "speaker", "Narrator",
                "choices", List.of(
                    Map.of(
                        "id", "choice_1",
                        "text", "Continue the adventure",
                        "consequence", "POSITIVE"
                    ),
                    Map.of(
                        "id", "choice_2",
                        "text", "Take a different path",
                        "consequence", "NEUTRAL"
                    )
                )
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "Next dialog retrieved successfully", nextDialog));
        } catch (Exception e) {
            log.error("Error retrieving next dialog: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                new ApiResponseDto<>(false, "Internal server error", null)
            );
        }
    }
}
