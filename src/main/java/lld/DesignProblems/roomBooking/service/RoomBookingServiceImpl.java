package lld.DesignProblems.roomBooking.service;

import lld.DesignProblems.roomBooking.dto.CreateRoomDto;
import lld.DesignProblems.roomBooking.enums.BookingStatus;
import lld.DesignProblems.roomBooking.model.Booking;
import lld.DesignProblems.roomBooking.model.Room;
import lld.DesignProblems.roomBooking.repository.BookingRepository;
import lld.DesignProblems.roomBooking.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomBookingServiceImpl implements RoomBookingService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomBookingServiceImpl(
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Room createRoom(CreateRoomDto request) {
        if(roomRepository.existsByRoomNameIgnoreCase(request.getRoomName())){
            throw new IllegalStateException(
                    "Room already exists with name: " + request.getRoomName());
        }
        return roomRepository.save(Room.from(request));
    }

    @Override
    public List<Room> getAvailableRooms(String building, String floor, LocalDateTime startTime, LocalDateTime endTime, int requiredCapacity) {
        if(startTime == null || endTime == null || !startTime.isBefore(endTime)){
            throw new IllegalArgumentException("Start time must be before end time");
        }

        return roomRepository.findAll()
                .stream()
                .filter(room -> room.getCapacity() >= requiredCapacity)
                .filter(room -> room.getLocation() != null)
                .filter(room -> room.getLocation().getBuildingName().equalsIgnoreCase(building))
                .filter(room -> room.getLocation().getFloor().equalsIgnoreCase(floor))
                .filter(room -> !bookingRepository
                        .existsByRoomRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                                room.getRoomId(),
                                BookingStatus.CONFIRMED,
                                endTime,
                                startTime))
                .toList();
    }

    @Override
    @Transactional
    public Booking createRoomBooking(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID is required");
        }

        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room does not exist with ID: " + roomId));

        boolean hasOverlappingBooking =
                bookingRepository.existsByRoomRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomId,
                        BookingStatus.CONFIRMED,
                        endTime,
                        startTime);

        if (hasOverlappingBooking) {
            throw new IllegalStateException(
                    "Room is already booked for the requested time");
        }

        return bookingRepository.save(
                Booking.confirmed(room, startTime, endTime));
    }

    @Override
    public Booking getRoomBooking(Long bookingId) {
        return bookingRepository.findWithRoomByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking does not exist with ID: " + bookingId));
    }

    @Override
    @Transactional
    public void cancelRoomBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking does not exist with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }
        booking.setStatus(BookingStatus.CANCELLED);
    }
}
