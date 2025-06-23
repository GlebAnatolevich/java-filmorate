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
        filmValidation(film);
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
        filmValidation(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм '{}' обновлён в коллекции сервиса, идентификатор: '{}'", film.getName(), film.getId());
        } else {
            throw new ValidationException("Ошибка! Попытка обновления данных незарегистрированного фильма");
        }
        return film;
    }

    private void filmValidation(Film film) {
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка! Некорректная дата выхода фильма");
        }
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidationException("Ошибка! Попытка зарегистрировать фильм без названия");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Ошибка! Продолжительность фильма (мин) не может быть <= 0");
        }
        if (film.getDescription().length() > 200 || film.getDescription().isEmpty()) {
            throw new ValidationException("Ошибка! Описание не может быть пустым или содержать более 200 символов");
        }
        if (film.getId() <= 0) {
            film.setId(++id);
            log.info("Идентификатор фильма: '{}", film.getId());
        }
    }
}
