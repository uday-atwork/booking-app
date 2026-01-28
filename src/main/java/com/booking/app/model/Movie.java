package com.booking.app.model;

import com.booking.app.constant.Genre;
import com.booking.app.constant.Language;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @ElementCollection
    @CollectionTable(
            name = "movie_cast",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "cast_member")
    private List<String> casteMembers;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "genre")
    private List<Genre> genre;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "movie_language",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "language")
    private List<Language> languages;

    @Column(name = "duration")
    private float duration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<String> getCasteMembers() {
        return casteMembers;
    }

    public void setCasteMembers(List<String> casteMembers) {
        this.casteMembers = casteMembers;
    }

    public List<Genre> getGenre() {
        return genre;
    }

    public void setGenre(List<Genre> genre) {
        this.genre = genre;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }
}
