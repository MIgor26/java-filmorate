package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос на создание нового пользователя {}", user);
        // Проверяем имейл на соответствие условиям.
        if (!validateEmail(user.getEmail())) {
            String errorMessage = String.format("Имайл = %s не прошёл валидацию. " +
                    "Имейл не должен быть пустым и должен иметь верный формат.", user.getEmail());
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        // Проверяем, есть ли пользователь в базе с таким же имейлом.
        if (users.entrySet().stream()
                .anyMatch(entry -> user.getEmail().equals(entry.getValue().getEmail()))) {
            String errorMessage = String.format("Имайл %s уже используется", user.getEmail());
            log.error(errorMessage);
            throw new DuplicatedDataException(errorMessage);
        }
        // Проверяем логин на соответствие условиям.
        if (!validateLogin(user.getLogin())) {
            String errorMessage = String.format("Логин %s не прошёл валидацию. " +
                    "Логин не должен быть пустым и не может содержать пробелы.", user.getLogin());
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        // Проверяем дату рождения на соответствие условию.
        if (!validateBirthday(user.getBirthday())) {
            String errorMessage = "Дата рождения " + user.getBirthday() + " не прошла валидацию. " +
                    "Дата рождения не может быть в будущем";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        // Добавляем id
        user.setId(getNextId());
        // Если имя пользователя не задано или пустое, то задаём логин для отображения имени.
        if (!validateName(user.getName())) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не задано. Используем логин для отображения имени.");
        }
        // Добавляем нового пользователя в базу.
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно создан.", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Получен запрос на изменение данных пользователя {}", user);
        // Проверяем имеется ли id пользователя в теле запроса.
        if (user.getId() == null) {
            String errorMessage = "Не указан id пользователя. Запрос не может быть обработан";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (users.containsKey(user.getId())) {
            // Находим данного пользователя в базе.
            User oldUser = users.get(user.getId());
            // Если имейл отличается от старого имейла, то проверяем, есть ли пользователь в базе с таким имейлом.
            if (user.getEmail() != null && !user.getEmail().equals(oldUser.getEmail())) {
                if (users.entrySet().stream()
                        .anyMatch(entry -> user.getEmail().equals(entry.getValue().getEmail()))) {
                    String errorMessage = String.format("Имайл %s уже используется", user.getEmail());
                    log.error(errorMessage);
                    throw new DuplicatedDataException(errorMessage);
                }
            }
            // Обновдяем поля, в случае удачной валидации.
            if (validateEmail(user.getEmail())) {
                log.info("Имаил пользователя обновлён.");
                oldUser.setEmail(user.getEmail());
            }
            if (validateLogin(user.getLogin())) {
                log.info("Логин пользователя обновлён.");
                oldUser.setLogin(user.getLogin());
            }
            if (validateName(user.getName())) {
                log.info("Имя пользователя обновлёно.");
                oldUser.setName(user.getName());
            } else {
                oldUser.setName(oldUser.getLogin());
                log.info("Имя пользователя не задано. Для отображения будет использоваться логин.");
            }
            if (validateBirthday(user.getBirthday())) {
                log.info("Дата рождения пользователя обновлёна.");
                oldUser.setBirthday(user.getBirthday());
            }
            log.info("Данные пользователя {} успешно обновлёны.", oldUser);
            return oldUser;
        }
        String erorMessage = String.format("Пользователь с id = %d не найден", user.getId());
        log.error(erorMessage);
        throw new NotFoundException(erorMessage);
    }

    // Вспомогательный метод для генерации id нового пользователя.
    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    // Вспомогательный метод для валидации имейла.
    private boolean validateEmail(String email) {
        return (email != null && !email.isBlank() && email.contains("@"));
    }

    // Вспомогательный метод для валидации логина.
    private boolean validateLogin(String login) {
        return (login != null && !login.isBlank() && !login.contains(" "));
    }

    // Вспомогательный метод для валидации даты рождения.
    private boolean validateBirthday(LocalDate birthday) {
        return (birthday.isBefore(LocalDate.now()));
    }

    // Вспомогательный метод для валидации имени.
    private boolean validateName(String name) {
        return (name != null && !name.isBlank());
    }
}
