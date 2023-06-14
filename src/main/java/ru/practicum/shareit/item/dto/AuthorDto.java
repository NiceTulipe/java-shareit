package ru.practicum.shareit.item.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorDto {
    private Long id;
    private String authorName;
    private String email;
}