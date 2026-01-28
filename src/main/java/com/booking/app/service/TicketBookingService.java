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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketBookingService {
    private static final Logger logger = LoggerFactory.getLogger(TicketBookingService.class);

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

    /**
     * Book tickets
     *
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public BookTicketResponse bookTickets(Long userId, BookTicketRequest request) {
        logger.info("Starting ticket booking - userId: {}, showId: {}, seatIds: {}", userId, request.showId(), request.seatIds());
        try {
            lockAndReserveSeats(userId, request);
            BookTicketResponse response = confirmAndBookSeats(userId, request);
            logger.info("Ticket booking completed successfully - userId: {}, bookingId: {}, seatIds: {}",
                    userId, response.bookingId(), response.seatIds());
            return response;
        } catch (IllegalStateException ex) {
            logger.warn("Ticket booking failed - userId: {}, showId: {}, reason: {}", userId, request.showId(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error during ticket booking - userId: {}, showId: {}", userId, request.showId(), ex);
            throw ex;
        }

    }


    /**
     * Lock and reserve seats for booking
     *
     * @param userId  User requesting the booking
     * @param request BookTicketRequest with details of the seat to be booked
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void lockAndReserveSeats(Long userId, BookTicketRequest request) {

        List<SeatAvailability> availableSeats = seatAvailabilityRepository.findByShowIdAndSeatIdIn(request.showId(), request.seatIds());
        logger.debug("Retrieved {} seat availability records for {} requested seats",
                availableSeats.size(), request.seatIds().size());

        if (availableSeats.size() != request.seatIds().size()) {
            logger.warn("Seat count mismatch - userId: {}, requested: {}, found: {}", userId, request.seatIds().size(), availableSeats.size());

            throw new IllegalStateException("Selected number of seats are not available. Try with a lesser number!");
        }

        //Validate seat availability
        for (SeatAvailability seat : availableSeats) {
            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                logger.warn("Seat not available - userId: {}, seatId: {}, status: {}", userId, seat.getSeat().getId(), seat.getSeatStatus());
                throw new IllegalStateException("One or more seats are not vacant. Please try again!");
            }
        }

        logger.debug("All seats validated as available - userId: {}, count: {}", userId, availableSeats.size());

        // Mark available seat(s) as locked
        availableSeats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.LOCKED);
            seat.setUpdatedAt(LocalDateTime.now());
        });

        // Explicit flush to ensure LOCKED status is persisted
        seatAvailabilityRepository.saveAll(availableSeats);
        seatAvailabilityRepository.flush(); //TODO: Remove this line
        logger.info("Seats locked and persisted - userId: {}, count: {}", userId, availableSeats.size());
        // Transaction commits here — LOCKED status now visible in DB
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private BookTicketResponse confirmAndBookSeats(Long userId, BookTicketRequest request) {
        try {
            Show show = showRepository.findById(request.showId()).orElseThrow(() -> {
                logger.error("Show not found - showId: {}, userId: {}", request.showId(), userId);
                return new IllegalStateException("Show not found!");
            });

            User user = userRepository.findById(userId).orElseThrow(() -> {
                logger.error("User not found - userId: {}", userId);
                return new IllegalStateException("User not found");
            });

            //Re-fetch seats
            List<SeatAvailability> seats = seatAvailabilityRepository.findByShowIdAndSeatIdIn(request.showId(), request.seatIds());

            // Mark locked seat(s) as booked
            seats.forEach(seat -> {
                seat.setSeatStatus(SeatStatus.BOOKED);
                seat.setUpdatedAt(LocalDateTime.now());
            });
            seatAvailabilityRepository.saveAll(seats);
            logger.debug("Seats marked as BOOKED - userId: {}, count: {}", userId, seats.size());


            //Create booking
            Booking booking = new Booking();
            booking.setBookedShow(show);
            booking.setSeats(seats);
            booking.setUser(user);
            booking.setBookedAt(LocalDateTime.now());
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            Booking savedBooking = bookingRepository.save(booking);
            bookingRepository.flush();
            logger.info("Booking confirmed - bookingId: {}, userId: {}, showId: {}, seatCount: {}",
                    savedBooking.getId(), userId, show.getId(), seats.size());

            return new BookTicketResponse(
                    savedBooking.getId(),
                    show.getId(),
                    seats.stream().map(seat -> seat.getSeat().getId()).toList(),
                    savedBooking.getBookedAt()
            );
        } catch (IllegalStateException ex) {
            logger.warn("Failed to confirm booking - userId: {}, showId: {}, error: {}",
                    userId, request.showId(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error while confirming booking - userId: {}, showId: {}",
                    userId, request.showId(), ex);
            throw ex;
        }
    }
}
