package lld.DesignProblems.roomBooking.repository;

import lld.DesignProblems.roomBooking.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomNameIgnoreCase(String roomName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Room> findByRoomId(Long roomId);
}
