package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@SpringBootTest
public class UserControllerTest {
    private final InMemoryUserStorage storage = new InMemoryUserStorage();
    private final UserService service = new UserService(storage);
    private final UserController controller = new UserController(service);
    private final User user = User.builder()
            .id(1L)
            .email("gg@yandex.ru")
            .login("gg")
            .name("GGA")
            .birthday(LocalDate.of(1997, 8, 4))
            .friends(new HashSet<>())
            .build();
    private final User anotherUser = User.builder()
            .id(2L)
            .email("gg2@yandex.ru")
            .login("gg2")
            .name("GGA2")
            .birthday(LocalDate.of(1997, 8, 4))
            .friends(new HashSet<>())
            .build();
    private final User commonUser = User.builder()
            .id(3L)
            .email("gg3@yandex.ru")
            .login("gg3")
            .name("GGA3")
            .birthday(LocalDate.of(1997, 8, 4))
            .friends(new HashSet<>())
            .build();

    @Test
    void createShouldCreateUser() {
        controller.createUser(user);

        Assertions.assertEquals(1, controller.getUsers().size());
    }

    @Test
    void updateShouldUpdateUserData() {
        User thisUser = new User(1L, "gg1@yandex.ru", "gg1", "GGA1",
                LocalDate.of(1997, 8, 4), new HashSet<>());
        controller.createUser(user);
        controller.updateUser(thisUser);

        Assertions.assertEquals("gg1@yandex.ru", thisUser.getEmail());
        Assertions.assertEquals(user.getId(), thisUser.getId());
        Assertions.assertEquals(1, controller.getUsers().size());
    }

    @Test
    void createShouldCreateUserIfNameIsEmpty() {
        User thisUser = new User(1L, "gg@yandex.ru", "gg", null,
                LocalDate.of(1997, 8, 4), new HashSet<>());
        controller.createUser(thisUser);

        Assertions.assertEquals(1, thisUser.getId());
        Assertions.assertEquals("gg", thisUser.getName());
    }

    @Test
    void addFriendShouldAddFriendToOtherUsersSet() {
        controller.createUser(user);
        controller.createUser(anotherUser);
        controller.addFriend(user.getId(), anotherUser.getId());

        Assertions.assertTrue(user.getFriendsQuantity() != 0);
        Assertions.assertTrue(anotherUser.getFriendsQuantity() != 0);
    }

    @Test
    void deleteFriendShouldDeleteFriendFromOtherUsersSet() {
        controller.createUser(user);
        controller.createUser(anotherUser);
        controller.addFriend(user.getId(), anotherUser.getId());
        controller.removeFriend(user.getId(), anotherUser.getId());

        Assertions.assertEquals(0, user.getFriendsQuantity());
        Assertions.assertEquals(0, anotherUser.getFriendsQuantity());
    }

    @Test
    void getCommonFriendsShouldReturnListWithSizeOne() {
        controller.createUser(user);
        controller.createUser(anotherUser);
        controller.addFriend(user.getId(), anotherUser.getId());
        controller.createUser(commonUser);
        controller.addFriend(user.getId(), commonUser.getId());
        controller.addFriend(anotherUser.getId(), commonUser.getId());
        List<User> commonFriendList = controller.getCommonFriends(user.getId(), anotherUser.getId());

        Assertions.assertEquals(1, commonFriendList.size());
    }

    @Test
    void getFriendsShouldReturnFriendsListOfUser() {
        controller.createUser(user);
        controller.createUser(anotherUser);
        controller.createUser(commonUser);
        controller.addFriend(user.getId(), anotherUser.getId());
        controller.addFriend(user.getId(), commonUser.getId());
        List<User> listOfUsersFriends = controller.getFriends(user.getId());

        Assertions.assertEquals(2, listOfUsersFriends.size());
    }
}
