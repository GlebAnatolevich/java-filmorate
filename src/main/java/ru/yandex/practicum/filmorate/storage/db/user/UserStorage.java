package ru.yandex.practicum.filmorate.storage.db.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    User getUserById(Long id);

    List<User> getUsers();

    List<User> getFriendsByUserId(Long userId);

    List<User> getCommonFriends(Long userId, Long friendId);

    Boolean isContains(Long id);
}
