package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long id = 0L;

    @Override
    public Film createFilm(Film film) {
        film.setId(++id);
        log.info("Идентификатор фильма: '{}", film.getId());
        films.put(film.getId(), film);
        log.info("Фильм '{}' добавлен в коллекцию сервиса, идентификатор: '{}'", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
            films.put(film.getId(), film);
            log.info("Фильм '{}' обновлён в коллекции сервиса, идентификатор: '{}'", film.getName(), film.getId());
            return film;
    }

    @Override
    public void deleteFilms() {
        films.clear();
        log.info("Коллекция сервиса теперь пуста");
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public List<Film> getFilms() {
        log.info("В данный момент в коллекции сервиса '{}' фильмов", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Boolean existById(Long id) {
        return films.containsKey(id);
    }
}
