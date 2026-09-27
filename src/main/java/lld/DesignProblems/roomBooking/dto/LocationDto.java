package lld.DesignProblems.roomBooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LocationDto {
    @NotBlank String buildingName;
    @NotBlank String floor;
}
