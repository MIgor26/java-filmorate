package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Autowired
    private UserController userController;

    @Test
    void emailValidationTest() throws ValidationException {
        User user = new User();
        user.setEmail("mail");
        user.setLogin("Login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1980, 10, 10));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(!violations.isEmpty(), "Валидация имайла работает не верно");
    }

    @Test
    void loginValidationTest() throws ValidationException {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("Lo gin");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1980, 10, 10));
        assertThrows(ValidationException.class, () -> userController.create(user), "Валидация логина работает не верно.");
    }

    @Test
    void loginEmptyValidationTest() throws ValidationException {
        User user = new User();
        user.setEmail("user2@mail.ru");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1980, 10, 10));
        assertThrows(ValidationException.class, () -> userController.create(user), "Валидация логина работает не верно.");
    }

    @Test
    void birthdayValidationTest() throws ValidationException {
        User user = new User();
        user.setEmail("user3@mail.ru");
        user.setLogin("Login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2026, 10, 10));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(!violations.isEmpty(), "Валидация даты рождения работает не верно");
    }

    @Test
    void emptyNameEqualsLoginTest() {
        User user = new User();
        user.setEmail("user4@mail.ru");
        user.setLogin("Login");
        user.setName("");
        user.setBirthday(LocalDate.of(1980, 10, 10));
        User newUser = userController.create(user);
        assertEquals(newUser.getName(), user.getLogin(), "Пустое имя не заменяется на логин.");
    }

    @Test
    void duplicateUsersValidationTest() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("Login1");
        user1.setName("User1");
        user1.setBirthday(LocalDate.of(1980, 10, 10));
        userController.create(user1);
        // Создаём второго пользователя с таким же имайлом
        User user2 = new User();
        user2.setEmail(user1.getEmail());
        user2.setLogin("Login2");
        user1.setName("User2");
        user1.setBirthday(LocalDate.of(1970, 10, 10));
        assertThrows(DuplicatedDataException.class, () -> userController.create(user2), "Двух пользователей с одинаковым имайл создать можно.");
    }

    @Test
    void notFoundByIdTest() {
        User user = new User();
        user.setUser_id((long)100);
        user.setEmail("user@mail.ru");
        user.setLogin("Login1");
        user.setName("user");
        user.setBirthday(LocalDate.of(1980, 10, 10));
        assertThrows(NotFoundException.class, () -> userController.update(user), "Пользователь обновляется по неверному id");
    }
}
