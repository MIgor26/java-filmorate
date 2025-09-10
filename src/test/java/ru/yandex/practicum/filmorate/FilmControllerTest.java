//package ru.yandex.practicum.filmorate;
//
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.Validation;
//import jakarta.validation.Validator;
//import jakarta.validation.ValidatorFactory;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import ru.yandex.practicum.filmorate.controller.FilmController;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.exception.ValidationException;
//import ru.yandex.practicum.filmorate.model.Film;
//
//import java.time.LocalDate;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//public class FilmControllerTest {
//
//    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
//    private final Validator validator = validatorFactory.getValidator();
//
//    @Autowired
//    private FilmController filmController;
//
//    @Test
//    public void emptyNameValidateTest() {
//        Film film = new Film();
//        film.setTitle("");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(1980, 10, 10));
//        film.setDuration(180);
//        Set<ConstraintViolation<Film>> violations = validator.validate(film);
//        System.out.println(violations);
//        assertTrue(!violations.isEmpty(), "Валидация имени работает не верно");
//    }
//
//    @Test
//    public void maxLengthDescriptionValidateTest() {
//        Film film = new Film();
//        film.setTitle("Film");
//        film.setDescription("1".repeat(201));
//        film.setReleaseDate(LocalDate.of(1980, 10, 10));
//        film.setDuration(180);
//        Set<ConstraintViolation<Film>> violations = validator.validate(film);
//        assertTrue(!violations.isEmpty(), "Валидация длины описания работает не верно");
//    }
//
//    @Test
//    public void releaseDateValidateTest() {
//        Film film = new Film();
//        film.setTitle("Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(1500, 10, 10));
//        film.setDuration(180);
//        assertThrows(ValidationException.class, () -> filmController.create(film), "Валидация даты релиза работает не верно");
//    }
//
//    @Test
//    public void durationValidateTest() {
//        Film film = new Film();
//        film.setTitle("Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(1980, 10, 10));
//        film.setDuration(-100);
//        Set<ConstraintViolation<Film>> violations = validator.validate(film);
//        assertTrue(!violations.isEmpty(), "Валидация продолжительности фильма работает не верно");
//    }
//
//    @Test
//    void notFoundByIdTest() {
//        Film film = new Film();
//        film.setId((long)100);
//        film.setTitle("Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(1980, 10, 10));
//        film.setDuration(-100);
//        assertThrows(NotFoundException.class, () -> filmController.update(film), "Пользователь обновляется по неверному id");
//    }
//}
