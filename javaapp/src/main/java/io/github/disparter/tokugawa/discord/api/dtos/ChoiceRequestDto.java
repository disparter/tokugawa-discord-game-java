package io.github.disparter.tokugawa.discord.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving player choice requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceRequestDto {
    private Long playerId;
    private String choiceId;
}