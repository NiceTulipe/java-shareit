package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.RequestFailedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;


@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public BookingDto addBooking(Long bookerId, BookItemRequestDto bookingDto) {
        checkDates(bookingDto);
        User user = checkUser(bookerId);
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(()
                -> new ObjectNotFoundException("Предмет с id нет " + bookingDto.getItemId()));
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет с id недоступен " + item.getId());
        }
        Long ownerId = item.getOwner().getId();
        if (ownerId.equals(bookerId)) {
            throw new ObjectNotFoundException("Пользователь является обладатлем вещи");
        }
        return toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingDto, item, user)));
    }

    @Transactional
    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ObjectNotFoundException("Данной брони не существует " + bookingId));
        User owner = checkUser(ownerId);
        Item item = booking.getItem();
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new RequestFailedException("Бронь в статусе waiting");
        }
        if (Objects.equals(booking.getBooker().getId(), ownerId)) {
            throw new ObjectNotFoundException("Пользователь является обладатлем вещи");
        }
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(status);
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto getBooking(Long bookerId, Long id) {
        return bookingRepository.findById(id)
                .filter(b -> Objects.equals(b.getBooker().getId(), bookerId)
                        || Objects.equals(b.getItem().getOwner().getId(), bookerId))
                .map(BookingMapper::toBookingDto)
                .orElseThrow(() -> new ObjectNotFoundException("Booking with id= " + bookerId + " not found"));
    }

    @Transactional
    @Override
    public List<BookingDto> getBooking(String state, Long userId, int from, int size) {
        User user = checkUser(userId);
        BookingState stateFromText = BookingState.getStateFromText(state);
        Pageable page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        switch (stateFromText) {
            case ALL:
                bookings = bookingRepository
                        .findAllByBooker_IdOrderByStartDesc(userId, page);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStart(userId, now, now, page);
                break;
            case PAST:
                bookings = bookingRepository
                        .findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId, now, page);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId, now, page);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(userId, now,
                                BookingStatus.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findAllByBooker_IdAndStatusIsOrderByStartDesc(userId, BookingStatus.REJECTED, page);
                break;
        }
        return BookingMapper.toBookingDtoList(bookings);
    }

    @Transactional
    @Override
    public List<BookingDto> ownerItemsBookingLists(String state, Long ownerId, int from, int size) {
        User user = checkUser(ownerId);
        BookingState stateFromText = BookingState.getStateFromText(state);
        Pageable page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        switch (stateFromText) {
            case ALL:
                bookings = bookingRepository
                        .findAllByOwnerItems(ownerId, page);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findAllCurrentBookingsOwner(ownerId, now, page);
                break;
            case PAST:
                bookings = bookingRepository
                        .findAllPastBookingsOwner(ownerId, now, page);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findAllFutureBookingsOwner(ownerId, now, page);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.REJECTED, page);
                break;
        }
        return BookingMapper.toBookingDtoList(bookings);
    }


    public void checkDates(BookItemRequestDto bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Ошибка со временем бронирования");
        }
    }


    private User checkUser(Long checkedUserId) {
        User user = userRepository.findById(checkedUserId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Пользователь не найден");
        });
        return user;
    }
}
