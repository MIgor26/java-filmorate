//package ru.yandex.practicum.filmorate.dal;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.exception.ValidationException;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.validator.DateReleaseValidator;
//
//import java.util.*;
//
//@Slf4j
//@Component
//@Qualifier("memoryUserService")
//public class InMemoryFilmStorage implements FilmStorage {
//    private final Map<Long, Film> films = new HashMap<>();
//    private final Comparator<Film> likeComparator = Comparator.comparing(film -> film.getLike().size());
//
//    @Override
//    public Optional<Film> findById(long filmId) {
//        return Optional.ofNullable(films.get(filmId));
//    }
//
//    @Override
//    public Collection<Film> getPopular(Long count) {
//        long correctCount = (count == null || count < 0) ? 10 : count;
//        return films.values()
//                .stream()
//                .sorted(likeComparator.reversed())
//                .limit(correctCount)
//                .toList();
//    }
//
//    @Override
//    public Collection<Film> getAll() {
//        return films.values();
//    }
//
//    @Override
//    public Film create(Film film) {
//        // Добавляем id.
//        film.setId(getNextId());
//        // Добавляем фильм в базу.
//        films.put(film.getId(), film);
//        log.info("Фильм {} успешно добавлен в базу.", film);
//        return film;
//    }
//
//    @Override
//    public Film update(Film film) {
//        // Находим данный фильм в базе.
//        Film oldFilm = getFilmById(film.getId());
//        oldFilm.setTitle(film.getTitle());
//        log.info("Название фильма обновлено.");
//        oldFilm.setDescription(film.getDescription());
//        log.info("Описание фильма обновлено.");
//        if (!DateReleaseValidator.isValid(film.getReleaseDate())) {
//            String errorMessage = "Указана дата релиза фильма " + film.getReleaseDate() + "\n"
//                    + " Дата релиза фильма должна быть после 28 декабря 1895 года.";
//            throw new ValidationException(errorMessage);
//        }
//        oldFilm.setReleaseDate(film.getReleaseDate());
//        log.info("Дата релиза фильма обновлена.");
//        oldFilm.setDuration(film.getDuration());
//        log.info("Продолжительность релиза фильма обновлена.");
//        log.info("Данные фильма {} успешно обновлены", oldFilm);
//        return oldFilm;
//    }
//
//    @Override
//    public Film likeFilm(long id, long userId) {
//        Film film = getFilmById(id);
//        film.getLike().add(userId);
//        return film;
//    }
//
//    @Override
//    public Film delLikeFilm(long id, long userId) {
//        Film film = getFilmById(id);
//        film.getLike().remove(userId);
//        return film;
//    }
//
//    // Вспомогательный метод для генерации id нового фильма.
//    private long getNextId() {
//        long currentMaxId = films.keySet()
//                .stream()
//                .mapToLong(id -> id)
//                .max()
//                .orElse(0);
//        return ++currentMaxId;
//    }
//
//    // Метод вспомогательный, но используется в сервисе, поэтому публичный.
//    public Film getFilmById(long id) {
//        if (!films.containsKey(id)) {
//            String errorMessage = String.format("Фильм с id = %d не найден.", id);
//            throw new NotFoundException(errorMessage);
//        }
//        return films.get(id);
//    }
//}
