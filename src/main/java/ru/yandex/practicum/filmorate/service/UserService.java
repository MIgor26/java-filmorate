package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    @Qualifier("dbUserService")
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // Готово
    public User findById(@PathVariable int id) {
        return userStorage.findById(id);
    }

    // Готово
    public List<User> getUsersFriends(int id) {
        return userStorage.getUsersFriends(id);
    }

    // Готово
    public List<User> commonFriends(int id, int otherId) {
        return userStorage.commonFriends(id, otherId);
    }

    // Готово
    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    // Готово
    public User create(User user) {
        log.info("Получен запрос на создание нового пользователя {}", user);
        // Проверяем, есть ли пользователь в базе с таким же имейлом.
        userStorage.checkDuplicateEmail(user);
        // Проверяем логин на соответствие условиям.
        if (!validateLogin(user.getLogin())) {
            String errorMessage = String.format("Логин %s не прошёл валидацию. " +
                    "Логин не должен быть пустым и не может содержать пробелы.", user.getLogin());
            throw new ValidationException(errorMessage);
        }
        return userStorage.create(user);
    }

    // Готово
    public User update(User user) {
        log.info("Получен запрос на изменение данных пользователя {}", user);
        // Проверяем имеется ли id пользователя в теле запроса.
        if (user.getId() == null) {
            String errorMessage = "Не указан id пользователя. Запрос не может быть обработан";
            throw new ValidationException(errorMessage);
        }
        return userStorage.update(user);
    }

    // Готово
    public List<User> addFriends(int id, int friendsId) {
        return userStorage.addFriends(id, friendsId);
    }

    // Готово
    public List<User> delFriends(int id, int friendsId) {
        return userStorage.delFriends(id, friendsId);
    }

    // Вспомогательный метод для валидации логина.
    private boolean validateLogin(String login) {
        return (login != null && !login.isBlank() && !login.contains(" "));
    }
}
