package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUserModel(userDto);
        User newUser = userStorage.createUser(user);
        return UserMapper.toUserDto(newUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = UserMapper.toUserModel(userDto);
        User newUser = userStorage.userUpdate(user);
        return UserMapper.toUserDto(newUser);
    }

    @Override
    public List<UserDto> getUsersList() {
        return UserMapper.toUserDtoList(userStorage.findAllUsers());
    }

    @Override
    public UserDto getUser(Long id) {
        return UserMapper.toUserDto(userStorage.getUserById(id));
    }
}
