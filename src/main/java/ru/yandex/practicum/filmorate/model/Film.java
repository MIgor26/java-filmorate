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
@EqualsAndHashCode(of = {"film_id"})
public class Film {
    private Long id;
    @NotBlank(message = "Наименование фильма не должно быть пустым.")
    private String title;
    @Size(min = 0, max = 200, message = "Размер описания должен быть не более 200 символов.")
    private String description;
    @PastOrPresent
    private LocalDate releaseDate;
    @Min(value = 1, message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;
    private Set<Long> like = new HashSet<>();
    private Set<Integer> genre; // RF В случае предоставления списка жанров сделать Enum
    private String mpa; // Потом сделать Enum
}
