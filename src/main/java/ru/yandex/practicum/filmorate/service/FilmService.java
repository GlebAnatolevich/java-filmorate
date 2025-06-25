package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public void like(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        userService.getUserStorage().getUserById(userId);
        if (film == null) {
            throw new ObjectNotFoundException("Ошибка! Фильм отсутствует в коллекции сервиса");
        }
        film.addLike(userId);
        log.info("Пользователь с идентификатором '{}' поставил лайк фильму с идентификатором '{}'", userId, filmId);
    }

    public void dislike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        userService.getUserStorage().getUserById(userId);
        if (film == null) {
            throw new ObjectNotFoundException("Ошибка! Фильм отсутствует в коллекции сервиса");
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
}
