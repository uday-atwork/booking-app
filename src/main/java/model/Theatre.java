package model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "THEATRE")
public class Theatre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theatre_name", nullable = false)
    private String theatreName;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "movies_running")
    private List<String> moviesRunning;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getMoviesRunning() {
        return moviesRunning;
    }

    public void setMoviesRunning(List<String> moviesRunning) {
        this.moviesRunning = moviesRunning;
    }
}
