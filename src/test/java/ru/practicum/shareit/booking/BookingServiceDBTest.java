package ru.practicum.shareit.booking;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BookingServiceDBTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private UserDto testUser;
    private UserDto secondTestUser;
    private ItemDto itemDtoFromDB;
    private BookItemRequestDto bookItemRequestDto;
    private BookItemRequestDto secondBookItemRequestDto;

    @BeforeEach
    public void setUp() {
        ItemDto itemDto = ItemDto.builder()
                .name("Poke Ball")
                .description("The Poke Ball is a sphere")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .name("Ash")
                .email("ash@gmail.com")
                .build();

        UserDto secondUserDto = UserDto.builder()
                .name("Misty")
                .email("misty@gmail.com")
                .build();

        testUser = userService.addUser(userDto);
        secondTestUser = userService.addUser(secondUserDto);
        itemDtoFromDB = itemService.addItem(testUser.getId(), itemDto);

        bookItemRequestDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().plusNanos(1))
                .end(LocalDateTime.now().plusNanos(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        secondBookItemRequestDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(4))
                .itemId(itemDtoFromDB.getId())
                .build();
    }

    @Test
    void createBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);

        assertThat(bookingDtoFromDB.getId(), notNullValue());
        checkBookingsAreTheSame(bookingDtoFromDB,
                bookItemRequestDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.WAITING);
    }

    @Test
    void approveBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);
        BookingDto approveBooking = bookingService.approve(testUser.getId(), bookingDtoFromDB.getId(), true);

        checkBookingsAreTheSame(approveBooking,
                bookItemRequestDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.APPROVED);
    }

    @Test
    void getBookingByIdTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);
        BookingDto approveBooking = bookingService.approve(testUser.getId(), bookingDtoFromDB.getId(), true);
        BookingDto bookingById = bookingService.getBooking(testUser.getId(), approveBooking.getId());

        checkBookingsAreTheSame(bookingById, bookItemRequestDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBooking(999L, approveBooking.getId()));
    }

    @Test
    void getAllBookingsTest() {
        List<BookItemRequestDto> bookingDtos = List.of(bookItemRequestDto, secondBookItemRequestDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);
        BookingDto secondBooking = bookingService.addBooking(secondTestUser.getId(), secondBookItemRequestDto);
        List<BookingDto> bookings = bookingService.getBooking("ALL",
                secondTestUser.getId(), 0, 3);

        assertThat(bookings.size(), equalTo(bookingDtos.size()));
        for (BookItemRequestDto dto : bookingDtos) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(dto.getStart())),
                    hasProperty("end", equalTo(dto.getEnd())))));
        }
        List<BookingDto> approvedBookings = bookingService.getBooking("WAITING",
                secondTestUser.getId(), 0, 3);
        BookingDto waitingBooking = approvedBookings.get(0);

        assertThat(approvedBookings.size(), equalTo(1));
        assertThat(waitingBooking.getId(), equalTo(secondBooking.getId()));
        checkBookingsAreTheSame(waitingBooking,
                secondBookItemRequestDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.WAITING);
    }

    @Test
    void getAllOwnerBookingsTest() {
        List<BookItemRequestDto> bookingDtos = List.of(bookItemRequestDto, secondBookItemRequestDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);
        BookingDto secondBooking = bookingService.addBooking(secondTestUser.getId(), secondBookItemRequestDto);

        List<BookingDto> bookings = bookingService.ownerItemsBookingLists("ALL",
                testUser.getId(), 0, 3);

        assertThat(bookings.size(), equalTo(bookingDtos.size()));
        for (BookItemRequestDto dto : bookingDtos) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(dto.getStart())),
                    hasProperty("end", equalTo(dto.getEnd())))));
        }

        List<BookingDto> approvedBookings = bookingService.ownerItemsBookingLists("WAITING",
                testUser.getId(), 0, 3);
        BookingDto waitingBooking = approvedBookings.get(0);

        assertThat(approvedBookings.size(), equalTo(1));
        assertThat(waitingBooking.getId(), equalTo(secondBooking.getId()));
        checkBookingsAreTheSame(waitingBooking,
                secondBookItemRequestDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.WAITING);
    }

    @Test
    void approveBookingWrongOwnerTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approve(secondTestUser.getId(), bookingDtoFromDB.getId(), true));
    }

    @Test
    void getAllBookingsNonExistentStateTest() {
        String nonExistentState = "nonExistentState";
        bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);

        Assertions.assertThrows(RequestFailedException.class,
                () -> bookingService.getBooking(nonExistentState, secondTestUser.getId(), 0, 3));
    }

    @Test
    void getAllBookingsRejectedStateTest() {
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookItemRequestDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), false);

        List<BookingDto> rejectedBookings = bookingService.getBooking("REJECTED",
                secondTestUser.getId(), 0, 3);
        BookingDto rejectedBooking = rejectedBookings.get(0);

        assertThat(rejectedBookings.size(), equalTo(1));
        checkBookingsAreTheSame(rejectedBooking,
                bookItemRequestDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.REJECTED);
    }

    @Test
    void getAllBookingsCurrentStateTest() {
        BookItemRequestDto bookingDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookItemRequestDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> currentBookings = bookingService.getBooking("CURRENT",
                secondTestUser.getId(), 0, 3);
        BookingDto currentBooking = currentBookings.get(0);

        assertThat(currentBookings.size(), equalTo(bookingDtos.size()));
        checkBookingsAreTheSame(currentBooking,
                bookingDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.APPROVED);
    }

    @Test
    void getAllBookingsFutureStateTest() {
        BookItemRequestDto bookingDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookItemRequestDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);

        List<BookingDto> futureBookings = bookingService.getBooking("FUTURE",
                secondTestUser.getId(), 0, 3);
        BookingDto futureBooking = futureBookings.get(0);

        assertThat(futureBookings.size(), equalTo(bookingDtos.size()));
        assertThat(futureBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(futureBooking,
                bookingDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.WAITING);
    }

    @Test
    void getAllBookingsPastStateTest() {
        BookItemRequestDto bookingDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookItemRequestDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> pastBookings = bookingService.getBooking("PAST",
                secondTestUser.getId(), 0, 3);
        BookingDto pastBooking = pastBookings.get(0);

        assertThat(pastBookings.size(), equalTo(bookingDtos.size()));
        assertThat(pastBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(pastBooking,
                bookingDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.APPROVED);
    }

    @Test
    void getAllOwnerBookingsFutureStateTest() {
        BookItemRequestDto bookingDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookItemRequestDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> futureBookings = bookingService.ownerItemsBookingLists("FUTURE",
                testUser.getId(), 0, 3);
        BookingDto futureBooking = futureBookings.get(0);

        assertThat(futureBookings.size(), equalTo(bookingDtos.size()));
        assertThat(futureBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(futureBooking,
                bookingDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.APPROVED);
    }

    @Test
    void getAllOwnerBookingsPastStateTest() {
        BookItemRequestDto bookingDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookItemRequestDto> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> pastBookings = bookingService.ownerItemsBookingLists("PAST",
                testUser.getId(), 0, 3);
        BookingDto pastBooking = pastBookings.get(0);

        assertThat(pastBookings.size(), equalTo(bookingDtos.size()));
        assertThat(pastBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(pastBooking,
                bookingDto,
                secondTestUser,
                itemDtoFromDB,
                BookingStatus.APPROVED);
    }

    @Test
    void createBookingItemStartLaterThanFinishTest() {
        BookItemRequestDto bookingDto = BookItemRequestDto.builder()
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.addBooking(secondTestUser.getId(), bookingDto));
    }

    private void checkBookingsAreTheSame(
            BookingDto booking, BookItemRequestDto secondBooking, UserDto user, ItemDto item, BookingStatus status) {
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStatus(), equalTo(status));
        assertThat(booking.getStart(), equalTo(secondBooking.getStart()));
        assertThat(booking.getEnd(), equalTo(secondBooking.getEnd()));
        assertThat(booking.getBooker().getId(), equalTo(user.getId()));
        assertThat(booking.getItem().getId(), equalTo(item.getId()));
        assertThat(booking.getItem().getName(), equalTo(item.getName()));
    }

}
