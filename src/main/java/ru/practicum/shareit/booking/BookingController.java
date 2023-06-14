package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.utils.Headers;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader(value = Headers.IdOwner) Long bookerId,
                                 @Valid @RequestBody BookItemRequestDto bookingDto) {
        log.info("Получен запрос на добавление нового запроса на бронирование " +
                "к эндпоинту: 'POST /bookings'");
        return bookingService.addBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@RequestHeader(value = Headers.IdOwner) Long ownerId,
                                    @PathVariable Long bookingId,
                                    @RequestParam(name = "approved") boolean approved) {
        log.info("Получен запрос на обновление запроса на бронирование " +
                "к эндпоинту: 'PATCH /bookings/{bookingId}'");
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(value = Headers.IdOwner) Long bookerId,
                                 @PathVariable Long bookingId) {
        log.info("Получен запрос на получение запроса на бронирование под номером " +
                "к эндпоинту: 'GET /bookings/{bookingId}'");
        return bookingService.getBooking(bookerId, bookingId);
    }

    @Validated
    @GetMapping
    public List<BookingDto> getBooking(@RequestParam(defaultValue = "ALL") String state,
                                       @RequestHeader(value = Headers.IdOwner) Long userId,
                                       @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                       @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос на получение списка всех запросов на бронирование " +
                "к эндпоинту: 'GET /bookings/{bookingId}'");
        return bookingService.getBooking(state, userId, from, size);
    }

    @Validated
    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookedItemList(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader(value = Headers.IdOwner) Long userId,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from) {
        log.info("Получен запрос на получение списка бронирований для всех вещей текущего пользователя " +
                "к эндпоинту: 'GET /bookings/owner'");
        return bookingService.ownerItemsBookingLists(state, userId, from, size);
    }
}
