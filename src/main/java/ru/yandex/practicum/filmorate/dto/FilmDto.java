package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Недоступно для десериализации Json в объект
    private Long film_id;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> like = new HashSet<>(); // ?? Нужна ли инициализация?
    private Set<String> genre; // RF В случае предоставления списка жанров сделать Enum
    private Enum<Mpa> mpa;
}
