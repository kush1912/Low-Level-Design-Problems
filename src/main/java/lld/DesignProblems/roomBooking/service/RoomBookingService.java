package lld.DesignProblems.roomBooking.service;

import lld.DesignProblems.roomBooking.dto.CreateRoomDto;
import lld.DesignProblems.roomBooking.model.Booking;
import lld.DesignProblems.roomBooking.model.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomBookingService {

    Room createRoom(CreateRoomDto request);

    List<Room> getAvailableRooms(
            String building,
            String floor,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int requiredCapacity);

    Booking createRoomBooking(
            Long roomId,
            LocalDateTime startTime,
            LocalDateTime endTime);

    Booking getRoomBooking(Long bookingId);

    void cancelRoomBooking(Long bookingId);
}
