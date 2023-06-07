package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.Positive;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@Builder
public class ItemDto {
    @Positive
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final User ownerId;
    private final Long requestId;
}

