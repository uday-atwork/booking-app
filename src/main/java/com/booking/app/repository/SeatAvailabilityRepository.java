package com.booking.app.repository;

import com.booking.app.model.SeatAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SeatAvailability> findByShowIdAndSeatIdIn(Long showId, List<Long> seatIds);
}
