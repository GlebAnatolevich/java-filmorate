package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@SpringBootTest
public class FilmControllerTest {
    private final FilmStorage storage = new InMemoryFilmStorage();
    private final UserStorage userStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(userStorage);
    private final FilmService service = new FilmService(storage, userService);
    private final FilmController controller = new FilmController(storage, service);
    private final Film film = Film.builder()
            .id(1L)
            .name("Омерзительная восьмёрка")
            .description("Американский вестерн режиссёра и сценариста Квентина Тарантино")
            .releaseDate(LocalDate.of(2016, 1, 14))
            .duration(167)
            .likes(new HashSet<>())
            .build();
    private final Film updatedFilm = Film.builder()
            .id(1L)
            .name("Новая Омерзительная восьмёрка")
            .description("Новый американский вестерн режиссёра и сценариста Квентина Тарантино")
            .releaseDate(LocalDate.of(2016, 1, 14))
            .duration(167)
            .likes(new HashSet<>())
            .build();
    private final User user = User.builder()
            .id(1L)
            .email("gg@yandex.ru")
            .login("gg")
            .name("GGA")
            .birthday(LocalDate.of(1997, 8, 4))
            .friends(new HashSet<>())
            .build();

    @Test
    void createShouldAddMovie() {
        controller.createFilm(film);

        Assertions.assertEquals(1, controller.getFilms().size());
    }

    @Test
    void updateShouldUpdateMovieData() {
        controller.createFilm(film);
        controller.updateFilm(updatedFilm);

        Assertions.assertEquals("Новый американский вестерн режиссёра и сценариста Квентина Тарантино",
                updatedFilm.getDescription());
        Assertions.assertEquals(1, controller.getFilms().size());
    }

    @Test
    void createShouldNotAddMovieWithDateReleaseEarlier1895() {
        film.setReleaseDate(LocalDate.of(1894, 12, 12));

        Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film));
        Assertions.assertEquals(0, controller.getFilms().size());
    }

    @Test
    void getFilmByIdShouldReturnMovieWithIdOne() {
        controller.createFilm(film);
        Film thisFilm = controller.getFilmById(film.getId());

        Assertions.assertEquals(1, thisFilm.getId());
    }

    @Test
    void likeMovieShouldAddLikeToMovie() {
        userStorage.createUser(user);
        controller.createFilm(film);
        controller.likeAMovie(film.getId(), user.getId());

        Assertions.assertTrue(film.getLikesQuantity() != 0);
    }

    @Test
    void removeLikeShouldRemoveLikeFromMovie() {
        userStorage.createUser(user);
        controller.createFilm(film);
        controller.likeAMovie(film.getId(), user.getId());
        controller.removeLike(film.getId(), user.getId());

        Assertions.assertEquals(0, film.getLikesQuantity());
    }

    @Test
    void getPopularMoviesShouldReturnListOfPopularMovies() {
        userStorage.createUser(user);
        controller.createFilm(film);
        controller.likeAMovie(film.getId(), user.getId());
        List<Film> popularMoviesList = service.getPopularMovies(1);

        Assertions.assertEquals(1, popularMoviesList.size());
    }
}
