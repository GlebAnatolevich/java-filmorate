package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        validateFilm(film);
        filmStorage.createFilm(film);
        return film;
    }

    public Film updateFilm(Film film) {
        if (filmStorage.existById(film.getId())) {
            validateFilm(film);
            filmStorage.updateFilm(film);
            return film;
        } else {
            throw new ObjectNotFoundException("Ошибка! Попытка обновления данных незарегистрированного фильма");
        }
    }

    public void deleteFilms() {
        filmStorage.deleteFilms();
    }

    public Film getFilmById(Long id) {
        if (!filmStorage.existById(id)) {
            throw new ObjectNotFoundException("Ошибка! Фильм с идентификатором " + id + " отсутствует в коллекции");
        }
        return filmStorage.getFilmById(id);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void like(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        if (film == null) {
            throw new ObjectNotFoundException("Ошибка! Фильм отсутствует в коллекции сервиса");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Ошибка! Пользователь с идентификатором " + userId + " отсутствует в " +
                    "хранилище");
        }
        film.addLike(userId);
        log.info("Пользователь с идентификатором '{}' поставил лайк фильму с идентификатором '{}'", userId, filmId);
    }

    public void dislike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        if (film == null) {
            throw new ObjectNotFoundException("Ошибка! Фильм отсутствует в коллекции сервиса");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Ошибка! Пользователь с идентификатором " + userId + " отсутствует в " +
                    "хранилище");
        }
        film.removeLike(userId);
        log.info("Пользователь с идентификатором '{}' убрал лайк фильму с идентификатором '{}'", userId, filmId);
    }

    public List<Film> getPopularMovies(int count) {
        log.info("Список наиболее популярных фильмов по количеству лайков: ");
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesQuantity).reversed())
                .limit(count).collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка! Некорректная дата выхода фильма");
        }
    }
}
