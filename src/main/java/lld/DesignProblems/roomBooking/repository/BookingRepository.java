package lld.DesignProblems.roomBooking.repository;

import lld.DesignProblems.roomBooking.enums.BookingStatus;
import lld.DesignProblems.roomBooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomRoomId(Long roomId);

    @EntityGraph(attributePaths = {"room", "room.amenities"})
    Optional<Booking> findWithRoomByBookingId(Long bookingId);

    boolean existsByRoomRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            BookingStatus status,
            LocalDateTime requestedEndTime,
            LocalDateTime requestedStartTime);
}
