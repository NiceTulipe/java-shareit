package ru.practicum.shareit.user.dto;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
public class UserDto {
    @Positive(message = "Id должен быть положительным")
    private Long id;

    @NotBlank
    @Pattern(regexp = "[a-zA-Zа-яА-Я\\s]*", message = "Поле name не должно содержать спец символов")
    private String name;

    @Email(message = "введен некорректный email")
    @NotNull
    @NotBlank
    @NotEmpty
    private String email;
}