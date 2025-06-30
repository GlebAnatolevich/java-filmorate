package ru.yandex.practicum.filmorate.storage.db.film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        KeyHolder keyHolder;
        keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("updateFilm({}).", film);
        jdbcTemplate.update(
                "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE film_id=?",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        Film thisFilm = getFilmById(film.getId());
        log.trace("Фильм {} был обновлен в базе данных", thisFilm);
        return thisFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        log.debug("getFilmById({})", id);
        Film thisFilm = jdbcTemplate.queryForObject(
                "SELECT film_id, name, description, release_date, duration, mpa_id FROM films WHERE film_id=?",
                new FilmMapper(), id);
        log.trace("Фильм: {} ", thisFilm);
        return thisFilm;
    }

    @Override
    public List<Film> getFilms() {
        log.debug("getFilms()");
        List<Film> films = jdbcTemplate.query(
                "SELECT film_id, name, description, release_date, duration, mpa_id FROM films", new FilmMapper());
        log.trace("Фильмы в базе данных: {}", films);
        return films;
    }

    @Override
    public List<Film> getPopularMovies(Integer count) {
        log.debug("getPopularMovies({})", count);
        List<Film> popularMovies = jdbcTemplate.query(
                """
                        SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id\s
                        FROM likes as l
                        RIGHT OUTER JOIN films as f ON f.film_id=l.film_id
                        GROUP BY f.film_id
                        ORDER BY COUNT(l.user_id) DESC
                        LIMIT ?""", new FilmMapper(), count);
        log.trace("Самые популярные фильмы: {}", popularMovies);
        return popularMovies;
    }

    @Override
    public Boolean isContains(Long id) {
        log.debug("isContains({})", id);
        try {
            getFilmById(id);
            log.trace("Фильм с идентификатором {} был найден", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("Информации для фильма с идентификатором {} не найдено", id);
            return false;
        }
    }

    @Override
    public void addGenres(Long filmId, Set<Genre> genres) {
        log.debug("addGenres({}, {})", filmId, genres);
        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
            log.trace("Жанры были добавлены к фильму с идентификатором {}", filmId);
        }
    }

    @Override
    public void updateGenres(Long filmId, Set<Genre> genres) {
        log.debug("updateGenres({}, {})", filmId, genres);
        deleteGenres(filmId);
        addGenres(filmId, genres);
    }

    @Override
    public Set<Genre> getGenres(Long filmId) {
        log.debug("getGenres({})", filmId);
        Set<Genre> genres = new LinkedHashSet<>(jdbcTemplate.query(
                "SELECT f.genre_id, g.genre_type FROM film_genre AS f " +
                        "LEFT OUTER JOIN genre AS g ON f.genre_id = g.genre_id WHERE f.film_id=? ORDER BY g.genre_id",
                new GenreMapper(), filmId));
        log.trace("Жанры фильма с идентификатором {} :", filmId);
        return genres;
    }

    @Override
    public void deleteGenres(Long filmId) {
        log.debug("deleteGenres({})", filmId);
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id=?", filmId);
        log.trace("Все жанры фильма с идентификатором {} были удалены", filmId);
    }
}

