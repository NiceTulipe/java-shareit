package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegTest {
    private final ItemRequestService requestService;
    private final UserService userService;

    @BeforeEach
    public void setUp() {
        createRequestor();
    }

    @Test
    public void shouldSuccessAddRequest() {
        UserDto requestorDto = userService.getUser(1L);
        ItemRequestDto itemRequest = new ItemRequestDto(null, "Allneded thing", requestorDto, LocalDateTime.now(), null);
        ItemRequestDto newItemRequest = requestService.create(1, itemRequest);

        Assertions.assertNotNull(newItemRequest);
        Assertions.assertEquals(newItemRequest.getDescription(), itemRequest.getDescription());
    }

    private void createRequestor() {
        UserDto userDto = new UserDto(1L,
                "mail@gmail.com",
                "name"
        );
        userService.addUser(userDto);
    }
}