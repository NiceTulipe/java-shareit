package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ItemRequestDto create(long userId, ItemRequestDto itemRequestDto) {
        User user = checkUser(userId);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Transactional
    @Override
    public ItemRequestDto getById(long userId, long RequestId) {
        User user = checkUser(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(RequestId)
                .orElseThrow(() -> new ObjectNotFoundException("Запрос под номером " + RequestId + " не найден"));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<Item> itemList = itemRepository.findByRequestId(itemRequestDto.getId());
        itemRequestDto.setItems(ItemMapper.toItemDtoList(itemList));
        return itemRequestDto;
    }

    @Transactional
    @Override
    public List<ItemRequestDto> getAllUserRequest(Long userId) {
        User requestor = checkUser(userId);
        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestorOrderByCreated(requestor);
        List<ItemRequestDto> itemRequestDtoList = ItemRequestMapper.toItemRequestDtoList(itemRequestList);
        addItems(itemRequestDtoList);
        return itemRequestDtoList;
    }

    @Transactional
    @Override
    public List<ItemRequestDto> getAllRequest(Long userId, Integer from, Integer size) {
        User user = checkUser(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        List<ItemRequestDto> itemRequestDtoList = itemRequestRepository.findAllByRequestorIdNot(
                        userId,
                        pageRequest)
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());

        addItems(itemRequestDtoList);
        return itemRequestDtoList;
    }

    private User checkUser(Long checkedUserId) {
        User user = userRepository.findById(checkedUserId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Пользователь не найден");
        });
        return user;
    }

    private void addItems(List<ItemRequestDto> itemRequestDtoList) {
        List<Long> requestIds = itemRequestDtoList.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(Item::getRequestId));
        itemRequestDtoList.forEach(requestDto -> {
            List<Item> itemList = itemsByRequestId.getOrDefault(requestDto.getId(), Collections.emptyList());
            requestDto.setItems(ItemMapper.toItemDtoList(itemList));
        });
    }

}
