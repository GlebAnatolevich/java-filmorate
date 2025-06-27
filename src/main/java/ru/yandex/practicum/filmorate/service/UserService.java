package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        validateUser(user);
        userStorage.createUser(user);
        return user;
    }

    public User updateUser(User user) {
        if (userStorage.existById(user.getId())) {
            validateUser(user);
            userStorage.updateUser(user);
            return user;
        } else {
            throw new ObjectNotFoundException("Ошибка! Попытка обновления данных незарегистрированного пользователя");
        }
    }

    public void deleteUsers() {
        userStorage.deleteUsers();
    }

    public User getUserById(Long id) {
        if (!userStorage.existById(id)) {
            throw new ObjectNotFoundException("Ошибка! Пользователь с идентификатором " + id + " отсутствует в " +
                    "хранилище");
        }
        return userStorage.getUserById(id);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(userId);
        log.info("Пользователи с идентификаторами '{}' и '{}' добавили друг друга в друзья",
                userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        log.info("Пользователи с идентификаторами '{}' и '{}' удалили друг друга из друзей",
                userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.getUserById(userId);
        Set<Long> friends = user.getFriends();
        return friends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        Set<Long> userFriends = user.getFriends();
        Set<Long> friendFriends = friend.getFriends();
        log.info("Список общих друзей пользователей с идентификаторами '{}' и '{}''", userId, friendId);
        if (userFriends.stream().anyMatch(friendFriends::contains)) {
            return userFriends.stream()
                    .filter(userFriends::contains)
                    .filter(friendFriends::contains)
                    .map(userStorage::getUserById).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя с идентификатором '{}' установлено: '{}'", user.getId(), user.getName());
        }
    }
}
