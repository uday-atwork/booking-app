package com.booking.app.controller;

import com.booking.app.dto.request.BookTicketRequest;
import com.booking.app.dto.response.BookTicketResponse;
import com.booking.app.service.TicketBookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/bookings")
@RestController
public class TicketBookingController {
    private static final Logger logger = LoggerFactory.getLogger(TicketBookingController.class);

    private final TicketBookingService bookingService;

    public TicketBookingController(TicketBookingService bookingService) {
        this.bookingService = bookingService;
    }


    /**
     * Book tickets for a user
     *
     * @param userId  UserID from request header
     * @param request BookTicketRequest containing showId and seatIds to be booked
     * @return BookTicketResponse with booking confirmation
     */

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<BookTicketResponse> bookTicket(@RequestHeader("USER-ID") Long userId, @RequestBody BookTicketRequest request) {
        return ResponseEntity.ok(bookingService.bookTickets(userId, request));
    }
}
