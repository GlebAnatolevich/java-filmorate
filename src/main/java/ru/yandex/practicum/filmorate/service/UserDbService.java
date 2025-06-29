package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.friendship.FriendshipDao;
import ru.yandex.practicum.filmorate.storage.db.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
public class UserDbService {
    private final UserStorage userStorage;
    private final FriendshipDao friendshipDao;

    @Autowired
    public UserDbService(@Qualifier("UserDbStorage") UserDbStorage userStorage,
                         FriendshipDao friendshipDao) {
        this.userStorage = userStorage;
        this.friendshipDao = friendshipDao;
    }

    public User createUser(User user) {
        if (user.getId() != null) {
            if (userStorage.isContains(user.getId())) {
                throw new ObjectAlreadyExistsException(format("Пользователь с id %d уже существует", user.getId()));
            } else {
                throw new IllegalArgumentException("Не удалось установить идентификатор");
            }
        }
        validate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (!userStorage.isContains(user.getId())) {
            throw new ObjectNotFoundException("Ошибка! Попытка обновления данных незарегистрированного пользователя");
        }
        validate(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(Long id) {
        if (!userStorage.isContains(id)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", id));
        }
        return userStorage.getUserById(id);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        checkIfFriend(userId, friendId);
        boolean isFriend = friendshipDao.isFriend(userId, friendId);
        friendshipDao.addFriend(userId, friendId, isFriend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        checkIfNotFriend(userId, friendId);
        friendshipDao.deleteFriend(userId, friendId);
    }

    public List<User> getFriendsList(Long id) {
        if (!userStorage.isContains(id)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", id));
        }
        List<User> friends = friendshipDao.getFriends(id).stream()
                .mapToLong(Long::valueOf)
                .mapToObj(userStorage::getUserById)
                .collect(Collectors.toList());
        log.trace("Друзья пользователя: {}", friends);
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", userId));
        }
        if (!userStorage.isContains(friendId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", friendId));
        }
        if (userId.equals(friendId)) {
            throw new ObjectAlreadyExistsException("Невозможно посмотреть список общих друзей, id " + userId);
        }
        List<User> userFriends = getFriendsList(userId);
        List<User> friendFriends = getFriendsList(friendId);
        return friendFriends.stream()
                .filter(userFriends::contains)
                .filter(friendFriends::contains)
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        log.debug("validate({})", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя с идентификатором '{}' установлено: '{}'", user.getId(), user.getName());
        }
    }

    private void checkIfFriend(Long userId, Long friendId) {
        log.debug("checkIfFriend({}, {})", userId, friendId);
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", userId));
        }
        if (!userStorage.isContains(friendId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", userId));
        }
        if (userId.equals(friendId)) {
            throw new ObjectAlreadyExistsException("Ошибка! Попытка добавить себя в список друзей, id " + userId);
        }
        if (friendshipDao.isFriend(userId, friendId)) {
            throw new ValidationException(
                    format("Пользователь с id %d уже есть в списке друзей пользователя с id %d", userId, friendId));
        }
    }

    private void checkIfNotFriend(Long userId, Long friendId) {
        log.debug("checkIfNotFriend({}, {})", userId, friendId);
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", userId));
        }
        if (!userStorage.isContains(friendId)) {
            throw new ObjectNotFoundException(format("Пользователь с id %d не найден", userId));
        }
        if (userId.equals(friendId)) {
            throw new ObjectAlreadyExistsException(
                    "Ошибка! Попытка удалить себя из списка друзей, id " + userId);
        }
    }
}
