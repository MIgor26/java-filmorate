package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {
    private String title;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<String> genre; // RF В случае предоставления списка жанров сделать Enum
    private Enum<Mpa> mpa;
}
