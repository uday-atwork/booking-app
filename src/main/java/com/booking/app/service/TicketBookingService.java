package com.booking.app.service;

import com.booking.app.constant.SeatStatus;
import com.booking.app.dto.request.BookTicketRequest;
import com.booking.app.dto.response.BookTicketResponse;
import com.booking.app.model.Booking;
import com.booking.app.model.Show;
import com.booking.app.model.ShowSeatAvailability;
import com.booking.app.model.User;
import com.booking.app.repository.SeatAvailabilityRepository;
import com.booking.app.repository.SeatBookingRepository;
import com.booking.app.repository.ShowRepository;
import com.booking.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
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


    public BookTicketResponse bookTickets(Long userId, BookTicketRequest request) {
        Show show = showRepository.findById(request.showId()).orElseThrow(() -> new IllegalStateException("Show not found!"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));

        //Seat locking
        List<ShowSeatAvailability> availableSeats = seatAvailabilityRepository.findByShowIdAndSeatIdIgnoreCaseIn(request.showId(), request.seatIds());

        if (availableSeats.size() != request.seatIds().size()) {
            throw new IllegalStateException("Selected number of seats are not available. Try with a lesser number!");
        }

        //Validate availability
        for (ShowSeatAvailability seat : availableSeats) {
            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("Availability status changed. Try afresh!");
            }
        }

        // Mark available seat(s) as booked
        availableSeats.forEach(seat -> seat.setSeatStatus(SeatStatus.BOOKED));

        //Create booking
        Booking booking = new Booking();
        booking.setBookedShow(show);
        booking.setSeats(availableSeats);
        booking.setUser(user);
        booking.setBookedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        return new BookTicketResponse(
                savedBooking.getId(),
                show.getId(),
                availableSeats.stream().map(seat -> seat.getSeat().getId()).toList(),
                savedBooking.getBookedAt()
        );
    }
}
