package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    @Positive(message = "Id должен быть положительным")
    private final Long id;

    @NotBlank
    @Email(message = "введен некорректный email")
    private final String email;

    @NotBlank
    @Pattern(regexp = "[a-zA-Zа-яА-Я\\s]*", message = "Поле name не должно содержать спец символов")
    private final String name;
}