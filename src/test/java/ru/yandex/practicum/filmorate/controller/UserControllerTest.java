package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
public class UserControllerTest {
    private final UserController controller = new UserController();
    private final User user = User.builder()
            .id(1)
            .email("gg@yandex.ru")
            .login("gg")
            .name("GGA")
            .birthday(LocalDate.of(1997, 8, 4))
            .build();

    @Test
    void updateShouldUpdateUserData() {
        User thisUser = new User(1, "gg1@yandex.ru", "gg1", "GGA1",
                LocalDate.of(1997, 8, 4));
        controller.create(user);
        controller.update(thisUser);

        Assertions.assertEquals("gg1@yandex.ru", thisUser.getEmail());
        Assertions.assertEquals(user.getId(), thisUser.getId());
        Assertions.assertEquals(1, controller.getUsers().size());
    }

    @Test
    void createShouldCreateUser() {
        User thisUser = new User(1, "gg@yandex.ru", "gg", "GGA",
                LocalDate.of(1997, 8, 4));
        controller.create(thisUser);

        Assertions.assertEquals(user, thisUser);
        Assertions.assertEquals(1, controller.getUsers().size());
    }

    @Test
    void createShouldCreateUserIfNameIsEmpty() {
        User thisUser = new User(1, "gg@yandex.ru", "gg", null,
                LocalDate.of(1997, 8, 4));
        controller.create(thisUser);

        Assertions.assertEquals(1, thisUser.getId());
        Assertions.assertEquals("gg", thisUser.getName());
    }

    @Test
    void createShouldNotAddUserIfEmailIncorrect() {
        user.setEmail("gg.yandex.ru");

        Assertions.assertThrows(ValidationException.class, () -> controller.create(user));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void createShouldNotAddUserIfEmailIsEmpty() {
        user.setEmail("");

        Assertions.assertThrows(ValidationException.class, () -> controller.create(user));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void createShouldNotAddUserIfLoginIsEmpty() {
        user.setLogin("");

        Assertions.assertThrows(ValidationException.class, () -> controller.create(user));
        Assertions.assertEquals(0, controller.getUsers().size());
    }

    @Test
    void createShouldNotAddUserIfBirthdayIsInTheFuture() {
        user.setBirthday(LocalDate.of(2025, 8, 4));

        Assertions.assertThrows(ValidationException.class, () -> controller.create(user));
        Assertions.assertEquals(0, controller.getUsers().size());
    }
}
