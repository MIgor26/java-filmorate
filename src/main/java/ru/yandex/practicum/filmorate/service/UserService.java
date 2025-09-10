package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class UserService {
    @Qualifier("dbUserStorage")
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        log.info("Поступление запроса на получение всех пользователей");
        Collection<User> users = userStorage.getAll();
        if (users.isEmpty()) log.warn("Пользователи отсутствуют");
        log.info("Найдено {} пользователей", users.size());
        return userStorage.getAll();
    }

    public User findById(@PathVariable long id) {
        log.info("Поступление запроса на получение пользователя c id = {}", id);
        User user = userStorage.findById(id);
        if (user == null) log.warn("Пользователь с id = {} не найден", id);
        log.info("Пользователь с id = {} найден", id);
        return user;
    }

    public List<User> getUsersFriends(long id) {
        log.info("Запрос на список друзей пользователя с id = {}", id);
        User user = userStorage.findById(id);
        if (user == null) log.warn("Пользователь с id = {} не найден", id);
        List<User> friends = userStorage.getUsersFriends(id);

        if (friends.isEmpty()) log.warn("Список друзей пустой");
        log.info("У пользователя с id = {} найдено друзей в количестве {}", id, friends.size());
        return friends;
    }

    public List<User> commonFriends(long id, long otherId) {
        log.info("Запрос на общих друзей между пользователями с id = {} и id = {}", id, otherId);
        User firstUser = userStorage.findById(id);
        if (firstUser == null) log.warn("Пользователь с id = {} не найден", id);
        User secondUser = userStorage.findById(otherId);
        if (secondUser == null) log.warn("Пользователь с id = {} не найден", otherId);

        List<User> commonFriends = userStorage.commonFriends(id, otherId);
        if (commonFriends.isEmpty()) {
            log.warn("Общие друзья между пользователями с id = {} и id = {} не найдены", id, otherId);
        } else {
            log.info("Найдено {} общих друзей между пользователями с id = {} и id = {}",
                    commonFriends.size(), id, otherId);
        }
        return commonFriends;
    }

    public User create(User user) {
        log.info("Запрос на создание пользователя: {}", user.getName());
        if (userStorage.emilExists(user)) {
            log.warn("Email {} уже существует", user.getEmail());
            throw new ValidationException("Email уже существует");
        }
        if (userStorage.loginExists(user)) {
            log.warn("Login {} уже существует", user.getEmail());
            throw new ValidationException("Login уже существует");
        }

        User createUser = userStorage.create(user);
        if (createUser == null) log.warn("Ошибка при создание пользователя {}.", user.getName());
        log.info("Пользователь успешно создан: {}", createUser.getName());
        return createUser;
    }

    public User update(User newUser) {
        log.info("Запрос на обновление пользователя c id = {}", newUser.getId());
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: передан null в качестве ID");
            throw new ValidationException("Ошибка валидации: передан null в качестве ID");
        }

        User oldUser = userStorage.findById(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с ID {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с не найден");
        }

        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());

        User updateUser = userStorage.update(oldUser);
        if (updateUser == null) log.warn("Ошибка при обновлении пользователя с id = {}", newUser.getId());

        userStorage.deleteAllFriends(oldUser.getId());
        if (newUser.getFriends() == null || newUser.getFriends().isEmpty()) {
            updateUser.setFriends(new HashSet<>());
        } else {
            newUser.getFriends().forEach(friendId -> {
                userStorage.addFriends(updateUser.getId(), friendId);
            });
            updateUser.setFriends(newUser.getFriends());
        }

        log.info("Пользователь с id = {} успешно обновлён", updateUser.getId());
        return updateUser;
    }

    public void addFriends(Long userId, Long friendId) {
        log.info("Запрос на добавления пользователя с id = {} в друзья к пользователю с id = {}", friendId, userId);

        User user = userStorage.findById(userId);
        if (user == null) log.warn("Пользователь с id = {} не найден", userId);

        User friend = userStorage.findById(friendId);
        if (friend == null) log.warn("Пользователь с id = {} не найден", friendId);

        if (user.getId().equals(friend.getId())) {
            log.warn("Добавление в друзья пользователей с одинаковым id не возможно");
            throw new ValidationException("Добавление в друзья пользователей с одинаковым id не возможно");
        }
        userStorage.addFriends(userId, friendId);

        log.info("Пользователь с id = {} успешно добавлен в друзья к пользователю с id = {}", friendId, userId);
    }

    public void delFriends(Long userId, Long friendId) {
        log.info("Запрос на удаление из друзей пользователя с id = {} у пользователя с id = {}", friendId, userId);

        User user = userStorage.findById(userId);
        if (user == null) log.warn("Пользователь с id = {} не найден", userId);

        User friend = userStorage.findById(friendId);
        if (friend == null) log.warn("Пользователь с id = {} не найден", friendId);

        if (user.getId().equals(friend.getId())) {
            log.warn("Удаление друзей с одинаковым id пользователя не возможно");
            throw new ValidationException("Удаление друзей с одинаковым id пользователя не возможно");
        }
        userStorage.delFriends(userId, friendId);

        log.info("Пользователь с id = {} успешно удален из друзей у пользователю с id = {}", friendId, userId);
    }
}
