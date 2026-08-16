package com.ticketBookingEngine.ticketBookingEngine.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.ticketBookingEngine.ticketBookingEngine.dto.BookingRequestDTO;
import com.ticketBookingEngine.ticketBookingEngine.dto.BookingResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.entity.*;
import com.ticketBookingEngine.ticketBookingEngine.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DoubleBookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

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

    private Long testShowId;
    private Long testShowSeatId;

    @BeforeEach
    void setUpTestData() {
        // 1. Clean database in reverse order of foreign key dependencies
        bookingItemRepository.deleteAllInBatch();
        bookingRepository.deleteAllInBatch();
        showSeatRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        showRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        venueRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 2. Create 50 Users for concurrent requests
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            users.add(User.builder()
                    .name("Test User " + i)
                    .email("user" + i + "@example.com")
                    .passwordHash("hashed_password_" + i)
                    .build());
        }
        userRepository.saveAll(users);

        // 3. Create Venue & Event
        Venue venue = venueRepository.save(Venue.builder()
                .name("PVR IMAX")
                .city("Mumbai")
                .totalCapacity(100)
                .build());

        Event event = eventRepository.save(Event.builder()
                .title("Interstellar IMAX Special")
                .category("MOVIE")
                .durationMinutes(169)
                .build());

        // 4. Create Show
        Show show = showRepository.save(Show.builder()
                .event(event)
                .venue(venue)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .build());
        this.testShowId = show.getId();

        // 5. Create Physical Seat
        Seat seat = seatRepository.save(Seat.builder()
                .venue(venue)
                .seatNumber("A1")
                .seatType("VIP")
                .build());

        // 6. Create ShowSeat (Target seat for concurrency testing)
        ShowSeat showSeat = showSeatRepository.save(ShowSeat.builder()
                .show(show)
                .seat(seat)
                .price(new BigDecimal("500.00"))
                .status(SeatStatus.AVAILABLE)
                .version(0L)
                .build());
        this.testShowSeatId = showSeat.getId();
    }

    @Test
    @DisplayName("Demonstrate Double Booking Bug under 50 Concurrent Requests")
    void demonstrateDoubleBookingBug() throws InterruptedException, ExecutionException {
        // ---GIVEN---
        int numberOfConcurrentUsers = 50;

        // Count successful bookings and exceptions using thread-safe atomic counters
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Create a fixed thread pool of 50 threads
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentUsers);

        // CountDownLatch forces all 50 threads to wait and launch AT THE EXACT SAME INSTANT
        CountDownLatch latch = new CountDownLatch(1);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        List<User> users = userRepository.findAll();

        // --- WHEN ---
        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            final Long userId = users.get(i).getId();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Wait at the starting line until latch releases
                    latch.await();

                    BookingRequestDTO request = BookingRequestDTO.builder()
                            .userId(userId)
                            .showId(testShowId)
                            .showSeatIds(List.of(testShowSeatId))
                            .build();

                    // Call the Booking Service (Unprotected)
                    BookingResponseDTO response = bookingService.createBooking(request);
                    if (response != null && response.getBookingId() != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Expecting failure for losing threads once seat is taken
                    failureCount.incrementAndGet();
                }
            }, executorService);

            futures.add(future);
        }

        // Release all 50 threads simultaneously
        latch.countDown();

        // Wait for all 50 threads to finish execution
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        // --- THEN (Verification) ---
        ShowSeat updatedSeat = showSeatRepository.findById(testShowSeatId).orElseThrow();
        List<BookingItem> itemsForSeat = bookingItemRepository.findByShowSeatId(testShowSeatId);

        System.out.println("==========================================");
        System.out.println("CONCURRENCY TEST RESULTS:");
        System.out.println("Total Concurrent Requests : " + numberOfConcurrentUsers);
        System.out.println("Successful Bookings      : " + successCount.get());
        System.out.println("Failed Bookings          : " + failureCount.get());
        System.out.println("Actual DB Booking Records : " + itemsForSeat.size());
        System.out.println("Final Seat Status in DB  : " + updatedSeat.getStatus());
        System.out.println("==========================================");

        assertEquals(1, successCount.get(),
                "CRITICAL BUG: Expected exactly 1 booking to succeed, but " + successCount.get() + " succeeded!");
    }
}
