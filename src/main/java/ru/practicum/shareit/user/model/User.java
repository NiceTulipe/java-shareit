package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NonNull
@AllArgsConstructor

public class User {
    @Positive(message = "Id должен быть положительным")
    private Long id;

    @NotBlank
    @Email(message = "введен некорректный email")
    @Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
            message = "Поле email не должно содержать спец символов")
    private String email;

    @NotBlank
    @Pattern(regexp = "[a-zA-Zа-яА-Я\\s]*", message = "Поле name не должно содержать спец символов")
    private String name;
}
