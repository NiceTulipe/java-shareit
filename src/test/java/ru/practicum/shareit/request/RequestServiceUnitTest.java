package ru.practicum.shareit.request;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestServiceUnitTest {

    private final ItemRequestRepository mockItemRequestRepository = Mockito.mock(ItemRequestRepository.class);
    private final UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
    private final ItemRepository mockItemRepository = Mockito.mock(ItemRepository.class);
    private final ItemRequestService mockItemRequestService = Mockito.mock(ItemRequestService.class);
    private final UserService mockUserService = Mockito.mock(UserService.class);

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private UserService userService;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private Item item;


    @BeforeEach
    void start() {
        user = new User(1L, "ash@gmail.com", "Ash");
        itemRequestDto = new ItemRequestDto(1L, "The Poke Ball is a sphere", null, LocalDateTime.now(), new ArrayList<>());
        itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        item = new Item(1L, "Poke Ball", "The Poke Ball is a sphere", true, null, 1L);
    }

    @Test
    public void shouldSuccessCreate() {
        User requestor = new User(1L, "misty@gmail.com", "Misty");
        when(mockUserRepository.findById(1L)).thenReturn(Optional.of(requestor));

        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "is a Poke Ball that has a 2x catch rate modifier", null, LocalDateTime.now(), null);

        ItemRequestDto newItemRequestDto = itemRequestService.create(1, itemRequestDto);

        Assertions.assertNotNull(newItemRequestDto);
        assertEquals(newItemRequestDto.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    void createWrongUser() {
        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.create(1L, itemRequestDto));
    }

    @Test
    void getRequestsInformationEmpty() {
        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        List<ItemRequestDto> requestDtoList = itemRequestService.getAllUserRequest(user.getId());
        assertEquals(0, requestDtoList.size());
    }


    @Test
    void getRequestsInformationWrongUser() {
        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getAllUserRequest(1L));
    }

    @Test
    void getRequestInformationWrongRequest() {
        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(user));
        when(mockItemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getById(user.getId(), 1L));
    }

    @Test
    void getRequestInformationWrongUser() {
        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getById(user.getId(), 1L));
    }


    @Test
    public void getAllUserRequest_withInvalidUserId_shouldThrowNotFoundException() {
        Long userId = 1L;
        when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getAllUserRequest(userId));
    }

    @Test
    public void getAllRequest_validInput_returnsList() {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        User user = new User();
        user.setId(userId);
        when(mockUserRepository.existsById(userId)).thenReturn(true);

        List<ItemRequest> itemRequests = new ArrayList<>();
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        User requestor = new User();
        requestor.setId(2L);
        itemRequest.setRequestor(requestor);
        itemRequests.add(itemRequest);
        when(mockItemRequestRepository.findAllByRequestorIdNot(userId, PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(new PageImpl<>(itemRequests));
        when(mockUserRepository.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        List<ItemRequestDto> expectedItemRequestDtoList = new ArrayList<>();
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        UserDto requestorDto = new UserDto(
                2L,
                null,
                null
        );
        itemRequestDto.setRequestor(requestorDto);
        itemRequestDto.setItems(new ArrayList<>());
        expectedItemRequestDtoList.add(itemRequestDto);
        List<ItemRequestDto> actualItemRequestDtoList = itemRequestService.getAllRequest(userId, from, size);

        assertEquals(expectedItemRequestDtoList, actualItemRequestDtoList);
    }
}