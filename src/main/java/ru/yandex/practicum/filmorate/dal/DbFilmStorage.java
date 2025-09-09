package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@Qualifier("dbFilmService")
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {
    protected final JdbcTemplate jdbc;
    // Экстрактор для возвращения карты фильмов со всеми полями кроме Like
    FilmResultSetExtractor filmResultSetExtractor;

    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id" +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id LEFT JOIN genre g ON fg.genre_id = g.genre_id WHERE f.id = ?";
    private static final String FIND_FILM_ALL_QUERY = "SELECT * FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id" +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id LEFT JOIN genre g ON fg.genre_id = g.genre_id";
    private static final String CREATE_QUERY = "INSERT INTO films (title, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String LIKE_FILM_QUERY = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_FILM_QUERY = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_QUERY = "SELECT f.*, COUNT(fl.film_id) AS likes_count " +
            "FROM films f JOIN film_like fl ON f.id = fl.film_id GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";

    // Вполне возможно что Оптионал не нужен здесь
    public Optional<Film> findById(long filmId) {
        Map<Long, Film> result = jdbc.query(FIND_FILM_BY_ID_QUERY, filmResultSetExtractor, filmId);
        if (result.isEmpty()) {
            String errorMessage = String.format("Фильм с id = %d не найден", filmId);
            throw new NotFoundException(errorMessage);
        }
        // Просто нет времени поэтому оборачиваю в оптианал так как в предыдущем ТЗ было оптионал
        Optional<Film> optionalValue = Optional.ofNullable(result.values().iterator().next());
        return optionalValue;
    }

    public Collection<Film> getAll() {
        Map<Long, Film> result = jdbc.query(FIND_FILM_ALL_QUERY, filmResultSetExtractor);
        if (result.isEmpty()) {
            String errorMessage = String.format("Фильмы в базе не найдены");
            throw new NotFoundException(errorMessage);
        }
        return result.values();
    }

    public Film create(Film film) {
        // Создание объекта для возвращения сгенерированных ключей
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        // Создание списка из полей пользователя
        List<Object> fields = List.of(film.getTitle(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa());
        Set<Integer> genre = film.getGenre();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
            // Заполнение полей таблиц в БД полями из объекта (учтено что в БД поля начинаются с 1, а в списке с 0)
            for (int idx = 0; idx < fields.size(); idx++) {
                ps.setObject(idx + 1, fields.get(idx));
            }
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }

        for (Integer genreId : genre) {
            jdbc.update(FILM_GENRE_QUERY, id, genreId);
        }
        // Присвоения id объекту user
        film.setId(id);
        return film;
    }

    public Film update(Film film) {
        int rows = jdbc.update(UPDATE_QUERY, film.getTitle(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa());
        if (rows == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return film;
    }

    public Film likeFilm(long id, long userId) {
        int rows = jdbc.update(LIKE_FILM_QUERY, id, userId);
        if (rows == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        // Может ненужная конструкция Оптионал, используется наподобие прошлого ТЗ
        return findById(id).get();
    }

    public Film delLikeFilm(long id, long userId) {
        int rows = jdbc.update(DELETE_LIKE_FILM_QUERY, id, userId);
        if (rows == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        // Может ненужная конструкция Оптионал, используется наподобие прошлого ТЗ
        return findById(id).get();
    }

    // !!Метод кривой. Нужно доработать
    public Collection<Film> getPopular(Long count) {
        long correctCount = (count == null || count < 0) ? 10 : count;

        // Пишу ткод таким образом. Как будет время и мысли перепишу оптимальней
        List<Long> popularFilmsId = jdbc.query(
                "SELECT f.id FROM films f JOIN film_like fl ON f.id = fl.film_id GROUP BY f.id " +
                        "ORDER BY COUNT(fl.film_id) DESC LIMIT ?",
                new Object[]{correctCount},
                (rs, rowNum) -> rs.getLong("id")
        );
        // Скорее всего это лишнее обращение к БД
        Collection<Film> filmsAll = getAll();

        List<Film> popularFilms = new ArrayList<>();

        for (Long filmId : popularFilmsId) {
            for (Film film : filmsAll) {
                if (film.getId().equals(filmId)) {
                    popularFilms.add(film);
                    break; // Находим следующий фильм в списке популярных
                }
            }
        }
        return popularFilms;
    }

    // !! Данный метод необходим в InMemoryUserStorage, поэтому содержится в интерфейсе. Для этого реализован здесь
    // По сути просто обращается к методу findById(id). Может удалить при неиспользовании InMemoryUserStorage
    public Film getFilmById(long id) {
        return findById(id).get(); // Оптионал может быть убрать
    }
}
