package lld.DesignProblems.inventoryManagementSystem.exception;

import lombok.Getter;

import java.time.Instant;

/**
 * Uniform error payload returned for every failure by {@link GlobalExceptionHandler},
 * so API consumers only need to handle one error shape.
 */
@Getter
public class ApiError {

    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
