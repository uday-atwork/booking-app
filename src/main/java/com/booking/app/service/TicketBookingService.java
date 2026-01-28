package com.booking.app.service;

import com.booking.app.constant.BookingStatus;
import com.booking.app.constant.SeatStatus;
import com.booking.app.dto.request.BookTicketRequest;
import com.booking.app.dto.response.BookTicketResponse;
import com.booking.app.model.Booking;
import com.booking.app.model.SeatAvailability;
import com.booking.app.model.Show;
import com.booking.app.model.User;
import com.booking.app.repository.SeatAvailabilityRepository;
import com.booking.app.repository.SeatBookingRepository;
import com.booking.app.repository.ShowRepository;
import com.booking.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketBookingService {
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final SeatBookingRepository bookingRepository;

    public TicketBookingService(SeatAvailabilityRepository seatAvailabilityRepository, ShowRepository showRepository, UserRepository userRepository, SeatBookingRepository bookingRepository) {
        this.seatAvailabilityRepository = seatAvailabilityRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }


    @Transactional
    public BookTicketResponse bookTickets(Long userId, BookTicketRequest request) {

        List<SeatAvailability> lockedSeats = lockAndReserveSeats(userId, request);

        // TODO: Remove this code! For simulation only.
        try {
            Thread.sleep(20_000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Booking interrupted!");
        }

        return confirmAndBookSeats(userId, request, lockedSeats);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private List<SeatAvailability> lockAndReserveSeats(Long userId, BookTicketRequest request) {

        List<SeatAvailability> availableSeats = seatAvailabilityRepository.findByShowIdAndSeatIdIn(request.showId(), request.seatIds());

        if (availableSeats.size() != request.seatIds().size()) {
            throw new IllegalStateException("Selected number of seats are not available. Try with a lesser number!");
        }

        //Validate availability
        for (SeatAvailability seat : availableSeats) {
            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("One or more seats are not vacant. Please try again!");
            }
        }

        // Mark available seat(s) as locked
        availableSeats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.LOCKED);
            seat.setUpdatedAt(LocalDateTime.now());
        });

        // Explicit flush to ensure LOCKED status is persisted
        seatAvailabilityRepository.saveAll(availableSeats);
        seatAvailabilityRepository.flush(); //TODO: Remove this line
        System.out.println("Seats marked as LOCKED and flushed to DB");
        // Transaction commits here — LOCKED status now visible in DB

        return availableSeats;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private BookTicketResponse confirmAndBookSeats(Long userId, BookTicketRequest request) {

        Show show = showRepository.findById(request.showId()).orElseThrow(() -> new IllegalStateException("Show not found!"));

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        //Re-fetch seats
        List<SeatAvailability> seats = seatAvailabilityRepository.findByShowIdAndSeatIdIn(request.showId(), request.seatIds());

        // Mark locked seat(s) as booked
        seats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.BOOKED);
            seat.setUpdatedAt(LocalDateTime.now());
        });
        seatAvailabilityRepository.saveAll(seats);

        //Create booking
        Booking booking = new Booking();
        booking.setBookedShow(show);
        booking.setSeats(seats);
        booking.setUser(user);
        booking.setBookedAt(LocalDateTime.now());
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);
        bookingRepository.flush();

        return new BookTicketResponse(
                savedBooking.getId(),
                show.getId(),
                seats.stream().map(seat -> seat.getSeat().getId()).toList(),
                savedBooking.getBookedAt()
        );
    }
}
