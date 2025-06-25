package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
public class FilmController {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private int id = 0;

    @ResponseBody
    @PostMapping("/films")
    public Film create(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(++id);
        log.info("Идентификатор фильма: '{}", film.getId());
        films.put(film.getId(), film);
        log.info("Фильм '{}' добавлен в коллекцию сервиса, идентификатор: '{}'", film.getName(), film.getId());
        return film;
    }

    @ResponseBody
    @GetMapping("/films")
    public List<Film> getFilms() {
        log.info("В данный момент в коллекции сервиса '{}' фильмов", films.size());
        return new ArrayList<>(films.values());
    }

    @ResponseBody
    @PutMapping("/films")
    public Film update(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            validateFilm(film);
            films.put(film.getId(), film);
            log.info("Фильм '{}' обновлён в коллекции сервиса, идентификатор: '{}'", film.getName(), film.getId());
        } else {
            throw new ValidationException("Ошибка! Попытка обновления данных незарегистрированного фильма");
        }
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка! Некорректная дата выхода фильма");
        }
    }
}
