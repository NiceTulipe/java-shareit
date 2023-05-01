package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class itemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Name cant be empty");
        } else if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Description cant be empty");
        } else if (itemDto.getAvailable() == null) {
            throw new ValidationException("Status cant be empty");
        } else {
            userStorage.checkerUserID(ownerId);
            Item item = ItemMapper.toItem(itemDto);
            User user = userStorage.getUserById(ownerId);
            item.setOwner(user);
            Item newItem = itemStorage.addItem(item);
            return ItemMapper.toItemDto(newItem);
        }
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        userStorage.checkerUserID(ownerId);
        if (itemStorage.getItem(itemId).getOwner().getId().equals(ownerId)) {
            Item item = ItemMapper.toItem(itemDto);
            User user = userStorage.getUserById(ownerId);
            item.setOwner(user);
            item.setId(itemId);
            Item newItem = itemStorage.updateItem(item);
            return ItemMapper.toItemDto(newItem);
        } else {
            throw new ObjectNotFoundException("User not found");
        }
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return ItemMapper.toItemDto(itemStorage.getItem(itemId));
    }

    @Override
    public List<ItemDto> getItemsOwner(Long ownerId) {
        userStorage.checkerUserID(ownerId);
        return ItemMapper.toItemDtoList(itemStorage.getItemsOwner(userStorage.getUserById(ownerId)));
    }

    @Override
    public List<ItemDto> getItemsText(String text) {
        if (!text.isBlank()) {
            return ItemMapper.toItemDtoList(itemStorage.getItemsText(text));
        }
        return new ArrayList<>();
    }
}
