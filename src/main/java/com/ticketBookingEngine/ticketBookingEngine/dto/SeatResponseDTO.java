package com.ticketBookingEngine.ticketBookingEngine.dto;

import com.ticketBookingEngine.ticketBookingEngine.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponseDTO {
    private Long showSeatId;
    private String seatNumber;
    private String seatType;
    private BigDecimal price;
    private SeatStatus status;
}
