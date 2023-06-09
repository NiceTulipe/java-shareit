package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utils.Headers;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
    public ItemsDto getItem(@RequestHeader(value = Headers.IdOwner) Long userId,
                            @PathVariable Long id) {
        log.info("Получен запрос на получение предмета к эндпоинту: 'GET /items/id'");
        return itemService.getItem(id, userId);
    }

    @Validated
    @GetMapping()
    public List<ItemsDto> getItems(@RequestHeader(value = Headers.IdOwner) Long idOwner,
                                   @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                   @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение всех предметов пользователя к эндпоинту: 'GET /items'");
        return itemService.getItemsOwner(idOwner, from, size);
    }

    @Validated
    @GetMapping("/search")
    public List<ItemDto> getItems(@RequestParam(name = "text") String text,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение всех предметов имеющих в нвзвании или описании заданный текст к эндпоинту: 'GET /items/search'");
        return itemService.getItemsText(text, from, size);
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@RequestHeader(value = Headers.IdOwner) Long authorId,
                                 @PathVariable Long id,
                                 @Validated @RequestBody CommentDto commentBody) {
        return itemService.addComment(authorId, id, commentBody);
    }
}