package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Component
public class FilmResultSetExtractor implements ResultSetExtractor<Map<Long, Film>> {
    @Override
    public Map<Long, Film> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        Map<Long, Film> filmMap = new HashMap<>();

        while (resultSet.next()) {
            Long id = resultSet.getLong("id");
            String title = resultSet.getString("title");
            String description = resultSet.getString("description");
            Timestamp releaseDate = resultSet.getTimestamp("release_date");
            int duration = resultSet.getInt("duration");
            Integer genre = resultSet.getInt("genre_id");
            String mpa = resultSet.getString("mpa_name");

            // Получение текущего фильма по его id
            Film film = filmMap.get(id);
            // Если такой фильм уже есть в карте, то заново не заполняем его поля, кроме списка жанров
            if (film == null) {
                film = new Film();
                film.setId(id);
                film.setTitle(title);
                film.setDescription(description);
                film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());
                film.setDuration(duration);
                film.setMpa(mpa);
            }

            // Так как жанров у фильма может быть много, то дополняем список его жанров
            if (genre != null) {
                film.getGenre().add(genre);
            }
        }
        return filmMap;
    }
}
