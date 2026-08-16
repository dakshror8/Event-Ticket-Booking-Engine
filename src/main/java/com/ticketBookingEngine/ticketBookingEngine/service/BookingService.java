package com.ticketBookingEngine.ticketBookingEngine.service;

import com.ticketBookingEngine.ticketBookingEngine.dto.BookingRequestDTO;
import com.ticketBookingEngine.ticketBookingEngine.dto.BookingResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.entity.*;
import com.ticketBookingEngine.ticketBookingEngine.repository.BookingRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowSeatRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        try {
            // 1. Fetch User and Show
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

            Show show = showRepository.findById(request.getShowId())
                    .orElseThrow(() -> new IllegalArgumentException("Show not found with ID: " + request.getShowId()));

            // 2. Fetch requested seats
            List<ShowSeat> showSeats = showSeatRepository.findAllById(request.getShowSeatIds());

            if (showSeats.size() != request.getShowSeatIds().size()) {
                throw new IllegalArgumentException("One or more invalid seat IDs provided");
            }

            // 3. Check seat availability
            for (ShowSeat showSeat : showSeats) {
                if (showSeat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new IllegalStateException("Seat " + showSeat.getSeat().getSeatNumber() + " is no longer available!");
                }
            }

            // 4. Update status and compute total
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<String> bookedSeatNumbers = new ArrayList<>();

            for (ShowSeat showSeat : showSeats) {
                showSeat.setStatus(SeatStatus.HELD);
                totalAmount = totalAmount.add(showSeat.getPrice());
                bookedSeatNumbers.add(showSeat.getSeat().getSeatNumber());
            }

            // 5. Create Booking Entity
            Booking booking = Booking.builder()
                    .user(user)
                    .show(show)
                    .totalAmount(totalAmount)
                    .status(BookingStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();

            for (ShowSeat showSeat : showSeats) {
                BookingItem item = BookingItem.builder()
                        .booking(booking)
                        .showSeat(showSeat)
                        .price(showSeat.getPrice())
                        .build();
                booking.getItems().add(item);
            }

            // 6. Save Booking (Triggers version check on ShowSeat during transaction commit)
            Booking savedBooking = bookingRepository.save(booking);

            return BookingResponseDTO.builder()
                    .bookingId(savedBooking.getId())
                    .userId(user.getId())
                    .showId(show.getId())
                    .totalAmount(savedBooking.getTotalAmount())
                    .status(savedBooking.getStatus())
                    .expiresAt(savedBooking.getExpiresAt())
                    .seatNumbers(bookedSeatNumbers)
                    .build();

        } catch (ObjectOptimisticLockingFailureException e) {
            // Catch JPA Optimistic Lock Failure and translate to clean domain message
            throw new IllegalStateException("Seat booking conflict: Another user completed booking first. Please choose another seat.");
        }
    }
}
