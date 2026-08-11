package com.ticketBookingEngine.ticketBookingEngine.controller;

import com.ticketBookingEngine.ticketBookingEngine.dto.BookingRequestDTO;
import com.ticketBookingEngine.ticketBookingEngine.dto.BookingResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(BookingRequestDTO request){
        BookingResponseDTO response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
