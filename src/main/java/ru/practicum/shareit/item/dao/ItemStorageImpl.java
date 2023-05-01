package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    private Long generatedId = 1L;

    @Override
    public List<Item> getItemsOwner(User user) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsText(String text) {
        String fixedText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable()
                        && (item.getName().toLowerCase().contains(fixedText)
                        || (item.getDescription().toLowerCase().contains(fixedText))))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItem(Long id) {
        return items.get(id);
    }

    @Override
    public Item addItem(Item item) {
        item.setId(generatedId);
        items.put(generatedId, item);
        generatedId++;
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Item oldItem = items.get(item.getId());
        if (item.getName() != null && !item.getName().isBlank()) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        items.put(oldItem.getId(), oldItem);
        return oldItem;
    }
}
