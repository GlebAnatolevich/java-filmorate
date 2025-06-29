package ru.yandex.practicum.filmorate.storage.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.film.FilmStorage;

import java.util.*;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Long> likes = new HashMap<>();
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
    public Film getFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public List<Film> getFilms() {
        log.info("В данный момент в коллекции сервиса '{}' фильмов", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        if (likes.containsKey(filmId)) {
            likes.put(filmId, userId);
        } else {
            throw new ObjectNotFoundException("Невозможно добавить лайк. Фильм " + filmId + " отсутствует");
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        if (likes.containsKey(filmId)) {
            likes.remove(filmId, userId);
        } else {
            throw new ObjectNotFoundException("Нет лайков от пользователя " + userId + " фильму " + filmId);
        }
    }

    @Override
    public int getLikesQuantity(Long filmId) {
        int sum = 0;
        if (likes.containsKey(filmId)) {
            for (Map.Entry<Long, Long> entry : likes.entrySet()) {
                Long film = entry.getKey();
                Long user = entry.getValue();
                if (film.equals(filmId)) {
                    sum += user;
                }
            }
        }
        return sum;
    }

    @Override
    public Boolean isContains(Long id) {
        return films.containsKey(id);
    }

    @Override
    public void addGenres(Long filmId, Set<Genre> genres) {

    }

    @Override
    public void updateGenres(Long filmId, Set<Genre> genres) {

    }

    @Override
    public Set<Genre> getGenres(Long filmId) {
        return null;
    }

    @Override
    public void deleteGenres(Long filmId) {

    }
}
