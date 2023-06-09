package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Headers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mockMvc;
    private ItemRequestDto itemRequestDto;
    private User user;

    @BeforeEach
    void start() throws Exception {
        user = new User(1L, "ash@gmail.com", "Ash");
        itemRequestDto = new ItemRequestDto(1L, "allneeded", null, LocalDateTime.now(), new ArrayList<>());
    }

    @Test
    void create() throws Exception {
        when(itemRequestService.create(anyLong(), any()))
                .thenReturn(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Headers.IdOwner, 1L)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestsInformation() throws Exception {
        ItemRequestMapper.toItemRequest(itemRequestDto, user);
        when(itemRequestService.getAllUserRequest(anyLong()))
                .thenReturn(List.of(itemRequestDto));
        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Headers.IdOwner, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestInformation() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(itemRequestDto);
        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Headers.IdOwner, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests() throws Exception {
        when(itemRequestService.getAllRequest(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestDto));
        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(Headers.IdOwner, 1L))
                .andExpect(status().isOk());
    }
}