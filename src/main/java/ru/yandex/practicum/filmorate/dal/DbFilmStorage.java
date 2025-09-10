package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.dal.mappers.LikeMapper;
import ru.yandex.practicum.filmorate.dal.mappers.RatingMpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository("DbFilmStorage")
public class DbFilmStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public static final String FIND_ALL_FILM = "SELECT * FROM films f LEFT JOIN mpa r ON f.mpa_id = r.mpa_id " +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
            "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
            "LEFT JOIN film_like l ON f.id = l.film_id ORDER BY f.id";
    public static final String FIND_ALL_GENRE = "SELECT genre_id, genre_name FROM genre";
    public static final String FIND_ALL_RATINGS = "SELECT * from mpa";
    public static final String FIND_GENRE_BY_FILM_ID = "SELECT g.genre_id, g.genre_name FROM film_genre fg " +
            "JOIN genre g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
    public static final String FIND_LIKES_BY_FILM_ID = "SELECT * FROM film_like WHERE film_id = ?";

    public static final String FIND_GENRE_BY_ID = "SELECT * FROM genre WHERE genre_id = ?";
    public static final String FIND_FILM_BY_ID = "SELECT * FROM films f " +
            "LEFT JOIN mpa r ON f.mpa_id = r.mpa_id WHERE f.id = ?";
    public static final String INSERT_FILM = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    public static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";
    public static final String FIND_RATING = "SELECT mpa_id, mpa_name FROM mpa WHERE mpa_id = ?";
    public static final String IS_LIKE = "SELECT COUNT(*) FROM film_like WHERE film_id = ? AND user_id = ?";
    public static final String INSERT_LIKE = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
    public static final String DELETE_LIKE = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    public static final String DELETE_ALL_LIKE = "DELETE FROM film_like WHERE film_id = ?";
    public static final String DELETE_FILM_GENRE = "DELETE FROM film_genre WHERE film_id = ?";
    public static final String INSERT_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

    @Override
    public Collection<Film> getFilms() {
        Map<Long, Film> filmMap = new HashMap<>();

        jdbcTemplate.query(FIND_ALL_FILM, rs -> {
            Long filmId = rs.getLong("id");
            Film film = filmMap.computeIfAbsent(filmId, id -> {
                try {
                    return Film.builder()
                            .id(id)
                            .name(rs.getString("name"))
                            .description(rs.getString("description"))
                            .duration(rs.getInt("duration"))
                            .releaseDate(rs.getDate("release_date").toLocalDate())
                            .genres(new HashSet<>())
                            .like(new HashSet<>())
                            .build();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            long ratingId = rs.getLong("mpa_id");
            String ratingName = rs.getString("mpa_name");
            if (ratingId > 0 && ratingName != null) {
                Mpa ratingMpa = Mpa.builder()
                        .id(ratingId)
                        .name(ratingName)
                        .build();
                film.setMpa(ratingMpa);
            }

            long genreId = rs.getLong("genre_id");
            String genreName = rs.getString("genre_name");
            if (genreId > 0 && genreName != null) {
                Genre genre = Genre.builder()
                        .id(genreId)
                        .name(genreName)
                        .build();
                film.getGenres().add(genre);
            }

            Long likeUserId = rs.getLong("user_id");
            Long likeFilmId = rs.getLong("film_id");
            if (likeUserId > 0 && likeFilmId.equals(film.getId())) {
                Like like = Like.builder()
                        .filmId(likeFilmId)
                        .userId(likeUserId)
                        .build();
                film.getLike().add(like);
            } else {
                log.warn("Пропущена запись лайка: filmId={}, userId={}", likeFilmId, likeUserId);
            }
        });

        return new ArrayList<>(filmMap.values());
    }


    @Override
    public Collection<Film> getPopular(Integer count) {
        return getFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLike().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Genre> getGenres() {
        return jdbcTemplate.query(FIND_ALL_GENRE, new GenreMapper());
    }

    @Override
    public Collection<Mpa> getRatings() {
        return jdbcTemplate.query(FIND_ALL_RATINGS, new RatingMpaMapper());
    }

    private Collection<Genre> getGenresByFilmId(Long filmId) {
        return jdbcTemplate.query(FIND_GENRE_BY_FILM_ID, new GenreMapper(), filmId);
    }

    @Override
    public Collection<Like> getLikesByFilmId(Long filmId) {
        return jdbcTemplate.query(FIND_LIKES_BY_FILM_ID, new LikeMapper(), filmId);
    }

    @Override
    public Genre getGenresById(Long genreId) {
        return jdbcTemplate.queryForObject(FIND_GENRE_BY_ID, new GenreMapper(), genreId);
    }

    @Override
    public Film getFilmById(Long filmId) {
        Film film = jdbcTemplate.queryForObject(FIND_FILM_BY_ID, new FilmMapper(), filmId);
        film.setGenres(new HashSet<>(getGenresByFilmId(film.getId())));
        film.setLike(new HashSet<>(getLikesByFilmId(film.getId())));
        return film;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_FILM,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(generatedId);

        return film;
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(
                UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Film delete(Film film) {
        jdbcTemplate.update(DELETE_FILM, film.getId());
        return film;
    }

    @Override
    public Mpa getRatingMpaById(Long ratingId) {
        System.out.println("Смотрим рэйтинг");
        return jdbcTemplate.queryForObject(FIND_RATING, new RatingMpaMapper(), ratingId);
    }

    @Override
    public boolean isLiked(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(IS_LIKE, Integer.class, filmId, userId);
        return count > 0;
    }

    @Override
    public void addLikes(Long filmId, Long userId) {
        jdbcTemplate.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void delAllLikes(Long filmId) {
        jdbcTemplate.update(DELETE_ALL_LIKE, filmId);
    }

    @Override
    public void delLike(Long filmId, Long userId) {
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public void delLinkFilmGenres(Long filmId) {
        jdbcTemplate.update(DELETE_FILM_GENRE, filmId);
    }

    @Override
    public void addLinkFilmGenres(Long filmId, Long genreId) {
        jdbcTemplate.update(INSERT_FILM_GENRE, filmId, genreId);
    }
}
