package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.utils.Headers;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@Validated
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    private BookingState bookingState;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestHeader(value = Headers.IdOwner) Long bookerId,
                                 @Validated @RequestBody BookItemRequestDto bookingDto) {
        return bookingService.addBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@RequestHeader(value = Headers.IdOwner) Long ownerId,
                                    @PathVariable Long bookingId,
                                    @RequestParam(name = "approved") boolean approved) {
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(value = Headers.IdOwner) Long bookerId,
                                 @PathVariable Long bookingId) {
        return bookingService.getBooking(bookerId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBooking(@RequestParam(defaultValue = "ALL") String state,
                                       @RequestHeader(value = Headers.IdOwner) Long userId,
                                       @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                       @RequestParam(defaultValue = "10") @Positive Integer size)  {
        checkerState(state);
        return bookingService.getBooking(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookedItemList(@RequestParam(defaultValue = "ALL") String state,
                                                   @RequestHeader(value = Headers.IdOwner) Long userId,
                                                   @RequestParam(value = "size", defaultValue = "10") @Positive int size,
                                                   @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from){
        checkerState(state);
        return bookingService.ownerItemsBookingLists(state, userId, from, size);
    }

    private void checkerState(String state) {
        try {
            BookingState.valueOf(state);
        } catch (Exception e) {
            throw new RequestFailedException(String.format("Unknown state: %s", state));
        }
    }
}
