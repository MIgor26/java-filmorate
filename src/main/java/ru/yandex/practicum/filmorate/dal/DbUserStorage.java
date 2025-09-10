package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository("dbUserStorage")
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {
    protected final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_USERS = "SELECT * FROM users " +
            "LEFT JOIN friendship ON users.id = friendship.user_id";

    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE users.id = ?";

    private static final String FIND_FRIENDS_ID = "SELECT friend_id FROM friendship WHERE user_id = ?";

    private static final String FIND_ALL_FRIENDS_USER = "SELECT u.* FROM users u " +
            "JOIN friendship f ON u.id = f.friend_id WHERE f.user_id = ?";

    private static final String FIND_COMMON_FRIENDS = "SELECT u.* FROM users u " +
            "JOIN friendship f1 ON u.id = f1.friend_id " +
            "JOIN friendship f2 ON u.id = f2.friend_id WHERE f1.user_id = ? AND f2.user_id = ?";

    public static final String INSERT_USER = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    public static final String EMILE_EXISTS = "SELECT COUNT(*) FROM users WHERE email = ?";
    public static final String LOGIN_EXISTS = "SELECT COUNT(*) FROM users WHERE login = ?";
    public static final String UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    public static final String INSERT_FRIENDS = "INSERT INTO friendship (user_id, friend_id, status) VALUES(?, ?, false)";
    public static final String DELETE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    public static final String DELETE_ALL_FRIENDS = "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";

    @Override
    public Collection<User> getAll() {
        Map<Long, User> userMap = new HashMap<>();

        jdbcTemplate.query(FIND_ALL_USERS, rs -> {
            Long userId = rs.getLong("id");
            User user = userMap.computeIfAbsent(userId, id -> {
                try {
                    return User.builder()
                            .id(id)
                            .email(rs.getString("email"))
                            .login(rs.getString("login"))
                            .name((rs.getString("name")))
                            .birthday(rs.getDate("birthday").toLocalDate())
                            .friends(new HashSet<>())
                            .build();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Long friendId = rs.getLong("friend_id");
            if (friendId > 0 && !friendId.equals(userId)) {
                user.getFriends().add(friendId);
            } else {
                log.warn("Запись или связь дружбы отсутствует в базе данных: userId={}, friendId={}", userId, friendId);
            }
        });

        return new ArrayList<>(userMap.values());
    }

    @Override
    public User findById(Long userId) {
        try {
            User user = jdbcTemplate.queryForObject(FIND_USER_BY_ID, new UserMapper(), userId);
            user.setFriends(new HashSet<>(getFriendsId(user.getId())));
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    @Override
    public List<User> getUsersFriends(long userId) {
        List<User> users = jdbcTemplate.query(FIND_ALL_FRIENDS_USER, new UserMapper(), userId);
        for (User user : users) {
            user.setFriends(new HashSet<>(getFriendsId(user.getId())));
        }
        return users;
    }

    @Override
    public List<User> commonFriends(long firstUserId, long secondUserId) {
        List<User> users = jdbcTemplate.query(FIND_COMMON_FRIENDS, new UserMapper(), firstUserId, secondUserId);
        for (User user : users) {
            user.setFriends(new HashSet<>(getFriendsId(user.getId())));
        }
        return users;
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(generatedId);

        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public void addFriends(long userId, long friendId) {
        jdbcTemplate.update(INSERT_FRIENDS, userId, friendId);
    }

    @Override
    public void delFriends(long userId, long friendId) {
        jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
    }

    @Override
    public void deleteAllFriends(Long userId) {
        jdbcTemplate.update(DELETE_ALL_FRIENDS, userId, userId);
    }

    // Новые методы
    @Override
    public boolean emilExists(User user) {
        Integer count = jdbcTemplate.queryForObject(EMILE_EXISTS, Integer.class, user.getEmail());
        return count > 0;
    }

    @Override
    public boolean loginExists(User user) {
        Integer count = jdbcTemplate.queryForObject(LOGIN_EXISTS, Integer.class, user.getLogin());
        return count > 0;
    }

    // Вспомогательные методы
    private Collection<Long> getFriendsId(Long userId) {
        return jdbcTemplate.queryForList(FIND_FRIENDS_ID, Long.class, userId);
    }
}
