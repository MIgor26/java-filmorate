package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.DateReleaseValidator;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage inMemoryFilmStorage;

    @Autowired
    // ?? Сомневаюсь. Может в параметрах конструктора как раз нужна переменная класса InMemoryFilmStorage?
    public FilmService(FilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public Optional<Film> findById(int filmId) {
        return inMemoryFilmStorage.findById(filmId);
    }

    public Collection<Film> getPopular(Integer count) {
        return inMemoryFilmStorage.getPopular(count);
    }

    public Collection<Film> getAll() {
        return inMemoryFilmStorage.getAll();
    }

    public Film create(Film film) {
        log.info("Получен запрос на занесение в базу фильма {}", film);
        // Проверяем дату релиза фильма на соответствие условиям.
        if (!DateReleaseValidator.isValid(film.getReleaseDate())) {
            String errorMessage = "Указана дата релиза фильма " + film.getReleaseDate() + "\n"
                    + " Дата релиза фильма должна быть после 28 декабря 1895 года.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Получен запрос на изменение данных фильма {}", film);
        // Проверяем имеется ли id в теле запроса.
        if (film.getId() == null) {
            String errorMessage = "Не указан id фильма. Запрос не может быть обработан.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        return inMemoryFilmStorage.update(film);
    }

    public Film likeFilm(int id, int userId) {
        return inMemoryFilmStorage.likeFilm(id, userId);
    }

    public Film delLikeFilm(int id, int userId) {
        return inMemoryFilmStorage.delLikeFilm(id, userId);
    }
}
