package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceDBest {

    private final EntityManager entityManager;
    private final UserService userService;

    @Test
    void createUser() {
        UserDto userDto = new UserDto(1L, "ash@gmail.com", "Ash");
        userService.addUser(userDto);


        TypedQuery<User> query = entityManager
                .createQuery("Select u from User u where u.email = :email", User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), equalTo(1L));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }


    @Test
    void getUserById() {
        UserDto userDto = new UserDto(null, "ash@gmail.com", "Ash");
        userService.addUser(userDto);

        UserDto user = userService.getUser(1L);

        assertThat(user.getId(), equalTo(1L));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void editUser() {
        UserDto userToCreate = new UserDto(1L, "ash@gmail.com", "Ash");
        UserDto userToUpdate = new UserDto(1L, "Update1ash@gmail.com", "AshUpdate1");
        UserDto userToUpdate2 = new UserDto(1L, "Update2ash@gmail.com", "AshUpdate2");
        UserDto userToUpdate3 = new UserDto(1L, "Update3ash@gmail.com", "AshUpdate3");

        userService.addUser(userToCreate);

        UserDto updatedUser = userService.updateUser(userToUpdate);

        assertThat(updatedUser.getName(), equalTo(userToUpdate.getName()));
        assertThat(updatedUser.getEmail(), equalTo(userToUpdate.getEmail()));

        updatedUser = userService.updateUser(userToUpdate2);

        assertThat(updatedUser.getEmail(), equalTo(userToUpdate2.getEmail()));

        updatedUser = userService.updateUser(userToUpdate3);

        assertThat(updatedUser.getName(), equalTo(userToUpdate3.getName()));
    }

    @Test
    void deleteUser() {
        UserDto userDto = new UserDto(1L, "ash@gmail.com", "Ash");
        userService.addUser(userDto);

        userService.deleteUser(1L);

        Assertions.assertThrows(ObjectNotFoundException.class, () -> userService.getUser(1L));
    }

}
