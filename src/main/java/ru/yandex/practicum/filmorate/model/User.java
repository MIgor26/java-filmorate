package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(of = {"email"})
public class User {
    private Long id;
    @NotBlank(message = "Имайл не должен быть пустым")
    @Email(message = "Должен быть корректный имайл")
    private String email;
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
}
