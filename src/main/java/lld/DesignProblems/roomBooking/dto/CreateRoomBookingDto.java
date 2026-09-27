package lld.DesignProblems.roomBooking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateRoomBookingDto(
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {
}
