package com.ticketBookingEngine.ticketBookingEngine.scheduler;

import com.ticketBookingEngine.ticketBookingEngine.entity.*;
import com.ticketBookingEngine.ticketBookingEngine.repository.*;
import com.ticketBookingEngine.ticketBookingEngine.service.SeatReleaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ExpiredSeatSchedulerTest {

    @Autowired
    private SeatReleaseService seatReleaseService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("Expired HELD seat should revert to AVAILABLE when releaseExpiredSeats executes")
    void testReleaseExpiredSeats() {
        // Setup data
        User user = userRepository.save(User.builder().name("Test").email("t@e.com").passwordHash("x").build());
        Venue venue = venueRepository.save(Venue.builder().name("IMAX").city("Delhi").totalCapacity(100).build());
        Event event = eventRepository.save(Event.builder().title("Movie").category("FILM").durationMinutes(120).build());
        Show show = showRepository.save(Show.builder().event(event).venue(venue).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusHours(2)).build());
        Seat seat = seatRepository.save(Seat.builder().venue(venue).seatNumber("B1").seatType("REGULAR").build());

        // Create HELD seat with version = 0
        ShowSeat showSeat = showSeatRepository.save(ShowSeat.builder()
                .show(show)
                .seat(seat)
                .price(new BigDecimal("300.00"))
                .status(SeatStatus.HELD)
                .version(0L)
                .build());

        // Create EXPIRED booking (expiresAt set in the past: 15 minutes ago)
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .totalAmount(new BigDecimal("300.00"))
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusMinutes(15))
                .build();

        BookingItem item = BookingItem.builder().booking(booking).showSeat(showSeat).price(showSeat.getPrice()).build();
        booking.getItems().add(item);
        bookingRepository.save(booking);

        // Execute scheduled logic
        seatReleaseService.releaseExpiredSeats();

        // Verify assertions
        ShowSeat updatedSeat = showSeatRepository.findById(showSeat.getId()).orElseThrow();
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();

        assertEquals(SeatStatus.AVAILABLE, updatedSeat.getStatus(), "Seat status should revert to AVAILABLE");
        assertEquals(BookingStatus.EXPIRED, updatedBooking.getStatus(), "Booking status should update to EXPIRED");
    }
}

