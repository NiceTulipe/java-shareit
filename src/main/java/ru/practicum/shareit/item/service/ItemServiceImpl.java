package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {

        if (ownerId == null) {
            throw new ValidationException("Owner ID не может быть null");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустой");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустой");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус не может быть пустой");
        }
        checkOwner(ownerId);
        Item item = ItemMapper.toItem(itemDto);
        Optional<User> user = userRepository.findById(ownerId);
        item.setOwner(user.get());
        Item newItem = itemRepository.save(item);
        return ItemMapper.toItemDto(newItem);
    }

    @Transactional
    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        checkOwner(ownerId);
        Item oldItem = itemRepository.findById(itemId).get();
        if (!oldItem.getOwner().getId().equals(ownerId)) {
            throw new ObjectNotFoundException("Пользователь не найден");
        } else {
            Item item = ItemMapper.toItem(itemDto);
            User user = userRepository.findById(ownerId).get();
            item.setOwner(user);
            item.setId(itemId);
            if (item.getAvailable() == null) {
                item.setAvailable(oldItem.getAvailable());
            }
            if (item.getName() == null || item.getName().isBlank()) {
                item.setName(oldItem.getName());
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                item.setDescription(oldItem.getDescription());
            }
            if (item.getRequest() == null) {
                item.setRequest(oldItem.getRequest());
            }
            Item newItem = itemRepository.save(item);
            return ItemMapper.toItemDto(newItem);
        }
    }

    @Transactional
    @Override
    public ItemsDto getItem(Long itemId, Long userId) {
        Item newItem = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException("Предмет не найден"));
        return fillWithBookingInfo(List.of(newItem), userId).get(0);
    }

    @Transactional
    @Override
    public List<ItemsDto> getItemsOwner(Long ownerId) {
        checkOwner(ownerId);
        return fillWithBookingInfo(itemRepository.findAllByOwnerIdOrderById(ownerId), ownerId);
    }

    @Transactional
    @Override
    public List<ItemDto> getItemsText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.toItemDtoList(itemRepository.getItemsText(text));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(authorId).orElseThrow(() ->
                new ObjectNotFoundException("Автор не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException("Предмет не найден"));
        List<Booking> authorBooked = bookingRepository.findBookingsByItem(item, BookingStatus.APPROVED, authorId, LocalDateTime.now());
        if (authorBooked.isEmpty()) {
            throw new ValidationException("Неверные параметры");
        }
        CommentDto newComment = CommentMapper.toCommentDto(commentRepository
                .save(CommentMapper.toComment(commentDto, user, item)));
        newComment.setAuthorName(user.getName());
        return newComment;
    }

    private List<ItemsDto> fillWithBookingInfo(List<Item> items, Long userId) {
        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(
                        items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
        Map<Item, List<Booking>> bookings = bookingRepository.findByItemInAndStatus(
                        items, BookingStatus.APPROVED,
                        Sort.by(DESC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
        LocalDateTime now = LocalDateTime.now();
        return items.stream().map(item -> addBookingAndComment(item, userId, comments.getOrDefault(item, List.of()),
                        bookings.getOrDefault(item, List.of()), now))
                .collect(toList());
    }

    private ItemsDto addBookingAndComment(Item item,
                                          Long userId,
                                          List<Comment> comments,
                                          List<Booking> bookings,
                                          LocalDateTime now) {
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemsDto(item, null, null, CommentMapper.commentDtoList(comments));
        }

        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .findFirst()
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((a, b) -> a.getStart().isBefore(b.getStart()) ? a : b)
                .orElse(null);

        ItemBookingInfoDto lastBookingDto = lastBooking != null
                ? BookingMapper.toItemBookingInfoDto(lastBooking) : null;
        ItemBookingInfoDto nextBookingDto = nextBooking != null
                ? BookingMapper.toItemBookingInfoDto(nextBooking) : null;
        return ItemMapper.toItemsDto(item, lastBookingDto, nextBookingDto, CommentMapper.commentDtoList(comments));
    }

    private void checkOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь не найден"));
    }
}
