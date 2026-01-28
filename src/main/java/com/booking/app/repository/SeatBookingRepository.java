package com.booking.app.repository;

import com.booking.app.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatBookingRepository extends JpaRepository<Booking, Long> {
}
