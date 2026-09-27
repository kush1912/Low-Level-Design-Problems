package lld.DesignProblems.roomBooking.controller;

import lld.DesignProblems.roomBooking.dto.CreateRoomBookingDto;
import lld.DesignProblems.roomBooking.dto.CreateRoomDto;
import lld.DesignProblems.roomBooking.model.Booking;
import lld.DesignProblems.roomBooking.model.Room;
import lld.DesignProblems.roomBooking.service.RoomBookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RoomController {

    private final RoomBookingService roomBookingService;

    public RoomController(RoomBookingService roomBookingService) {
        this.roomBookingService = roomBookingService;
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(
            @Valid @RequestBody CreateRoomDto request) {

        Room room = roomBookingService.createRoom(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(room);
    }

    @GetMapping("/rooms/available")
    public ResponseEntity<List<Room>> getAvailableRooms(
            @RequestParam String building,
            @RequestParam String floor,
            @RequestParam int requiredCapacity,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {

        List<Room> rooms = roomBookingService.getAvailableRooms(
                building,
                floor,
                startTime,
                endTime,
                requiredCapacity);

        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/rooms/{roomId}/bookings")
    public ResponseEntity<Booking> createBooking(
            @PathVariable Long roomId,
            @Valid @RequestBody CreateRoomBookingDto request) {

        Booking booking = roomBookingService.createRoomBooking(
                roomId,
                request.startTime(),
                request.endTime());

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<Booking> getBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                roomBookingService.getRoomBooking(bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId) {

        roomBookingService.cancelRoomBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

}
