package com.ticketBookingEngine.ticketBookingEngine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatsResponseDTO {
    private Long showId;
    private String eventTitle;
    private String venueName;
    private LocalDateTime startTime;
    private List<SeatResponseDTO> seats;
}
