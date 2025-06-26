package io.github.disparter.tokugawa.discord.api.dtos;

import io.github.disparter.tokugawa.discord.core.models.Progress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for transferring player progress information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDto {
    private Long id;
    private Long playerId;
    private String currentArc;
    private String currentChapter;
    private List<String> completedChapters;
    private List<String> completedArcs;
    private Map<String, String> choices;
    private Map<String, String> triggeredEvents;
    private double completionPercentage;
    
    /**
     * Convert a Progress entity to a ProgressDto.
     *
     * @param progress the Progress entity
     * @param completionPercentage the completion percentage
     * @return the ProgressDto
     */
    public static ProgressDto fromEntity(Progress progress, double completionPercentage) {
        if (progress == null) {
            return null;
        }
        
        ProgressDto dto = new ProgressDto();
        dto.setId(progress.getId());
        dto.setPlayerId(progress.getPlayer() != null ? progress.getPlayer().getId() : null);
        dto.setCurrentArc(progress.getCurrentArc());
        dto.setCurrentChapter(progress.getCurrentChapter());
        dto.setCompletedChapters(progress.getCompletedChapters());
        dto.setCompletedArcs(progress.getCompletedArcs());
        dto.setChoices(progress.getChoices());
        dto.setTriggeredEvents(progress.getTriggeredEvents());
        dto.setCompletionPercentage(completionPercentage);
        
        return dto;
    }
}