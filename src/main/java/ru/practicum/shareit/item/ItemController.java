package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utils.Headers;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping()
    public ItemDto create(@RequestHeader(value = Headers.IdOwner) Long idOwner,
                          @Valid @RequestBody @NotNull ItemDto item) {
        return itemService.addItem(idOwner, item);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(value = Headers.IdOwner) Long idOwner,
                          @PathVariable Long id,
                          @Valid @RequestBody @NotNull ItemDto item) {
        return itemService.update(idOwner, id, item);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable Long id) {
        return itemService.getItem(id);
    }

    @GetMapping()
    public List<ItemDto> getItems(@RequestHeader(value = Headers.IdOwner) Long idOwner) {
        return itemService.getItemsOwner(idOwner);
    }

    @GetMapping("/search")
    public List<ItemDto> getItems(@RequestParam(name = "text") String text) {
        return itemService.getItemsText(text);
    }
}
