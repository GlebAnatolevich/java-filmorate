package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@SpringBootTest
public class FilmControllerTest {
    private final FilmController controller = new FilmController();
    private final Film film = Film.builder()
            .id(1)
            .name("Омерзительная восьмёрка")
            .description("Американский вестерн режиссёра и сценариста Квентина Тарантино")
            .releaseDate(LocalDate.of(2016, 1, 14))
            .duration(167)
            .build();

    @Test
    void updateShouldUpdateMovieData() {
        Film thisFilm = new Film(1, "The Hateful Eight",
                "American western thriller film written and directed by Quentin Tarantino",
                LocalDate.of(2015, 12, 7), 168);
        controller.create(film);
        controller.update(thisFilm);

        Assertions.assertEquals("American western thriller film written and directed by Quentin Tarantino",
                thisFilm.getDescription());
        Assertions.assertEquals(1, controller.getFilms().size());
    }

    @Test
    void createShouldAddMovie() {
        Film thisFilm = new Film(1, "Омерзительная восьмёрка",
                "Американский вестерн режиссёра и сценариста Квентина Тарантино",
                LocalDate.of(2016, 1, 14), 167);
        controller.create(thisFilm);

        Assertions.assertEquals(film, thisFilm);
        Assertions.assertEquals(1, controller.getFilms().size());
    }

    @Test
    void createShouldNotAddMovieWithEmptyName() {
        film.setName("");

        Assertions.assertThrows(ValidationException.class, () -> controller.create(film));
        Assertions.assertEquals(0, controller.getFilms().size());
    }

    @Test
    void createShouldNotAddMovieWithDescriptionMoreThan200() {
        film.setDescription("США после Гражданской войны. Легендарный охотник за головами Джон Рут по кличке " +
                "Вешатель конвоирует заключенную. По пути к ним прибиваются еще несколько путешественников. Снежная " +
                "буря вынуждает компанию искать укрытие в лавке на отшибе, где уже расположилось весьма пёстрое " +
                "общество: генерал конфедератов, мексиканец, ковбой… И один из них — не тот, за кого себя выдает.");

        Assertions.assertThrows(ValidationException.class, () -> controller.create(film));
        Assertions.assertEquals(0, controller.getFilms().size());
    }

    @Test
    void createShouldNotAddMovieWithDateReleaseEarlier1895() {
        film.setReleaseDate(LocalDate.of(1894, 12, 12));

        Assertions.assertThrows(ValidationException.class, () -> controller.create(film));
        Assertions.assertEquals(0, controller.getFilms().size());
    }

    @Test
    void createShouldNotAddMovieIfDurationIsLessThan0() {
        film.setDuration(-4);

        Assertions.assertThrows(ValidationException.class, () -> controller.create(film));
        Assertions.assertEquals(0, controller.getFilms().size());
    }
}
