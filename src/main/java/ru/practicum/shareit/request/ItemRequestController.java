package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.utils.Headers;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@Validated
@Slf4j
@RequestMapping(path = "/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(value = Headers.IdOwner) Long userId,
                                 @Valid @RequestBody @NotNull ItemRequestDto itemRequestDto) {
        log.info("Получен запрос на создание нового запроса вещи к эндпоинту: 'POST /requests'");
        return requestService.create(userId, itemRequestDto);
    }

    @GetMapping("{id}")
    public ItemRequestDto getRequestById(@RequestHeader(value = Headers.IdOwner) Long userId,
                                         @PathVariable Long id) {
        log.info("Получен запрос на получение данных о запросе под номером к эндпоинту: 'GET /requests/id'");
        return requestService.getById(userId, id);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserRequest(
            @RequestHeader(value = Headers.IdOwner) Long userId) {
        log.info("Получен запрос на получение всех запросов к эндпоинту: 'GET /requests'");
        return requestService.getAllUserRequest(userId);
    }

    @Validated
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(value = Headers.IdOwner) Long userId,
                                               @PositiveOrZero
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Получен запрос на получение всех запросов созданных другими пользователями к эндпоинту: 'GET /requests/all?from={from}&size={size}'");
        return requestService.getAllRequest(userId, from, size);
    }
}