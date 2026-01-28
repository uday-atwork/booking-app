package com.booking.app.controller;

import com.booking.app.dto.request.BookTicketRequest;
import com.booking.app.dto.response.BookTicketResponse;
import com.booking.app.service.TicketBookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/booking")
@RestController
public class TicketBookingController {
    private final TicketBookingService bookingService;

    public TicketBookingController(TicketBookingService bookingService) {
        this.bookingService = bookingService;
    }


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<BookTicketResponse> bookTicket(@RequestHeader("USER-ID") Long userId, @RequestBody BookTicketRequest request) {
        return ResponseEntity.ok(bookingService.bookTickets(userId, request));
    }
}
