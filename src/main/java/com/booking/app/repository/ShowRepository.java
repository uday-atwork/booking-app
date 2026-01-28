package com.booking.app.repository;

import com.booking.app.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

    List<Show> findByMovieIdAndTheatreCityIgnoreCaseAndShowDate(Long movieId, String city, LocalDate date);
}