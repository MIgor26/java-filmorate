package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{filmId}")
    public Optional<Film> findById(@PathVariable int filmId) {
        return filmService.findById(filmId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(required = false) Long count) {
        return filmService.getPopular(count);
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film likeFilm(@PathVariable int id, @PathVariable int userId) {
        return filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film delLikeFilm(@PathVariable int id, @PathVariable int userId) {
        return filmService.delLikeFilm(id, userId);
    }
}
