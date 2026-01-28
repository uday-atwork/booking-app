package com.booking.app.controller;

import com.booking.app.dto.response.MovieShowResponse;
import com.booking.app.service.MovieShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/movies")
public class MovieShowController {
    private static final Logger logger = LoggerFactory.getLogger(MovieShowController.class);

    private final MovieShowService movieShowService;

    MovieShowController(MovieShowService movieShowService) {
        this.movieShowService = movieShowService;
    }

    /**
     * Fetches shows of a given movie, in a given city on a given date.
     *
     * @param movieId
     * @param city
     * @param date
     * @return
     */
    @RequestMapping(value = "/{movieId}/shows", method = RequestMethod.GET)
    public ResponseEntity<MovieShowResponse> getMovieShows(
            @PathVariable Long movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        logger.info("Fetching movie shows - movieId: {}, city: {}, date: {}", movieId, city, date);

        MovieShowResponse response = movieShowService.getMovieShows(movieId, city, date);

        if (response == null || response.theatres().isEmpty()) {
            logger.warn("No shows found for movieId: {}, city: {}, date: {}", movieId, city, date);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(response);
    }

}
