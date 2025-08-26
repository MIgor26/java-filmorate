package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public User findById(@PathVariable int id) {
        return inMemoryUserStorage.findById(id);
    }

    public List<User> getUsersFriends(int id) {
        return inMemoryUserStorage.getUsersFriends(id);
    }

    public List<User> commonFriends(int id, int otherId) {
        return inMemoryUserStorage.commonFriends(id, otherId);
    }

    public Collection<User> getAll() {
        return inMemoryUserStorage.getAll();
    }

    public User create(User user) {
        log.info("Получен запрос на создание нового пользователя {}", user);
        // Проверяем, есть ли пользователь в базе с таким же имейлом.
        inMemoryUserStorage.checkDuplicateEmail(user);
        // Проверяем логин на соответствие условиям.
        if (!validateLogin(user.getLogin())) {
            String errorMessage = String.format("Логин %s не прошёл валидацию. " +
                    "Логин не должен быть пустым и не может содержать пробелы.", user.getLogin());
            throw new ValidationException(errorMessage);
        }
        return inMemoryUserStorage.create(user);
    }

    public User update(User user) {
        log.info("Получен запрос на изменение данных пользователя {}", user);
        // Проверяем имеется ли id пользователя в теле запроса.
        if (user.getId() == null) {
            String errorMessage = "Не указан id пользователя. Запрос не может быть обработан";
            throw new ValidationException(errorMessage);
        }
        return inMemoryUserStorage.update(user);
    }

    public List<User> addFriends(int id, int friendsId) {
        return inMemoryUserStorage.addFriends(id, friendsId);
    }

    public List<User> delFriends(int id, int friendsId) {
        return inMemoryUserStorage.delFriends(id, friendsId);
    }

    // Вспомогательный метод для валидации логина.
    private boolean validateLogin(String login) {
        return (login != null && !login.isBlank() && !login.contains(" "));
    }
}
