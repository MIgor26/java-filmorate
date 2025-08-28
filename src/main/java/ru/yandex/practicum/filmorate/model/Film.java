package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Integer id; // В БД film_id
    @NotBlank(message = "Наименование фильма не должно быть пустым.")
    private String name; // В БД title
    @Size(min = 0, max = 200, message = "Размер описания должен быть не более 200 символов.")
    private String description;
    @PastOrPresent
    private LocalDate releaseDate; // В БД release_date
    @Min(value = 1, message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;
    private Set<Integer> like = new HashSet<>(); // В БД отдельная таблица film_like
    private Enum<Rating> rating; // !!new В БД типа enum нет, поэтому там varchar
    private Enum<Genre> genre; // !!new Может тип данных String??
}
