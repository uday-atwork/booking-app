package com.booking.app.repository;

import com.booking.app.model.ShowSeatAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface SeatAvailabilityRepository extends JpaRepository<ShowSeatAvailability, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ShowSeatAvailability> findByShowIdAndSeatIdIgnoreCaseIn(Long showId, List<Long> seatIds);
}
