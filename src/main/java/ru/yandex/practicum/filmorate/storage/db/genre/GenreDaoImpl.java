package ru.yandex.practicum.filmorate.storage.db.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenreById(Integer id) {
        log.debug("getGenreById({})", id);
        Genre genre = jdbcTemplate.queryForObject("SELECT genre_id, genre_type FROM genre WHERE genre_id=?",
                new GenreMapper(), id);
        log.trace("Тип жанра с id {} был найден", id);
        return genre;
    }

    @Override
    public List<Genre> getGenres() {
        log.debug("getGenres()");
        List<Genre> genreList = jdbcTemplate.query("SELECT genre_id, genre_type FROM genre ORDER BY genre_id",
                new GenreMapper());
        log.trace("Все типы жанров: {}", genreList);
        return genreList;
    }

    @Override
    public boolean isContains(Integer id) {
        log.debug("isContains({})", id);
        try {
            getGenreById(id);
            log.trace("Жанр с идентификатором {} был найден", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("Нет информации для жанра с идентификатором {}", id);
            return false;
        }
    }
}
