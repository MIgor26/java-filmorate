package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.validator.DateReleaseValidator;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    private String title;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<String> genre; // RF В случае предоставления списка жанров сделать Enum
    private Enum<Mpa> mpa;

    public boolean hasTitle() {
        return ! (title == null || title.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return ! (releaseDate == null || !DateReleaseValidator.isValid(releaseDate)); // !! Может быть валидатор здесь и лишний?
    }

    public boolean hasDuration() {
        return ! (duration < 1); // !! Если не положительное число, то считаем что его нет
    }

    public boolean hasGenre() {
        return ! (genre == null);
    }

    public boolean hasEnum() {
        return ! (mpa == null);
    }
}
