package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.Positive;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class ItemDto {
    @Positive
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User ownerId;
    private Long request;
}

