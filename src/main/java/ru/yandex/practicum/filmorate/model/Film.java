package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Long id;
    @NotBlank(message = "Наименование фильма не должно быть пустым.")
    private String name;
    @Size(min = 0, max = 200, message = "Размер описания должен быть не более 200 символов.")
    private String description;
    @PastOrPresent(message = "Дата не может быть в будущем")
    private LocalDate releaseDate;
    @Min(value = 1, message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;
    @JsonProperty("mpa")
    @Builder.Default
    private Mpa mpa = new Mpa(1L, "G");
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    @Builder.Default
    private Set<Like> like = new HashSet<>();
}
