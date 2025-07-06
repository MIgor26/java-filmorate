package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Integer id;
    @NotBlank (message = "Наименование фильма не должно быть пустым.")
    private String name;
    @Size(min = 0, max = 200, message = "Размер описания должен быть не более 200 символов.")
    private String description;
    // В следующем ТЗ планирую написать свою аннотацию для валидации даты
    private LocalDate releaseDate;
    @Min(value = 1, message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;
}
