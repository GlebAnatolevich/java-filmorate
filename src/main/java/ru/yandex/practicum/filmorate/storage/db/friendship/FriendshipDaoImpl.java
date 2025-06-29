package ru.yandex.practicum.filmorate.storage.db.friendship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.mapper.FriendshipMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipDaoImpl implements FriendshipDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(Long userId, Long friendId, boolean isFriend) {
        log.debug("addFriend({}, {}, {})", userId, friendId, isFriend);
        jdbcTemplate.update("INSERT INTO friends (user_id, friend_id, is_friend) VALUES(?, ?, ?)",
                userId, friendId, isFriend);
        Friendship friendship = getFriend(userId, friendId);
        log.trace("Эти пользователи сейчас друзья: {}", friendship);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        log.debug("deleteFriend({}, {})", userId, friendId);
        Friendship friendship = Objects.requireNonNull(getFriend(userId, friendId));
        jdbcTemplate.update("DELETE FROM friends WHERE user_id=? AND friend_id=?", userId, friendId);
        if (friendship.isFriend()) {
            jdbcTemplate.update("UPDATE friends SET is_friend=false WHERE user_id=? AND friend_id=?",
                    userId, friendId);
            log.debug("Дружба между {} и {} закончена", userId, friendId);
        }
        log.trace("Теперь они не друзья: {}", friendship);
    }

    @Override
    public List<Long> getFriends(Long userId) {
        log.debug("getFriends({})", userId);
        List<Long> friendsList = jdbcTemplate.query(
                        "SELECT user_id, friend_id, is_friend FROM friends WHERE user_id=?",
                        new FriendshipMapper(), userId)
                .stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());
        log.trace("Друзья пользователя с идентификатором {} : {}", userId, friendsList);
        return friendsList;
    }

    @Override
    public Friendship getFriend(Long userId, Long friendId) {
        log.debug("getFriend({}, {})", userId, friendId);
        return jdbcTemplate.queryForObject(
                "SELECT user_id, friend_id, is_friend FROM friends WHERE user_id=? AND friend_id=?",
                new FriendshipMapper(), userId, friendId);
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        log.debug("isFriend({}, {})", userId, friendId);
        try {
            getFriend(userId, friendId);
            log.trace("Найдена дружба между {} и {}", userId, friendId);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("Не найдено дружбы между {} и {}", userId, friendId);
            return false;
        }
    }
}
