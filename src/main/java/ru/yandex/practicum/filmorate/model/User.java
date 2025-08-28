package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
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
@EqualsAndHashCode(of = {"email"})
public class User {
    private Integer id; // В БД user_id
    @NotBlank(message = "Имайл не должен быть пустым")
    @Email(message = "Должен быть корректный имайл")
    private String email;
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>(); // В БД таблица friendship
    // !!new Нужно ещё логику продумать.
    // То ли мапу друзей отдельно создать - и тогда поле friends, вроде, не нужно.
    // То ло это должно быть множество объектов с полями id и флаг(да/нет)
}
