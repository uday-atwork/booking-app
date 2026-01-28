package com.booking.app.dto.request;

import java.util.List;

public record BookTicketRequest(
        Long showId,
        List<Long> seatIds) {
}
