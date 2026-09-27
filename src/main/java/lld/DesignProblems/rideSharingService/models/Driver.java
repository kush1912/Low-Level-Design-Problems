package lld.DesignProblems.rideSharingService.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Driver {
    private final String driverId;
    private final String name;
    private final String vehicleNumber;
    private final List<String> rideIds;

    public Driver(
            String driverId,
            String name,
            String vehicleNumber
    ) {
        this.driverId = driverId;
        this.name = name;
        this.vehicleNumber = vehicleNumber;
        this.rideIds = new ArrayList<>();
    }
}
