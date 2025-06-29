package ru.yandex.practicum.filmorate.storage.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.util.*;

@Slf4j
@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Long> friends = new HashMap<>();
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
    public User getUserById(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> getUsers() {
        log.info("В данный момент в хранилище сервиса '{}' пользователей", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (friends.containsKey(userId)) {
            friends.put(userId, friendId);
        } else {
            throw new ObjectNotFoundException("Ошибка! Попытка добавить в друзья незарегистрированного пользователя " +
                    userId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (friends.containsKey(userId)) {
            friends.remove(userId, friendId);
        } else {
            throw new ObjectNotFoundException("Ошибка! Попытка удалить из друзей незарегистрированного пользователя " +
                    userId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        List<User> friendsList = new ArrayList<>();
        if (friends.containsKey(userId)) {
            for (Map.Entry<Long, Long> entry : friends.entrySet()) {
                Long id = entry.getKey();
                if (id.equals(userId)) {
                    User friend = getUserById(entry.getValue());
                    friendsList.add(friend);
                }
            }
        }
        return friendsList;
    }

    @Override
    public Boolean isContains(Long id) {
        return users.containsKey(id);
    }
}
