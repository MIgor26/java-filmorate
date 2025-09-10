package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.parse("1895-12-28", DateTimeFormatter.ISO_LOCAL_DATE);

    @Autowired
    public FilmService(@Qualifier("DbFilmStorage") FilmStorage filmStorage, @Qualifier("dbUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getFilms() {
        log.info("Запрос на получение всех фильмов");

        Collection<Film> films = filmStorage.getFilms();
        if (films.isEmpty()) log.warn("Список фильмов пустой");

        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    public Collection<Film> getPopular(Integer count) {
        log.info("Получен запрос на список популярных фильмов");
        if (count < 1) {
            throw new ValidationException("Количество фильмов для вывода не должно быть меньше 1");
        }

        Collection<Film> topFilms = filmStorage.getPopular(count);
        if (topFilms.isEmpty()) log.warn("Список популярных фильмов пустой");

        log.info("Сформирован список из {} популярных фильмов", topFilms.size());
        return topFilms;
    }

    public Collection<Genre> getGenres() {
        log.info("Получен запрос на список всех жанров");

        Collection<Genre> genres = filmStorage.getGenres();
        if (genres.isEmpty()) log.warn("Список жанров пустой");

        log.info("Найдено {} жанров", genres.size());
        return genres;
    }

    public Collection<Mpa> getRatings() {
        log.info("Запрос на получение всех рейтингов");

        Collection<Mpa> ratings = filmStorage.getRatings();
        if (ratings.isEmpty()) log.warn("Список рейтингов пустой");

        log.info("Найдено {} рейтингов", ratings.size());
        return ratings;
    }

    public Genre getGenreById(Long genreId) {
        log.info("Запрос жанра с id = {}", genreId);

        Genre genre = filmStorage.getGenresById(genreId);

        log.info("Жанр с id = {} найден", genreId);
        return genre;
    }

    public Mpa getRatingById(Long ratingId) {
        log.info("Запрос на рейтинг с id = {}", ratingId);

        Mpa rating = filmStorage.getRatingMpaById(ratingId);

        log.info("Рейтинг с id = {} успешно найден", ratingId);
        return rating;
    }

    public Film getFilmById(Long filmId) {
        log.info("Запрос фильма c id = {}", filmId);

        Film film = filmStorage.getFilmById(filmId);

        log.info("Фильм с id = {} найден", filmId);
        return film;
    }

    public Film create(Film film) {
        log.info("Запрос на создание фильма {}", film.getName());

        if (film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            log.warn("Попытка создать фильм с недопустимой датой релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        System.out.println("Запуск метода сетрейтинг и лайк");

        setRatingGenresLikes(film);

        System.out.println("Метод сетрейтинг отработал");

        Film newFilm = Film.builder()
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .build();
        System.out.println("Ньюфильм создался");

        Film createFilm = filmStorage.create(newFilm);

        for (Genre genre : film.getGenres()) {
            filmStorage.addLinkFilmGenres(createFilm.getId(), genre.getId());
        }
        createFilm.setGenres(film.getGenres());

        for (Like like : film.getLike()) {
            filmStorage.addLikes(createFilm.getId(), like.getUserId());
        }
        createFilm.setLike(film.getLike());

        log.info("Фильм {} успешно создан", createFilm.getName());
        return createFilm;
    }

    public Film update(Film newFilm) {
        log.info("Запрос на обновление фильма c id = {}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: передан null в качестве Id");
            throw new ValidationException("Ошибка валидации: передан null в качестве Id");
        }

        if (newFilm.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            log.warn("Попытка обновить фильм с недопустимой датой релиза: {}", newFilm.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        setRatingGenresLikes(newFilm);

        Film oldFilm = filmStorage.getFilmById(newFilm.getId());
        oldFilm.setId(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());
        oldFilm.setMpa(newFilm.getMpa());

        Film updateFilm = filmStorage.update(oldFilm);

        filmStorage.delLinkFilmGenres(oldFilm.getId());
        for (Genre genre : newFilm.getGenres()) {
            filmStorage.addLinkFilmGenres(updateFilm.getId(), genre.getId());
        }
        updateFilm.setGenres(newFilm.getGenres());

        filmStorage.delAllLikes(newFilm.getId());
        for (Like like : newFilm.getLike()) {
            filmStorage.addLikes(updateFilm.getId(), like.getUserId());
        }
        updateFilm.setLike(newFilm.getLike());

        log.info("Фильм с id = {} успешно обновлён", updateFilm.getId());
        return updateFilm;
    }

    public Film delete(Long filmId) {
        log.info("Запрос на удаление фильма с id = {}", filmId);

        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        Film deleteFilm = filmStorage.delete(film);
        if (deleteFilm == null) {
            log.warn("Ошибка DAO при удаление фильма с id = {}", film.getId());
            throw new ValidationException("Ошибка при обновлении фильма");
        }

        log.info("Фильм с id = {} успешно удален", filmId);
        return deleteFilm;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Получен запрос от пользователя с id = {} поставить лайк фильму с id = {}", userId, filmId);

        if (filmStorage.getFilmById(filmId) == null) {
            log.warn("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        if (userStorage.findById(userId) == null) {
            log.warn("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        if (filmStorage.isLiked(filmId, userId)) {
            log.warn("Пользователь с id = {} уже поставил лайк", userId);
            throw new ValidationException("Пользователь уже поставил лайк");
        }

        filmStorage.addLikes(filmId, userId);
        log.info("Лайк успешно добавлен");
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Получен запрос от пользователя с id = {} удалить лайк фильму с id = {}", userId, filmId);

        if (filmStorage.getFilmById(filmId) == null) {
            log.warn("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        if (userStorage.findById(userId) == null) {
            log.warn("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        if (!filmStorage.isLiked(filmId, userId)) {
            log.warn("Лайк от пользователя с id = {} не найден", userId);
            throw new ValidationException("Нельзя удалить лайк, которого нет");
        }

        filmStorage.delLike(filmId, userId);
        log.info("Лайк успешно удален");
    }

    private Mpa customMpa() {
        System.out.println("Записывается МПА по умолчанию");

        return Mpa.builder()
                .id(1L)
                .name("G")
                .build();
    }

    private void setRatingGenresLikes(Film film) {
        film.setMpa(
                Optional.ofNullable(film.getMpa())
                        .map(r -> filmStorage.getRatingMpaById(r.getId()))
                        .orElseGet(this::customMpa)
        );

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> sortedGenres = new ArrayList<>();
            List<Genre> listAllGenre = filmStorage.getGenres().stream().toList();

            for (Genre genre : film.getGenres()) {
                listAllGenre.stream()
                        .filter(g -> g.getId().equals(genre.getId()))
                        .findFirst()
                        .ifPresentOrElse(
                                sortedGenres::add,
                                () -> {
                                    log.warn("Жанр с id = {} не найден", genre.getId());
                                    throw new NotFoundException("Жанр не найден");
                                }
                        );
            }

            sortedGenres.sort(Comparator.comparingLong(Genre::getId));
            film.setGenres(new LinkedHashSet<>(sortedGenres));
        } else {
            film.setGenres(new HashSet<>());
        }

        if (film.getLike() == null || film.getLike().isEmpty()) {
            film.setLike(new HashSet<>());
        }
    }
}
