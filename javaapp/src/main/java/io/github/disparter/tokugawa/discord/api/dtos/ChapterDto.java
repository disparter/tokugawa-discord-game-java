package io.github.disparter.tokugawa.discord.api.dtos;

import io.github.disparter.tokugawa.discord.core.models.Chapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for transferring chapter information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDto {
    private Long id;
    private String chapterId;
    private String title;
    private String description;
    private String type;
    private String arcId;
    private Integer phase;
    private Integer completionExp;
    private Integer completionReward;
    private List<String> choices;
    
    /**
     * Convert a Chapter entity to a ChapterDto.
     *
     * @param chapter the Chapter entity
     * @return the ChapterDto
     */
    public static ChapterDto fromEntity(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        
        ChapterDto dto = new ChapterDto();
        dto.setId(chapter.getId());
        dto.setChapterId(chapter.getChapterId());
        dto.setTitle(chapter.getTitle());
        dto.setDescription(chapter.getDescription());
        dto.setType(chapter.getType() != null ? chapter.getType().name() : null);
        dto.setArcId(chapter.getArcId());
        dto.setPhase(chapter.getPhase());
        dto.setCompletionExp(chapter.getCompletionExp());
        dto.setCompletionReward(chapter.getCompletionReward());
        dto.setChoices(chapter.getChoices());
        
        return dto;
    }
}