package io.github.disparter.tokugawa.discord.api.dtos;

import io.github.disparter.tokugawa.discord.core.models.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for transferring event information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String eventId;
    private String name;
    private String title;
    private String description;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> rewards;
    
    /**
     * Convert an Event entity to an EventDto.
     *
     * @param event the Event entity
     * @return the EventDto
     */
    public static EventDto fromEntity(Event event) {
        if (event == null) {
            return null;
        }
        
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setEventId(event.getEventId());
        dto.setName(event.getName());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setType(event.getType() != null ? event.getType().name() : null);
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setRewards(event.getRewards());
        
        return dto;
    }
}