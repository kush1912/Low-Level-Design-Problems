package lld.DesignProblems.roomBooking.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class Location {
    private String buildingName;
    private String floor;
}
