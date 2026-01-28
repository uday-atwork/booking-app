package com.booking.app.dto.request;

import com.booking.app.constant.Genre;
import com.booking.app.constant.Language;

import java.time.LocalDate;
import java.util.List;

public record AddMovieRequest(
        String name,
        LocalDate releaseDate,
        List<String> castMembers,
        List<Genre> genres,
        List<Language> languages,
        float duration) {
}
