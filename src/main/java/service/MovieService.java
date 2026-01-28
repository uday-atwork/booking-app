package service;

import dto.request.AddMovieRequest;
import dto.response.MovieShowResponse;
import jakarta.transaction.Transactional;
import model.Movie;
import model.Theatre;
import org.springframework.stereotype.Service;
import repository.MovieRepository;
import repository.TheatreRepository;

import java.time.LocalDate;

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
