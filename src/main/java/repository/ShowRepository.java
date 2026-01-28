package repository;

import model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

    List<Show> findShowForMovieInCity(Long movieId, String city, LocalDate date);
}
