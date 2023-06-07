package ru.practicum.shareit.booking;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testItemDto() throws IOException {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Poke Ball")
                .description("The Poke Ball is a sphere")
                .available(true)
                .requestId(1L)
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("ash@gmail.com")
                .name("Ash")
                .build();

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(dateTime.plusSeconds(1))
                .end(dateTime.plusSeconds(2))
                .item(itemDto)
                .booker(userDto)
                .status(BookingStatus.WAITING)
                .build();
        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(dateTime.plusSeconds(1)
                        .truncatedTo(ChronoUnit.SECONDS)
                        .toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(dateTime.plusSeconds(2)
                        .truncatedTo(ChronoUnit.SECONDS)
                        .toString());
        assertThat(result).extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name")
                .isEqualTo("Poke Ball");
        assertThat(result).extractingJsonPathStringValue("$.item.description")
                .isEqualTo("The Poke Ball is a sphere");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name")
                .isEqualTo("Ash");
        assertThat(result).extractingJsonPathStringValue("$.booker.email")
                .isEqualTo("ash@gmail.com");
    }

}
