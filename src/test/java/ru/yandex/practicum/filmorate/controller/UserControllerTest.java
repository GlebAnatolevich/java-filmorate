package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserDbService;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    private final UserDbService userService;
    private final JdbcTemplate jdbcTemplate;

    private final User user = new User("gg@ya.ru", "GGA",
            "Gleb", LocalDate.of(1997, 8, 4));
    private final User updatedUser = new User("vasya@ya.ru", "VAS",
            "Vasily", LocalDate.of(1995, 5, 15));
    private final User friend = new User("petya@ya.ru", "PET",
            "Petya", LocalDate.of(1993, 1, 30));
    private final User friendOfBoth = new User("ivan@ya.ru", "IVN",
            "Ivan", LocalDate.of(2002, 3, 4));

    @AfterEach
    void afterEach() {
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM films");
    }

    @Test
    public void createUserShouldCreateUser() {
        userService.createUser(user);

        Assertions.assertFalse(userService.getUsers().isEmpty());
    }

    @Test
    public void createUserShouldNotCreateUserWithId1() {
        userService.createUser(updatedUser);
        Assertions.assertThrows(DuplicateKeyException.class, () -> userService.createUser(updatedUser));
    }

    @Test
    public void updateUserShouldUpdateUser() {
        User thisUser = userService.createUser(user);
        User thisUpdatedUser = userService.updateUser(thisUser);

        Assertions.assertEquals(thisUser.getEmail(), thisUpdatedUser.getEmail());
    }

    @Test
    public void updateUserShouldNotUpdateUserIfNotExists() {
        Assertions.assertThrows(ObjectNotFoundException.class, () -> userService.updateUser(user));
    }

    @Test
    public void getUserByIdShouldReturnUserWithId1() {
        User newUser = userService.createUser(user);
        User thisUser = userService.getUserById(newUser.getId());

        Assertions.assertEquals(newUser.getEmail(), thisUser.getEmail());
    }

    @Test
    public void getUserByIdShouldNotReturnUserIfNotExists() {
        Assertions.assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    public void addFriendShouldAddFriend() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);
        userService.addFriend(thisUser.getId(), thisFriend.getId());
        userService.addFriend(thisFriend.getId(), thisUser.getId());

        Assertions.assertFalse(userService.getFriendsList(thisUser.getId()).isEmpty());
        Assertions.assertFalse(userService.getFriendsList(thisFriend.getId()).isEmpty());
    }

    @Test
    public void addFriendShouldStayAsFollower() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);
        userService.addFriend(thisUser.getId(), thisFriend.getId());

        Assertions.assertFalse(userService.getFriendsList(thisUser.getId()).isEmpty());
        Assertions.assertTrue(userService.getFriendsList(thisFriend.getId()).isEmpty());
    }

    @Test
    public void addFriendShouldThrowExceptionIfAddingYourselfIntoAFriendsList() {
        User thisUser = userService.createUser(user);
        Assertions.assertThrows(ObjectAlreadyExistsException.class,
                () -> userService.addFriend(thisUser.getId(), thisUser.getId()));
    }

    @Test
    public void deleteFriendShouldDeleteFriend() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);
        userService.addFriend(thisUser.getId(), thisFriend.getId());
        userService.addFriend(thisFriend.getId(), thisUser.getId());
        userService.deleteFriend(thisUser.getId(), thisFriend.getId());
        userService.deleteFriend(thisFriend.getId(), thisUser.getId());

        Assertions.assertTrue(userService.getFriendsList(thisUser.getId()).isEmpty());
        Assertions.assertTrue(userService.getFriendsList(thisFriend.getId()).isEmpty());
    }

    @Test
    public void deleteFriendShouldThrowExceptionIfNotFriends() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);

        Assertions.assertThrows(ValidationException.class,
                () -> userService.deleteFriend(thisUser.getId(), thisFriend.getId()));
        Assertions.assertThrows(ValidationException.class,
                () -> userService.deleteFriend(thisFriend.getId(), thisUser.getId()));
    }

    @Test
    public void getFriendsListShouldReturnFriendsListWithSize1() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);
        userService.addFriend(thisUser.getId(), thisFriend.getId());
        userService.addFriend(thisFriend.getId(), thisUser.getId());

        Assertions.assertEquals(1, userService.getFriendsList(thisUser.getId()).size());
        Assertions.assertEquals(1, userService.getFriendsList(thisFriend.getId()).size());
    }

    @Test
    public void getCommonFriendsShouldReturnListOfCommonFriends() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);
        User thisFriendOfBoth = userService.createUser(friendOfBoth);
        userService.addFriend(thisUser.getId(), thisFriend.getId());
        userService.addFriend(thisFriend.getId(), thisUser.getId());
        userService.addFriend(thisUser.getId(), thisFriendOfBoth.getId());
        userService.addFriend(thisFriend.getId(), thisFriendOfBoth.getId());

        Assertions.assertTrue(userService.getCommonFriends(thisUser.getId(), thisFriend.getId())
                .contains(thisFriendOfBoth));
    }

    @Test
    public void getCommonFriendsShouldReturnAnEmptyListsOfCommonFriends() {
        User thisUser = userService.createUser(user);
        User thisFriend = userService.createUser(friend);
        userService.addFriend(thisUser.getId(), thisFriend.getId());
        userService.addFriend(thisFriend.getId(), thisUser.getId());

        Assertions.assertTrue(userService.getCommonFriends(thisUser.getId(), thisFriend.getId()).isEmpty());
        Assertions.assertTrue(userService.getCommonFriends(thisFriend.getId(), thisUser.getId()).isEmpty());
    }
}
