package io.github.disparter.tokugawa.discord.api.controllers;

import io.github.disparter.tokugawa.discord.api.dtos.ApiResponseDto;
import io.github.disparter.tokugawa.discord.api.dtos.ChapterDto;
import io.github.disparter.tokugawa.discord.api.dtos.ChoiceRequestDto;
import io.github.disparter.tokugawa.discord.api.dtos.EventDto;
import io.github.disparter.tokugawa.discord.api.dtos.ProgressDto;
import io.github.disparter.tokugawa.discord.core.models.Chapter;
import io.github.disparter.tokugawa.discord.core.models.Event;
import io.github.disparter.tokugawa.discord.core.models.Progress;
import io.github.disparter.tokugawa.discord.core.services.EventService;
import io.github.disparter.tokugawa.discord.core.services.NarrativeService;
import io.github.disparter.tokugawa.discord.core.services.ProgressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for story mode API endpoints.
 */
@RestController
@RequestMapping("/api/story")
@Tag(name = "Story", description = "Story mode API endpoints")
public class StoryController {

    private final NarrativeService narrativeService;
    private final ProgressService progressService;
    private final EventService eventService;

    @Autowired
    public StoryController(NarrativeService narrativeService, ProgressService progressService, EventService eventService) {
        this.narrativeService = narrativeService;
        this.progressService = progressService;
        this.eventService = eventService;
    }

    /**
     * Get all available chapters.
     *
     * @return list of all chapters
     */
    @Operation(summary = "Get all chapters", description = "Returns a list of all available chapters in the game")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chapters retrieved successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/chapters")
    public ResponseEntity<ApiResponseDto<List<ChapterDto>>> getAllChapters() {
        List<Chapter> chapters = narrativeService.getAllChapters();
        List<ChapterDto> chapterDtos = chapters.stream()
                .map(ChapterDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success(chapterDtos));
    }

    /**
     * Get a specific chapter by ID.
     *
     * @param id the chapter ID
     * @return the chapter details
     */
    @Operation(summary = "Get chapter by ID", description = "Returns the details of a specific chapter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chapter found",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class))),
        @ApiResponse(responseCode = "200", description = "Chapter not found (with error message)",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/chapters/{id}")
    public ResponseEntity<ApiResponseDto<ChapterDto>> getChapterById(
            @Parameter(description = "ID of the chapter to retrieve", required = true)
            @PathVariable Long id) {
        Chapter chapter = narrativeService.findChapterById(id);

        if (chapter == null) {
            return ResponseEntity.ok(ApiResponseDto.error("Chapter not found"));
        }

        return ResponseEntity.ok(ApiResponseDto.success(ChapterDto.fromEntity(chapter)));
    }

    /**
     * Start a chapter for a player.
     *
     * @param id the chapter ID
     * @param playerId the player ID
     * @return the started chapter
     */
    @Operation(summary = "Start a chapter", description = "Starts a specific chapter for a player")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chapter started successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class))),
        @ApiResponse(responseCode = "200", description = "Failed to start chapter (with error message)",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/chapters/{id}/start")
    public ResponseEntity<ApiResponseDto<ChapterDto>> startChapter(
            @Parameter(description = "ID of the chapter to start", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID of the player", required = true)
            @RequestParam Long playerId) {

        try {
            Chapter chapter = narrativeService.startChapter(id, playerId);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter started successfully", ChapterDto.fromEntity(chapter)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseDto.error("Failed to start chapter: " + e.getMessage()));
        }
    }

    /**
     * Make a choice in a chapter.
     *
     * @param id the chapter ID
     * @param request the choice request
     * @return the updated progress
     */
    @Operation(summary = "Make a choice in a chapter", description = "Allows a player to make a choice in a specific chapter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Choice recorded successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class))),
        @ApiResponse(responseCode = "200", description = "Failed to record choice (with error message)",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/chapters/{id}/choices")
    public ResponseEntity<ApiResponseDto<ProgressDto>> makeChoice(
            @Parameter(description = "ID of the chapter", required = true)
            @PathVariable Long id,
            @Parameter(description = "Choice request containing player ID and choice ID", required = true)
            @RequestBody ChoiceRequestDto request) {

        try {
            // Update progress with the player's choice
            Progress progress = progressService.getSpecificProgress(request.getPlayerId(), id, "CHAPTER");

            if (progress == null) {
                return ResponseEntity.ok(ApiResponseDto.error("No progress found for this chapter"));
            }

            // Add the choice to the player's progress
            progress.getChoices().put(id.toString(), request.getChoiceId());
            progress = progressService.save(progress);

            // Get completion percentage
            double completionPercentage = progressService.getCompletionPercentage(request.getPlayerId());

            return ResponseEntity.ok(ApiResponseDto.success(
                    "Choice recorded successfully",
                    ProgressDto.fromEntity(progress, completionPercentage)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseDto.error("Failed to record choice: " + e.getMessage()));
        }
    }

    /**
     * Get player progress.
     *
     * @param playerId the player ID
     * @return the player's progress
     */
    @Operation(summary = "Get player progress", description = "Returns the player's progress in the story")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progress retrieved successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class))),
        @ApiResponse(responseCode = "200", description = "No progress found (with error message)",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/progress")
    public ResponseEntity<ApiResponseDto<ProgressDto>> getPlayerProgress(
            @Parameter(description = "ID of the player", required = true)
            @RequestParam Long playerId) {
        try {
            List<Progress> progressList = progressService.getPlayerProgress(playerId);

            if (progressList.isEmpty()) {
                return ResponseEntity.ok(ApiResponseDto.error("No progress found for this player"));
            }

            // Get the first progress record (assuming one player has one progress record)
            Progress progress = progressList.get(0);

            // Get completion percentage
            double completionPercentage = progressService.getCompletionPercentage(playerId);

            return ResponseEntity.ok(ApiResponseDto.success(
                    ProgressDto.fromEntity(progress, completionPercentage)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseDto.error("Failed to get progress: " + e.getMessage()));
        }
    }

    /**
     * Get all available events.
     *
     * @return list of all events
     */
    @Operation(summary = "Get all events", description = "Returns a list of all available events in the game")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/events")
    public ResponseEntity<ApiResponseDto<List<EventDto>>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventDto> eventDtos = events.stream()
                .map(EventDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success(eventDtos));
    }

    /**
     * Trigger an event for a player.
     *
     * @param id the event ID
     * @param playerId the player ID
     * @return the triggered event
     */
    @Operation(summary = "Trigger an event", description = "Triggers a specific event for a player")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event triggered successfully",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class))),
        @ApiResponse(responseCode = "200", description = "Failed to trigger event (with error message)",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/events/{id}/trigger")
    public ResponseEntity<ApiResponseDto<EventDto>> triggerEvent(
            @Parameter(description = "ID of the event to trigger", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID of the player", required = true)
            @RequestParam Long playerId) {

        try {
            Event event = eventService.triggerEvent(id, playerId);
            return ResponseEntity.ok(ApiResponseDto.success("Event triggered successfully", EventDto.fromEntity(event)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseDto.error("Failed to trigger event: " + e.getMessage()));
        }
    }
}
