package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    public Optional<Film> findById(long filmId);

    public Collection<Film> getPopular(Long count);

    public Collection<Film> getAll();

    public Film create(Film film);

    public Film update(Film film);

    public Film likeFilm(long id, long userId);

    public Film delLikeFilm(long id, long userId);

    public Film getFilmById(long id);
}
