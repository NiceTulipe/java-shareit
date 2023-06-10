package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItemId(booking.getItem().getId());
        dto.setItem(ItemMapper.toItemDto(booking.getItem()));
        dto.setStatus(booking.getStatus());
        return dto;
    }

    public static List<BookingDto> toBookingDtoList(List<Booking> bookingList) {
        return bookingList.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public static ItemBookingInfoDto toItemBookingInfoDto(Booking booking) {
        return new ItemBookingInfoDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd());
    }

    public static Booking toBooking(BookItemRequestDto bookingDto, Item item, User user) {
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}