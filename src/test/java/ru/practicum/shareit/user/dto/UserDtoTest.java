package ru.practicum.shareit.user.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDtoTest {
    final UserDto user1 = new UserDto(1L, "emai1@mail.com", "testname");
    private final UserService service;
    private final ItemService items;

    @Test
    void updateUserEmailInContext_expectedCorrect_returnUserDtoBeforeUpdate() {
        service.addUser(user1);

        UserDto updateUser = user1;


        UserDto userBeforeUpdate = service.updateUser(updateUser);

        assertEquals(updateUser.getEmail(), userBeforeUpdate.getEmail());
        assertEquals(updateUser.getName(), userBeforeUpdate.getName());
        assertEquals(updateUser.getId(), userBeforeUpdate.getId());
    }

}