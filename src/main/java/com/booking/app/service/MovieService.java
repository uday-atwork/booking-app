package com.booking.app.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.booking.app.repository.MovieRepository;
import com.booking.app.repository.TheatreRepository;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    public MovieService(MovieRepository movieRepository, TheatreRepository theatreRepository) {
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
    }
}
