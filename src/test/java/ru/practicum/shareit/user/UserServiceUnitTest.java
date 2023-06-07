package ru.practicum.shareit.user;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository repository;

    private final User user = new User(1L, "ash@gmail.com", "Ash");
    private final UserDto userDto = new UserDto(1L, "ash@gmail.com", "Ash");
    private final User user2 = new User(2L, "misty@gmail.com", "Misty");
    private final UserDto user2Dto = new UserDto(2L, "misty@gmail.com", "Misty");

    @Test
    public void createUserTest() {
        when(repository.save(any()))
                .thenReturn(user);

        assertThat(userService.addUser(userDto), equalTo(userDto));
    }

    @Test
    public void getUserByIdExistTest() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        assertThat(userDto, equalTo(userService.getUser(1L)));
    }

    @Test
    public void getUserByIdNotExistTest() {
        when(repository.findById(anyLong()))
                .thenReturn(empty());

        Exception exception = Assertions.assertThrows(ObjectNotFoundException.class, () -> userService.getUser(1L));
        assertThat(exception.getMessage(), equalTo("пользователь не найден " + user.getId()));
    }

    @Test
    public void getAllUsersExistTest() {
        when(repository.findAll())
                .thenReturn(List.of(user, user2));

        List<UserDto> users = userService.getUsersList();
        assertThat(users, equalTo(List.of(userDto, user2Dto)));
    }

    @Test
    public void editUserThrowNotFoundExceptionWhenUserNotExists() {
        when(repository.findById(anyLong()))
                .thenReturn(empty());

        Exception exception = Assertions.assertThrows(ObjectNotFoundException.class, () -> userService.getUser(1L));
        assertThat(exception.getMessage(), equalTo("пользователь не найден " + user.getId()));
    }

    @Test
    public void deleteUser_shouldDeleteUser() {
        userService.deleteUser(anyLong());
        verify(repository, times(1)).deleteById(anyLong());
    }

    @Test
    public void testToUserDto() {
        User user = new User(1L, "ProfessorOak@gmail.com", "Professor Oak");
        UserDto userDto = UserMapper.toUserDto(user);
        assertEquals(1L, userDto.getId());
        assertEquals("Professor Oak", userDto.getName());
        assertEquals("ProfessorOak@gmail.com", userDto.getEmail());

        user = new User(2L, null, "");
        userDto = UserMapper.toUserDto(user);
        assertEquals(2L, userDto.getId());
        assertEquals("", userDto.getName());
        assertNull(userDto.getEmail());
    }

}
