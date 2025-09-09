package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Component
// Данный экстрактор возвращает карту объектов класса User со всеми полями
public class UserResultSetExtractor implements ResultSetExtractor<Map<Long, User>> {
    @Override
    public Map<Long, User> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        // Создание карты объектов класса User
        Map<Long, User> userMap = new HashMap<>();
        // Множество resultSet возвращает таблицу, в которой будут объединены таблица users и таблица friendship из БД
        // Так как у одного пользователя может быть много друзей, то, согласно логике запроса
        // SELECT * FROM users LEFT JOIN friendship ON users.user_id = friendship.user_send_id
        // множество resultSet может содержать несколько строк с одинаковыми полями id пользователя и т.д., но с разными
        // id друзей. Чтобы собрать карту объектов класса User со всеми полями реализована логика

        // Получение всех полей из множества
        while (resultSet.next()) {
            Long userId = resultSet.getLong("id");
            String email = resultSet.getString("email");
            String login = resultSet.getString("login");
            String name = resultSet.getString("name");
            Timestamp birthday = resultSet.getTimestamp("birthday");
            Long friendId = resultSet.getLong("friend_id");

            // Получение текущего пользователя по его id
            User user = userMap.get(userId);
            // Если такой пользователь уже есть в карте, то заново не заполняем его поля, кроме списка друзей
            if (user == null) {
                user = new User();
                user.setId(userId);
                user.setEmail(email);
                user.setLogin(login);
                user.setName(name);
                user.setBirthday(birthday.toLocalDateTime().toLocalDate());
                userMap.put(userId, user);
            }

            // Так как друзей у пользователя может быть много, то дополняем список его друзей
            if (friendId != null) {
                user.getFriends().add(friendId);
            }
        }
        return userMap;
    }
}
