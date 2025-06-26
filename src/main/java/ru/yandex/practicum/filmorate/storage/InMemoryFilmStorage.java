package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films;
    private Long id;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
        id = 0L;
    }

    @Override
    public Film createFilm(Film film) {
        validateFilm(film);
        film.setId(++id);
        log.info("Идентификатор фильма: '{}", film.getId());
        films.put(film.getId(), film);
        log.info("Фильм '{}' добавлен в коллекцию сервиса, идентификатор: '{}'", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            validateFilm(film);
            films.put(film.getId(), film);
            log.info("Фильм '{}' обновлён в коллекции сервиса, идентификатор: '{}'", film.getName(), film.getId());
            return film;
        } else {
            throw new ObjectNotFoundException("Ошибка! Попытка обновления данных незарегистрированного фильма");
        }
    }

    @Override
    public void deleteFilms() {
        films.clear();
        log.info("Коллекция сервиса теперь пуста");
    }

    @Override
    public Film getFilmById(Long id) {
        if (!films.containsKey(id)) {
            throw new ObjectNotFoundException("Ошибка! Фильм с идентификатором " + id + " отсутствует в коллекции");
        }
        return films.get(id);
    }

    @Override
    public List<Film> getFilms() {
        log.info("В данный момент в коллекции сервиса '{}' фильмов", films.size());
        return new ArrayList<>(films.values());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка! Некорректная дата выхода фильма");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
    }
}
