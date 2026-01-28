package com.booking.app.repository;

import com.booking.app.model.SeatAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SeatAvailability> findByShowIdAndSeatIdIn(Long showId, List<Long> seatIds);
}
