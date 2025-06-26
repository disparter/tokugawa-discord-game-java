package io.github.disparter.tokugawa.discord.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response DTO for standardized responses.
 *
 * @param <T> the type of data in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    
    /**
     * Create a successful response with data.
     *
     * @param data the response data
     * @param <T> the type of data
     * @return the API response
     */
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, "Success", data);
    }
    
    /**
     * Create a successful response with a message and data.
     *
     * @param message the success message
     * @param data the response data
     * @param <T> the type of data
     * @return the API response
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }
    
    /**
     * Create an error response with a message.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return the API response
     */
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null);
    }
}