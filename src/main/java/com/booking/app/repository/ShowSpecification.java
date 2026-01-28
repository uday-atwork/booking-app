package com.booking.app.repository;

import com.booking.app.model.Show;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ShowSpecification {
    public static Specification<Show> movieInCityOnDate(Long movieId, String city, LocalDate date) {
        return null;
    }
}
