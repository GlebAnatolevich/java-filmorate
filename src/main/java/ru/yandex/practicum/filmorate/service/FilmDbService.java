package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.db.genre.GenreDao;
import ru.yandex.practicum.filmorate.storage.db.like.LikeDao;
import ru.yandex.practicum.filmorate.storage.db.mpa.MpaDao;
import ru.yandex.practicum.filmorate.storage.db.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
public class FilmDbService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;
    private final LikeDao likeDao;

    @Autowired
    public FilmDbService(@Qualifier("FilmDbStorage") FilmDbStorage filmStorage,
                         @Qualifier("UserDbStorage") UserDbStorage userStorage,
                         GenreDao genreDao,
                         MpaDao mpaDao,
                         LikeDao likeDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
        this.likeDao = likeDao;
    }

    public Film createFilm(Film film) {
        checkIfExists(film);
        validate(film);
        Film thisFilm = filmStorage.createFilm(film);
        filmStorage.addGenres(thisFilm.getId(), film.getGenres());
        thisFilm.setGenres(filmStorage.getGenres(thisFilm.getId()));
        thisFilm.setMpa(mpaDao.getMpaById(thisFilm.getMpa().getId()));
        return thisFilm;
    }

    public Film updateFilm(Film film) {
        checkIfNotExists(film);
        validate(film);
        Film thisFilm = filmStorage.updateFilm(film);
        filmStorage.updateGenres(thisFilm.getId(), film.getGenres());
        thisFilm.setGenres(filmStorage.getGenres(thisFilm.getId()));
        thisFilm.setMpa(mpaDao.getMpaById(thisFilm.getMpa().getId()));
        return thisFilm;
    }

    public Film getFilmById(Long filmId) {
        if (!filmStorage.isContains(filmId)) {
            throw new ObjectNotFoundException("Невозможно найти фильм с id " + filmId);
        }
        Film film = filmStorage.getFilmById(filmId);
        film.setGenres(filmStorage.getGenres(filmId));
        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
        return film;
    }

    public Collection<Film> getFilms() {
        var films = filmStorage.getFilms();
        for (Film film : films) {
            film.setGenres(filmStorage.getGenres(film.getId()));
            film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
        }
        return films;
    }

    public Collection<Film> getPopularMovies(Integer count) {
        log.debug("getPopularMovies({})", count);
        List<Film> popularMovies = getFilms()
                .stream()
                .sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
        log.trace("Самые популярные фильмы: {}", popularMovies);
        return popularMovies;
    }

    public void like(Long filmId, Long userId) {
        checkLike(filmId, userId);
        if (likeDao.isLiked(filmId, userId)) {
            throw new ObjectAlreadyExistsException(format("Пользователь с id %d уже поставил лайк фильму %d",
                    userId, filmId));
        }
        likeDao.like(filmId, userId);
    }

    public void dislike(Long filmId, Long userId) {
        checkLike(filmId, userId);
        if (!likeDao.isLiked(filmId, userId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не ставил лайк фильму %d", userId, filmId));
        }
        likeDao.dislike(filmId, userId);
    }

    private void checkIfNotExists(Film film) {
        log.debug("checkIfNotExists({})", film);
        if (film.getId() != null) {
            if (!filmStorage.isContains(film.getId())) {
                throw new ObjectNotFoundException(format("Фильм с id %d не найден", film.getId()));
            }
        }
        if (!mpaDao.isContains(film.getMpa().getId())) {
            throw new ObjectNotFoundException(format("MPA для фильма с id %d не найден", film.getId()));
        }
        for (Genre genre : film.getGenres()) {
            if (!genreDao.isContains(genre.getId())) {
                throw new ObjectNotFoundException(format("Невозможно найти жанр для фильма с id %d", film.getId()));
            }
        }
    }

    private void checkIfExists(Film film) {
        if (film.getId() != null) {
            if (filmStorage.isContains(film.getId())) {
                throw new ObjectAlreadyExistsException(format("Фильм с id %d уже существует", film.getId()));
            } else {
                throw new IllegalArgumentException("Не удалось установить идентификатор");
            }
        }
        if (!mpaDao.isContains(film.getMpa().getId())) {
            throw new ObjectNotFoundException(format("MPA для фильма с id %d не найден", film.getId()));
        }
        for (Genre genre : film.getGenres()) {
            if (!genreDao.isContains(genre.getId())) {
                throw new ObjectNotFoundException(format("Невозможно найти жанр для фильма с id %d", film.getId()));
            }
        }
    }

    private void validate(Film film) {
        log.debug("validate({})", film);
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Ошибка! Некорректная дата выхода фильма");
        }
    }

    private int compare(Film film, Film otherFilm) {
        return Integer.compare(likeDao.countLikes(otherFilm.getId()), likeDao.countLikes(film.getId()));
    }

    private void checkLike(Long filmId, Long userId) {
        log.debug("checkLike({}, {})", filmId, userId);
        if (!filmStorage.isContains(filmId)) {
            throw new ObjectNotFoundException(format("Невозможно найти фильм с id %d", filmId));
        }
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("Невозможно найти пользователя с id %d", userId));
        }
    }
}
