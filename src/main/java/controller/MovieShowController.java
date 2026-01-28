package controller;

import dto.response.MovieShowResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import service.MovieShowService;

import java.time.LocalDate;

@RestController
@RequestMapping("/movies")
public class MovieShowController {
    private final MovieShowService movieShowService;

    MovieShowController(MovieShowService movieShowService){
        this.movieShowService = movieShowService;
    }

    @RequestMapping(value = "/{movieId}/{city}/{date}", method = RequestMethod.GET)
    public ResponseEntity<MovieShowResponse> getMovieShows(
            @PathVariable Long movieId,
            @PathVariable String city,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date){
        MovieShowResponse response = movieShowService.getMovieShows(movieId, city, date);
        return ResponseEntity.ok(response);
    }

}
