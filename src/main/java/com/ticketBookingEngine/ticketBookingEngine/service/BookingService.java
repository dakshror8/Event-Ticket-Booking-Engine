package com.ticketBookingEngine.ticketBookingEngine.service;

import com.ticketBookingEngine.ticketBookingEngine.dto.BookingRequestDTO;
import com.ticketBookingEngine.ticketBookingEngine.dto.BookingResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.entity.*;
import com.ticketBookingEngine.ticketBookingEngine.repository.BookingRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowSeatRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public BookingResponseDTO createBooking(BookingRequestDTO request){
        // Validate user and show existence
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with ID: " + request.getShowId()));

        // fetch all requested seats
        List<ShowSeat> showSeats = showSeatRepository.findAllById(request.getShowSeatIds());

        if(showSeats.size() != request.getShowSeatIds().size()){
            throw new RuntimeException("One or more invalid show IDs are entered");
        }

        // Check availability of requested seats
        // --- RACE CONDITION BUG HERE ---
        for(ShowSeat showSeat : showSeats){
            if(showSeat.getStatus() != SeatStatus.AVAILABLE){
                throw new RuntimeException("Seat " + showSeat.getSeat().getSeatNumber() + " is no longer available!");
            }
        }


        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> bookedSeatNumbers = new ArrayList<>();
        for(ShowSeat showSeat : showSeats){
            showSeat.setStatus(SeatStatus.HELD);
            totalAmount = totalAmount.add(showSeat.getPrice());
            bookedSeatNumbers.add(showSeat.getSeat().getSeatNumber());
        }

        // CREATE Booking entity
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // 10-minute window
                .build();

        // create BookingItem Junction records
        for(ShowSeat showSeat : showSeats){
            BookingItem bookingItem = BookingItem.builder()
                    .booking(booking)
                    .showSeat(showSeat)
                    .price(showSeat.getPrice())
                    .build();
            booking.getItems().add(bookingItem);
        }

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
    }

}
