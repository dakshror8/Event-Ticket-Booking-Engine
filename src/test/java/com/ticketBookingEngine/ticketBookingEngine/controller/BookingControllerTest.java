package com.ticketBookingEngine.ticketBookingEngine.controller;

import com.ticketBookingEngine.ticketBookingEngine.dto.BookingRequestDTO;
import com.ticketBookingEngine.ticketBookingEngine.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @Test
    @DisplayName("Should return 409 Conflict when Optimistic Locking fails")
    void whenOptimisticLockFails_thenReturn409Conflict() throws Exception {
        BookingRequestDTO requestDTO = BookingRequestDTO.builder()
                .userId(1L)
                .showId(1L)
                .showSeatIds(List.of(101L))
                .build();

        Mockito.when(bookingService.createBooking(Mockito.any(BookingRequestDTO.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException("ShowSeat", 101L));

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

}
