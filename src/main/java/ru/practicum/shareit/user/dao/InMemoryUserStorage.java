package ru.practicum.shareit.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.shareit.exception.EmailEarlyContains;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import java.util.*;


@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    Map<Long, String> emails = new HashMap<>();

    private Long idGenerate = 1L;

    @Override
    public Map<Long, User> findAllUsers() {
        return users;
    }

    @Override
    public User getUserById(Long userId) {
        checkerUserID(userId);
        log.info("Получен запрос GET user by Id, возвращаем пользователя с id  '{}' под именем '{}' ", users.get(userId), users.get(userId).getName());
        return users.get(userId);
    }

    @Override
    public User createUser(@Valid @RequestBody User user) {
        checkerUserEmail(user);
        user.setId(idGenerate++);
        users.put(user.getId(), user);
        emails.put(user.getId(), user.getEmail());
        log.info("Получен запрос POST user, добавлен пользователь id '{}' email '{}'  ", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public User userUpdate(@Valid @RequestBody User user) {
        checkerUserID(user.getId());
        if (user.getEmail() != null) {
            checkerUserEmail(user);
            emails.remove(user.getId());
            user.setEmail(user.getEmail());
            emails.put(user.getId(), user.getEmail());
        } else {
            user.setEmail(users.get(user.getId()).getEmail());
        }
        if (user.getName() != null) {
            user.setName(user.getName());
        } else {
            user.setName(users.get(user.getId()).getName());
        }
        users.put(user.getId(), user);
        log.info("Получен запрос PATCH user, обновлен user id '{}' Email '{}'  ", user.getId(), user.getEmail());
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        checkerUserID(userId);
        log.info("Получен запрос DELETE user, удален пользователь id '{}' имя '{}'  ", users.get(userId), users.get(userId).getName());
        emails.remove(userId);
        users.remove(userId);

    }

    @Override
    public void deleteAllUsers() {
        log.info("Получен запрос на удавление всех пользователей");
        users.clear();
    }

    @Override
    public boolean checkerUserID(Long userId) {
        if (!users.containsKey(userId)) {
            log.warn("Пользователь с таким id '{}' не обнаружен ", users.get(userId));
            throw new ObjectNotFoundException("Пользователь с таким id не обнаружен");
        }
        return false;
    }

    private void checkerUserEmail(User user) {
        if (emails.containsValue(user.getEmail())) {
            if (!emails.get(user.getId()).equals(user.getEmail())) {
                throw new EmailEarlyContains("Пользователь с таким email существует");
            }
        }
    }
}
