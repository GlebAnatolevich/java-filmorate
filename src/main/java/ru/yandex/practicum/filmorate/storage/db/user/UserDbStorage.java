package ru.yandex.practicum.filmorate.storage.db.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.sql.Date;
import java.util.List;

@Slf4j
@Component("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        log.debug("createUser({})", user);
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) "
                        + "VALUES (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()));
        User thisUser = jdbcTemplate.queryForObject(
                "SELECT user_id, email, login, name, birthday "
                        + "FROM users "
                        + "WHERE email=?", new UserMapper(), user.getEmail());
        log.trace("{} был добавлен в базу данных", thisUser);
        return thisUser;
    }

    @Override
    public User updateUser(User user) {
        log.debug("updateUser({})", user);
        jdbcTemplate.update("UPDATE users "
                        + "SET email=?, login=?, name=?, birthday=? "
                        + "WHERE user_id=?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        User thisUser = getUserById(user.getId());
        log.trace("{} был обновлён в базе данных", thisUser);
        return thisUser;
    }

    @Override
    public User getUserById(Long id) {
        log.debug("getUserById({})", id);
        User thisUser = jdbcTemplate.queryForObject(
                "SELECT user_id, email, login, name, birthday FROM users "
                        + "WHERE user_id=?", new UserMapper(), id);
        log.trace("{} был возвращён", thisUser);
        return thisUser;
    }

    @Override
    public List<User> getUsers() {
        log.debug("getUsers()");
        List<User> users = jdbcTemplate.query(
                "SELECT user_id, email, login, name, birthday FROM users ",
                new UserMapper());
        log.trace("Пользователи в базе данных: {}", users);
        return users;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {

    }

    @Override
    public void removeFriend(Long userId, Long friendId) {

    }

    @Override
    public List<User> getFriends(Long userId) {
        return null;
    }

    @Override
    public Boolean isContains(Long id) {
        log.debug("isContains({})", id);
        try {
            getUserById(id);
            log.trace("Пользователь с id {} был найден", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("Не найдено информации для пользователя с id {}", id);
            return false;
        }
    }
}
