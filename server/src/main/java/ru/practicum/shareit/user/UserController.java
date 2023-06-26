package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping()
    public List<UserDto> getUsers() {
        log.info("Получен запрос на получение всех пользователей к эндпоинту: 'GET /users'");
        return userService.getUsersList();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        log.info("Получен запрос на получение пользователя к эндпоинту: 'GET /users/id'");
        return userService.getUser(id);
    }

    @PostMapping()
    public UserDto create(@RequestBody UserDto user) {
        log.info("Получен запрос на создание нового пользователя к эндпоинту: 'POST /users'");
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto user) {
        log.info("Получен запрос на обновление пользователя к эндпоинту: 'PATCH /users/id'");
        return userService.updateUser(new UserDto(id, user.getEmail(), user.getName()));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Получен запрос на удаление пользователя к эндпоинту: 'DELETE /users/id'");
        userService.deleteUser(id);
    }
}