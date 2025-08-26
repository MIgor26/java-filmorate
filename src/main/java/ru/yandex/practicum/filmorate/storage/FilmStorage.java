package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    public Optional<Film> findById(int filmId);

    public Collection<Film> getPopular(Integer count);

    public Collection<Film> getAll();

    public Film create(Film film);

    public Film update(Film film);

    public Film likeFilm(Film film, int userId);

    public Film delLikeFilm(Film film, int userId);

    public Film getFilmById(int id);
}
