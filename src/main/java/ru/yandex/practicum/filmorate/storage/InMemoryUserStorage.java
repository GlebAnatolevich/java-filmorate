package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users;
    private Long id;

    public InMemoryUserStorage() {
        users = new HashMap<>();
        id = 0L;
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        user.setId(++id);
        log.info("Идентификатор пользователя: '{}'", user.getId());
        users.put(user.getId(), user);
        log.info("Пользователь '{}' успешно создан с идентификатором '{}'", user.getEmail(), user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            validateUser(user);
            users.put(user.getId(), user);
            log.info("Данные пользователя '{}' с идентификатором '{}' успешно обновлены",
                    user.getLogin(), user.getId());
            return user;
        } else {
            throw new ObjectNotFoundException("Ошибка! Попытка обновления данных незарегистрированного пользователя");
        }
    }

    @Override
    public void deleteUsers() {
        users.clear();
        log.info("Хранилище пользователей теперь пусто");
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Ошибка! Пользователь с идентификатором " + id + " отсутствует в " +
                    "хранилище");
        }
        return users.get(id);
    }

    @Override
    public List<User> getUsers() {
        log.info("В данный момент в хранилище сервиса '{}' пользователей", users.size());
        return new ArrayList<>(users.values());
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя с идентификатором '{}' установлено: '{}'", user.getId(), user.getName());
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
    }
}
