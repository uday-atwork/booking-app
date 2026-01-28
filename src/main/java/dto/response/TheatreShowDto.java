package dto.response;

import java.util.List;

public record TheatreShowDto(Long theatreId,
                             String theatreName,
                             List<ShowTimeDto> shows){}
