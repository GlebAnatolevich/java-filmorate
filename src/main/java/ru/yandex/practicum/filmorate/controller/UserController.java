package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int id = 0;

    @ResponseBody
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        userValidation(user);
        users.put(user.getId(), user);
        log.info("Пользователь '{}' успешно создан с идентификатором '{}'", user.getEmail(), user.getId());
        return user;
    }

    @ResponseBody
    @GetMapping
    public List<User> getUsers() {
        log.info("Количество пользователей: '{}'", users.size());
        return new ArrayList<>(users.values());
    }

    @ResponseBody
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        userValidation(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Данные пользователя '{}' с идентификатором '{}' успешно обновлены",
                    user.getLogin(), user.getId());
        } else {
            throw new ValidationException("Ошибка! Попытка обновления данных незарегистрированного пользователя");
        }
        return user;
    }

    private void userValidation(User user) {
        if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
            throw new ValidationException("Некорректная дата рождения пользователя с идентификатором '"
                    + user.getId() + "'");
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный адрес электронной почты пользователя с идентификатором '"
                    + user.getId() + "'");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя с идентификатором '{}' установлено: '{}'", user.getId(), user.getName());
        }
        if (user.getId() == 0 || user.getId() < 0) {
            user.setId(++id);
            log.info("Идентификатор пользователя: '{}'", user.getId());
        }
        if (user.getLogin().isBlank() || user.getLogin().isEmpty()) {
            throw new ValidationException("Некорректный логин пользователя с идентификатором '" + user.getId() + "'");
        }
    }
}
