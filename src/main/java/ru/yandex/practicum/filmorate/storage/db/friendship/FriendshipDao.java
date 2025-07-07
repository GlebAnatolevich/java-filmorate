package ru.yandex.practicum.filmorate.storage.db.friendship;

import java.util.List;

public interface FriendshipDao {

    void addFriend(Long userId, Long friendId, boolean isFriend);

    void deleteFriend(Long userId, Long friendId);

    List<Long> getFriends(Long userId);

    boolean isFriend(Long userId, Long friendId);
}
