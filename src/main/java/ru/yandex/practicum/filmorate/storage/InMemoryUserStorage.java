package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public User createUser(User user) {
        user.setId(++id);
        log.info("Идентификатор пользователя: '{}'", user.getId());
        users.put(user.getId(), user);
        log.info("Пользователь '{}' успешно создан с идентификатором '{}'", user.getEmail(), user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
            users.put(user.getId(), user);
            log.info("Данные пользователя '{}' с идентификатором '{}' успешно обновлены",
                    user.getLogin(), user.getId());
            return user;
    }

    @Override
    public void deleteUsers() {
        users.clear();
        log.info("Хранилище пользователей теперь пусто");
    }

    @Override
    public User getUserById(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> getUsers() {
        log.info("В данный момент в хранилище сервиса '{}' пользователей", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public Boolean existById(Long id) {
        return users.containsKey(id);
    }
}
