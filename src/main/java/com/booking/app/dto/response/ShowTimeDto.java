package com.booking.app.dto.response;

import java.time.LocalTime;

public record ShowTimeDto(Long showId,
                          LocalTime startTime,
                          LocalTime endTime) {
}
