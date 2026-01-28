package dto.response;

import java.time.LocalDate;
import java.util.List;

public record MovieShowResponse(Long movieId,
                                String movieName,
                                LocalDate date,
                                String city,
                                List<TheatreShowDto> theatres){}
