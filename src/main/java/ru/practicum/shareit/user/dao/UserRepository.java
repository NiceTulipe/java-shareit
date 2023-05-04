package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.Map;

public interface UserRepository {
    Map<Long, User> findAllUsers();

    User getUserById(Long userId);

    User createUser(User user);

    User userUpdate(User user);

    void deleteUser(Long userId);

    void deleteAllUsers();

    boolean checkerUserID(Long id);
}
