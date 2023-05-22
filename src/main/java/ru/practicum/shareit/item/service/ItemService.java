package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
        ItemDto addItem(Long ownerId, ItemDto itemDto);

        ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

        ItemDto getItem(Long itemId);

        List<ItemDto> getItemsOwner(Long ownerId);

        List<ItemDto> getItemsText(String text);
}
