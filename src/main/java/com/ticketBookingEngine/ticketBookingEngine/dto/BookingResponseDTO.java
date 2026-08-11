package com.ticketBookingEngine.ticketBookingEngine.dto;

import com.ticketBookingEngine.ticketBookingEngine.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Long bookingId;
    private Long userId;
    private Long showId;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime expiresAt;
    private List<String> seatNumbers;
}
