
package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getFilms();

    Collection<Film> getPopular(Integer count);

    Collection<Like> getLikesByFilmId(Long filmId);

    Collection<Genre> getGenres();

    Collection<Mpa> getRatings();

    Genre getGenresById(Long genreId);

    Mpa getRatingMpaById(Long ratingId);

    Film getFilmById(Long filmId);

    Film create(Film film);

    Film update(Film film);

    Film delete(Film film);

    boolean isLiked(Long filmId, Long userId);

    void addLikes(Long filmId, Long userId);

    void delAllLikes(Long filmId);

    void delLike(Long filmId, Long userId);

    void addLinkFilmGenres(Long filmId, Long genreId);

    void delLinkFilmGenres(Long filmId);
}
