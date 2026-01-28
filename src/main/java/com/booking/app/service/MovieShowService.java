package com.booking.app.service;

import com.booking.app.dto.response.MovieShowResponse;
import com.booking.app.dto.response.ShowTimeDto;
import com.booking.app.dto.response.TheatreShowDto;
import com.booking.app.model.Show;
import com.booking.app.model.Theatre;
import com.booking.app.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovieShowService {

    private final ShowRepository showRepository;

    MovieShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    /***
     * Get theaters currently running the show for given movie in a city on given date.
     * @param movieId
     * @param city
     * @param date
     * @return
     */
    public MovieShowResponse getMovieShows(Long movieId, String city, LocalDate date) {
        List<Show> showList = showRepository.findByMovieIdAndTheatreCityIgnoreCaseAndShowDate(movieId, city, date);

        if (showList.isEmpty()) {
            return null;
        }

        String movieName = showList.get(0).getMovie().getName();

        Map<Theatre, List<Show>> theatreShows = showList.stream().collect(Collectors.groupingBy(Show::getTheatre));

        List<TheatreShowDto> theatreShowDtos = theatreShows.entrySet().stream().map(entry -> {
            Theatre theatre = entry.getKey(); //Theatre

            List<ShowTimeDto> showTimes = entry.getValue().stream().map(show -> new ShowTimeDto(show.getId(),
                    show.getStartTime(),
                    show.getEndTime())).collect(Collectors.toUnmodifiableList());

            return new TheatreShowDto(theatre.getId(),
                    theatre.getTheatreName(),
                    showTimes);
        }).toList();

        return new MovieShowResponse(
                movieId,
                movieName,
                date,
                city,
                theatreShowDtos
        );
    }
}
