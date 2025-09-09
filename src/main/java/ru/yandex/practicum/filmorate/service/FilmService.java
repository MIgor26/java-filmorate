package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.validator.DateReleaseValidator;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    @Qualifier("dbFilmService")
    private final FilmStorage filmStorage;
    // private final UserStorage inMemoryUserStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
        // this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public Optional<Film> findById(long filmId) {
        return filmStorage.findById(filmId);
    }

    public Collection<Film> getPopular(Long count) {
        return filmStorage.getPopular(count);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        log.info("Получен запрос на занесение в базу фильма {}", film);
        // Проверяем дату релиза фильма на соответствие условиям.
        if (!DateReleaseValidator.isValid(film.getReleaseDate())) {
            String errorMessage = "Указана дата релиза фильма " + film.getReleaseDate() + "\n"
                    + " Дата релиза фильма должна быть после 28 декабря 1895 года.";
            throw new ValidationException(errorMessage);
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Получен запрос на изменение данных фильма {}", film);
        // Проверяем имеется ли id в теле запроса.
        if (film.getId() == null) {
            String errorMessage = "Не указан id фильма. Запрос не может быть обработан.";
            throw new ValidationException(errorMessage);
        }
        return filmStorage.update(film);
    }

    public Film likeFilm(long id, long userId) {
//        Film film = filmStorage.getFilmById(id);
//        // !! Выглядит как костыль, но работает как проверка. Если пользователь не найден - вернётся ошибка.
//        // Если пользователь найден -- программа продолжит работу
//        // А переменная типа user не заведена, так как пользователя нам возвращать не надо
//        inMemoryUserStorage.getUserById(userId);
        return filmStorage.likeFilm(id, userId);
    }

    public Film delLikeFilm(long id, long userId) {
//        Film film = filmStorage.getFilmById(id);
//        inMemoryUserStorage.getUserById(userId);
        return filmStorage.delLikeFilm(id, userId);
    }
}
