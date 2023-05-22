package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
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
import java.util.List;
import java.util.Objects;

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
    public BookingDto addBooking(Long bookerId, BookingDtoShort bookingDto) {
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
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        return toBookingDto(booking);
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
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Бронь под номером не найдена " + id));
        if (booking.getBooker().getId().equals(bookerId) || booking.getItem().getOwner().getId().equals(bookerId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new ObjectNotFoundException("К брони имеют доступ лишь пользователь и обладатель");
        }
    }

    @Transactional
    @Override
    public List<BookingDto> getBooking(String state, Long userId) {
        User user = checkUser(userId);
        BookingState stateFromText = BookingState.getStateFromText(state);
        LocalDateTime now = LocalDateTime.now();
        switch (stateFromText) {
            case ALL:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdOrderByStartDesc(userId));
            case CURRENT:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, now, now));
            case PAST:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId, now));
            case FUTURE:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId, now));
            case WAITING:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(userId, now,
                                BookingStatus.WAITING));
            case REJECTED:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStatusIsOrderByStartDesc(userId, BookingStatus.REJECTED));
        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }

    @Transactional
    @Override
    public List<BookingDto> ownerItemsBookingLists(String state, Long ownerId) {
        User user = checkUser(ownerId);
        BookingState stateFromText = BookingState.getStateFromText(state);
        LocalDateTime now = LocalDateTime.now();
        switch (stateFromText) {
            case ALL:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByOwnerItems(ownerId));
            case CURRENT:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllCurrentBookingsOwner(ownerId, now));
            case PAST:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllPastBookingsOwner(ownerId, now));
            case FUTURE:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllFutureBookingsOwner(ownerId, now));
            case WAITING:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.WAITING));
            case REJECTED:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.REJECTED));
        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }


    private void checkDates(BookingDtoShort bookingDto) {
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
