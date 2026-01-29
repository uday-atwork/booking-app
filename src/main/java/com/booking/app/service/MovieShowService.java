package com.booking.app.service;

import com.booking.app.dto.response.MovieShowResponse;
import com.booking.app.dto.response.ShowTimeDto;
import com.booking.app.dto.response.TheatreShowDto;
import com.booking.app.model.Show;
import com.booking.app.model.Theatre;
import com.booking.app.repository.ShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovieShowService {

    private static final Logger logger = LoggerFactory.getLogger(MovieShowService.class);

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
        try {
            logger.info("Fetching movie shows - movieId: {}, city: {}, date: {}", movieId, city, date);
            List<Show> showList = showRepository.findByMovieIdAndTheatreCityIgnoreCaseAndShowDate(movieId, city, date);

            if (showList.isEmpty()) {
                logger.warn("No shows found for movieId: {}, city: {}, date: {}", movieId, city, date);
                return null;
            }
            logger.debug("Found {} shows for movieId: {} in city: {} on date: {}", showList.size(), movieId, city, date);

            String movieName = showList.get(0).getMovie().getName(); // Get movie name

            Map<Theatre, List<Show>> theatreShows = showList.stream().collect(Collectors.groupingBy(Show::getTheatre));
            logger.debug("Grouped shows by {} theatres", theatreShows.size());

            List<TheatreShowDto> theatreShowDtos = buildTheatreShowDtos(theatreShows);

            return new MovieShowResponse(
                    movieId,
                    movieName,
                    date,
                    city,
                    theatreShowDtos
            );
        } catch (Exception ex) {
            logger.error("Error fetching movie shows for movieId: {}, city: {}, date: {}", movieId, city, date, ex);
            throw ex;
        }
    }


    private List<TheatreShowDto> buildTheatreShowDtos(Map<Theatre, List<Show>> theatreShows) {
        List<TheatreShowDto> result = new ArrayList<>();

        for (Map.Entry<Theatre, List<Show>> entry : theatreShows.entrySet()) {
            Theatre theatre = entry.getKey();
            List<Show> shows = entry.getValue();

            logger.debug("Processing theatre: {} with {} shows",
                    theatre.getTheatreName(), shows.size());

            List<ShowTimeDto> showTimes = shows.stream()
                    .map(show -> new ShowTimeDto(show.getId(), show.getStartTime(), show.getEndTime()))
                    .toList();

            result.add(new TheatreShowDto(theatre.getId(), theatre.getTheatreName(), showTimes));
        }

        return result;
    }
}
