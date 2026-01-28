package com.booking.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BookTicketResponse(
        Long bookingId,
        Long showId,
        List<Long> seatIds,
        LocalDateTime bookedAt
) {
}
