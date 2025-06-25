package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

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
        if (friends.isEmpty()) {
            throw new ObjectNotFoundException("Список друзей пользователя с идентификатором " + userId + " пуст");
        }
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
}
