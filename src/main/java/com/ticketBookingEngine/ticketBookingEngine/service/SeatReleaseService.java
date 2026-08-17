package com.ticketBookingEngine.ticketBookingEngine.service;

import com.ticketBookingEngine.ticketBookingEngine.entity.Booking;
import com.ticketBookingEngine.ticketBookingEngine.entity.BookingItem;
import com.ticketBookingEngine.ticketBookingEngine.entity.BookingStatus;
import com.ticketBookingEngine.ticketBookingEngine.entity.SeatStatus;
import com.ticketBookingEngine.ticketBookingEngine.repository.BookingRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReleaseService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional
    public void releaseExpiredSeats(){
        LocalDateTime now = LocalDateTime.now();

        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired booking(s) to process for seat release.", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            // Mark booking as EXPIRED
            booking.setStatus(BookingStatus.EXPIRED);

            // Revert each associated ShowSeat back to AVAILABLE
            for (BookingItem item : booking.getItems()) {
                if (item.getShowSeat().getStatus() == SeatStatus.HELD) {
                    item.getShowSeat().setStatus(SeatStatus.AVAILABLE);
                    log.info("Released Seat ID {} back to AVAILABLE for Show ID {}.",
                            item.getShowSeat().getSeat().getId(),
                            item.getShowSeat().getShow().getId());
                }
            }
        }


        log.info("Successfully released seats for {} expired booking(s).", expiredBookings.size());


    }

}
