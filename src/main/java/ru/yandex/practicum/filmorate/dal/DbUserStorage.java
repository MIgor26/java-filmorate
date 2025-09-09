package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FriendsIdRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserResultSetExtractor;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.SQLOutput;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Qualifier("dbUserService")
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {
    protected final JdbcTemplate jdbc;
    // Экстрактор для возвращения карты пользователей (объекты класса User) со всеми полями
    protected final UserResultSetExtractor userResultSetExtractor;
    // Маппер для возвращения списка id друзей
    protected final FriendsIdRowMapper friendsIdRowMapper;

    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users " +
            "LEFT JOIN friendship ON users.id = friendship.id " +
            "WHERE users.id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users " +
            "LEFT JOIN friendship ON users.id = friendship.id";
    private static final String FIND_USERS_FRIENDS_ID_QUERY = "SELECT friend_id FROM friendship WHERE id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT f1.friend_id FROM friendship f1 " +
            "JOIN friendship f2 ON f1.friend_id = f2.friend_id WHERE f1.id = ? AND f2.id = ?";
    private static final String CREATE_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE id = ?";
    private static final String ADD_FRIENDS_QUERRY = "INSERT INTO friendship " +
            "(id, friend_id) VALUES(?, ?)";
    private static final String DELETE_FRIENDS_QUERY = "DELETE FROM friendship WHERE id = ? " +
            "AND friend_id = ?";

    // Готово
    public User findById(long id) {
        Map<Long, User> result = jdbc.query(FIND_USER_BY_ID_QUERY, userResultSetExtractor, id);
        if (result.isEmpty()) {// Метод выше не может вернуть null, поэтому проверку на null не включаю??
            String errorMessage = String.format("Пользователь с id = %d не найден", id);
            throw new NotFoundException(errorMessage);
        }
        // Так как, согласно запросу, в карте result может быть только одна строка, то возвращаем первую
        return result.values().iterator().next();
    }

    // Готово
    public Collection<User> getAll() {
        Map<Long, User> result = jdbc.query(FIND_ALL_USERS_QUERY, userResultSetExtractor);
        if (result.isEmpty()) {// Метод выше не может вернуть null, поэтому проверку на null не включаю
            String errorMessage = "Пользователей в базе нет";
            throw new NotFoundException(errorMessage);
        }
        return result.values();
    }

    // Готово
    public List<User> getUsersFriends(long id) {
        //List<Long> friendsId = jdbc.query(FIND_USERS_FRIENDS_ID_QUERY, friendsIdRowMapper, id);
        System.out.println("Запрос списка друзей");
        List<Long> friendsId = jdbc.queryForList(FIND_USERS_FRIENDS_ID_QUERY, Long.class, id);
        System.out.println("Получен список " + friendsId);
        if (friendsId.isEmpty()) {// Метод выше не может вернуть null, поэтому проверку на null не включаю
            String errorMessage = String.format("У пользователя с id = %d нет друзей", id);
            throw new NotFoundException(errorMessage);
        } // Если у пользователя есть друзья, то users не может быть пустой - поэтому далее проверки нет
        Map<Long, User> users =  jdbc.query(FIND_ALL_USERS_QUERY, userResultSetExtractor);
        // Инициация списка друзей запрашиваемого пользователя
        List<User> usersFriends = new ArrayList<>();
        // Проход по всему списку id друзей и выборка из карты всех пользователей
        // только пользователей, которые являются друзьями запрашиваемого пользователя
        for (Long friendId : friendsId) {
            if (users.containsKey(friendId)) {
                usersFriends.add(users.get(friendId));
            }
        }
        return usersFriends;
    }

    // Готово
    public List<User> commonFriends(long id, long otherId) {
        List<Long> friendsId = jdbc.query(FIND_COMMON_FRIENDS_QUERY, friendsIdRowMapper, id, otherId);
        if (friendsId.isEmpty()) {// Метод выше не может вернуть null, поэтому проверку на null не включаю
            String errorMessage = "У данных пользователей нет общих друзей";
            throw new NotFoundException(errorMessage);
        } // Если у пользователя есть общие друзья, то users не может быть пустой - поэтому далее проверки нет
        Map<Long, User> users =  jdbc.query(FIND_ALL_USERS_QUERY, userResultSetExtractor);
        // Инициация списка общих друзей запрашиваемых пользователей
        List<User> usersFriends = new ArrayList<>();
        // Проход по всему списку id общих друзей и выборка из карты всех пользователей
        // только пользователей, которые являются общими друзьями запрашиваемых пользователей
        for (Long friendId : friendsId) {
            if (users.containsKey(friendId)) {
                usersFriends.add(users.get(friendId));
            }
        }
        return usersFriends;
    }

    // Готово
    public User create(User user) {
        // Создание объекта для возвращения сгенерированных ключей
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        // Создание списка из полей пользователя
        List<Object> fields = List.of(user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
            // Заполнение полей таблиц в БД полями из объекта (учтено что в БД поля начинаются с 1, а в списке с 0)
            for (int idx = 0; idx < fields.size(); idx++) {
                ps.setObject(idx + 1, fields.get(idx));
            }
            return ps;
        },keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        // Имя исключения взято из учебного проекта Catsgram
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }
        // Присвоения id объекту user
        user.setId(id);
        return user;
    }

    // Готово
    // !! Данный метод по максимуму использует 3 обращения к базе. Если другой способ проверки дубликации имайлов,
    // то лучше применить его.
    public User update(User user) {
        User oldUser = findById(user.getId());
        if (user.getEmail() != null && !oldUser.getEmail().equals(user.getEmail())) {
            checkDuplicateEmail(user);
        }
        int rows = jdbc.update(UPDATE_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return user;
    }

    // Готово
    // На данном этапе дружба односторонняя, поэтому поле confirmation таблицы friendship не используется
    // Поле оставлено с целью дальнейшего расширения функционала, если понадобится реализовать подтверждение дружбы
    public List<User> addFriends(long id, long friendsId) {
        int rows = jdbc.update(ADD_FRIENDS_QUERRY, id, friendsId);
        if (rows == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return getUsersFriends(id);
    }

    // Готово
    public List<User> delFriends(long id, long friendsId) {
        int rows = jdbc.update(DELETE_FRIENDS_QUERY, id, friendsId);
        if (rows == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
        return getUsersFriends(id);
    }

    // Метод проверки дубликации имайлов. Затратный, так как приходится доставать из БД всю таблицу users.
    // ?? Но другого способа не придумал
    @Override
    public void checkDuplicateEmail(User user) {
        Map<Long, User> result = jdbc.query(FIND_ALL_USERS_QUERY, userResultSetExtractor);
        if (result.isEmpty()) return;

        if (result.values().stream()
                .anyMatch(existingUser -> user.getEmail().equals(existingUser.getEmail()))) {
            String errorMessage = String.format("Имайл %s уже используется", user.getEmail());
            throw new DuplicatedDataException(errorMessage);
        }
    }

    // !! Данный метод необходим в InMemoryUserStorage, поэтому содержится в интерфейсе. Для этого реализован здесь
    // По сути просто обращается к методу findById(id). Может удалить при неиспользовании InMemoryUserStorage
    public User getUserById(long id) {
        return findById(id);
    }
}
