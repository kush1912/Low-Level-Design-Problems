package lld.DesignProblems.roomBooking.dto;

import lld.DesignProblems.roomBooking.enums.Amenities;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.util.Set;

@Getter
public class CreateRoomDto {
    @NotBlank
    private String roomName;
    @Valid
    private LocationDto location;
    @Positive
    private Integer capacity;
    private Set<Amenities> amenities;
}
