package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    public void emptyNameValidateTest() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1980, 10, 10));
        film.setDuration(180);
        assertThrows(ValidationException.class, () -> filmController.create(film), "Валидация имени работает не верно");
    }

    @Test
    public void maxLengthDescriptionValidateTest() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
                "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
                "0123456789_0123456789_0123456789");
        film.setReleaseDate(LocalDate.of(1980, 10, 10));
        film.setDuration(180);
        assertThrows(ValidationException.class, () -> filmController.create(film), "Валидация длины описания работает не верно");
    }

    @Test
    public void releaseDateValidateTest() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1500, 10, 10));
        film.setDuration(180);
        assertThrows(ValidationException.class, () -> filmController.create(film), "Валидация даты релиза работает не верно");
    }

    @Test
    public void durationValidateTest() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1980, 10, 10));
        film.setDuration(-100);
        assertThrows(ValidationException.class, () -> filmController.create(film), "Валидация продолжительности работает не верно");
    }

    @Test
    void notFoundByIdTest() {
        Film film = new Film();
        film.setId(100);
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1980, 10, 10));
        film.setDuration(-100);
        assertThrows(NotFoundException.class, () -> filmController.update(film), "Пользователь обновляется по неверному id");
    }
}
