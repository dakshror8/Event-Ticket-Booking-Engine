package com.ticketBookingEngine.ticketBookingEngine.scheduler;

import com.ticketBookingEngine.ticketBookingEngine.service.SeatReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredSeatScheduler {

    private final SeatReleaseService seatReleaseService;

    /**
     * Runs every 30 seconds (fixedDelay = 30000ms).
     * fixedDelay ensures the next run starts 30s AFTER the previous execution finishes.
     */
    @Scheduled(fixedDelay = 30000)
    public void scheduleSeatRelease() {
        try {
            seatReleaseService.releaseExpiredSeats();
        } catch (Exception e) {
            log.error("Error occurred while executing expired seat release job", e);
        }
    }
}
