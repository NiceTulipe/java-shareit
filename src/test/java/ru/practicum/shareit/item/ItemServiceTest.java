package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.AuthorDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {


    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentDto commentDto;

    @Test
    void testAddItem_validAdd() {
        Long ownerId = 1L;
        ItemDto itemDto = new ItemDto(null,
                "1st Item",
                "All needed thing",
                true,
                null,
                null);
        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(new Item());

        ItemDto result = itemService.addItem(ownerId, itemDto);

        assertNotNull(result);
    }

    @Test
    public void testUpdateItem_valid() {
        Long ownerId = 1L;
        Long itemId = 2L;
        ItemDto itemDto = new ItemDto(1L,
                "test",
                "description",
                true,
                null,
                3L);
        ItemDto updateItemDto = new ItemDto(1L,
                "test_update",
                "description_update",
                true,
                null,
                3L);

        User user = new User();
        user.setId(ownerId);
        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setOwner(user);
        oldItem.setName("old name");
        oldItem.setDescription("old description");
        oldItem.setAvailable(false);
        oldItem.setRequestId(4L);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        ItemDto result = itemService.update(ownerId, itemId, itemDto);
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("test", result.getName());
        assertEquals("description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(3L, result.getRequestId());

        ItemDto resultUpdated = itemService.update(ownerId, itemId, updateItemDto);
        assertNotNull(resultUpdated);
        assertEquals(itemId, result.getId());
        assertEquals("test_update", resultUpdated.getName());
        assertEquals("description_update", resultUpdated.getDescription());
        assertTrue(resultUpdated.getAvailable());
        assertEquals(3L, resultUpdated.getRequestId());
    }

    @Test
    public void testGetItem() {
        Long itemId = 1L;
        Long userId = 2L;
        User owner = new User(1L, "Ash", "Ketchum");
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(new Item(itemId, "Poke Ball",
                        "The Poke Ball is a sphere",
                        true,
                        owner,
                        null)));
        Mockito.when(commentRepository.findByItemIn(Mockito.anyList(),
                Mockito.any(Sort.class))).thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findByItemInAndStatus(Mockito.anyList(),
                eq(BookingStatus.APPROVED), Mockito.any(Sort.class))).thenReturn(Collections.emptyList());

        ItemsDto result = itemService.getItem(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Poke Ball", result.getName());
        assertEquals("The Poke Ball is a sphere", result.getDescription());
        assertTrue(result.getAvailable());
        assertNull(result.getRequestId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    public void testUpdate_invalidOwnerId_throwsException() {
        Long ownerId = 1L;
        Long itemId = 2L;
        ItemDto itemDto = new ItemDto(1L,
                "test1",
                "description1",
                true,
                null,
                null);
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemService.update(ownerId, itemId, itemDto));
    }

    @Test
    public void failAddItem_invalidParams() {
        User owner = new User(1L, "test@gmail.com", "Tester");

        ItemDto newItem = new ItemDto(null,
                null,
                null,
                null,
                null,
                null);
        ValidationException exception = assertThrows(ValidationException.class, () ->
                itemService.addItem(owner.getId(), newItem));

        ItemDto newItemWithoutName = new ItemDto(null,
                null,
                null,
                true,
                null,
                null);
        assertThrows(ValidationException.class, () -> itemService.addItem(owner.getId(), newItemWithoutName));
        Assertions.assertNotNull(exception);

        ItemDto newItemWithoutDescription = new ItemDto(null,
                "testName",
                null,
                true,
                null,
                null);
        assertThrows(ValidationException.class, () -> itemService.addItem(owner.getId(), newItemWithoutDescription));
    }

    @Test
    public void testAddItemWithoutOwnerId() {
        ItemDto itemDto = new ItemDto(null,
                "Item1",
                "new item1",
                true,
                null,
                null);

        assertThrows(ValidationException.class, () -> {
            itemService.addItem(null, itemDto);
        });
    }

    @Test
    public void shouldMapToCommentDtoList() {
        User owner = new User(1L,
                "Ash@gmail.com",
                "Ash");
        Item item = new Item(1L,
                "Poke Ball",
                "The Poke Ball is a sphere",
                true,
                owner,
                null);

        User author = new User(3L,
                "test@gmail.com",
                "Tester");
        Comment comment1 = new Comment(1L, "text1", item, author, LocalDateTime.now());
        Comment comment2 = new Comment(1L, "text2", item, author, LocalDateTime.now());
        List<Comment> commentList = List.of(comment1, comment2);
        List<CommentDto> commentDto = CommentMapper.commentDtoList(commentList);
        Assertions.assertNotNull(commentDto);
        assertEquals(commentDto.get(0).getText(), comment1.getText());
        assertEquals(commentDto.get(1).getText(), comment2.getText());
    }

    @Test
    public void testAddBookingAndComment() {
        User owner = new User(1L,
                "test@gmail.com",
                "Tester");
        Item item = Item.builder()
                .id(1L)
                .name("Poke Ball")
                .description("The Poke Ball is a sphere")
                .owner(owner)
                .build();
        List<Comment> comments = List.of(
                new Comment(1L, "My 1st Poke Ball", item, owner, LocalDateTime.now()),
                new Comment(2L, "Very compact", item, owner, LocalDateTime.now())
        );
        List<Booking> bookings = List.of(
                new Booking(1L,
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now().minusDays(1),
                        item,
                        owner,
                        BookingStatus.APPROVED),
                new Booking(2L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        item,
                        owner,
                        BookingStatus.APPROVED)
        );
        LocalDateTime now = LocalDateTime.now();

        ItemsDto result = itemService.addBookingAndComment(item, 1L, comments, bookings, now);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertNull(result.getAvailable());
        assertNull(result.getRequestId());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertEquals(comments.size(), result.getComments().size());
        assertEquals(comments.get(0).getId(), result.getComments().get(0).getId());
        assertEquals(comments.get(1).getId(), result.getComments().get(1).getId());
    }

    @Test
    public void testGetItem_invalid_throwsException() {
        Long itemId = 1L;
        Long userId = 2L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemService.getItem(itemId, userId));
    }

    @Test
    public void testGetItems_invalidOwnerId_throwsException() {
        Long ownerId = -1L;
        int from = 0;
        int size = 10;

        assertThrows(ObjectNotFoundException.class, () -> itemService.getItemsOwner(ownerId, from, size));
    }

    @Test
    public void testGetItems_invalidValue_throwsException() {
        Long ownerId = 1L;
        int from = -1;
        int size = 10;

        assertThrows(ObjectNotFoundException.class, () -> itemService.getItemsOwner(ownerId, from, size));
    }

    @Test
    public void testGetItems_Negative() {
        Long ownerId = null;
        int from = 0;
        int size = 10;

        try {
            itemService.getItemsOwner(ownerId, from, size);
            fail("Expected NotFoundException");
        } catch (ObjectNotFoundException e) {

            assertEquals("Пользователь не найден", e.getMessage());
        }
    }

    @Test
    public void testToItemDtoList() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(1L,
                "Poke Ball",
                "The Poke Ball is a sphere",
                true,
                null,
                null));
        itemList.add(new Item(2L,
                "Great Bal",
                " is a type of Poké Ball that has a 50% higher chance to successfully " +
                        "catch a Pokémon than that of a regular Poké Ball",
                false,
                null,
                null));
        List<ItemDto> expectedResult = new ArrayList<>();
        expectedResult.add(new ItemDto(1L,
                "Poke Ball",
                "The Poke Ball is a sphere",
                true,
                null,
                null));
        expectedResult.add(new ItemDto(2L,
                "Great Bal",
                " is a type of Poké Ball that has a 50% higher chance to successfully " +
                        "catch a Pokémon than that of a regular Poké Ball",
                false,
                null,
                null));

        List<ItemDto> actualResult = ItemMapper.toItemDtoList(itemList);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testToItemDtoList_EmptyList() {
        List<Item> itemList = Collections.emptyList();
        List<ItemDto> expectedResult = Collections.emptyList();

        List<ItemDto> actualResult = ItemMapper.toItemDtoList(itemList);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetAllItemsByLike_withBlankText() {
        String text = "";
        Pageable page = PageRequest.of(0, 10);
        List<Item> expectedResult = Collections.emptyList();

        List<Item> actualResult = itemRepository.getItemsText(text, page);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetItems() {
        String searchText = "test";
        int from = 0;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);
        List<Item> itemList = Arrays.asList(
                new Item(1L,
                        "Poke Ball",
                        "The Poke Ball is a sphere",
                        true,
                        null,
                        null),
                new Item(2L,
                        "Great Bal",
                        " is a type of Poke Ball that has a 50% higher chance to successfully " +
                                "catch a Pokémon than that of a regular Poké Ball",
                        true,
                        null,
                        null),
                new Item(3L, "Ultra Ball",
                        "is a Poke Ball that has a 2x catch rate modifier",
                        true,
                        null,
                        null)
        );
        Mockito.when(itemRepository.getItemsText(eq(searchText), eq(page))).thenReturn(itemList);

        List<ItemDto> result = itemService.getItemsText(searchText, from, size);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        assertEquals("Poke Ball", result.get(0).getName());
        assertEquals("The Poke Ball is a sphere", result.get(0).getDescription());
        assertTrue(result.get(0).getAvailable());
        assertNull(result.get(0).getRequestId());
        assertEquals(2L, result.get(1).getId().longValue());
        assertEquals("Great Bal", result.get(1).getName());
        assertEquals(" is a type of Poke Ball that has a 50% higher chance to successfully " +
                "catch a Pokémon than that of a regular Poké Ball", result.get(1).getDescription());
        assertTrue(result.get(1).getAvailable());
        assertNull(result.get(1).getRequestId());
    }

    @Test
    public void getItems_withBlankText() {
        String text = "";
        int from = 0;
        int size = 10;

        List<ItemDto> actualResult = itemService.getItemsText(text, from, size);

        assertTrue(actualResult.isEmpty());
    }

    @Test
    public void testAddComment__authorNull_throwException() {
        Long authorId = 1L;
        Long itemId = 3L;
        CommentDto commentDto = new CommentDto(1L,
                "Test comment",
                null,
                "Nobody",
                LocalDateTime.now());
        assertThrows(ObjectNotFoundException.class, () -> itemService.addComment(authorId, itemId, commentDto));
    }

    @Test
    public void addComment_authorHasNotBookedItem_throwsException() {
        Long authorId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        User user = new User();
        user.setId(authorId);
        Item item = new Item();
        item.setId(itemId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findBookingsByItem(item,
                BookingStatus.APPROVED,
                authorId,
                LocalDateTime.now())).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> {
            itemService.addComment(authorId, itemId, commentDto);
        });
    }

    @Test
    public void addComment_itemNotFound_throwsException() {
        Long authorId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        User user = new User();
        user.setId(authorId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> {
            itemService.addComment(authorId, itemId, commentDto);
        });
    }

    @Test
    public void testGettersSetters() {
        commentDto = CommentDto.builder()
                .id(1L)
                .text("This is a comment")
                .author(AuthorDto.builder().id(2L).authorName("Misty").build())
                .authorName("Misty")
                .created(LocalDateTime.now())
                .build();

        Long newId = 0L;
        String newText = "Updated comment";
        AuthorDto newAuthor = AuthorDto.builder().id(4L).authorName("Professor Oak").build();
        String newAuthorName = "Professor Oak";
        LocalDateTime newCreated = LocalDateTime.now().plusDays(1);

        commentDto.setId(newId);
        commentDto.setText(newText);
        commentDto.setAuthor(newAuthor);
        commentDto.setAuthorName(newAuthorName);
        commentDto.setCreated(newCreated);

        assertEquals(newId, commentDto.getId());
        assertEquals(newText, commentDto.getText());
        assertEquals(newAuthor, commentDto.getAuthor());
        assertEquals(newAuthorName, commentDto.getAuthorName());
        assertEquals(newCreated, commentDto.getCreated());
    }

    @Test
    public void testToString() {
        commentDto = CommentDto.builder()
                .id(1L)
                .text("Misty comment")
                .author(AuthorDto.builder().id(2L).authorName("Misty").build())
                .authorName("Misty")
                .created(LocalDateTime.now())
                .build();

        String expectedString = "CommentDto(id=1, " +
                "text=Misty comment, " +
                "author=AuthorDto(id=2, " +
                "authorName=Misty, " +
                "email=null), " +
                "authorName=Misty, " +
                "created=" + commentDto.getCreated().toString() + ")";

        String resultString = commentDto.toString();

        assertEquals(expectedString, resultString);
    }

    @Test
    void shouldCreateComment() {
        User user = new User(1L, "Ash@gmail.com", "Ash");
        Item item = new Item(1L,
                "Poke Ball",
                "The Poke Ball is a sphere",
                true,
                user,
                null);
        Booking bookingLast = new Booking(1L, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item,
                user,
                BookingStatus.APPROVED);
        Comment comment1 = new Comment(1L,
                "Tets comment",
                item,
                user,
                LocalDateTime.now());
        CommentDto commentDto1 = CommentMapper.toCommentDto(comment1);
        CommentDto commentDtoOutput = CommentMapper.toCommentDto(comment1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findBookingsByItem(any(),
                eq(BookingStatus.APPROVED),
                anyLong(),
                any(LocalDateTime.class))).thenReturn(List.of(bookingLast));
        when(commentRepository.save(any())).thenReturn(comment1);

        CommentDto commentDtoOutputAfter = itemService.addComment(1L, 1L, commentDto1);
        assertEquals(commentDtoOutput.getId(), commentDtoOutputAfter.getId());
        assertEquals(commentDtoOutput.getText(), commentDtoOutputAfter.getText());
        assertEquals(commentDtoOutput.getAuthorName(), commentDtoOutputAfter.getAuthorName());
        assertEquals(commentDtoOutput.getCreated(), commentDtoOutputAfter.getCreated());
    }
}






