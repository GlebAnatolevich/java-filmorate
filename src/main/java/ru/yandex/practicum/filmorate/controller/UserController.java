package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

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
        validateUser(user);
        user.setId(++id);
        log.info("Идентификатор пользователя: '{}'", user.getId());
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
        validateUser(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Данные пользователя '{}' с идентификатором '{}' успешно обновлены",
                    user.getLogin(), user.getId());
        } else {
            throw new ValidationException("Ошибка! Попытка обновления данных незарегистрированного пользователя");
        }
        return user;
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя с идентификатором '{}' установлено: '{}'", user.getId(), user.getName());
        }
    }
}
