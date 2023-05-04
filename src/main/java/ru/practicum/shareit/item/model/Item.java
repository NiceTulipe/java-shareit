package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.Positive;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class Item {
    @Positive
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private Long request;
}
