package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.utils.Headers;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestHeader(value = Headers.IdOwner) Long bookerId,
                                 @Validated @RequestBody BookingDtoShort bookingDto) {
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
                                       @RequestHeader(value = Headers.IdOwner) Long userId) {
        return bookingService.getBooking(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookedItemList(@RequestParam(defaultValue = "ALL") String state,
                                                   @RequestHeader(value = Headers.IdOwner) Long userId) {
        return bookingService.ownerItemsBookingLists(state, userId);
    }
}
