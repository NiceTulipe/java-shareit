package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userStorage;

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUserModel(userDto);
        User newUser = userStorage.save(user);
        return UserMapper.toUserDto(newUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        userStorage.deleteById(userId);
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = userStorage.findById(userDto.getId()).orElseThrow(() ->
                new ObjectNotFoundException("пользователь не найден " + userDto.getId()));

        if (userDto.getName() != null && !(userDto.getName().isBlank())) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !(userDto.getEmail().isBlank())) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public List<UserDto> getUsersList() {
        return UserMapper.toUserDtoList(userStorage.findAll());
    }

    @Transactional
    @Override
    public UserDto getUser(Long id) {
        return UserMapper.toUserDto(userStorage.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("пользователь не найден " + id)));
    }
}
