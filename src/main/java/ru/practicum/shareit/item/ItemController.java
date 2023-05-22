package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping()
    public ItemDto create(@RequestHeader(value = Headers.IdOwner) Long idOwner,
                          @Valid @RequestBody @NotNull ItemDto item) {
        log.info("Получен запрос на создание нового предмета к эндпоинту: 'POST /items'");
        return itemService.addItem(idOwner, item);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(value = Headers.IdOwner) Long idOwner,
                          @PathVariable Long id,
                          @Valid @RequestBody @NotNull ItemDto item) {
        log.info("Получен запрос на обновление предмета к эндпоинту: 'PATCH /items/id'");
        return itemService.update(idOwner, id, item);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable Long id) {
        log.info("Получен запрос на получение предмета к эндпоинту: 'GET /items/id'");
        return itemService.getItem(id);
    }

    @GetMapping()
    public List<ItemDto> getItems(@RequestHeader(value = Headers.IdOwner) Long idOwner) {
        log.info("Получен запрос на получение всех предметов пользователя к эндпоинту: 'GET /items'");
        return itemService.getItemsOwner(idOwner);
    }

    @GetMapping("/search")
    public List<ItemDto> getItems(@RequestParam(name = "text") String text) {
        log.info("Получен запрос на получение всех предметов имеющих в нвзвании или описании заданный текст к эндпоинту: 'GET /items/search'");
        return itemService.getItemsText(text);
    }
}
