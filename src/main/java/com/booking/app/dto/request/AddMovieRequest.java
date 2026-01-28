package com.booking.app.dto.request;

import com.booking.app.constant.Genre;
import com.booking.app.constant.Language;

import java.time.LocalDate;
import java.util.List;

public class AddMovieRequest {
    private String name;
    private LocalDate releaseDate;
    private List<String> castMembers;
    private List<Genre> genres;
    private List<Language> languages;
    private float duration;
}
