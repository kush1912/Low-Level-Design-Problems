package lld.DesignProblems.roomBooking.model;

import lld.DesignProblems.roomBooking.dto.CreateRoomDto;
import lld.DesignProblems.roomBooking.enums.Amenities;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false, unique = true, length = 100)
    private String roomName;

    @Column(nullable = false)
    private Integer capacity;

    @Embedded
    private Location location;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "room_amenities",
            joinColumns = @JoinColumn(name = "room_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", nullable = false)
    private Set<Amenities> amenities = new HashSet<>();

    public static Room from(CreateRoomDto request){
        Room room =  new Room();
        room.setAmenities(request.getAmenities());
        room.setCapacity(request.getCapacity());
        Location location = new Location();
        location.setBuildingName(request.getLocation().getBuildingName());
        location.setFloor(request.getLocation().getFloor());
        room.setLocation(location);
        room.setRoomName(request.getRoomName());
        return room;
    }
}
