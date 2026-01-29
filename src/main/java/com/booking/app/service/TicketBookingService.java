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
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketBookingService {
    private static final Logger logger = LoggerFactory.getLogger(TicketBookingService.class);
    private static final int MAX_RETRIES = 3;

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

        /**--- Pessimistic Lock ---**/
        /*
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
        */

        /*--- Optimistic Lock ---*/

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return attemptBookingWithOptimsticLock(userId, request);
            } catch (OptimisticLockingFailureException | StaleObjectStateException ex) {
                // Last attempt - fail immediately
                if (attempt == MAX_RETRIES) {
                    logger.error("Booking failed after {} attempts - userId: {}, showId: {}",
                            MAX_RETRIES, userId, request.showId());
                    throw ex;
                }

                // Not last attempt - retry with exponential backoff
                logger.warn("Optimistic lock conflict (attempt {}/{}), retrying - userId: {}", attempt, MAX_RETRIES, userId);
                sleepWithBackoff(attempt);
            }
        }
        throw new IllegalStateException("Booking failed unexpectedly");
    }


    private void sleepWithBackoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", e);
        }
    }

    /**
     * Attempt a single booking transaction. Throw exception, if optimistic lock detects concurrent modification for same seat(s).
     *
     * @return
     */
    private BookTicketResponse attemptBookingWithOptimsticLock(Long userId, BookTicketRequest request) {
        try {
            Show show = loadShow(request.showId());
            User user = loadUser(userId);

            List<SeatAvailability> availableSeats = loadAndValidateSeats(request, userId);
            logger.debug("All seats validated as available - userId: {}, count: {}", userId, availableSeats.size());

            Booking booking = createBooking(show, user, availableSeats);
            markSeatsAsBooked(availableSeats, booking);

            Booking savedBooking = saveBooking(booking, availableSeats);
            logger.info("Booking created successfully - bookingId: {}, userId: {}, version: {}", savedBooking.getId(), userId, savedBooking.getVersion());

            return buildBookingResponse(savedBooking);
        } catch (OptimisticLockException e) {
            logger.warn("Booking failed due to seat conflict - userId: {}, retrying...", userId);
            throw new SeatAlreadyBookedException("Seat was just booked. Please select different seats.", e);
        }
    }


    private Show loadShow(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private List<SeatAvailability> loadAndValidateSeats(BookTicketRequest request, Long userId) {
        List<SeatAvailability> seats = seatAvailabilityRepository
                .findByShowIdAndSeatIdIn(request.showId(), request.seatIds());

        validateSeatCount(seats, request.seatIds().size(), userId);
        validateSeatsAvailable(seats, userId);

        logger.debug("All seats validated as available - userId: {}, count: {}", userId, seats.size());
        return seats;
    }

    private void validateSeatCount(List<SeatAvailability> foundSeats, int requestedCount, Long userId) {
        if (foundSeats.size() != requestedCount) {
            logger.warn("Seat count mismatch - userId: {}, requested: {}, found: {}",
                    userId, requestedCount, foundSeats.size());
            throw new SeatNotFoundException("Selected number of seats are not available!");
        }
    }

    private void validateSeatsAvailable(List<SeatAvailability> seats, Long userId) {
        for (SeatAvailability seat : seats) {
            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                logger.warn("Seat not available - userId: {}, seatId: {}, status: {}",
                        userId, seat.getSeat().getId(), seat.getSeatStatus());
                throw new SeatNotAvailableException("One or more seats are not vacant!");
            }
        }
    }

    private Booking createBooking(Show show, User user, List<SeatAvailability> seats) {
        Booking booking = new Booking();
        booking.setBookedShow(show);
        booking.setUser(user);
        booking.setSeats(seats);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookedAt(LocalDateTime.now());
        return booking;
    }

    private void markSeatsAsBooked(List<SeatAvailability> seats, Booking booking) {
        seats.forEach(seat -> {
            seat.setSeatStatus(SeatStatus.BOOKED);
            seat.setUpdatedAt(LocalDateTime.now());
            seat.setBooking(booking);
        });
    }

    private Booking saveBooking(Booking booking, List<SeatAvailability> seats) {
        Booking savedBooking = bookingRepository.save(booking);
        seatAvailabilityRepository.saveAll(seats);
        return savedBooking;
    }

    private BookTicketResponse buildBookingResponse(Booking booking) {
        return new BookTicketResponse(
                booking.getId(),
                booking.getBookedShow().getId(),
                booking.getSeats().stream()
                        .map(seat -> seat.getSeat().getId())
                        .toList(),
                booking.getBookedAt()
        );
    }


    /*** -- Pessimistic Locking --- ***/
    /**
     * Lock and reserve seats for booking (for pessimistic locking)
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

    // Custom exceptions for better error handling
    public class ShowNotFoundException extends RuntimeException {
        public ShowNotFoundException(String message) {
            super(message);
        }
    }

    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public class SeatNotFoundException extends RuntimeException {
        public SeatNotFoundException(String message) {
            super(message);
        }
    }

    public class SeatNotAvailableException extends RuntimeException {
        public SeatNotAvailableException(String message) {
            super(message);
        }
    }

    public class SeatAlreadyBookedException extends RuntimeException {
        public SeatAlreadyBookedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
