package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmDbService;
import ru.yandex.practicum.filmorate.service.UserDbService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmControllerTest {
    private final FilmDbService filmService;
    private final UserDbService userService;
    private final JdbcTemplate jdbcTemplate;
    private final User user = new User("gg@yandex.ru", "GGA", "Gleb",
            LocalDate.of(1996, 12, 3));
    private final Film film = new Film("Первый фильм", "Описание первого",
            LocalDate.of(2021, 10, 22), 107);
    private final Film updatedFilm = new Film("Второй фильм",
            "Описание второго",
            LocalDate.of(2011, 10, 22), 107);
    private final Film oneMoreFilm = new Film("Ещё один фильм", "Описание ещё одного",
            LocalDate.of(2022, 1, 20), 113);
    private final Film unexistingFilm = new Film("Непонятный фильм", "Описание непонятного",
            LocalDate.of(2025, 2, 2), 100);
    private final Film popularFilm = new Film("Популярный",
            "Описание популярного", LocalDate.of(1998, 12, 15), 220);

    @AfterEach
    void afterEach() {
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM films");
    }

    @Test
    public void createFilmShouldCreateFilm() {
        film.setMpa(new Mpa(1));
        film.setGenres(Set.of(new Genre(2)));
        filmService.createFilm(film);

        Assertions.assertFalse(filmService.getFilms().isEmpty());
    }

    @Test
    public void updateFilmShouldUpdateFilm() {
        film.setMpa(new Mpa(1));
        film.setGenres(Set.of(new Genre(1)));
        Film newFilm = filmService.createFilm(film);
        newFilm.setGenres(Set.of(new Genre(3), new Genre(2)));
        Film filmUpdated = filmService.updateFilm(newFilm);

        Assertions.assertEquals(filmService.getFilmById(newFilm.getId()).getName(),
                filmService.getFilmById(filmUpdated.getId()).getName());
    }

    @Test
    public void getFilmByIdShouldReturnFilm() {
        film.setMpa(new Mpa(1));
        film.setGenres(Set.of(new Genre(1)));
        Film newFilm = filmService.createFilm(film);

        Assertions.assertEquals(newFilm, filmService.getFilmById(newFilm.getId()));
    }

    @Test
    public void getFilmByIdShouldNotReturnFilmIfIdIsIncorrect() {
        Assertions.assertThrows(ObjectNotFoundException.class, () -> filmService.getFilmById(145L));
    }

    @Test
    public void getFilmsShouldReturnListOfFilms() {
        film.setMpa(new Mpa(1));
        film.setGenres(Set.of(new Genre(1)));
        filmService.createFilm(film);
        popularFilm.setMpa(new Mpa(1));
        popularFilm.setGenres(Set.of(new Genre(1), new Genre(2)));
        filmService.createFilm(popularFilm);

        Assertions.assertEquals(2, filmService.getFilms().size());
    }

    @Test
    public void getFilmsShouldReturnAnEmptyListOfFilms() {
        Assertions.assertTrue(filmService.getFilms().isEmpty());
    }

    @Test
    public void getPopularMoviesShouldReturnListOfPopularMovies() {
        User newUser = userService.createUser(user);
        updatedFilm.setMpa(new Mpa(3));
        updatedFilm.setGenres(Set.of(new Genre(1), new Genre(2)));
        filmService.createFilm(updatedFilm);
        popularFilm.setMpa(new Mpa(4));
        popularFilm.setGenres(Set.of(new Genre(2), new Genre(3)));
        Film likedMovie = filmService.createFilm(popularFilm);
        filmService.like(likedMovie.getId(), newUser.getId());
        Collection<Film> films = filmService.getPopularMovies(1);

        Assertions.assertTrue(films.contains(likedMovie));
    }

    @Test
    public void likeShouldLikeAMovie() {
        User newUser = userService.createUser(user);
        unexistingFilm.setMpa(new Mpa(3));
        unexistingFilm.setGenres(Set.of(new Genre(1), new Genre(2)));
        Film newFilm = filmService.createFilm(unexistingFilm);
        filmService.like(newFilm.getId(), newUser.getId());

        Assertions.assertEquals(1, filmService.getPopularMovies(1).size());
    }

    @Test
    public void likeShouldNotLikeAMoviesIfItsAlreadyLikedByUser() {
        User thisUser = userService.createUser(user);
        oneMoreFilm.setMpa(new Mpa(3));
        oneMoreFilm.setGenres(Set.of(new Genre(1), new Genre(2)));
        Film thisOneMoreFilm = filmService.createFilm(oneMoreFilm);
        filmService.like(thisOneMoreFilm.getId(), thisUser.getId());

        Assertions.assertThrows(ObjectAlreadyExistsException.class,
                () -> filmService.like(thisOneMoreFilm.getId(), thisUser.getId()));
    }

    @Test
    public void dislikeShouldNotDislikeAMovieIfItWasNotLiked() {
        userService.createUser(user);
        film.setMpa(new Mpa(3));
        film.setGenres(Set.of(new Genre(1), new Genre(2)));
        filmService.createFilm(film);

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> filmService.dislike(film.getId(), user.getId()));
    }
}
