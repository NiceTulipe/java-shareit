package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CommentDto {
    private Long id;
    @NotBlank
    private String text;
    private AuthorDto author;
    private String authorName;
    private LocalDateTime created;


}
