package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository {
    List<Item> getItemsOwner(User user);

    List<Item> getItemsText(String text);

    Item getItem(Long id);

    Item addItem(Item item);

    Item updateItem(Item item);
}
