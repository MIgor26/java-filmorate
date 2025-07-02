package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на занесение в базу фильма {}", film);
        // Проверяем название на соответствие условиям.
        if (!validateName(film.getName())) {
            String errorMessage = "Название фильма не может быть пустым.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        // Проверяем описание на соответствие условиям.
        if (!validateDescription(film.getDescription())) {
            String errorMessage = String.format("Описание фильма составляет %d символов. " +
                    "Описание должно быть менее 200 символов.", film.getDescription().length());
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        // Проверяем дату релиза фильма на соответствие условиям.
        if (!validateReleaseDate(film.getReleaseDate())) {
            String errorMessage = "Дата релиза фильма " + film.getReleaseDate() +
                    " Дата релиза фильма должна быть после 28 декабря 1895 года.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        // Проверяем продолжительность фильма на соответствие условиям.
        if (!validateDuration(film.getDuration())) {
            String errorMessage = "Продолжительность фильма " + film.getDuration() +
                    " Продолжительность фильма должна быть положительным числом.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        // Добавляем id.
        film.setId(getNextId());
        // Добавляем фильм в базу.
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен в базу.", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос на изменение данных фильма {}", film);
        // Проверяем имеется ли id пользователя в теле запроса.
        if (film.getId() == null) {
            String errorMessage = "Не указан id фильма. Запрос не может быть обработан.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (films.containsKey(film.getId())) {
            // Находим данный фильм в базе.
            Film oldFilm = films.get(film.getId());
            // Обновдяем поля, в случае удачной валидации.
            if (validateName(film.getName())) {
                log.info("Название фильма обновлено.");
                oldFilm.setName(film.getName());
            }
            if (validateDescription(film.getDescription())) {
                log.info("Описание фильма обновлено.");
                oldFilm.setDescription(film.getDescription());
            }
            if (validateReleaseDate(film.getReleaseDate())) {
                log.info("Дата релиза фильма обновлена.");
                oldFilm.setReleaseDate(film.getReleaseDate());
            }
            if (validateDuration(film.getDuration())) {
                log.info("Продолжительность релиза фильма обновлена.");
                oldFilm.setDuration(film.getDuration());
            }
            log.info("Данные фильма {} успешно обновлены", oldFilm);
            return oldFilm;
        }
        String errorMessage = String.format("Фильм с id = %d не найден.", film.getId());
        log.error(errorMessage);
        throw new NotFoundException(errorMessage);
    }

    // Вспомогательный метод для генерации id нового пользователя.
    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    // Вспомогательный метод для валидации названия фильма.
    private boolean validateName(String name) {
        return (name != null && !name.isBlank());
    }

    // Вспомогательный метод для валидации описания фильма.
    private boolean validateDescription(String description) {
        return (description.length() < 200);
    }

    // Вспомогательный метод для валидации даты релиза фильма.
    private boolean validateReleaseDate(LocalDate releaseDate) {
        return (releaseDate.isAfter(LocalDate.of(1895, 12, 28)));
    }

    // Вспомогательный метод для валидации продолжительности фильма.
    private boolean validateDuration(int duration) {
        return (duration > 0);
    }
}
